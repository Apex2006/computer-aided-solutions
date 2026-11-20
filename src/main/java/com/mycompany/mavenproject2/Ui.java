package com.mycompany.mavenproject2;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import javafx.scene.shape.Circle;

/**
 * Small building blocks shared by every screen: labels, buttons, chips,
 * cards, dialogs, and running database work in the background so the
 * window never freezes while the cloud database answers.
 *
 * @author Luis Gabriel Santiago
 */
public final class Ui {

    private static final ExecutorService WORKERS = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "db-worker");
        t.setDaemon(true);
        return t;
    });

    private Ui() {
    }

    public static String stylesheet() {
        return Ui.class.getResource("app.css").toExternalForm();
    }

    public static void shutdown() {
        WORKERS.shutdownNow();
    }

    // ------------------------------------------------------------------ background work

    /** Runs database work off the screen thread, then hands the result back to the screen. */
    public static <T> void async(Callable<T> work, Consumer<T> onDone) {
        async(work, onDone, null);
    }

    /** Same as async(work, onDone), with an extra step when the work fails. */
    public static <T> void async(Callable<T> work, Consumer<T> onDone, Runnable onFail) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onDone.accept(task.getValue()));
        task.setOnFailed(e -> {
            showError(task.getException());
            if (onFail != null) {
                onFail.run();
            }
        });
        WORKERS.submit(task);
    }

    /** Shows a friendly message for an error. Rule messages are shown as they are. */
    public static void showError(Throwable error) {
        Runnable show = () -> {
            boolean rule = error instanceof IllegalArgumentException || error instanceof IllegalStateException;
            Alert alert = new Alert(rule ? Alert.AlertType.WARNING : Alert.AlertType.ERROR);
            alert.setTitle(rule ? "Please check" : "Something went wrong");
            alert.setHeaderText(null);
            if (rule) {
                alert.setContentText(error.getMessage());
            } else if (error instanceof SQLException) {
                alert.setContentText("Can't reach the database right now. Check your internet connection "
                        + "and try again.\n\nDetails: " + error.getMessage());
            } else {
                alert.setContentText(String.valueOf(error.getMessage()));
                error.printStackTrace();
            }
            style(alert);
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) {
            show.run();
        } else {
            Platform.runLater(show);
        }
    }

    // ------------------------------------------------------------------ dialogs

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        style(alert);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /** Asks for a line or paragraph of text. Empty when cancelled. */
    public static Optional<String> askText(String title, String message, String prompt, boolean required) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setWrapText(true);
        area.setPrefRowCount(4);
        area.setPrefWidth(420);
        Label text = label(message, "muted");
        text.setWrapText(true);
        text.setMaxWidth(420);
        VBox box = new VBox(10, text, area);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Node ok = dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (required) {
            ok.disableProperty().bind(area.textProperty().isEmpty());
        }
        dialog.setResultConverter(b -> b == ButtonType.OK ? area.getText().trim() : null);
        style(dialog);
        Platform.runLater(area::requestFocus);
        return dialog.showAndWait();
    }

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        style(alert);
        alert.showAndWait();
    }

    private static void style(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(stylesheet());
        if (Nav.window() != null) {
            dialog.initOwner(Nav.window());
        }
    }

    // ------------------------------------------------------------------ text and buttons

    public static Label label(String text, String... styles) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styles);
        return label;
    }

    /** A label that wraps onto more lines inside its parent. */
    public static Label wrap(String text, String... styles) {
        Label label = label(text, styles);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    public static Button button(String text, String icon, String style) {
        Button button = new Button(text);
        button.getStyleClass().add(style);
        if (icon != null) {
            button.setGraphic(Icons.icon(icon, 16));
            button.setGraphicTextGap(8);
        }
        return button;
    }

    public static Button primary(String text, String icon) {
        return button(text, icon, "btn-primary");
    }

    public static Button outline(String text, String icon) {
        return button(text, icon, "btn-outline");
    }

    // ------------------------------------------------------------------ chips and badges

    public static HBox chip(String text, String icon, String... styles) {
        HBox chip = new HBox();
        chip.getStyleClass().add("chip");
        chip.getStyleClass().addAll(styles);
        if (icon != null) {
            chip.getChildren().add(Icons.icon(icon, 13));
        }
        chip.getChildren().add(label(text));
        chip.setMaxWidth(Region.USE_PREF_SIZE);
        chip.setMinWidth(Region.USE_PREF_SIZE);
        return chip;
    }

    /** "Low risk", "Moderate risk" or "High risk" with a colored dot. */
    public static HBox riskChip(String risk) {
        Circle dot = new Circle(3);
        dot.getStyleClass().add("dot");
        HBox chip = new HBox(dot, label(capitalize(risk) + " risk"));
        chip.getStyleClass().addAll("chip", "risk-" + risk);
        chip.setMaxWidth(Region.USE_PREF_SIZE);
        return chip;
    }

    public static HBox statusChip(String status) {
        HBox chip = new HBox(label(capitalize(status)));
        chip.getStyleClass().addAll("chip", "status-" + status);
        chip.setMaxWidth(Region.USE_PREF_SIZE);
        return chip;
    }

    public static HBox expertApprovedChip() {
        return chip("Expert Approved", "shield-check", "chip-expert");
    }

    public static Label countBadge(int count) {
        Label badge = label(String.valueOf(count), "count-badge");
        badge.setMinWidth(Region.USE_PREF_SIZE);
        return badge;
    }

    public static StackPane avatar(String initials, double size) {
        StackPane avatar = new StackPane(label(initials));
        avatar.getStyleClass().add("avatar");
        avatar.setMinSize(size, size);
        avatar.setPrefSize(size, size);
        avatar.setMaxSize(size, size);
        return avatar;
    }

    /** A round icon background, like the category tiles. */
    public static StackPane iconCircle(String icon, double size, boolean filled) {
        StackPane circle = new StackPane(Icons.icon(icon, size * 0.45));
        circle.getStyleClass().add(filled ? "icon-circle-filled" : "icon-circle");
        circle.setMinSize(size, size);
        circle.setPrefSize(size, size);
        circle.setMaxSize(size, size);
        return circle;
    }

    /** A colored message box, kind is danger, info, warning or success. */
    public static HBox banner(String kind, String icon, String text) {
        Label label = wrap(text);
        HBox.setHgrow(label, Priority.ALWAYS);
        HBox banner = new HBox(Icons.icon(icon, 18), label);
        banner.getStyleClass().addAll("banner", "banner-" + kind);
        banner.setAlignment(Pos.CENTER_LEFT);
        return banner;
    }

    // ------------------------------------------------------------------ containers

    public static VBox card(double padding, double spacing) {
        VBox card = new VBox(spacing);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(padding));
        return card;
    }

    /** Lays nodes out in equal columns that stretch with the window. */
    public static GridPane grid(List<? extends Node> nodes, int columns) {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        for (int i = 0; i < columns; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / columns);
            column.setFillWidth(true);
            grid.getColumnConstraints().add(column);
        }
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
            }
            grid.add(node, i % columns, i / columns);
        }
        return grid;
    }

    public static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static HBox row(double spacing, Node... nodes) {
        HBox row = new HBox(spacing, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public static Node loading() {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(36, 36);
        VBox box = new VBox(12, spinner, label("Loading...", "muted"));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));
        return box;
    }

    /** A friendly empty state, for example "No guides yet". */
    public static Node empty(String icon, String title, String text) {
        VBox box = new VBox(10, iconCircle(icon, 52, false), label(title, "h3"), wrap(text, "muted"));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.getStyleClass().add("card");
        return box;
    }

    // ------------------------------------------------------------------ formatting

    public static String peso(double amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMaximumFractionDigits(amount % 1 == 0 ? 0 : 2);
        return "₱" + format.format(amount);
    }

    public static String date(Date date) {
        return date == null ? "" : new SimpleDateFormat("MMM d, yyyy", Locale.US).format(date);
    }

    public static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        String clean = word.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    /** Splits a text block into its non-empty lines. */
    public static List<String> lines(String text) {
        if (text == null) {
            return List.of();
        }
        return text.lines().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    // ------------------------------------------------------------------ pictures

    public static Image image(byte[] bytes) {
        return new Image(new ByteArrayInputStream(bytes));
    }

    /** A picture that fits inside width x height, with rounded corners. */
    public static ImageView picture(Image image, double width, double height, double radius) {
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(width);
        view.setFitHeight(height);
        double scale = Math.min(width / image.getWidth(), height / image.getHeight());
        Rectangle clip = new Rectangle(image.getWidth() * scale, image.getHeight() * scale);
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        view.setClip(clip);
        return view;
    }

    /** Shows a picture big, in its own window (click a step photo or a schematic). */
    public static void viewImage(Image image, String title) {
        Stage stage = new Stage();
        stage.initOwner(Nav.window());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(title);
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        StackPane box = new StackPane(view);
        box.setStyle("-fx-background-color: #101410;");
        box.setPadding(new Insets(16));
        Scene scene = new Scene(box, Math.min(1200, image.getWidth() + 32), Math.min(820, image.getHeight() + 32));
        view.fitWidthProperty().bind(scene.widthProperty().subtract(32));
        view.fitHeightProperty().bind(scene.heightProperty().subtract(32));
        scene.setOnMouseClicked(e -> stage.close());
        stage.setScene(scene);
        stage.show();
    }
}
