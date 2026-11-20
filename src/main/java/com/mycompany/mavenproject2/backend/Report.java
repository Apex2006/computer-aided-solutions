package com.mycompany.mavenproject2.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A report or an edit request on a guide (the REPORTS table).
 * Users file them with User.reportGuide(); an admin resolves or dismisses them.
 *
 * @author Luis Gabriel Santiago
 */
public class Report {

    public static final List<String> TYPES = List.of("REPORT", "EDIT_REQUEST");

    private final int reportId;
    private final Guide guide;
    private final User reporter;
    private final String type;
    private final String reason;
    private String status;
    private User resolvedBy;
    private final Date createdAt;

    public Report(int reportId, Guide guide, User reporter, String type, String reason,
            String status, User resolvedBy, Date createdAt) {
        this.reportId = reportId;
        this.guide = guide;
        this.reporter = reporter;
        this.type = type;
        this.reason = reason;
        this.status = status;
        this.resolvedBy = resolvedBy;
        this.createdAt = createdAt;
    }

    /** Marks the report as handled. */
    public void resolve(User admin) throws SQLException {
        close(admin, "RESOLVED");
    }

    /** Closes the report without action. */
    public void dismiss(User admin) throws SQLException {
        close(admin, "DISMISSED");
    }

    private void close(User admin, String newStatus) throws SQLException {
        if (admin == null || !admin.isAdmin()) {
            throw new IllegalStateException("Only admins can do this.");
        }
        int changed;
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "UPDATE reports SET status = ?, resolved_by = ? WHERE report_id = ? AND status = 'OPEN'")) {
            ps.setString(1, newStatus);
            ps.setInt(2, admin.getUserId());
            ps.setInt(3, reportId);
            changed = ps.executeUpdate();
        }
        if (changed == 0) {
            throw new IllegalStateException("This report was already handled.");
        }
        status = newStatus;
        resolvedBy = admin;
    }

    /** Open reports and edit requests for the admin panel, oldest first. */
    public static List<Report> findOpen() throws SQLException {
        return find("WHERE r.status = 'OPEN' ORDER BY r.created_at, r.report_id", 0);
    }

    /** Open edit requests on one author's guides, for the Home screen notice. */
    public static List<Report> findOpenEditRequestsFor(User author) throws SQLException {
        return find("WHERE r.status = 'OPEN' AND r.type = 'EDIT_REQUEST' AND g.author_id = ? "
                + "ORDER BY r.created_at, r.report_id", author.getUserId());
    }

    private static List<Report> find(String where, int authorId) throws SQLException {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT r.report_id, r.guide_id, g.title, r.reporter_id, u.username, r.type, r.reason, "
                + "r.status, r.created_at FROM reports r "
                + "JOIN guides g ON g.guide_id = r.guide_id JOIN users u ON u.user_id = r.reporter_id " + where;
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            if (authorId > 0) {
                ps.setInt(1, authorId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Guide guide = Guide.stub(rs.getInt("guide_id"), rs.getString("title"));
                    User reporter = new User();
                    reporter.setUserId(rs.getInt("reporter_id"));
                    reporter.setUsername(rs.getString("username"));
                    reports.add(new Report(rs.getInt("report_id"), guide, reporter, rs.getString("type"),
                            rs.getString("reason"), rs.getString("status"), null, rs.getTimestamp("created_at")));
                }
            }
        }
        return reports;
    }

    public int getReportId() {
        return reportId;
    }

    public Guide getGuide() {
        return guide;
    }

    public User getReporter() {
        return reporter;
    }

    public String getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "#" + reportId + " " + type + " on \"" + (guide == null ? "?" : guide.getTitle()) + "\" by "
                + reporter.getUsername() + ": " + reason + " [" + status + "]";
    }
}
