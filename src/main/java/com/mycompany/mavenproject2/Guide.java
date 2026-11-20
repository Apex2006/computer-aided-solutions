package com.mycompany.mavenproject2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A DIY guide (the GUIDES table).
 *
 * Drafts are guides with status DRAFT. The expert approval from the board
 * (risk factor, Y/N) is stored here too: approval, approvedBy, approvedAt.
 * Media: a cover image, a schematic (wiring diagram), one photo per step
 * (packed together in step_images) and a video link.
 *
 * @author Luis Gabriel Santiago
 */
public class Guide {

    public static final List<String> CATEGORIES = List.of("FOOD_COOKING", "GARDENING", "STUDIES",
            "HOUSEKEEPING", "ENERGY_SAVING", "HOME_SECURITY", "HEALTH_WELLNESS", "PETS");
    private static final Map<String, String> LABELS = Map.of(
            "FOOD_COOKING", "Food and Cooking", "GARDENING", "Gardening and Planting",
            "STUDIES", "Studies and Productivity", "HOUSEKEEPING", "Housekeeping",
            "ENERGY_SAVING", "Energy Saving", "HOME_SECURITY", "Home Security",
            "HEALTH_WELLNESS", "Health and Wellness", "PETS", "Pets");
    public static final List<String> DIFFICULTIES = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
    public static final List<String> RISK_FACTORS = List.of("LOW", "MODERATE", "HIGH");
    public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    /** All pictures of one guide together. TiDB keeps one row under 6 MB. */
    public static final int MAX_MEDIA_BYTES = 5 * 1024 * 1024;
    private static final Pattern YOUTUBE = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|shorts/|embed/|live/)|youtu\\.be/)([A-Za-z0-9_-]{11})");

    /** Columns for lists, with vote counts. The image is left out so lists load fast. */
    private static final String COLUMNS = "g.guide_id, g.approved_by, g.author_id, u.username AS author_name, "
            + "u.full_name AS author_full_name, e.username AS expert_name, e.full_name AS expert_full_name, "
            + "g.title, g.category, g.summary, g.parts_list, g.precautions, g.steps, g.estimated_cost, "
            + "g.difficulty, g.risk_factor, g.status, g.approval, g.approved_at, g.created_at, g.video_url, "
            + "(SELECT COUNT(*) FROM reviews r WHERE r.guide_id = g.guide_id AND r.is_approved IS NOT NULL) AS vote_count, "
            + "(SELECT COUNT(*) FROM reviews r WHERE r.guide_id = g.guide_id AND r.is_approved = TRUE) AS approve_count";
    private static final String FROM = " FROM guides g JOIN users u ON u.user_id = g.author_id "
            + "LEFT JOIN users e ON e.user_id = g.approved_by";

    private int guideId;
    private User author;
    private User approvedBy;
    private String title;
    private String category;
    private String summary = "";
    private String partsList = "";
    private String precautions = "";
    private String steps = "";
    private byte[] image;
    private byte[] schematic;
    private Map<Integer, byte[]> stepImages = new TreeMap<>();
    private String videoUrl;
    private double estimatedCost;
    private String difficulty = "BEGINNER";
    private String riskFactor = "LOW";
    private String status = "DRAFT";
    private char approval;
    private Date approvedAt;
    private Date createdAt;
    private int voteCount;
    private int approveCount;

    public Guide() {
    }

    public Guide(String title, String category, String riskFactor) {
        this.title = title;
        this.category = category;
        this.riskFactor = riskFactor;
    }

    public boolean isHighRisk() {
        return "HIGH".equals(riskFactor);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /** Saves the guide with status DRAFT. Unfinished fields are allowed. */
    public void saveAsDraft() throws SQLException {
        save("DRAFT");
    }

    /** Saves the guide with status PENDING so an expert can check it. */
    public void markPending() throws SQLException {
        save("PENDING");
    }

    /** Saves the guide with status PUBLISHED so everyone can see it. */
    public void publish() throws SQLException {
        save("PUBLISHED");
    }

    private void save(String newStatus) throws SQLException {
        validate(newStatus);
        try (Connection c = Database.getConnection()) {
            if (guideId == 0) {
                String sql = "INSERT INTO guides (title, category, summary, parts_list, precautions, steps, image, "
                        + "schematic, step_images, video_url, estimated_cost, difficulty, risk_factor, status, author_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    int i = fillContent(ps);
                    ps.setString(i++, newStatus);
                    ps.setInt(i, author.getUserId());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            guideId = keys.getInt(1);
                        }
                    }
                }
                createdAt = new Date();
            } else {
                // An edit clears the old expert approval: the expert checked the previous version
                String sql = "UPDATE guides SET title = ?, category = ?, summary = ?, parts_list = ?, precautions = ?, "
                        + "steps = ?, image = ?, schematic = ?, step_images = ?, video_url = ?, "
                        + "estimated_cost = ?, difficulty = ?, risk_factor = ?, status = ?, "
                        + "approval = NULL, approved_by = NULL, approved_at = NULL "
                        + "WHERE guide_id = ? AND author_id = ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    int i = fillContent(ps);
                    ps.setString(i++, newStatus);
                    ps.setInt(i++, guideId);
                    ps.setInt(i, author.getUserId());
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalStateException("Only the author can edit this guide.");
                    }
                }
                approval = '\0';
                approvedBy = null;
                approvedAt = null;
            }
        }
        status = newStatus;
    }

    /** Fills parameters 1 to 13 and returns the next parameter number. */
    private int fillContent(PreparedStatement ps) throws SQLException {
        int i = 1;
        ps.setString(i++, title.trim());
        ps.setString(i++, category);
        ps.setString(i++, summary);
        ps.setString(i++, partsList);
        ps.setString(i++, precautions);
        ps.setString(i++, steps);
        if (image == null) {
            ps.setNull(i++, Types.BLOB);
        } else {
            ps.setBytes(i++, image);
        }
        if (schematic == null) {
            ps.setNull(i++, Types.BLOB);
        } else {
            ps.setBytes(i++, schematic);
        }
        byte[] packed = packImages(stepImages);
        if (packed == null) {
            ps.setNull(i++, Types.BLOB);
        } else {
            ps.setBytes(i++, packed);
        }
        ps.setString(i++, User.isBlank(videoUrl) ? null : videoUrl.trim());
        if (estimatedCost <= 0) {
            ps.setNull(i++, Types.DECIMAL);
        } else {
            ps.setDouble(i++, estimatedCost);
        }
        ps.setString(i++, difficulty);
        ps.setString(i++, riskFactor);
        return i;
    }

    private void validate(String newStatus) {
        if (author == null || author.getUserId() == 0) {
            throw new IllegalStateException("A guide needs a logged-in author.");
        }
        if (User.isBlank(title) || title.trim().length() > 150) {
            throw new IllegalArgumentException("Title is required (up to 150 characters).");
        }
        if (!CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Pick one of the 8 categories.");
        }
        if (!DIFFICULTIES.contains(difficulty)) {
            throw new IllegalArgumentException("Difficulty must be BEGINNER, INTERMEDIATE or ADVANCED.");
        }
        if (!RISK_FACTORS.contains(riskFactor)) {
            throw new IllegalArgumentException("Risk factor must be LOW, MODERATE or HIGH.");
        }
        if (image != null && image.length > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("The image is too big. The limit is 5 MB.");
        }
        if (mediaBytes() > MAX_MEDIA_BYTES) {
            throw new IllegalArgumentException("The pictures add up to more than 5 MB. Remove a few step photos.");
        }
        if (!User.isBlank(videoUrl)) {
            String url = videoUrl.trim();
            if (url.length() > 500 || !(url.startsWith("https://") || url.startsWith("http://"))) {
                throw new IllegalArgumentException("The video link must start with https:// (up to 500 characters).");
            }
        }
        boolean finished = !User.isBlank(summary) && !User.isBlank(partsList)
                && !User.isBlank(precautions) && !User.isBlank(steps);
        if (!"DRAFT".equals(newStatus) && !finished) {
            throw new IllegalArgumentException("Fill in the summary, parts, precautions and steps before submitting.");
        }
    }

    /** A guide that only knows its id and title, for lists such as reports. */
    static Guide stub(int guideId, String title) {
        Guide guide = new Guide();
        guide.guideId = guideId;
        guide.title = title;
        return guide;
    }

    /** Called by User.approveGuide after the expert's decision is saved. */
    void setApprovalResult(char approval, String status, String riskFactor, User expert) {
        this.approval = approval;
        this.status = status;
        this.riskFactor = riskFactor;
        this.approvedBy = expert;
        this.approvedAt = new Date();
    }

    void setStatus(String status) {
        this.status = status;
    }

    // ------------------------------------------------------------------
    // Lookups
    // ------------------------------------------------------------------

    /** One guide, with its image. */
    public static Guide findById(int id) throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + ", g.image, g.schematic, g.step_images"
                        + FROM + " WHERE g.guide_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Guide guide = fromRow(rs);
                guide.image = rs.getBytes("image");
                guide.schematic = rs.getBytes("schematic");
                guide.stepImages = unpackImages(rs.getBytes("step_images"));
                return guide;
            }
        }
    }

    /** Published guides in one category, newest first. */
    public static List<Guide> findByCategory(String category) throws SQLException {
        return list(" WHERE g.status = 'PUBLISHED' AND g.category = ? ORDER BY g.created_at DESC, g.guide_id DESC", category);
    }

    /** Number of published guides in each category, for the category tiles. */
    public static Map<String, Integer> countByCategory() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String category : CATEGORIES) {
            counts.put(category, 0);
        }
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT category, COUNT(*) AS total FROM guides WHERE status = 'PUBLISHED' GROUP BY category");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getString("category"), rs.getInt("total"));
            }
        }
        return counts;
    }

    /** Guide management for the admin: everything except drafts. */
    public static List<Guide> findForAdmin() throws SQLException {
        return list(" WHERE g.status <> 'DRAFT' ORDER BY g.created_at DESC, g.guide_id DESC");
    }

    /** Everything the public can see, newest first. */
    public static List<Guide> findPublished() throws SQLException {
        return list(" WHERE g.status = 'PUBLISHED' ORDER BY g.created_at DESC, g.guide_id DESC");
    }

    /** High-risk guides waiting for an expert, oldest first. */
    public static List<Guide> findPending() throws SQLException {
        return list(" WHERE g.status = 'PENDING' ORDER BY g.created_at, g.guide_id");
    }

    /** A user's own guides, including drafts. */
    public static List<Guide> findByAuthor(User author) throws SQLException {
        return list(" WHERE g.author_id = ? ORDER BY g.created_at DESC, g.guide_id DESC", author.getUserId());
    }

    /** Dashboard: published guides in the user's chosen niches. No niches means everything. */
    public static List<Guide> findForInterests(User user) throws SQLException {
        List<String> interests = user.getInterests();
        if (interests.isEmpty()) {
            return findPublished();
        }
        String marks = String.join(", ", java.util.Collections.nCopies(interests.size(), "?"));
        return list(" WHERE g.status = 'PUBLISHED' AND g.category IN (" + marks + ") "
                + "ORDER BY g.created_at DESC, g.guide_id DESC", interests.toArray());
    }

    /** Search bar: every typed word must appear in the title or the summary. */
    public static List<Guide> search(String text) throws SQLException {
        StringBuilder where = new StringBuilder(" WHERE g.status = 'PUBLISHED'");
        List<Object> params = new ArrayList<>();
        for (String word : text.trim().split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            String pattern = "%" + word.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            where.append(" AND (g.title LIKE ? OR g.summary LIKE ?)");
            params.add(pattern);
            params.add(pattern);
        }
        where.append(" ORDER BY g.created_at DESC, g.guide_id DESC");
        return list(where.toString(), params.toArray());
    }

    private static List<Guide> list(String where, Object... params) throws SQLException {
        List<Guide> guides = new ArrayList<>();
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + FROM + where)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guides.add(fromRow(rs));
                }
            }
        }
        return guides;
    }

    private static Guide fromRow(ResultSet rs) throws SQLException {
        Guide g = new Guide();
        g.guideId = rs.getInt("guide_id");
        User writer = new User();
        writer.setUserId(rs.getInt("author_id"));
        writer.setUsername(rs.getString("author_name"));
        writer.setFullName(rs.getString("author_full_name"));
        g.author = writer;
        int expertId = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            User expert = new User();
            expert.setUserId(expertId);
            expert.setUsername(rs.getString("expert_name"));
            expert.setFullName(rs.getString("expert_full_name"));
            g.approvedBy = expert;
        }
        g.voteCount = rs.getInt("vote_count");
        g.approveCount = rs.getInt("approve_count");
        g.title = rs.getString("title");
        g.category = rs.getString("category");
        g.summary = rs.getString("summary");
        g.partsList = rs.getString("parts_list");
        g.precautions = rs.getString("precautions");
        g.steps = rs.getString("steps");
        g.estimatedCost = rs.getDouble("estimated_cost");
        g.difficulty = rs.getString("difficulty");
        g.riskFactor = rs.getString("risk_factor");
        g.status = rs.getString("status");
        String decided = rs.getString("approval");
        g.approval = (decided == null || decided.isEmpty()) ? '\0' : decided.charAt(0);
        g.approvedAt = rs.getTimestamp("approved_at");
        g.createdAt = rs.getTimestamp("created_at");
        g.videoUrl = rs.getString("video_url");
        return g;
    }

    // ------------------------------------------------------------------
    // Media helpers
    // ------------------------------------------------------------------

    /** Total size of the cover, the schematic and the step photos. */
    public int mediaBytes() {
        int total = (image == null ? 0 : image.length) + (schematic == null ? 0 : schematic.length);
        for (byte[] photo : stepImages.values()) {
            total += photo.length;
        }
        return total;
    }

    /** Packs the step photos into one zip (step-1.jpg, step-2.jpg, ...) for the step_images column. */
    static byte[] packImages(Map<Integer, byte[]> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.setLevel(0); // the photos are JPEGs already
            for (Map.Entry<Integer, byte[]> photo : images.entrySet()) {
                zip.putNextEntry(new ZipEntry("step-" + photo.getKey() + ".jpg"));
                zip.write(photo.getValue());
                zip.closeEntry();
            }
            zip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not pack the step photos.", e);
        }
    }

    /** Reads the step photos back: step number (from 1) to picture bytes. */
    static Map<Integer, byte[]> unpackImages(byte[] packed) {
        Map<Integer, byte[]> images = new TreeMap<>();
        if (packed == null || packed.length == 0) {
            return images;
        }
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(packed))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Matcher m = Pattern.compile("step-(\\d+)\\.jpg").matcher(entry.getName());
                if (m.matches()) {
                    images.put(Integer.valueOf(m.group(1)), zip.readAllBytes());
                }
            }
        } catch (IOException e) {
            return new TreeMap<>();
        }
        return images;
    }

    /** The 11-character YouTube id of the video link, or null if it is not a YouTube link. */
    public static String youtubeId(String url) {
        if (url == null) {
            return null;
        }
        Matcher m = YOUTUBE.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    // ------------------------------------------------------------------
    // Getters and setters
    // ------------------------------------------------------------------

    public int getGuideId() {
        return guideId;
    }

    public User getAuthor() {
        return author;
    }

    void setAuthor(User author) {
        this.author = author;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary == null ? "" : summary;
    }

    public String getPartsList() {
        return partsList;
    }

    public void setPartsList(String partsList) {
        this.partsList = partsList == null ? "" : partsList;
    }

    public String getPrecautions() {
        return precautions;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions == null ? "" : precautions;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps == null ? "" : steps;
    }

    public byte[] getImage() {
        return image;
    }

    public byte[] getSchematic() {
        return schematic;
    }

    public void setSchematic(byte[] schematic) {
        this.schematic = schematic;
    }

    /** Step number (from 1) to photo. */
    public Map<Integer, byte[]> getStepImages() {
        return stepImages;
    }

    public void setStepImages(Map<Integer, byte[]> stepImages) {
        this.stepImages = stepImages == null ? new TreeMap<>() : new TreeMap<>(stepImages);
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(String riskFactor) {
        this.riskFactor = riskFactor;
    }

    public String getStatus() {
        return status;
    }

    public char getApproval() {
        return approval;
    }

    public Date getApprovedAt() {
        return approvedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public int getApproveCount() {
        return approveCount;
    }

    /** Approval rate from votes, 0 to 100, or -1 when nobody has voted yet. */
    public int getApprovalPercent() {
        return voteCount == 0 ? -1 : (int) Math.round(approveCount * 100.0 / voteCount);
    }

    /** True when an expert approved this guide (Y). */
    public boolean isExpertApproved() {
        return approval == 'Y';
    }

    /** The readable name of a category, for example "Energy Saving". */
    public static String label(String category) {
        return LABELS.getOrDefault(category, category);
    }

    @Override
    public String toString() {
        return "#" + guideId + " " + title + " [" + category + ", " + riskFactor + " risk, " + status + "]"
                + (author != null && author.getUsername() != null ? " by " + author.getUsername() : "");
    }
}
