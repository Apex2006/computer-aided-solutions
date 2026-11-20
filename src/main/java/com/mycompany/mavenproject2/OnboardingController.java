package com.mycompany.mavenproject2;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * Registration step 2 (onboarding.fxml): pick an identity and niches, then
 * save the account (Sequence 1: save the new user, then identity and
 * interests).
 *
 * @author Luis Gabriel Santiago
 */
public class OnboardingController {

    private static final String[][] IDENTITIES = {
        {"STUDENT", "graduation-cap", "Student", "Learning and building for school"},
        {"PROFESSIONAL", "briefcase", "Professional", "Working in a technical field"},
        {"HOBBYIST", "hammer", "Hobbyist", "Tinkering for fun at home"},
        {"EDUCATOR", "presentation", "Educator", "Teaching or training others"},
    };

    @FXML private TilePane identityPane;
    @FXML private FlowPane nichePane;
    @FXML private Label errorLabel;
    @FXML private Button createButton;

    private final ToggleGroup identityGroup = new ToggleGroup();
    private final List<ToggleButton> niches = new ArrayList<>();
    private RegisterController.Draft draft;

    @FXML
    private void initialize() {
        for (String[] identity : IDENTITIES) {
            VBox text = new VBox(2, Ui.label(identity[2], "bold"), Ui.label(identity[3], "muted", "small"));
            HBox content = new HBox(12, Icons.icon(identity[1], 22), text);
            content.setAlignment(Pos.CENTER_LEFT);
            ToggleButton card = new ToggleButton();
            card.setGraphic(content);
            card.setUserData(identity[0]);
            card.setToggleGroup(identityGroup);
            card.getStyleClass().add("identity-card");
            card.setMaxWidth(Double.MAX_VALUE);
            card.setAlignment(Pos.CENTER_LEFT);
            identityPane.getChildren().add(card);
        }
        for (String category : Guide.CATEGORIES) {
            ToggleButton chip = new ToggleButton(Guide.label(category));
            chip.setGraphic(Icons.icon(Categories.style(category).icon(), 15));
            chip.setGraphicTextGap(7);
            chip.setUserData(category);
            chip.getStyleClass().add("toggle-chip");
            niches.add(chip);
            nichePane.getChildren().add(chip);
        }
    }

    public void setDraft(RegisterController.Draft draft) {
        this.draft = draft;
    }

    @FXML
    private void onBack() {
        Nav.showRegister(draft);
    }

    @FXML
    private void onCreate() {
        if (identityGroup.getSelectedToggle() == null) {
            showError("Pick the identity that fits you best.");
            return;
        }
        List<String> picked = new ArrayList<>();
        for (ToggleButton chip : niches) {
            if (chip.isSelected()) {
                picked.add((String) chip.getUserData());
            }
        }
        if (picked.isEmpty()) {
            showError("Pick at least one niche.");
            return;
        }
        String identity = (String) identityGroup.getSelectedToggle().getUserData();
        createButton.setDisable(true);
        createButton.setText("Creating account...");
        Ui.async(() -> {
            User account = new User(draft.username(), draft.email(), draft.password(), draft.fullName(), identity);
            account.register();
            User user = new User();
            user.login(draft.username(), draft.password());
            user.chooseInterests(picked);
            return user;
        }, user -> {
            Session.start(user);
            Nav.enterApp();
            Nav.toast("Account created. Welcome, " + LoginController.firstName(user) + "!");
        }, () -> {
            createButton.setDisable(false);
            createButton.setText("Create account");
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
