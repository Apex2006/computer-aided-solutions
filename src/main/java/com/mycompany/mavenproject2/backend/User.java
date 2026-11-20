package com.mycompany.mavenproject2.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;

/**
 * A registered account (the USERS table).
 *
 * Expert and Admin are roles of a User, as drawn on the class diagram, so
 * their actions live here under "Expert only" and "Admin only". This class
 * also holds the stratified sampling rule that turns a user into an expert.
 *
 * @author Luis Gabriel Santiago
 */
public class User {

    public static final List<String> IDENTITIES = List.of("STUDENT", "PROFESSIONAL", "HOBBYIST", "EDUCATOR");

    /** Stratified expert rating settings. */
    public static final double MARGIN_OF_ERROR = 0.05;
    public static final double PROMOTE_RATE = 0.80;
    public static final double REVOKE_RATE = 0.667;

    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String identity;
    private List<String> interests = new ArrayList<>();
    private String role = "USER";
    private String expertSource;
    private String credential;
    private Date expertSince;
    private String accountStatus = "ACTIVE";

    public User() {
    }

    /** A new account, ready for register(). */
    public User(String username, String email, String password, String fullName, String identity) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.identity = identity;
        setPassword(password);
    }

    // ------------------------------------------------------------------
    // Every user
    // ------------------------------------------------------------------

    /** Saves this new account in USERS (Sequence 1, sign-up). */
    public void register() throws SQLException {
        if (isBlank(username) || username.length() > 50) {
            throw new IllegalArgumentException("Username is required (up to 50 characters).");
        }
        if (isBlank(email) || !email.contains("@")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (isBlank(passwordHash)) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (isBlank(fullName)) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (!IDENTITIES.contains(identity)) {
            throw new IllegalArgumentException("Pick an identity: Student, Professional, Hobbyist or Educator.");
        }
        String sql = "INSERT INTO users (username, email, password_hash, full_name, identity, interests) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username.trim());
            ps.setString(2, email.trim());
            ps.setString(3, passwordHash);
            ps.setString(4, fullName.trim());
            ps.setString(5, identity);
            ps.setString(6, String.join(",", interests));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    userId = keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (isDuplicate(e)) {
                throw new IllegalArgumentException("That username or email is already taken.");
            }
            throw e;
        }
        role = "USER";
        accountStatus = "ACTIVE";
    }

    /** Checks the username (or email) and password. On success this object holds the account. */
    public boolean login(String username, String password) throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE username = ? OR email = ?")) {
            ps.setString(1, username == null ? "" : username.trim());
            ps.setString(2, username == null ? "" : username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || password == null || !BCrypt.checkpw(password, rs.getString("password_hash"))) {
                    return false;
                }
                if ("SUSPENDED".equals(rs.getString("account_status"))) {
                    throw new IllegalStateException("This account is suspended. Contact the admin.");
                }
                fill(rs);
                return true;
            }
        }
    }

    /** Forgets the logged-in account. */
    public void logout() {
        userId = 0;
        username = null;
        email = null;
        passwordHash = null;
        fullName = null;
        identity = null;
        interests = new ArrayList<>();
        role = "USER";
        expertSource = null;
        credential = null;
        expertSince = null;
        accountStatus = "ACTIVE";
    }

    /** Saves the niches picked on first login (any of the 8 categories). */
    public void chooseInterests(List<String> categories) throws SQLException {
        requireLoggedIn();
        for (String category : categories) {
            if (!Guide.CATEGORIES.contains(category)) {
                throw new IllegalArgumentException("Unknown category: " + category);
            }
        }
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE users SET interests = ? WHERE user_id = ?")) {
            ps.setString(1, String.join(",", categories));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        interests = new ArrayList<>(categories);
    }

    /** Sequence 2: Save Draft. */
    public void saveDraft(Guide guide) throws SQLException {
        requireLoggedIn();
        guide.setAuthor(this);
        guide.saveAsDraft();
    }

    /** Sequence 2: Submit. High risk from a non-expert waits for an expert, everything else is published. */
    public void submitGuide(Guide guide) throws SQLException {
        requireLoggedIn();
        guide.setAuthor(this);
        if (guide.isHighRisk() && !isExpert()) {
            guide.markPending();
        } else {
            guide.publish();
        }
    }

    /**
     * Sequence 3: a vote plus an optional comment in one go (used by the
     * console demo). The guide page uses vote() and comment() separately.
     */
    public Review reviewGuide(Guide guide, boolean approve, String comment) throws SQLException {
        Review current = myReview(guide);
        if (current != null && current.hasVote()) {
            throw new IllegalStateException("You already reviewed this guide.");
        }
        vote(guide, approve);
        if (!isBlank(comment)) {
            comment(guide, comment);
        }
        return myReview(guide);
    }

    /**
     * Sequence 3: approve (true), disapprove (false) or take back the vote (null).
     * A comment is not needed. The author's expert status is rechecked after.
     */
    public void vote(Guide guide, Boolean approve) throws SQLException {
        requireLoggedIn();
        if (guide.getAuthor() != null && guide.getAuthor().getUserId() == userId) {
            throw new IllegalStateException("You cannot review your own guide.");
        }
        requirePublished(guide);
        try (Connection c = Database.getConnection()) {
            if (approve == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE reviews SET is_approved = NULL WHERE guide_id = ? AND user_id = ?")) {
                    ps.setInt(1, guide.getGuideId());
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                removeEmptyReview(c, guide);
            } else {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO reviews (guide_id, user_id, is_approved) "
                        + "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE is_approved = VALUES(is_approved)")) {
                    ps.setInt(1, guide.getGuideId());
                    ps.setInt(2, userId);
                    ps.setBoolean(3, approve);
                    ps.executeUpdate();
                }
            }
        }
        User author = User.findById(guide.getAuthor().getUserId());
        if (author != null) {
            author.updateExpertStatus();
        }
    }

    /**
     * Sequence 3: writes, edits or (with an empty text) deletes this person's
     * comment. No vote is needed, and authors can answer on their own guide.
     */
    public void comment(Guide guide, String text) throws SQLException {
        requireLoggedIn();
        requirePublished(guide);
        String clean = isBlank(text) ? null : text.trim();
        if (clean != null && clean.length() > 2000) {
            throw new IllegalArgumentException("Comments can be up to 2000 characters.");
        }
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO reviews (guide_id, user_id, is_approved, comment) "
                    + "VALUES (?, ?, NULL, ?) ON DUPLICATE KEY UPDATE comment = VALUES(comment), "
                    + "created_at = CURRENT_TIMESTAMP")) {
                ps.setInt(1, guide.getGuideId());
                ps.setInt(2, userId);
                ps.setString(3, clean);
                ps.executeUpdate();
            }
            removeEmptyReview(c, guide);
        }
    }

    /** This person's vote and comment on a guide, or null. */
    public Review myReview(Guide guide) throws SQLException {
        for (Review review : Review.findByGuide(guide)) {
            if (review.getReviewer().getUserId() == userId) {
                return review;
            }
        }
        return null;
    }

    private void removeEmptyReview(Connection c, Guide guide) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM reviews WHERE guide_id = ? AND user_id = ? "
                + "AND is_approved IS NULL AND (comment IS NULL OR comment = '')")) {
            ps.setInt(1, guide.getGuideId());
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private static void requirePublished(Guide guide) {
        if (!"PUBLISHED".equals(guide.getStatus())) {
            throw new IllegalStateException("Only published guides can be reviewed.");
        }
    }

    /** Sequence 3: type is REPORT or EDIT_REQUEST. It goes to the admin as OPEN. */
    public Report reportGuide(Guide guide, String type, String reason) throws SQLException {
        requireLoggedIn();
        if (!Report.TYPES.contains(type)) {
            throw new IllegalArgumentException("Type must be REPORT or EDIT_REQUEST.");
        }
        if (isBlank(reason)) {
            throw new IllegalArgumentException("Please type a reason.");
        }
        int reportId = 0;
        String sql = "INSERT INTO reports (guide_id, reporter_id, type, reason) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, guide.getGuideId());
            ps.setInt(2, userId);
            ps.setString(3, type);
            ps.setString(4, reason.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reportId = keys.getInt(1);
                }
            }
        }
        return new Report(reportId, guide, this, type, reason.trim(), "OPEN", null, new Date());
    }

    /** Stratified approval rate p of this user's guides (0.0 to 1.0). */
    public double approvalRate() throws SQLException {
        return tally().rate();
    }

    /**
     * Promotes or revokes this user as an expert.
     * Promote: every identity quota is met and p >= 80%.
     * Revoke: earned experts only, when p <= 66.7%.
     * Admins and admin-added experts never change here.
     */
    public void updateExpertStatus() throws SQLException {
        if (isAdmin() || (isExpert() && "ADMIN".equals(expertSource))) {
            return;
        }
        Tally t = tally();
        if ("USER".equals(role) && t.quotasMet() && t.rate() >= PROMOTE_RATE) {
            runUpdate("UPDATE users SET role = 'EXPERT', expert_source = 'VOTES', expert_since = NOW() "
                    + "WHERE user_id = ? AND role = 'USER'", userId);
            role = "EXPERT";
            expertSource = "VOTES";
            expertSince = new Date();
        } else if (isExpert() && "VOTES".equals(expertSource) && t.totalVotes() > 0 && t.rate() <= REVOKE_RATE) {
            runUpdate("UPDATE users SET role = 'USER', expert_source = NULL, expert_since = NULL "
                    + "WHERE user_id = ? AND expert_source = 'VOTES'", userId);
            role = "USER";
            expertSource = null;
            expertSince = null;
        }
    }

    /** A readable breakdown of the expert check, for the profile screen and the demo. */
    public String expertSummary() throws SQLException {
        Tally t = tally();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Eligible voters N = %d, votes needed n = %d%n", t.population(), t.votesNeeded()));
        for (String group : IDENTITIES) {
            int size = t.groupSizes().get(group);
            if (size == 0) {
                continue;
            }
            sb.append(String.format("  %-12s %d of %d votes, %d approve%n", group,
                    t.votes().get(group), t.quotas().get(group), t.approvals().get(group)));
        }
        sb.append(String.format("Quotas met: %s, stratified approval rate p = %.1f%%",
                t.quotasMet() ? "yes" : "no", t.rate() * 100));
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Expert only
    // ------------------------------------------------------------------

    /** Sequence 4: sets the risk factor and Y/N. Only works while the guide is still PENDING. */
    public void approveGuide(Guide guide, String riskFactor, boolean approve) throws SQLException {
        requireLoggedIn();
        if (!isExpert()) {
            throw new IllegalStateException("Only experts can approve guides.");
        }
        if (guide.getAuthor() != null && guide.getAuthor().getUserId() == userId) {
            throw new IllegalStateException("You cannot approve your own guide.");
        }
        if (!Guide.RISK_FACTORS.contains(riskFactor)) {
            throw new IllegalArgumentException("Risk factor must be LOW, MODERATE or HIGH.");
        }
        String newStatus = approve ? "PUBLISHED" : "DRAFT";
        String sql = "UPDATE guides SET approval = ?, status = ?, risk_factor = ?, approved_by = ?, approved_at = NOW() "
                + "WHERE guide_id = ? AND status = 'PENDING'";
        int changed;
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, approve ? "Y" : "N");
            ps.setString(2, newStatus);
            ps.setString(3, riskFactor);
            ps.setInt(4, userId);
            ps.setInt(5, guide.getGuideId());
            changed = ps.executeUpdate();
        }
        if (changed == 0) {
            throw new IllegalStateException("This guide was already reviewed by another expert.");
        }
        guide.setApprovalResult(approve ? 'Y' : 'N', newStatus, riskFactor, this);
    }

    // ------------------------------------------------------------------
    // Admin only
    // ------------------------------------------------------------------

    /** Account management: blocks a user from logging in. */
    public void suspendUser(User user) throws SQLException {
        requireAdmin();
        if (user.getUserId() == userId) {
            throw new IllegalArgumentException("You cannot suspend your own account.");
        }
        runUpdate("UPDATE users SET account_status = 'SUSPENDED' WHERE user_id = ? AND role <> 'ADMIN'", user.getUserId());
        user.accountStatus = "SUSPENDED";
    }

    /** Account management: adds an expert (for example, a professor). A credential is required. */
    public void addExpert(User user, String credential) throws SQLException {
        requireAdmin();
        if (isBlank(credential)) {
            throw new IllegalArgumentException("Record the expert's credential first.");
        }
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("UPDATE users SET role = 'EXPERT', expert_source = 'ADMIN', "
                        + "credential = ?, expert_since = NOW() WHERE user_id = ? AND role <> 'ADMIN'")) {
            ps.setString(1, credential.trim());
            ps.setInt(2, user.getUserId());
            ps.executeUpdate();
        }
        user.role = "EXPERT";
        user.expertSource = "ADMIN";
        user.credential = credential.trim();
        user.expertSince = new Date();
    }

    /** Account management: removes any expert, earned or admin-added. */
    public void removeExpert(User user) throws SQLException {
        requireAdmin();
        runUpdate("UPDATE users SET role = 'USER', expert_source = NULL, credential = NULL, expert_since = NULL "
                + "WHERE user_id = ? AND role = 'EXPERT'", user.getUserId());
        user.role = "USER";
        user.expertSource = null;
        user.credential = null;
        user.expertSince = null;
    }

    /** Guide management: hides a guide from everyone. */
    public void hideGuide(Guide guide) throws SQLException {
        requireAdmin();
        runUpdate("UPDATE guides SET status = 'HIDDEN' WHERE guide_id = ?", guide.getGuideId());
        guide.setStatus("HIDDEN");
    }

    /** Account management: lets a suspended user log in again. */
    public void activateUser(User user) throws SQLException {
        requireAdmin();
        runUpdate("UPDATE users SET account_status = 'ACTIVE' WHERE user_id = ?", user.getUserId());
        user.accountStatus = "ACTIVE";
    }

    /** Guide management: shows a hidden guide again. */
    public void unhideGuide(Guide guide) throws SQLException {
        requireAdmin();
        runUpdate("UPDATE guides SET status = 'PUBLISHED' WHERE guide_id = ? AND status = 'HIDDEN'", guide.getGuideId());
        guide.setStatus("PUBLISHED");
    }

    /** Reports: marks a report or edit request as handled. */
    public void resolveReport(Report report) throws SQLException {
        requireAdmin();
        report.resolve(this);
    }

    // ------------------------------------------------------------------
    // Lookups
    // ------------------------------------------------------------------

    public static User findById(int id) throws SQLException {
        return findOne("SELECT * FROM users WHERE user_id = ?", id);
    }

    /** Every account, for the admin panel. */
    public static List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM users ORDER BY user_id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(fromRow(rs));
            }
        }
        return users;
    }

    /** Reloads this account from the database (for example after a promotion). */
    public void refresh() throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fill(rs);
                }
            }
        }
    }

    public static User findByUsername(String username) throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    // ------------------------------------------------------------------
    // Stratified sampling math (Slovin's formula + proportional quotas)
    // ------------------------------------------------------------------

    /** n = N / (1 + N x e^2), rounded up. With e = 0.05 this is 400N / (400 + N). */
    public static int requiredVotes(int totalUsers) {
        if (totalUsers <= 0) {
            return 0;
        }
        long k = Math.round(1 / (MARGIN_OF_ERROR * MARGIN_OF_ERROR));
        return (int) Math.ceilDiv(k * totalUsers, k + totalUsers);
    }

    /** n_h = n x (N_h / N), rounded up. */
    public static int groupQuota(int requiredVotes, int groupSize, int totalUsers) {
        if (totalUsers <= 0 || groupSize <= 0) {
            return 0;
        }
        return (int) Math.ceilDiv((long) requiredVotes * groupSize, totalUsers);
    }

    /** Counts eligible voters and votes per identity group for this author. */
    private Tally tally() throws SQLException {
        Map<String, Integer> sizes = emptyGroups();
        Map<String, Integer> votes = emptyGroups();
        Map<String, Integer> yes = emptyGroups();
        try (Connection c = Database.getConnection()) {
            // Eligible voters: active accounts except the author (the author cannot review their own guides)
            String sizeSql = "SELECT identity, COUNT(*) AS total FROM users "
                    + "WHERE account_status = 'ACTIVE' AND user_id <> ? GROUP BY identity";
            try (PreparedStatement ps = c.prepareStatement(sizeSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sizes.put(rs.getString("identity"), rs.getInt("total"));
                    }
                }
            }
            String voteSql = "SELECT v.identity, COUNT(*) AS vote_count, "
                    + "SUM(CASE WHEN r.is_approved = TRUE THEN 1 ELSE 0 END) AS approve_count "
                    + "FROM reviews r JOIN guides g ON g.guide_id = r.guide_id "
                    + "JOIN users v ON v.user_id = r.user_id "
                    + "WHERE g.author_id = ? AND v.account_status = 'ACTIVE' AND r.is_approved IS NOT NULL "
                    + "GROUP BY v.identity";
            try (PreparedStatement ps = c.prepareStatement(voteSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        votes.put(rs.getString("identity"), rs.getInt("vote_count"));
                        yes.put(rs.getString("identity"), rs.getInt("approve_count"));
                    }
                }
            }
        }
        int population = 0;
        for (int size : sizes.values()) {
            population += size;
        }
        int needed = requiredVotes(population);
        Map<String, Integer> quotas = emptyGroups();
        boolean quotasMet = population > 0;
        int totalVotes = 0;
        double rate = 0;
        for (String group : IDENTITIES) {
            int size = sizes.get(group);
            int quota = groupQuota(needed, size, population);
            int groupVotes = votes.get(group);
            quotas.put(group, quota);
            totalVotes += groupVotes;
            if (groupVotes < quota) {
                quotasMet = false;
            }
            if (size > 0 && groupVotes > 0) {
                // p = sum of (N_h / N) x p_h
                rate += ((double) size / population) * ((double) yes.get(group) / groupVotes);
            }
        }
        return new Tally(population, needed, sizes, quotas, votes, yes, quotasMet, totalVotes, rate);
    }

    private record Tally(int population, int votesNeeded, Map<String, Integer> groupSizes,
            Map<String, Integer> quotas, Map<String, Integer> votes, Map<String, Integer> approvals,
            boolean quotasMet, int totalVotes, double rate) {
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Hashes a plain password with BCrypt. The plain password is never stored. */
    public final void setPassword(String password) {
        if (isBlank(password) || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /** Two letters for the avatar, for example "LS" for Luis Gabriel Santiago. */
    public String getInitials() {
        String name = isBlank(fullName) ? username : fullName;
        if (isBlank(name)) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    /** "STUDENT" becomes "Student". */
    public static String identityLabel(String identity) {
        if (isBlank(identity)) {
            return "";
        }
        return identity.substring(0, 1) + identity.substring(1).toLowerCase();
    }

    public boolean isLoggedIn() {
        return userId > 0;
    }

    public boolean isExpert() {
        return "EXPERT".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /** "Admin approved" for admin-added experts, the approval rate for earned ones. */
    public String getExpertBadgeDetail() throws SQLException {
        if (!isExpert()) {
            return "";
        }
        if ("ADMIN".equals(expertSource)) {
            return "Admin approved";
        }
        return String.format("Approval rate %.1f%%", approvalRate() * 100);
    }

    private void requireLoggedIn() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Please log in first. Guests can only view guides.");
        }
    }

    private void requireAdmin() {
        requireLoggedIn();
        if (!isAdmin()) {
            throw new IllegalStateException("Only admins can do this.");
        }
    }

    private static Map<String, Integer> emptyGroups() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String group : IDENTITIES) {
            map.put(group, 0);
        }
        return map;
    }

    private static void runUpdate(String sql, int id) throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static User findOne(String sql, int id) throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    private static User fromRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.fill(rs);
        return user;
    }

    private void fill(ResultSet rs) throws SQLException {
        userId = rs.getInt("user_id");
        username = rs.getString("username");
        email = rs.getString("email");
        passwordHash = rs.getString("password_hash");
        fullName = rs.getString("full_name");
        identity = rs.getString("identity");
        String list = rs.getString("interests");
        interests = (list == null || list.isEmpty()) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(list.split(",")));
        role = rs.getString("role");
        expertSource = rs.getString("expert_source");
        credential = rs.getString("credential");
        expertSince = rs.getTimestamp("expert_since");
        accountStatus = rs.getString("account_status");
    }

    static boolean isDuplicate(SQLException e) {
        return e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode() == 1062;
    }

    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    // ------------------------------------------------------------------
    // Getters and setters
    // ------------------------------------------------------------------

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public List<String> getInterests() {
        return new ArrayList<>(interests);
    }

    void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getExpertSource() {
        return expertSource;
    }

    public String getCredential() {
        return credential;
    }

    public Date getExpertSince() {
        return expertSince;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    @Override
    public String toString() {
        return username + " (" + role + (isExpert() ? ", " + expertSource : "") + ")";
    }
}
