package com.mycompany.mavenproject2;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Registration step 1 (register.fxml): account details. Step 2 picks the
 * identity and niches, then the account is saved (Sequence 1).
 *
 * @author Luis Gabriel Santiago
 */
public class RegisterController {

    /** What the user typed on step 1, carried to step 2. */
    public record Draft(String fullName, String username, String email, String password) {
    }

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Button nextButton;

    /** Fills the form again when the user comes back from step 2. */
    public void restore(Draft draft) {
        if (draft == null) {
            return;
        }
        fullNameField.setText(draft.fullName());
        usernameField.setText(draft.username());
        emailField.setText(draft.email());
        passwordField.setText(draft.password());
        confirmField.setText(draft.password());
    }

    @FXML
    private void onNext() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String problem = null;
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            problem = "Please fill in every field.";
        } else if (!username.matches("[A-Za-z0-9._]{3,50}")) {
            problem = "Usernames use 3 to 50 letters, numbers, dots or underscores.";
        } else if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            problem = "Enter a valid email address.";
        } else if (password.length() < 8) {
            problem = "Passwords need at least 8 characters.";
        } else if (!password.equals(confirmField.getText())) {
            problem = "The two passwords don't match.";
        }
        if (problem != null) {
            showError(problem);
            return;
        }
        nextButton.setDisable(true);
        Ui.async(() -> User.findByUsername(username) != null, taken -> {
            nextButton.setDisable(false);
            if (taken) {
                showError("That username is already taken. Try another one.");
            } else {
                Nav.showOnboarding(new Draft(fullName, username, email, password));
            }
        }, () -> nextButton.setDisable(false));
    }

    @FXML
    private void onLogin() {
        Nav.showLogin();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
