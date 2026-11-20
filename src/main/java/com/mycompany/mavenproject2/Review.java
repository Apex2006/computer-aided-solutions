package com.mycompany.mavenproject2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * One person's vote (approve, disapprove, or none) and comment on a guide
 * (the REVIEWS table). Each person has at most one row per guide: the vote
 * and the comment can be set, changed or removed separately.
 * Reviews are created by User.reviewGuide().
 *
 * @author Luis Gabriel Santiago
 */
public class Review {

    private final int reviewId;
    private final Guide guide;
    private final User reviewer;
    private final Boolean isApproved; // null: a comment without a vote
    private final String comment;
    private final Date createdAt;

    public Review(int reviewId, Guide guide, User reviewer, Boolean isApproved, String comment, Date createdAt) {
        this.reviewId = reviewId;
        this.guide = guide;
        this.reviewer = reviewer;
        this.isApproved = isApproved;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    /** All reviews of a guide, newest first, for the guide page. */
    public static List<Review> findByGuide(Guide guide) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.review_id, r.user_id, u.username, u.full_name, u.role, r.is_approved, r.comment, r.created_at "
                + "FROM reviews r JOIN users u ON u.user_id = r.user_id "
                + "WHERE r.guide_id = ? ORDER BY r.created_at DESC, r.review_id DESC";
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, guide.getGuideId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User reviewer = new User();
                    reviewer.setUserId(rs.getInt("user_id"));
                    reviewer.setUsername(rs.getString("username"));
                    reviewer.setFullName(rs.getString("full_name"));
                    reviewer.setRole(rs.getString("role"));
                    boolean vote = rs.getBoolean("is_approved");
                    Boolean approved = rs.wasNull() ? null : vote;
                    reviews.add(new Review(rs.getInt("review_id"), guide, reviewer,
                            approved, rs.getString("comment"), rs.getTimestamp("created_at")));
                }
            }
        }
        return reviews;
    }

    public int getReviewId() {
        return reviewId;
    }

    public Guide getGuide() {
        return guide;
    }

    public User getReviewer() {
        return reviewer;
    }

    /** True only for an approve vote. */
    public boolean isApproved() {
        return Boolean.TRUE.equals(isApproved);
    }

    /** False for a comment without a vote. */
    public boolean hasVote() {
        return isApproved != null;
    }

    public boolean hasComment() {
        return comment != null && !comment.isBlank();
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return (isApproved == null ? "Comment" : isApproved ? "Approve" : "Disapprove") + " from " + reviewer.getUsername()
                + (comment == null ? "" : ": \"" + comment + "\"");
    }
}
