package com.mycompany.mavenproject2.ui;

import com.mycompany.mavenproject2.backend.Guide;
import com.mycompany.mavenproject2.backend.Database;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Moves the user between screens: the login screens, and the pages inside
 * the main window (Home, Browse, a guide, the Guide Builder and so on).
 *
 * @author Luis Gabriel Santiago
 */
public final class Nav {

    private static Stage stage;
    private static StackPane root;
    private static final StackPane SCREEN = new StackPane();
    private static ShellController shell;

    private Nav() {
    }

    public static void init(Stage window, StackPane appRoot) {
        stage = window;
        root = appRoot;
        root.getChildren().setAll(SCREEN);
    }

    public static Window window() {
        return stage;
    }

    // ------------------------------------------------------------------ before login

    /** Checks the database first, then shows the login screen. */
    public static void showStartup() {
        SCREEN.getChildren().setAll(Ui.loading());
        Ui.async(Database::testConnection, ok -> {
            if (ok) {
                showLogin();
            } else {
                showOffline();
            }
        }, Nav::showOffline);
    }

    private static void showOffline() {
        VBox box = new VBox(14,
                Ui.iconCircle("wifi-off", 64, false),
                Ui.label("Can't reach the database", "h1"),
                Ui.wrap("Check your internet connection and try again. If it still fails, check the "
                        + "database address and password in db.properties.", "muted"));
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(460);
        var retry = Ui.primary("Try again", "refresh-cw");
        retry.setOnAction(e -> showStartup());
        box.getChildren().add(retry);
        SCREEN.getChildren().setAll(new StackPane(box));
    }

    public static void showLogin() {
        shell = null;
        SCREEN.getChildren().setAll(load("login.fxml").root());
    }

    public static void showRegister(RegisterController.Draft draft) {
        Loaded loaded = load("register.fxml");
        ((RegisterController) loaded.controller()).restore(draft);
        SCREEN.getChildren().setAll(loaded.root());
    }

    public static void showOnboarding(RegisterController.Draft draft) {
        Loaded loaded = load("onboarding.fxml");
        ((OnboardingController) loaded.controller()).setDraft(draft);
        SCREEN.getChildren().setAll(loaded.root());
    }

    // ------------------------------------------------------------------ the main window

    /** Shows the main window (top bar and menu) with the Home page. */
    public static void enterApp() {
        Loaded loaded = load("shell.fxml");
        shell = (ShellController) loaded.controller();
        SCREEN.getChildren().setAll(loaded.root());
        home();
    }

    public static void home() {
        shell.show("home", HomePage.create());
    }

    public static void browse(String category) {
        shell.show("browse", BrowsePage.create(category, null));
    }

    public static void search(String query) {
        shell.show("browse", BrowsePage.create(null, query));
    }

    public static void openGuide(int guideId) {
        shell.show(shell.activeKey(), GuidePage.create(guideId));
    }

    /** Opens the Guide Builder, empty for a new guide or filled for an edit. */
    public static void builder(Guide existing) {
        if (!requireLogin("create guides")) {
            return;
        }
        Loaded loaded = load("builder.fxml");
        ((BuilderController) loaded.controller()).edit(existing);
        shell.show(existing == null ? "create" : "mine", loaded.root());
    }

    public static void myGuides() {
        shell.show("mine", MyGuidesPage.create());
    }

    public static void expertDrafts() {
        shell.show("expert", ExpertDraftsPage.create());
    }

    public static void admin() {
        shell.show("admin", AdminPage.create());
    }

    public static void profile() {
        shell.show("profile", ProfilePage.create());
    }

    /** Updates the menu after something changed (a new guide, a new expert role). */
    public static void refreshShell() {
        if (shell != null) {
            shell.refresh();
        }
    }

    /** Guests are asked to log in first. Returns true when a user is logged in. */
    public static boolean requireLogin(String action) {
        if (!Session.isGuest()) {
            return true;
        }
        if (Ui.confirm("Log in needed", "Guests can only browse and read guides. Log in to " + action + "?")) {
            showLogin();
        }
        return false;
    }

    public static void logout() {
        Session.end();
        showLogin();
        toast("You're logged out.");
    }

    // ------------------------------------------------------------------ small notice at the bottom

    public static void toast(String message) {
        HBox toast = new HBox(Icons.icon("circle-check", 16), Ui.label(message));
        toast.getStyleClass().add("toast");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        toast.setMouseTransparent(true);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 28, 0));
        root.getChildren().add(toast);
        FadeTransition in = new FadeTransition(Duration.millis(180), toast);
        in.setFromValue(0);
        in.setToValue(1);
        FadeTransition out = new FadeTransition(Duration.millis(400), toast);
        out.setToValue(0);
        SequentialTransition show = new SequentialTransition(in, new PauseTransition(Duration.seconds(2.6)), out);
        show.setOnFinished(e -> root.getChildren().remove(toast));
        show.play();
    }

    // ------------------------------------------------------------------ FXML loading

    private record Loaded(Parent root, Object controller) {
    }

    private static Loaded load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Nav.class.getResource(fxml));
            Parent parent = loader.load();
            return new Loaded(parent, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Could not open the screen " + fxml + ": " + e.getMessage(), e);
        }
    }

    /** For pages: the node currently in the page area, used by screenshots and tests. */
    static Node currentPage() {
        return shell == null ? SCREEN : shell.currentPage();
    }
}
