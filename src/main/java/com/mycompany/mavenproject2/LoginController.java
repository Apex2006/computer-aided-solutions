package com.mycompany.mavenproject2;

import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Login screen (login.fxml): log in with username or email, or continue as
 * a guest who can only browse.
 *
 * @author Luis Gabriel Santiago
 */
public class LoginController {

    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginController.class);

    @FXML private HBox userBox;
    @FXML private HBox passwordBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordShown;
    @FXML private Button eyeButton;
    @FXML private CheckBox rememberBox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button guestButton;

    @FXML
    private void initialize() {
        userBox.getChildren().add(0, Icons.icon("user", 16));
        passwordBox.getChildren().add(0, Icons.icon("lock", 16));
        eyeButton.setGraphic(Icons.icon("eye", 16));
        guestButton.setGraphic(Icons.icon("eye", 16));
        guestButton.setGraphicTextGap(8);
        passwordShown.textProperty().bindBidirectional(passwordField.textProperty());
        String remembered = PREFS.get("username", "");
        if (!remembered.isEmpty()) {
            usernameField.setText(remembered);
            rememberBox.setSelected(true);
        }
    }

    @FXML
    private void onTogglePassword() {
        boolean show = !passwordShown.isVisible();
        passwordShown.setVisible(show);
        passwordShown.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
        eyeButton.setGraphic(Icons.icon(show ? "eye-off" : "eye", 16));
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Enter your username or email and your password.");
            return;
        }
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
        hideError();
        User user = new User();
        Ui.async(() -> {
            try {
                return user.login(username, password) ? "OK" : "Wrong username or password.";
            } catch (IllegalStateException e) {
                return e.getMessage();
            }
        }, result -> {
            loginButton.setDisable(false);
            loginButton.setText("Log In");
            if (!"OK".equals(result)) {
                showError(result);
                return;
            }
            if (rememberBox.isSelected()) {
                PREFS.put("username", username);
            } else {
                PREFS.remove("username");
            }
            Session.start(user);
            Nav.enterApp();
            Nav.toast("Welcome back, " + firstName(user) + "!");
        }, () -> {
            loginButton.setDisable(false);
            loginButton.setText("Log In");
        });
    }

    @FXML
    private void onGuest() {
        Session.start(null);
        Nav.enterApp();
    }

    @FXML
    private void onRegister() {
        Nav.showRegister(null);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    static String firstName(User user) {
        String name = user.getFullName() == null ? user.getUsername() : user.getFullName();
        return name.trim().split("\\s+")[0];
    }
}
