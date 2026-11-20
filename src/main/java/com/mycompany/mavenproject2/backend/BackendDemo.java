package com.mycompany.mavenproject2.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Console demo of the backend for the Repository of Computer-Aided Solutions
 * for Common Everyday Problems. Run this file on its own (Run File in NetBeans);
 * the app itself starts from Mavenproject2.
 *
 * It runs every backend feature against cas_db in the order of the sequence
 * flowcharts, then removes the rows it created so it can be run again.
 * The JavaFX screens call these same classes.
 *
 * @author Luis Gabriel Santiago
 */
public class BackendDemo {

    private static final List<Integer> newGuides = new ArrayList<>();
    private static final List<Integer> newReviews = new ArrayList<>();
    private static final List<Integer> newReports = new ArrayList<>();
    private static final List<Integer> newUsers = new ArrayList<>();
    private static final List<Integer> promotedUsers = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== Computer-Aided Solutions: Backend Progress Demo ===");
        heading("1. Database connection");
        if (!Database.testConnection()) {
            System.out.println("    Could not reach the database at " + Database.getHost()
                    + ". Check that MySQL is running and db.properties is correct.");
            return;
        }
        System.out.println("    Connected to cas_db on " + Database.getHost());
        try {
            runDemo();
        } catch (SQLException | RuntimeException e) {
            System.out.println("    Demo stopped: " + e.getMessage());
        } finally {
            cleanUp();
        }
    }

    private static void runDemo() throws SQLException {
        // ---------------------------------------------------------------- Sequence 1: login
        heading("2. Login (Sequence 1)");
        User admin = login("admin", "Admin@2026");
        User professor = login("prof_sample", "Expert@2026");
        User student = login("student_test", "User@2026");
        User pro = login("pro_test", "User@2026");
        User hobbyist = login("hobby_test", "User@2026");
        login("educator_test", "User@2026");
        User stranger = new User();
        System.out.println("    student_test with a wrong password: "
                + (stranger.login("student_test", "wrongpass1") ? "accepted" : "rejected"));
        System.out.println("    prof_sample badge: Expert, " + professor.getExpertBadgeDetail());

        // ---------------------------------------------------------------- Browsing
        heading("3. Browsing, search and dashboard");
        System.out.println("    Published guides:");
        for (Guide guide : Guide.findPublished()) {
            System.out.println("      " + guide);
        }
        System.out.println("    Search \"door alarm\":");
        for (Guide guide : Guide.search("door alarm")) {
            System.out.println("      " + guide);
        }
        System.out.println("    Dashboard for student_test, niches " + student.getInterests() + ":");
        for (Guide guide : Guide.findForInterests(student)) {
            System.out.println("      " + guide);
        }

        // ---------------------------------------------------------------- Sequence 2: guides and drafts
        heading("4. Creating a guide and saving drafts (Sequence 2)");
        Guide draft = new Guide("Cleaning a Laptop Fan", "HOUSEKEEPING", "LOW");
        hobbyist.saveDraft(draft);
        newGuides.add(draft.getGuideId());
        System.out.println("    Save Draft   -> " + draft);

        Guide unfinished = new Guide("Half-written guide", "STUDIES", "LOW");
        try {
            hobbyist.submitGuide(unfinished);
        } catch (IllegalArgumentException e) {
            System.out.println("    Submit an unfinished guide -> blocked: " + e.getMessage());
        }

        Guide fan = new Guide("Replacing a Ceiling Fan Switch", "HOUSEKEEPING", "HIGH");
        fan.setSummary("Replace a broken pull-chain switch on a ceiling fan.");
        fan.setPartsList("Replacement pull-chain switch\nScrewdriver\nWire connectors\nVoltage tester");
        fan.setPrecautions("Turn off the breaker first.\nCheck with a voltage tester before touching wires.");
        fan.setSteps("1. Turn off the breaker.\n2. Remove the switch housing.\n"
                + "3. Move the wires to the new switch one at a time.\n4. Close the housing and test.");
        fan.setDifficulty("INTERMEDIATE");
        fan.setEstimatedCost(220);
        hobbyist.submitGuide(fan);
        newGuides.add(fan.getGuideId());
        System.out.println("    Submit a HIGH risk guide as a normal user -> " + fan.getStatus());

        // ---------------------------------------------------------------- Sequence 4: expert approval
        heading("5. Expert approval (Sequence 4)");
        System.out.println("    Pending guides:");
        for (Guide guide : Guide.findPending()) {
            System.out.println("      " + guide);
        }
        try {
            student.approveGuide(fan, "HIGH", true);
        } catch (IllegalStateException e) {
            System.out.println("    student_test tries to approve -> blocked: " + e.getMessage());
        }
        professor.approveGuide(fan, "HIGH", true);
        System.out.println("    prof_sample approves (risk HIGH, Y) -> " + fan.getStatus()
                + ", approval " + fan.getApproval());
        try {
            professor.approveGuide(fan, "HIGH", false);
        } catch (IllegalStateException e) {
            System.out.println("    A second decision on the same guide -> blocked: " + e.getMessage());
        }

        // ---------------------------------------------------------------- Sequence 3: reviews and expert rating
        heading("6. Reviews and the stratified expert rating (Sequence 3)");
        Guide plant = Guide.findById(1);
        System.out.println("    Guide: " + plant);
        for (Review review : Review.findByGuide(plant)) {
            System.out.println("      " + review);
        }
        try {
            hobbyist.reviewGuide(plant, true, "My own guide is great");
        } catch (IllegalStateException e) {
            System.out.println("    hobby_test reviews own guide -> blocked: " + e.getMessage());
        }
        try {
            student.reviewGuide(plant, false, "Second vote");
        } catch (IllegalStateException e) {
            System.out.println("    student_test reviews again -> blocked: " + e.getMessage());
        }
        System.out.println("    Expert check for hobby_test before new votes:");
        printIndented(hobbyist.expertSummary());
        System.out.println("    Role: " + User.findById(hobbyist.getUserId()).getRole());

        newReviews.add(admin.reviewGuide(plant, true, "Solid beginner project.").getReviewId());
        newReviews.add(professor.reviewGuide(plant, true, "Safe low-voltage build.").getReviewId());
        System.out.println("    admin and prof_sample approve the guide.");
        System.out.println("    Expert check for hobby_test after new votes:");
        printIndented(hobbyist.expertSummary());
        User updated = User.findById(hobbyist.getUserId());
        if (updated.isExpert()) {
            promotedUsers.add(updated.getUserId());
        }
        System.out.println("    Role: " + updated.getRole() + " (" + updated.getExpertBadgeDetail() + ")");

        // ---------------------------------------------------------------- Sequence 1: registration
        heading("7. Registration (Sequence 1)");
        User newcomer = new User("demo_newcomer", "newcomer@cas.local", "Welcome@2026", "Demo Newcomer", "STUDENT");
        newcomer.register();
        newUsers.add(newcomer.getUserId());
        System.out.println("    Registered demo_newcomer as user #" + newcomer.getUserId());
        User signedIn = new User();
        signedIn.login("demo_newcomer", "Welcome@2026");
        signedIn.chooseInterests(List.of("PETS", "STUDIES"));
        System.out.println("    Logged in and picked niches " + signedIn.getInterests());
        try {
            new User("demo_newcomer", "other@cas.local", "Welcome@2026", "Copycat", "HOBBYIST").register();
        } catch (IllegalArgumentException e) {
            System.out.println("    Same username again -> blocked: " + e.getMessage());
        }

        // ---------------------------------------------------------------- Sequence 3 + 5: reports and admin
        heading("8. Reports and admin management (Sequence 3 and 5)");
        Guide alarm = Guide.findById(2);
        Report report = student.reportGuide(alarm, "REPORT", "Step 2 does not say which pin the buzzer uses.");
        Report request = hobbyist.reportGuide(alarm, "EDIT_REQUEST", "Please add a photo of the finished alarm.");
        newReports.add(report.getReportId());
        newReports.add(request.getReportId());
        System.out.println("    Open reports and edit requests:");
        for (Report open : Report.findOpen()) {
            System.out.println("      " + open);
        }
        admin.resolveReport(report);
        request.dismiss(admin);
        System.out.println("    admin resolves #" + report.getReportId() + " -> " + report.getStatus()
                + ", dismisses #" + request.getReportId() + " -> " + request.getStatus());

        admin.addExpert(student, "Demo: licensed electrician");
        System.out.println("    admin adds student_test as an expert -> " + student.getRole()
                + " (" + student.getExpertBadgeDetail() + ")");
        admin.removeExpert(student);
        System.out.println("    admin removes the expert -> " + student.getRole());

        admin.hideGuide(draft);
        System.out.println("    admin hides guide #" + draft.getGuideId() + " -> " + draft.getStatus());
        try {
            student.hideGuide(alarm);
        } catch (IllegalStateException e) {
            System.out.println("    student_test tries to hide a guide -> blocked: " + e.getMessage());
        }

        admin.suspendUser(newcomer);
        try {
            new User().login("demo_newcomer", "Welcome@2026");
        } catch (IllegalStateException e) {
            System.out.println("    admin suspends demo_newcomer, login -> blocked: " + e.getMessage());
        }

        // ---------------------------------------------------------------- The math
        heading("9. Votes needed as the app grows (5% margin of error)");
        System.out.println("    Users N    Votes needed n");
        for (int n : new int[]{50, 100, 500, 1000, 5000, 10000}) {
            System.out.printf("    %7d    %d%n", n, User.requiredVotes(n));
        }
        int needed = User.requiredVotes(1000);
        System.out.printf("    Example quotas at N = 1000 (400 students, 300 professionals, 200 hobbyists, 100 educators):"
                + "%n    %d, %d, %d, %d votes%n",
                User.groupQuota(needed, 400, 1000), User.groupQuota(needed, 300, 1000),
                User.groupQuota(needed, 200, 1000), User.groupQuota(needed, 100, 1000));
    }

    private static User login(String username, String password) throws SQLException {
        User user = new User();
        if (!user.login(username, password)) {
            throw new IllegalStateException("Login failed for " + username + ". Did you run seed.sql?");
        }
        System.out.printf("    %-14s logged in as %s, identity %s%n", username, user.getRole(), user.getIdentity());
        return user;
    }

    /** Removes everything the demo added, so the database is back to the seed data. */
    private static void cleanUp() {
        heading("Cleaning up");
        try (Connection c = Database.getConnection()) {
            run(c, "DELETE FROM reviews WHERE review_id = ?", newReviews);
            run(c, "DELETE FROM reports WHERE report_id = ?", newReports);
            run(c, "DELETE FROM guides WHERE guide_id = ?", newGuides);
            run(c, "DELETE FROM users WHERE user_id = ?", newUsers);
            run(c, "UPDATE users SET role = 'USER', expert_source = NULL, credential = NULL, expert_since = NULL "
                    + "WHERE user_id = ?", promotedUsers);
            System.out.println("    Demo rows removed. The database is back to the seed data.");
        } catch (SQLException e) {
            System.out.println("    Could not clean up: " + e.getMessage());
        }
    }

    private static void run(Connection c, String sql, List<Integer> ids) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int id : ids) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    private static void heading(String title) {
        System.out.println();
        System.out.println("[" + title + "]");
    }

    private static void printIndented(String text) {
        for (String line : text.split("\\R")) {
            System.out.println("      " + line);
        }
    }
}
