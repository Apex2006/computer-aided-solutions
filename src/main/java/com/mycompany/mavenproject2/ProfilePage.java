package com.mycompany.mavenproject2;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * My profile: account details, niches (editable), and expert status with
 * the stratified expert rating breakdown.
 *
 * @author Luis Gabriel Santiago
 */
public final class ProfilePage {

    private ProfilePage() {
    }

    public static Node create() {
        User user = Session.user();
        VBox page = new VBox();
        Node view = Cards.scroll(page);
        page.getChildren().add(Ui.label("My Profile", "h1"));

        HBox columns = new HBox(22, account(user), expert(user));
        page.getChildren().add(columns);
        return view;
    }

    private static Node account(User user) {
        VBox card = Ui.card(24, 14);
        HBox.setHgrow(card, Priority.ALWAYS);
        HBox chips = Ui.row(8, Ui.chip(User.identityLabel(user.getIdentity()), Cards.identityIcon(user.getIdentity()),
                "chip-gray"));
        if (user.isExpert()) {
            chips.getChildren().add(Ui.chip("Expert", "badge-check", "chip-blue"));
        }
        if (user.isAdmin()) {
            chips.getChildren().add(Ui.chip("Admin", "shield", "chip-blue"));
        }
        VBox who = new VBox(4, Ui.label(user.getFullName(), "h2"), Ui.label("@" + user.getUsername(), "muted"),
                Ui.label(user.getEmail(), "muted", "small"), chips);
        HBox top = new HBox(18, Ui.avatar(user.getInitials(), 72), who);
        top.setAlignment(Pos.CENTER_LEFT);

        FlowPane niches = new FlowPane(8, 8);
        List<ToggleButton> toggles = new ArrayList<>();
        for (String category : Guide.CATEGORIES) {
            ToggleButton chip = new ToggleButton(Guide.label(category));
            chip.setGraphic(Icons.icon(Categories.style(category).icon(), 15));
            chip.setGraphicTextGap(7);
            chip.getStyleClass().add("toggle-chip");
            chip.setUserData(category);
            chip.setSelected(user.getInterests().contains(category));
            toggles.add(chip);
            niches.getChildren().add(chip);
        }
        Button save = Ui.primary("Save niches", "check");
        save.setOnAction(e -> {
            List<String> picked = new ArrayList<>();
            for (ToggleButton chip : toggles) {
                if (chip.isSelected()) {
                    picked.add((String) chip.getUserData());
                }
            }
            if (picked.isEmpty()) {
                Ui.info("Pick a niche", "Pick at least one niche so Home can recommend guides.");
                return;
            }
            Ui.async(() -> {
                user.chooseInterests(picked);
                return true;
            }, ok -> Nav.toast("Niches saved. Home now shows guides from them."));
        });
        Button logout = Ui.outline("Log out", "log-out");
        logout.setOnAction(e -> Nav.logout());

        card.getChildren().addAll(top, new VBox(4, Ui.label("Your niches", "h3"),
                Ui.label("Home recommends guides from these categories.", "muted", "small")), niches,
                Ui.row(10, save, Ui.spacer(), logout));
        return card;
    }

    private static Node expert(User user) {
        VBox card = Ui.card(24, 12);
        card.setMinWidth(380);
        card.setPrefWidth(420);
        card.setMaxWidth(420);
        card.getChildren().add(Ui.row(8, Icons.icon("shield-check", 20, "#1D4FA8"), Ui.label("Expert status", "h2")));
        if (user.isAdmin()) {
            card.getChildren().add(Ui.wrap("Admins add and remove experts from the Admin Panel.", "muted"));
            return card;
        }
        if (user.isExpert() && "ADMIN".equals(user.getExpertSource())) {
            card.getChildren().addAll(Ui.row(8, Ui.chip("Expert", "badge-check", "chip-blue"),
                    Ui.label("Admin approved", "bold")),
                    Ui.wrap("Credential: " + (user.getCredential() == null ? "not recorded" : user.getCredential()), "muted"),
                    Ui.wrap("Admin-added experts keep the badge unless an admin removes it.", "muted", "small"));
            return card;
        }
        card.getChildren().add(Ui.wrap(user.isExpert()
                ? "You earned the expert badge from votes. Keep your stratified approval rate above 66.7% to keep it."
                : "Earn the expert badge when people from every identity group approve your guides and your "
                        + "stratified approval rate reaches 80%.", "muted"));
        VBox breakdown = new VBox(4);
        breakdown.setStyle("-fx-background-color: #F5F5F0; -fx-background-radius: 10;");
        breakdown.setPadding(new Insets(12, 14, 12, 14));
        breakdown.getChildren().add(Ui.loading());
        card.getChildren().addAll(breakdown, Ui.wrap(
                "Votes needed: n = N / (1 + N × 0.05²), where N is the number of eligible voters. "
                + "Each identity group must give its share of n votes.", "faint", "small"));
        Ui.async(user::expertSummary, summary -> {
            breakdown.getChildren().clear();
            for (String line : summary.split("\\R")) {
                Label label = Ui.wrap(line.trim(), line.startsWith("  ") ? "small" : "bold");
                breakdown.getChildren().add(label);
            }
        });
        return card;
    }
}
