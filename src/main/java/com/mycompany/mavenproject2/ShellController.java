package com.mycompany.mavenproject2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The main window frame (shell.fxml): logo, search, Create Guide button and
 * account menu on top, the menu on the left, and the current page.
 *
 * @author Luis Gabriel Santiago
 */
public class ShellController {

    @FXML private HBox logoBox;
    @FXML private HBox searchBox;
    @FXML private TextField searchField;
    @FXML private Button createButton;
    @FXML private HBox accountBox;
    @FXML private VBox navList;
    @FXML private Label roleNote;
    @FXML private StackPane content;

    private final Map<String, HBox> items = new LinkedHashMap<>();
    private final Map<String, StackPane> badges = new LinkedHashMap<>();
    private String active = "home";

    @FXML
    private void initialize() {
        HBox logo = BrandPanel.logo(false);
        logo.setOnMouseClicked(e -> Nav.home());
        logo.setStyle("-fx-cursor: hand;");
        logoBox.getChildren().add(logo);
        searchBox.getChildren().add(0, Icons.icon("search", 16));
        searchField.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                Nav.search(query);
            }
        });
        createButton.setGraphic(Icons.icon("plus", 16));
        createButton.setGraphicTextGap(8);
        build();
    }

    /** Rebuilds the menu and account area for the current user. */
    private void build() {
        items.clear();
        badges.clear();
        navList.getChildren().clear();
        addItem("home", "house", "Home", Nav::home);
        addItem("browse", "layout-grid", "Browse Categories", () -> Nav.browse(null));
        if (!Session.isGuest()) {
            addItem("mine", "book-open", "My Guides", Nav::myGuides);
        }
        if (Session.isExpert()) {
            addItem("expert", "shield-check", "Expert Drafts", Nav::expertDrafts);
        }
        if (Session.isAdmin()) {
            addItem("admin", "users", "Admin Panel", Nav::admin);
        }
        highlight();
        buildAccount();
        roleNote.setText(Session.isGuest()
                ? "Browsing as a guest. Log in to vote, comment and publish."
                : "Logged in as " + Ui.capitalize(Session.user().getRole()).toLowerCase() + ".");
        loadCounts();
    }

    private void addItem(String key, String icon, String text, Runnable action) {
        StackPane badge = new StackPane();
        Label name = Ui.label(text);
        name.setMinWidth(Region.USE_PREF_SIZE);
        HBox item = new HBox(Icons.icon(icon, 18), name, Ui.spacer(), badge);
        item.getStyleClass().add("nav-item");
        item.setOnMouseClicked(e -> action.run());
        items.put(key, item);
        badges.put(key, badge);
        navList.getChildren().add(item);
    }

    private void buildAccount() {
        accountBox.getChildren().clear();
        if (Session.isGuest()) {
            Button login = Ui.outline("Log in", "user");
            login.setOnAction(e -> Nav.showLogin());
            accountBox.getChildren().add(login);
            return;
        }
        User user = Session.user();
        MenuButton menu = new MenuButton();
        HBox face = new HBox(8, Ui.avatar(user.getInitials(), 36));
        face.setAlignment(javafx.geometry.Pos.CENTER);
        menu.setGraphic(face);
        MenuItem profile = new MenuItem("My profile");
        profile.setOnAction(e -> Nav.profile());
        MenuItem logout = new MenuItem("Log out");
        logout.setOnAction(e -> Nav.logout());
        menu.getItems().addAll(profile, logout);
        VBox name = new VBox(0, Ui.label(user.getFullName(), "bold", "small"),
                Ui.label("@" + user.getUsername(), "faint", "tiny"));
        name.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        accountBox.getChildren().addAll(name, menu);
    }

    /** Count badges: own guides, and pending guides for experts. */
    private void loadCounts() {
        if (Session.isGuest()) {
            return;
        }
        User user = Session.user();
        Ui.async(() -> {
            int mine = Guide.findByAuthor(user).size();
            int pending = 0;
            if (user.isExpert()) {
                for (Guide guide : Guide.findPending()) {
                    if (guide.getAuthor().getUserId() != user.getUserId()) {
                        pending++;
                    }
                }
            }
            return List.of(mine, pending);
        }, counts -> {
            setBadge("mine", counts.get(0));
            setBadge("expert", counts.get(1));
        });
    }

    private void setBadge(String key, int count) {
        StackPane holder = badges.get(key);
        if (holder != null) {
            holder.getChildren().setAll(count > 0 ? List.of(Ui.countBadge(count)) : List.of());
        }
    }

    /** Reloads the user (the role may have changed) and rebuilds the menu. */
    public void refresh() {
        if (Session.isGuest()) {
            build();
            return;
        }
        Ui.async(() -> {
            Session.user().refresh();
            return true;
        }, ok -> build());
    }

    public void show(String key, Node page) {
        active = key;
        highlight();
        content.getChildren().setAll(page);
    }

    public String activeKey() {
        return active;
    }

    Node currentPage() {
        return content.getChildren().isEmpty() ? content : content.getChildren().get(0);
    }

    private void highlight() {
        items.forEach((key, item) -> {
            item.getStyleClass().remove("nav-item-active");
            if (key.equals(active)) {
                item.getStyleClass().add("nav-item-active");
            }
        });
    }

    @FXML
    private void onCreateGuide() {
        Nav.builder(null);
    }
}
