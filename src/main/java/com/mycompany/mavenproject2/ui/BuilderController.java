package com.mycompany.mavenproject2.ui;

import com.mycompany.mavenproject2.backend.User;
import com.mycompany.mavenproject2.backend.Guide;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 * Guide Builder (builder.fxml): fill in a guide, then Save Draft or Submit.
 * A high-risk guide from a non-expert waits for an expert (Sequence 2).
 * Each step is its own box with an optional photo; a guide can also have a
 * schematic and a video link.
 *
 * @author Luis Gabriel Santiago
 */
public class BuilderController {

    private static final int COVER_SIDE = 1280;
    private static final int SCHEMATIC_SIDE = 1600;
    private static final int STEP_PHOTO_SIDE = 1024;

    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private TextArea summaryArea;
    @FXML private TextArea partsArea;
    @FXML private TextArea precautionsArea;
    @FXML private VBox stepsList;
    @FXML private Button addStepButton;
    @FXML private TextField videoField;
    @FXML private ImageView schematicPreview;
    @FXML private Label noSchematicLabel;
    @FXML private Button chooseSchematicButton;
    @FXML private Label mediaSizeLabel;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> difficultyBox;
    @FXML private ComboBox<String> riskBox;
    @FXML private Label riskNote;
    @FXML private TextField costField;
    @FXML private ImageView imagePreview;
    @FXML private Label noImageLabel;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;
    @FXML private Button draftButton;
    @FXML private Button chooseImageButton;

    private Guide guide;
    private byte[] image;
    private byte[] schematic;
    private final List<StepRow> steps = new ArrayList<>();

    @FXML
    private void initialize() {
        for (String category : Guide.CATEGORIES) {
            categoryBox.getItems().add(Guide.label(category));
        }
        difficultyBox.getItems().setAll("Beginner", "Intermediate", "Advanced");
        difficultyBox.getSelectionModel().select(0);
        riskBox.getItems().setAll("Low", "Moderate", "High");
        riskBox.getSelectionModel().select(0);
        riskBox.setOnAction(e -> updateRiskNote());
        chooseImageButton.setGraphic(Icons.icon("image", 16));
        chooseImageButton.setGraphicTextGap(8);
        submitButton.setGraphic(Icons.icon("send", 16));
        submitButton.setGraphicTextGap(8);
        partsArea.setPromptText("Arduino Uno\nSoil moisture sensor\n5V relay module");
        addStepButton.setGraphic(Icons.icon("plus", 16));
        addStepButton.setGraphicTextGap(8);
        chooseSchematicButton.setGraphic(Icons.icon("cpu", 16));
        chooseSchematicButton.setGraphicTextGap(8);
        for (int i = 0; i < 3; i++) {
            addStep("", null);
        }
        updateRiskNote();
        updateMediaSize();
    }

    /** Opens an existing guide for editing, or starts a new one when null. */
    public void edit(Guide existing) {
        guide = existing;
        if (existing == null) {
            return;
        }
        titleLabel.setText("Edit Guide");
        titleField.setText(existing.getTitle());
        summaryArea.setText(existing.getSummary());
        partsArea.setText(existing.getPartsList());
        precautionsArea.setText(existing.getPrecautions());
        steps.clear();
        stepsList.getChildren().clear();
        List<String> lines = Ui.lines(existing.getSteps());
        for (int i = 0; i < lines.size(); i++) {
            addStep(lines.get(i).replaceFirst("^\\d+[.)]\\s*", ""), existing.getStepImages().get(i + 1));
        }
        if (lines.isEmpty()) {
            addStep("", null);
        }
        videoField.setText(existing.getVideoUrl() == null ? "" : existing.getVideoUrl());
        showSchematic(existing.getSchematic());
        categoryBox.getSelectionModel().select(Guide.CATEGORIES.indexOf(existing.getCategory()));
        difficultyBox.getSelectionModel().select(Guide.DIFFICULTIES.indexOf(existing.getDifficulty()));
        riskBox.getSelectionModel().select(Guide.RISK_FACTORS.indexOf(existing.getRiskFactor()));
        if (existing.getEstimatedCost() > 0) {
            costField.setText(String.valueOf(existing.getEstimatedCost()).replaceAll("\\.0$", ""));
        }
        showImage(existing.getImage());
        updateRiskNote();
        updateMediaSize();
    }

    private void updateRiskNote() {
        boolean high = riskBox.getSelectionModel().getSelectedIndex() == 2;
        riskNote.getStyleClass().removeAll("muted", "error-text");
        if (high && !Session.isExpert()) {
            riskNote.setText("High-risk guides wait for an expert's approval before everyone can see them.");
            riskNote.getStyleClass().add("error-text");
        } else if (high) {
            riskNote.setText("You're an expert, so your high-risk guide is published right away.");
            riskNote.getStyleClass().add("muted");
        } else {
            riskNote.setText("Low and moderate risk guides are published right away.");
            riskNote.getStyleClass().add("muted");
        }
    }

    // ------------------------------------------------------------------ image

    @FXML
    private void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a cover image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));
        File file = chooser.showOpenDialog(Nav.window());
        if (file == null) {
            return;
        }
        Ui.async(() -> shrink(file, COVER_SIDE), bytes -> {
            image = bytes;
            showImage(bytes);
            updateMediaSize();
        });
    }

    @FXML
    private void onRemoveImage() {
        image = null;
        showImage(null);
        updateMediaSize();
    }

    private void showImage(byte[] bytes) {
        image = bytes;
        boolean has = bytes != null && bytes.length > 0;
        imagePreview.setImage(has ? new Image(new ByteArrayInputStream(bytes)) : null);
        noImageLabel.setVisible(!has);
    }

    // ------------------------------------------------------------------ schematic

    @FXML
    private void onChooseSchematic() {
        File file = choosePicture("Choose a schematic or wiring diagram");
        if (file == null) {
            return;
        }
        Ui.async(() -> shrink(file, SCHEMATIC_SIDE), bytes -> {
            showSchematic(bytes);
            updateMediaSize();
        });
    }

    @FXML
    private void onRemoveSchematic() {
        showSchematic(null);
        updateMediaSize();
    }

    private void showSchematic(byte[] bytes) {
        schematic = bytes;
        boolean has = bytes != null && bytes.length > 0;
        schematicPreview.setImage(has ? new Image(new ByteArrayInputStream(bytes)) : null);
        noSchematicLabel.setVisible(!has);
    }

    private static File choosePicture(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));
        return chooser.showOpenDialog(Nav.window());
    }

    /** Shows how much of the 5 MB picture budget is used. */
    private void updateMediaSize() {
        long total = (image == null ? 0 : image.length) + (schematic == null ? 0 : schematic.length);
        int photos = 0;
        for (StepRow step : steps) {
            if (step.photo != null) {
                total += step.photo.length;
                photos++;
            }
        }
        mediaSizeLabel.setText(String.format("Pictures: %.1f MB of 5 MB used (%d step %s).", total / 1048576.0, photos,
                photos == 1 ? "photo" : "photos"));
        mediaSizeLabel.getStyleClass().remove("error-text");
        if (total > Guide.MAX_MEDIA_BYTES) {
            mediaSizeLabel.getStyleClass().add("error-text");
        }
    }

    // ------------------------------------------------------------------ steps, each with an optional photo

    @FXML
    private void onAddStep() {
        StepRow step = addStep("", null);
        step.text.requestFocus();
    }

    private StepRow addStep(String text, byte[] photo) {
        StepRow step = new StepRow(text, photo);
        steps.add(step);
        stepsList.getChildren().add(step.node);
        renumber();
        return step;
    }

    private void move(StepRow step, int by) {
        int from = steps.indexOf(step);
        int to = from + by;
        if (to < 0 || to >= steps.size()) {
            return;
        }
        steps.remove(from);
        steps.add(to, step);
        stepsList.getChildren().setAll(steps.stream().map(s -> (javafx.scene.Node) s.node).toList());
        renumber();
    }

    private void remove(StepRow step) {
        steps.remove(step);
        stepsList.getChildren().remove(step.node);
        if (steps.isEmpty()) {
            addStep("", null);
        }
        renumber();
        updateMediaSize();
    }

    private void renumber() {
        for (int i = 0; i < steps.size(); i++) {
            StepRow step = steps.get(i);
            step.number.setText(String.valueOf(i + 1));
            step.up.setDisable(i == 0);
            step.down.setDisable(i == steps.size() - 1);
        }
    }

    /** One step in the builder: its text, its photo, and buttons to move or delete it. */
    private final class StepRow {

        final TextArea text = new TextArea();
        final Label number = new Label();
        final Button up = Ui.button("", "arrow-up", "btn-text");
        final Button down = Ui.button("", "arrow-down", "btn-text");
        final VBox photoBox = new VBox(6);
        final HBox node;
        byte[] photo;

        StepRow(String value, byte[] photo) {
            this.photo = photo;
            text.setText(value);
            text.setWrapText(true);
            text.setPrefRowCount(2);
            text.setPromptText("What to do in this step, e.g. Wire the sensor to pin A0.");
            text.getStyleClass().add("step-edit-text");
            StackPane badge = new StackPane(number);
            badge.getStyleClass().add("step-number");
            Button delete = Ui.button("", "trash", "btn-text-danger");
            up.setTooltip(new Tooltip("Move up"));
            down.setTooltip(new Tooltip("Move down"));
            delete.setTooltip(new Tooltip("Delete this step"));
            up.setOnAction(e -> move(this, -1));
            down.setOnAction(e -> move(this, 1));
            delete.setOnAction(e -> remove(this));
            VBox tools = new VBox(2, up, down, delete);
            tools.setAlignment(Pos.TOP_CENTER);
            VBox body = new VBox(8, text, photoBox);
            HBox.setHgrow(body, Priority.ALWAYS);
            node = new HBox(12, badge, body, tools);
            node.getStyleClass().add("step-editor");
            node.setAlignment(Pos.TOP_LEFT);
            drawPhoto();
        }

        void drawPhoto() {
            photoBox.getChildren().clear();
            Button choose = Ui.outline(photo == null ? "Add photo" : "Change photo", "image");
            choose.setOnAction(e -> {
                File file = choosePicture("Choose a photo for step " + number.getText());
                if (file != null) {
                    Ui.async(() -> shrink(file, STEP_PHOTO_SIDE), bytes -> {
                        photo = bytes;
                        drawPhoto();
                        updateMediaSize();
                    });
                }
            });
            if (photo == null) {
                photoBox.getChildren().add(Ui.row(10, choose, Ui.label("Optional. A photo makes the step easier to follow.",
                        "faint", "tiny")));
                return;
            }
            Button drop = Ui.button("Remove photo", null, "btn-text-danger");
            drop.setOnAction(e -> {
                photo = null;
                drawPhoto();
                updateMediaSize();
            });
            Image picture = Ui.image(photo);
            HBox row = Ui.row(12, Ui.picture(picture, 180, 110, 6), new VBox(6, choose, drop));
            photoBox.getChildren().add(row);
        }
    }

    /** Reads a picture and shrinks it so its longest side is at most maxSide pixels, as a JPEG. */
    private static byte[] shrink(File file, int maxSide) throws IOException {
        BufferedImage source = ImageIO.read(file);
        if (source == null) {
            throw new IllegalArgumentException("That file is not a picture we can read. Use JPG or PNG.");
        }
        double scale = Math.min(1.0, (double) maxSide / Math.max(source.getWidth(), source.getHeight()));
        int width = (int) Math.round(source.getWidth() * scale);
        int height = (int) Math.round(source.getHeight() * scale);
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(target, "jpg", out);
        if (out.size() > Guide.MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("The image is too big even after shrinking. Try a smaller one.");
        }
        return out.toByteArray();
    }

    // ------------------------------------------------------------------ save

    @FXML
    private void onSubmit() {
        save(true);
    }

    @FXML
    private void onSaveDraft() {
        save(false);
    }

    @FXML
    private void onCancel() {
        if (guide == null) {
            Nav.home();
        } else {
            Nav.openGuide(guide.getGuideId());
        }
    }

    private void save(boolean submit) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        if (categoryBox.getSelectionModel().getSelectedIndex() < 0) {
            showError("Pick a category.");
            return;
        }
        double cost = 0;
        String costText = costField.getText().trim().replace(",", "").replace("₱", "");
        if (!costText.isEmpty()) {
            try {
                cost = Double.parseDouble(costText);
            } catch (NumberFormatException e) {
                showError("Estimated cost must be a number, like 850.");
                return;
            }
            if (cost < 0 || cost > 99_999_999) {
                showError("Estimated cost looks wrong.");
                return;
            }
        }
        Guide target = guide == null ? new Guide() : guide;
        target.setTitle(titleField.getText().trim());
        target.setCategory(Guide.CATEGORIES.get(categoryBox.getSelectionModel().getSelectedIndex()));
        target.setDifficulty(Guide.DIFFICULTIES.get(difficultyBox.getSelectionModel().getSelectedIndex()));
        target.setRiskFactor(Guide.RISK_FACTORS.get(riskBox.getSelectionModel().getSelectedIndex()));
        target.setEstimatedCost(cost);
        target.setSummary(summaryArea.getText().trim());
        target.setPartsList(partsArea.getText().trim());
        target.setPrecautions(precautionsArea.getText().trim());
        StringBuilder text = new StringBuilder();
        Map<Integer, byte[]> photos = new TreeMap<>();
        int number = 0;
        for (StepRow step : steps) {
            String line = step.text.getText().replaceAll("\\s*\\R\\s*", " ").trim();
            if (line.isEmpty()) {
                continue;
            }
            number++;
            text.append(number == 1 ? "" : "\n").append(line);
            if (step.photo != null) {
                photos.put(number, step.photo);
            }
        }
        target.setSteps(text.toString());
        target.setStepImages(photos);
        target.setImage(image);
        target.setSchematic(schematic);
        target.setVideoUrl(videoField.getText().trim());

        User user = Session.user();
        setBusy(true);
        Ui.async(() -> {
            if (submit) {
                user.submitGuide(target);
            } else {
                user.saveDraft(target);
            }
            return target;
        }, saved -> {
            setBusy(false);
            guide = saved;
            Nav.refreshShell();
            if (!submit) {
                Nav.toast("Draft saved.");
            } else if (saved.isPending()) {
                Nav.toast("Sent to an expert for approval.");
            } else {
                Nav.toast("Your guide is published!");
            }
            Nav.openGuide(saved.getGuideId());
        }, () -> setBusy(false));
    }

    private void setBusy(boolean busy) {
        for (Button button : List.of(submitButton, draftButton)) {
            button.setDisable(busy);
        }
        submitButton.setText(busy ? "Saving..." : "Submit Guide");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
