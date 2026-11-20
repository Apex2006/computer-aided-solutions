package com.mycompany.mavenproject2;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Admin Panel (admins only), as on the board: account management, guide
 * management and reports (Sequence 5).
 *
 * @author Luis Gabriel Santiago
 */
public final class AdminPage {

    private record Data(List<User> users, List<Guide> guides, List<Report> reports) {
    }

    private final VBox page = new VBox();
    private final TabPane tabs = new TabPane();

    private AdminPage() {
    }

    public static Node create() {
        AdminPage admin = new AdminPage();
        Node view = Cards.scroll(admin.page);
        admin.load(0);
        return view;
    }

    private User admin() {
        return Session.user();
    }

    private void load(int tabIndex) {
        page.getChildren().setAll(header(), Ui.loading());
        Ui.async(() -> new Data(User.findAll(), Guide.findForAdmin(), Report.findOpen()), data -> {
            tabs.getTabs().setAll(
                    tab("Accounts (" + data.users().size() + ")", accounts(data.users())),
                    tab("Guides (" + data.guides().size() + ")", guides(data.guides())),
                    tab("Reports (" + data.reports().size() + ")", reports(data.reports())));
            tabs.getSelectionModel().select(tabIndex);
            page.getChildren().setAll(header(), tabs);
        });
    }

    private Node header() {
        return new VBox(4, Ui.label("Admin Panel", "h1"),
                Ui.label("Manage accounts and experts, guides, and reports and edit requests.", "muted"));
    }

    private static Tab tab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private static VBox listCard() {
        VBox card = Ui.card(8, 0);
        card.setPadding(new Insets(6, 20, 6, 20));
        return card;
    }

    private static HBox row(Node... nodes) {
        HBox row = new HBox(14, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("parts-row");
        row.setPadding(new Insets(12, 0, 12, 0));
        return row;
    }

    /** Runs an admin action, shows a notice, then reloads the panel on the same tab. */
    private void act(Callable<?> action, String done) {
        int tab = tabs.getSelectionModel().getSelectedIndex();
        Ui.async(() -> {
            action.call();
            return true;
        }, ok -> {
            Nav.toast(done);
            load(tab);
        });
    }

    // ------------------------------------------------------------------ accounts

    private Node accounts(List<User> users) {
        VBox card = listCard();
        for (User user : users) {
            VBox text = new VBox(3, Ui.label(user.getFullName(), "bold"),
                    Ui.label("@" + user.getUsername() + "  ·  " + user.getEmail(), "muted", "small"));
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox roleChip;
            if (user.isAdmin()) {
                roleChip = Ui.chip("Admin", "shield", "chip-blue");
            } else if (user.isExpert()) {
                roleChip = Ui.chip("ADMIN".equals(user.getExpertSource()) ? "Expert (admin added)" : "Expert (earned)",
                        "badge-check", "chip-expert");
            } else {
                roleChip = Ui.chip("User", null, "chip-gray");
            }
            boolean suspended = "SUSPENDED".equals(user.getAccountStatus());
            HBox status = suspended ? Ui.chip("Suspended", "ban", "chip-gray") : Ui.chip("Active", null, "chip-green");
            HBox actions = new HBox(6);
            actions.setAlignment(Pos.CENTER_RIGHT);
            actions.setMinWidth(260);
            if (user.isAdmin()) {
                actions.getChildren().add(Ui.label("Admins can't be changed here", "faint", "small"));
            } else {
                Button suspend = Ui.outline(suspended ? "Activate" : "Suspend", suspended ? "rotate-ccw" : "ban");
                suspend.setOnAction(e -> {
                    if (Ui.confirm(suspended ? "Activate account" : "Suspend account",
                            (suspended ? "Let @" : "Stop @") + user.getUsername()
                            + (suspended ? " log in again?" : " from logging in?"))) {
                        act(() -> {
                            if (suspended) {
                                admin().activateUser(user);
                            } else {
                                admin().suspendUser(user);
                            }
                            return null;
                        }, suspended ? "Account activated." : "Account suspended.");
                    }
                });
                Button expert;
                if (user.isExpert()) {
                    expert = Ui.button("Remove expert", null, "btn-text-danger");
                    expert.setOnAction(e -> {
                        if (Ui.confirm("Remove expert", "Remove the expert role from @" + user.getUsername() + "?")) {
                            act(() -> {
                                admin().removeExpert(user);
                                return null;
                            }, "Expert removed.");
                        }
                    });
                } else {
                    expert = Ui.button("Add expert", "badge-check", "btn-text");
                    expert.setOnAction(e -> {
                        Optional<String> credential = Ui.askText("Add an expert",
                                "Record why @" + user.getUsername() + " is an expert (required).",
                                "e.g. Professor, College of Engineering / Licensed electrician", true);
                        credential.ifPresent(proof -> act(() -> {
                            admin().addExpert(user, proof);
                            return null;
                        }, "Expert added."));
                    });
                }
                actions.getChildren().addAll(expert, suspend);
            }
            card.getChildren().add(row(Ui.avatar(user.getInitials(), 38), text,
                    Ui.chip(User.identityLabel(user.getIdentity()), Cards.identityIcon(user.getIdentity()), "chip-gray"),
                    roleChip, status, actions));
        }
        return card;
    }

    // ------------------------------------------------------------------ guides

    private Node guides(List<Guide> guides) {
        if (guides.isEmpty()) {
            return Ui.empty("book-open", "No guides yet", "Published and pending guides show up here.");
        }
        VBox card = listCard();
        for (Guide guide : guides) {
            User author = guide.getAuthor();
            VBox text = new VBox(3, Ui.label(guide.getTitle(), "bold"),
                    Ui.label("by @" + author.getUsername() + "  ·  " + Guide.label(guide.getCategory())
                            + "  ·  " + Ui.date(guide.getCreatedAt()), "muted", "small"));
            HBox.setHgrow(text, Priority.ALWAYS);
            Button open = Ui.outline("Open", null);
            open.setOnAction(e -> Nav.openGuide(guide.getGuideId()));
            boolean hidden = "HIDDEN".equals(guide.getStatus());
            Button toggle = Ui.button(hidden ? "Unhide" : "Hide", hidden ? "eye" : "eye-off",
                    hidden ? "btn-text" : "btn-text-danger");
            toggle.setOnAction(e -> act(() -> {
                if (hidden) {
                    admin().unhideGuide(guide);
                } else {
                    admin().hideGuide(guide);
                }
                return null;
            }, hidden ? "Guide is visible again." : "Guide hidden."));
            HBox actions = new HBox(6, toggle, open);
            actions.setAlignment(Pos.CENTER_RIGHT);
            actions.setMinWidth(170);
            card.getChildren().add(row(Cards.categorySquare(guide.getCategory(), 42), text,
                    Ui.riskChip(guide.getRiskFactor()), Ui.statusChip(guide.getStatus()), actions));
        }
        return card;
    }

    // ------------------------------------------------------------------ reports

    private Node reports(List<Report> reports) {
        if (reports.isEmpty()) {
            return Ui.empty("inbox", "No open reports", "Reports and edit requests from users show up here.");
        }
        VBox card = listCard();
        for (Report report : reports) {
            boolean edit = "EDIT_REQUEST".equals(report.getType());
            HBox type = edit ? Ui.chip("Edit request", "pencil", "chip-gray") : Ui.chip("Report", "flag", "chip-gray");
            if (!edit) {
                type.setStyle("-fx-background-color: #FDE4E4; -icon-color: #C62828;");
            }
            Button title = new Button(report.getGuide().getTitle());
            title.getStyleClass().add("btn-text");
            title.setStyle("-fx-padding: 0;");
            title.setOnAction(e -> Nav.openGuide(report.getGuide().getGuideId()));
            VBox text = new VBox(3, title, Ui.wrap("\"" + report.getReason() + "\""),
                    Ui.label("from @" + report.getReporter().getUsername() + "  ·  "
                            + Ui.date(report.getCreatedAt()), "muted", "small"));
            HBox.setHgrow(text, Priority.ALWAYS);
            Button resolve = Ui.primary("Resolve", "check");
            resolve.setOnAction(e -> act(() -> {
                admin().resolveReport(report);
                return null;
            }, "Marked as resolved."));
            Button dismiss = Ui.outline("Dismiss", "x");
            dismiss.setOnAction(e -> act(() -> {
                report.dismiss(admin());
                return null;
            }, "Dismissed."));
            HBox actions = new HBox(8, resolve, dismiss);
            actions.setAlignment(Pos.CENTER_RIGHT);
            card.getChildren().add(row(type, text, actions));
        }
        return card;
    }
}
