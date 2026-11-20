package com.mycompany.mavenproject2;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * The guide page (the main function): cover, author and details, safety
 * notes, build video and schematic, parts, a step-by-step player with a
 * photo per step, and comments, plus the approval vote, edit request,
 * report and expert review panels (Sequences 3 and 4). Votes and comments
 * are separate: people can do either one, or both.
 *
 * @author Luis Gabriel Santiago
 */
public final class GuidePage {

    private final int guideId;
    private final VBox page = new VBox();
    private Guide guide;
    private List<Review> reviews;
    // Step player state. It stays when the page reloads after a vote or comment.
    private int stepIndex;
    private boolean[] stepDone = new boolean[0];
    private boolean showAllSteps;
    private boolean[] partsHave = new boolean[0];
    private VBox stepsBox;

    private GuidePage(int guideId) {
        this.guideId = guideId;
    }

    public static Node create(int guideId) {
        GuidePage view = new GuidePage(guideId);
        Node node = Cards.scroll(view.page);
        view.load();
        return node;
    }

    private void load() {
        page.getChildren().setAll(Ui.loading());
        Ui.async(() -> {
            Guide found = Guide.findById(guideId);
            if (found == null) {
                throw new IllegalStateException("This guide no longer exists.");
            }
            return new Object[]{found, Review.findByGuide(found)};
        }, data -> {
            guide = (Guide) data[0];
            @SuppressWarnings("unchecked")
            List<Review> list = (List<Review>) data[1];
            reviews = list;
            render();
        });
    }

    // ------------------------------------------------------------------ who can see what

    private User user() {
        return Session.user();
    }

    private boolean isAuthor() {
        return user() != null && guide.getAuthor().getUserId() == user().getUserId();
    }

    private boolean canView() {
        return switch (guide.getStatus()) {
            case "PUBLISHED" -> true;
            case "DRAFT" -> isAuthor();
            case "PENDING" -> isAuthor() || Session.isExpert() || Session.isAdmin();
            default -> isAuthor() || Session.isAdmin();
        };
    }

    private Review myReview() {
        if (user() == null) {
            return null;
        }
        for (Review review : reviews) {
            if (review.getReviewer().getUserId() == user().getUserId()) {
                return review;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ layout

    private void render() {
        page.getChildren().clear();
        if (!canView()) {
            page.getChildren().add(Ui.empty("lock", "This guide isn't available",
                    "It may be a draft, waiting for an expert, or hidden by an admin."));
            return;
        }
        VBox main = new VBox(18, hero());
        Node safety = safety();
        if (safety != null) {
            main.getChildren().add(safety);
        }
        Node media = media();
        if (media != null) {
            main.getChildren().add(media);
        }
        main.getChildren().addAll(parts(), steps(), comments());
        HBox.setHgrow(main, Priority.ALWAYS);
        main.setMinWidth(0);

        VBox side = new VBox(16, approvalCard(), actionsCard(), expertCard());
        side.setMinWidth(300);
        side.setPrefWidth(300);
        side.setMaxWidth(300);

        HBox columns = new HBox(22, main, side);
        page.getChildren().addAll(breadcrumb(), columns);
    }

    private Node breadcrumb() {
        Button browse = Ui.button("Browse Categories", "chevron-left", "btn-text");
        browse.setOnAction(e -> Nav.browse(null));
        Button category = new Button(Guide.label(guide.getCategory()));
        category.getStyleClass().add("btn-text");
        category.setStyle("-fx-text-fill: -text-2; -fx-font-weight: normal;");
        category.setOnAction(e -> Nav.browse(guide.getCategory()));
        HBox crumbs = Ui.row(4, browse, Icons.icon("chevron-right", 14), category,
                Icons.icon("chevron-right", 14), Ui.label(guide.getTitle(), "bold", "small"));
        crumbs.getStyleClass().add("breadcrumb");
        return crumbs;
    }

    private Node hero() {
        Categories.Style style = Categories.style(guide.getCategory());
        StackPane cover = new StackPane();
        cover.getStyleClass().add("cover");
        cover.setStyle("-fx-background-color: " + style.cover() + ";");
        cover.setMinHeight(250);
        cover.setPrefHeight(250);
        if (guide.getImage() != null && guide.getImage().length > 0) {
            ImageView image = new ImageView(new Image(new ByteArrayInputStream(guide.getImage())));
            image.setPreserveRatio(true);
            image.setFitHeight(250);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(cover.widthProperty());
            clip.setHeight(250);
            cover.setClip(clip);
            cover.getChildren().add(image);
        } else {
            cover.setMinHeight(190);
            cover.setPrefHeight(190);
            cover.setStyle("-fx-background-color: linear-gradient(to bottom right, " + style.cover() + ", derive("
                    + style.cover() + ", -12%));");
            Pane shapes = new Pane();
            shapes.setMouseTransparent(true);
            Circle big = new Circle(120, Color.web("#FFFFFF", 0.22));
            Circle small = new Circle(54, Color.web("#FFFFFF", 0.18));
            Circle dot = new Circle(18, Color.web(style.iconColor(), 0.18));
            big.centerXProperty().bind(cover.widthProperty().subtract(70));
            big.setCenterY(30);
            small.setCenterX(70);
            small.setCenterY(160);
            dot.centerXProperty().bind(cover.widthProperty().multiply(0.72));
            dot.setCenterY(150);
            shapes.getChildren().addAll(big, small, dot);
            Rectangle shapesClip = new Rectangle();
            shapesClip.widthProperty().bind(cover.widthProperty());
            shapesClip.heightProperty().bind(cover.heightProperty());
            shapes.setClip(shapesClip);
            StackPane tile = new StackPane(Icons.icon(style.icon(), 52, style.iconColor()));
            tile.getStyleClass().add("hero-tile");
            cover.getChildren().addAll(shapes, tile);
        }

        VBox body = new VBox(12);
        body.setPadding(new Insets(22, 26, 24, 26));
        body.getChildren().addAll(
                Ui.chip(Guide.label(guide.getCategory()), style.icon(), "chip-green"),
                Ui.wrap(guide.getTitle(), "h1"),
                Ui.wrap(guide.getSummary(), "muted"));

        User author = guide.getAuthor();
        String name = author.getFullName() == null ? author.getUsername() : author.getFullName();
        HBox by = Ui.row(10, Ui.avatar(author.getInitials(), 30), Ui.label("by " + name, "bold"),
                Ui.label("@" + author.getUsername() + "  ·  Posted " + Ui.date(guide.getCreatedAt()), "muted", "small"));
        HBox chips = Ui.row(8, Ui.chip(Ui.capitalize(guide.getDifficulty()), "list-checks", "chip-outline"));
        if (guide.getEstimatedCost() > 0) {
            chips.getChildren().add(Ui.chip(Ui.peso(guide.getEstimatedCost()) + " estimated cost", "list", "chip-outline"));
        }
        chips.getChildren().add(Ui.riskChip(guide.getRiskFactor()));
        body.getChildren().addAll(by, chips);

        switch (guide.getStatus()) {
            case "PENDING" -> body.getChildren().add(Ui.banner("warning", "clock",
                    "This high-risk guide is waiting for an expert to check it. Only the author, experts and admins can see it."));
            case "DRAFT" -> body.getChildren().add(Ui.banner("warning", "file-text",
                    guide.getApproval() == 'N'
                            ? "An expert returned this guide. Edit it and submit it again. Only you can see it."
                            : "This is a draft. Only you can see it."));
            case "HIDDEN" -> body.getChildren().add(Ui.banner("danger", "ban",
                    "An admin hid this guide. Only the author and admins can see it."));
            default -> {
                if (guide.isExpertApproved()) {
                    HBox approved = Ui.banner("info", "shield-check", "This guide is approved by an expert");
                    approved.setMaxWidth(Region.USE_PREF_SIZE);
                    body.getChildren().add(approved);
                }
            }
        }
        VBox card = new VBox(cover, body);
        card.getStyleClass().add("card");
        return card;
    }

    private Node safety() {
        List<String> lines = Ui.lines(guide.getPrecautions());
        if (lines.isEmpty()) {
            return null;
        }
        VBox box = new VBox(8, Ui.row(10, Icons.icon("triangle-alert", 18), Ui.label("Safety first", "bold")));
        for (String line : lines) {
            Circle dot = new Circle(2.5);
            dot.setStyle("-fx-fill: #B42318;");
            HBox row = new HBox(10, dot, Ui.wrap(line));
            row.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(row);
        }
        box.getStyleClass().addAll("banner", "banner-danger");
        return box;
    }

    private Node parts() {
        VBox card = Ui.card(22, 4);
        List<String> lines = Ui.lines(guide.getPartsList());
        if (partsHave.length != lines.size()) {
            partsHave = new boolean[lines.size()];
        }
        Label ready = Ui.label("", "chip-count");
        Runnable recount = () -> {
            int have = 0;
            for (boolean h : partsHave) {
                have += h ? 1 : 0;
            }
            ready.setText(have == lines.size() && have > 0 ? "All parts ready" : have + " of " + lines.size() + " ready");
        };
        recount.run();
        VBox title = new VBox(2, Ui.label("Parts and materials", "h2"),
                Ui.label("Tick what you already have.", "muted", "small"));
        card.getChildren().add(Ui.row(10, title, Ui.spacer(), ready));
        if (lines.isEmpty()) {
            card.getChildren().add(Ui.label("No parts listed yet.", "muted"));
        }
        for (int i = 0; i < lines.size(); i++) {
            int index = i;
            CheckBox have = new CheckBox();
            have.setSelected(partsHave[i]);
            Label name = Ui.wrap(lines.get(i));
            if (partsHave[i]) {
                name.getStyleClass().add("part-have");
            }
            have.selectedProperty().addListener((o, was, now) -> {
                partsHave[index] = now;
                name.getStyleClass().remove("part-have");
                if (now) {
                    name.getStyleClass().add("part-have");
                }
                recount.run();
            });
            name.setOnMouseClicked(e -> have.setSelected(!have.isSelected()));
            HBox row = Ui.row(12, have, Icons.icon("package", 16), name);
            row.getStyleClass().add("parts-row");
            card.getChildren().add(row);
        }
        if (guide.getEstimatedCost() > 0) {
            HBox total = Ui.row(8, Ui.label("Estimated total", "bold"), Ui.spacer(),
                    Ui.label(Ui.peso(guide.getEstimatedCost()), "h3", "green-text"));
            total.setPadding(new Insets(10, 4, 0, 4));
            card.getChildren().add(total);
        }
        return card;
    }

    // ------------------------------------------------------------------ video and schematic

    private Node media() {
        boolean video = !User.isBlank(guide.getVideoUrl());
        boolean schematic = guide.getSchematic() != null && guide.getSchematic().length > 0;
        if (!video && !schematic) {
            return null;
        }
        VBox card = Ui.card(22, 14);
        card.getChildren().add(Ui.label(video && schematic ? "Video and schematic" : video ? "Build video" : "Schematic", "h2"));
        HBox tiles = new HBox(16);
        if (video) {
            tiles.getChildren().add(videoTile());
        }
        if (schematic) {
            tiles.getChildren().add(schematicTile());
        }
        for (Node tile : tiles.getChildren()) {
            HBox.setHgrow(tile, Priority.ALWAYS);
        }
        card.getChildren().add(tiles);
        return card;
    }

    private Node videoTile() {
        String url = guide.getVideoUrl().trim();
        String youtube = Guide.youtubeId(url);
        StackPane screen = new StackPane();
        screen.getStyleClass().add("video-screen");
        screen.setMinHeight(210);
        screen.setPrefHeight(210);
        screen.setMaxHeight(210);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(screen.widthProperty());
        clip.setHeight(210);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        screen.setClip(clip);
        if (youtube != null) {
            ImageView thumb = new ImageView(new Image("https://img.youtube.com/vi/" + youtube + "/hqdefault.jpg", true));
            thumb.setPreserveRatio(true);
            thumb.fitWidthProperty().bind(screen.widthProperty());
            screen.getChildren().add(thumb);
        }
        StackPane play = new StackPane(Icons.icon("play", 28, "#FFFFFF"));
        play.getStyleClass().add("play-button");
        screen.getChildren().add(play);
        screen.setOnMouseClicked(e -> CasApp.openLink(url));
        String site = url.replaceFirst("^https?://(www\\.)?", "").replaceFirst("[/?].*$", "");
        Label siteLabel = Ui.label(site, "faint", "small");
        siteLabel.setMinWidth(0);
        HBox caption = Ui.row(8, Icons.icon("video", 16), Ui.label("Watch the build video", "bold"), Ui.spacer(), siteLabel);
        VBox tile = new VBox(10, screen, caption);
        tile.setMinWidth(0);
        tile.setPrefWidth(10);
        return tile;
    }

    private Node schematicTile() {
        Image picture = Ui.image(guide.getSchematic());
        StackPane frame = new StackPane(Ui.picture(picture, 460, 194, 10));
        frame.getStyleClass().add("schematic-frame");
        frame.setMinHeight(210);
        frame.setPrefHeight(210);
        frame.setMaxHeight(210);
        frame.setOnMouseClicked(e -> Ui.viewImage(picture, "Schematic: " + guide.getTitle()));
        HBox caption = Ui.row(8, Icons.icon("cpu", 16), Ui.label("Schematic", "bold"), Ui.spacer(),
                Icons.icon("maximize", 14), Ui.label("Click to enlarge", "faint", "small"));
        VBox tile = new VBox(10, frame, caption);
        tile.setMinWidth(0);
        tile.setPrefWidth(10);
        return tile;
    }

    // ------------------------------------------------------------------ steps (step by step, or all at once)

    private List<String> stepLines() {
        List<String> lines = new ArrayList<>();
        for (String line : Ui.lines(guide.getSteps())) {
            lines.add(line.replaceFirst("^\\d+[.)]\\s*", ""));
        }
        return lines;
    }

    private Node steps() {
        List<String> lines = stepLines();
        if (stepDone.length != lines.size()) {
            stepDone = new boolean[lines.size()];
            stepIndex = 0;
        }
        int photos = guide.getStepImages().size();
        ToggleGroup modes = new ToggleGroup();
        ToggleButton player = new ToggleButton("Step by step");
        ToggleButton all = new ToggleButton("All steps");
        player.setGraphic(Icons.icon("play", 13));
        all.setGraphic(Icons.icon("list", 13));
        for (ToggleButton mode : List.of(player, all)) {
            mode.setToggleGroup(modes);
            mode.getStyleClass().add("segment");
        }
        (showAllSteps ? all : player).setSelected(true);
        player.setOnAction(e -> {
            showAllSteps = false;
            player.setSelected(true);
            drawSteps();
        });
        all.setOnAction(e -> {
            showAllSteps = true;
            all.setSelected(true);
            drawSteps();
        });
        HBox segmented = new HBox(player, all);
        segmented.getStyleClass().add("segmented");
        segmented.setMinWidth(Region.USE_PREF_SIZE);
        String info = lines.size() + (lines.size() == 1 ? " step" : " steps")
                + (photos > 0 ? "  ·  " + photos + (photos == 1 ? " photo" : " photos") : "");
        VBox title = new VBox(2, Ui.label("Steps", "h2"), Ui.label(info, "muted", "small"));
        HBox head = Ui.row(12, title, Ui.spacer(), segmented);
        stepsBox = new VBox(16);
        VBox card = Ui.card(22, 16);
        card.getChildren().addAll(head, stepsBox);
        drawSteps();
        return card;
    }

    private void drawSteps() {
        List<String> lines = stepLines();
        stepsBox.getChildren().clear();
        int n = lines.size();
        if (n == 0) {
            stepsBox.getChildren().add(Ui.label("No steps yet.", "muted"));
            return;
        }
        int done = 0;
        for (boolean d : stepDone) {
            done += d ? 1 : 0;
        }
        HBox track = new HBox();
        track.getStyleClass().add("progress-track");
        track.setMinHeight(10);
        track.setPrefHeight(10);
        track.setMaxHeight(10);
        Region fill = new Region();
        fill.getStyleClass().add("progress-fill");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(done / (double) n));
        track.getChildren().add(fill);
        HBox.setHgrow(track, Priority.ALWAYS);
        Label count = Ui.label(done == n ? "All " + n + " done" : done + " of " + n + " done", "bold", "small");
        count.setMinWidth(Region.USE_PREF_SIZE);
        stepsBox.getChildren().add(Ui.row(12, track, count));
        if (showAllSteps) {
            for (int i = 0; i < n; i++) {
                stepsBox.getChildren().add(stepRow(i, lines.get(i)));
            }
        } else if (stepIndex >= n) {
            stepsBox.getChildren().add(finishCard(n));
        } else {
            stepsBox.getChildren().addAll(stepDots(n), stepPlayer(stepIndex, lines.get(stepIndex), n));
        }
    }

    /** The row of numbered dots: green when done, ringed for the current step. Click one to jump to it. */
    private Node stepDots(int n) {
        HBox dots = new HBox();
        dots.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                Region line = new Region();
                line.getStyleClass().addAll("dot-line", stepDone[i - 1] ? "dot-line-done" : "dot-line-todo");
                line.setMinHeight(3);
                line.setMaxHeight(3);
                line.setMinWidth(8);
                HBox.setHgrow(line, Priority.ALWAYS);
                dots.getChildren().add(line);
            }
            int index = i;
            Node mark = stepDone[i] ? Icons.icon("check", 14, "#FFFFFF") : Ui.label(String.valueOf(i + 1));
            StackPane dot = new StackPane(mark);
            dot.getStyleClass().addAll("step-dot", stepDone[i] ? "step-dot-done" : i == stepIndex ? "step-dot-current" : "step-dot-todo");
            dot.setOnMouseClicked(e -> {
                stepIndex = index;
                drawSteps();
            });
            dots.getChildren().add(dot);
        }
        return dots;
    }

    private Node stepPlayer(int i, String text, int n) {
        StackPane badge = new StackPane(Ui.label(String.valueOf(i + 1)));
        badge.getStyleClass().add("step-badge");
        VBox words = new VBox(6, Ui.label("STEP " + (i + 1) + " OF " + n, "step-kicker"), Ui.wrap(text, "step-text"));
        HBox.setHgrow(words, Priority.ALWAYS);
        words.setMinWidth(0);
        HBox top = new HBox(18, badge, words);
        top.setAlignment(Pos.TOP_LEFT);
        VBox card = new VBox(18, top);
        card.getStyleClass().add("step-player");
        byte[] photo = guide.getStepImages().get(i + 1);
        if (photo != null) {
            Image picture = Ui.image(photo);
            StackPane frame = new StackPane(Ui.picture(picture, 620, 340, 12));
            frame.getStyleClass().add("step-photo");
            frame.setOnMouseClicked(e -> Ui.viewImage(picture, "Step " + (i + 1) + ": " + guide.getTitle()));
            card.getChildren().add(frame);
        }
        Button back = Ui.outline("Previous", "chevron-left");
        back.setDisable(i == 0);
        back.setOnAction(e -> {
            stepIndex = i - 1;
            drawSteps();
        });
        boolean last = i == n - 1;
        Button next = Ui.primary(last ? "Done, finish" : "Done, next step", last ? "trophy" : "check");
        next.setOnAction(e -> {
            stepDone[i] = true;
            stepIndex = nextUnfinished(i, n);
            drawSteps();
        });
        HBox nav = Ui.row(10, back, Ui.spacer());
        if (!last) {
            Button skip = Ui.button("Skip for now", "chevron-right", "btn-text");
            skip.setOnAction(e -> {
                stepIndex = i + 1;
                drawSteps();
            });
            nav.getChildren().add(skip);
        }
        nav.getChildren().add(next);
        card.getChildren().add(nav);
        return card;
    }

    /** After finishing step i: the next step not done yet, then any skipped one, or n when all are done. */
    private int nextUnfinished(int i, int n) {
        for (int j = i + 1; j < n; j++) {
            if (!stepDone[j]) {
                return j;
            }
        }
        for (int j = 0; j < n; j++) {
            if (!stepDone[j]) {
                return j;
            }
        }
        return n;
    }

    private Node finishCard(int n) {
        Label done = Ui.label("You finished all " + n + " steps!", "h2");
        Label text = Ui.wrap("Did it work? Your vote helps others find guides that really work, "
                + "and a comment can help the author improve it.", "muted");
        text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button again = Ui.outline("Start over", "rotate-ccw");
        again.setOnAction(e -> {
            stepDone = new boolean[n];
            stepIndex = 0;
            drawSteps();
        });
        HBox actions = Ui.row(10, again);
        actions.setAlignment(Pos.CENTER);
        Review mine = myReview();
        if (user() != null && !isAuthor() && "PUBLISHED".equals(guide.getStatus()) && (mine == null || !mine.hasVote())) {
            Button approve = Ui.primary("Approve this guide", "thumbs-up");
            approve.setOnAction(e -> vote(Boolean.TRUE));
            actions.getChildren().add(0, approve);
        }
        VBox card = new VBox(12, Ui.iconCircle("trophy", 64, true), done, text, actions);
        card.getStyleClass().addAll("step-player", "step-finish");
        card.setAlignment(Pos.CENTER);
        return card;
    }

    /** One step in the "All steps" list, with its photo and a Done tick. */
    private Node stepRow(int i, String text) {
        StackPane circle = new StackPane(stepDone[i] ? Icons.icon("check", 14, "#FFFFFF") : Ui.label(String.valueOf(i + 1)));
        circle.getStyleClass().add("step-number");
        Label label = Ui.wrap(text);
        label.setMaxWidth(Double.MAX_VALUE);
        if (stepDone[i]) {
            label.getStyleClass().add("step-done");
        }
        VBox content = new VBox(10, label);
        HBox.setHgrow(content, Priority.ALWAYS);
        content.setMinWidth(0);
        byte[] photo = guide.getStepImages().get(i + 1);
        if (photo != null) {
            Image picture = Ui.image(photo);
            StackPane thumb = new StackPane(Ui.picture(picture, 280, 160, 8));
            thumb.setAlignment(Pos.CENTER_LEFT);
            thumb.setMaxWidth(Region.USE_PREF_SIZE);
            thumb.getStyleClass().add("step-thumb");
            thumb.setOnMouseClicked(e -> Ui.viewImage(picture, "Step " + (i + 1) + ": " + guide.getTitle()));
            content.getChildren().add(thumb);
        }
        CheckBox tick = new CheckBox();
        tick.setSelected(stepDone[i]);
        Label doneText = Ui.label("Done", "muted", "small");
        doneText.setOnMouseClicked(e -> tick.setSelected(!tick.isSelected()));
        tick.selectedProperty().addListener((o, was, now) -> {
            stepDone[i] = now;
            drawSteps();
        });
        HBox doneBox = new HBox(6, tick, doneText);
        doneBox.setAlignment(Pos.CENTER);
        doneBox.setMinWidth(Region.USE_PREF_SIZE);
        HBox step = new HBox(14, circle, content, doneBox);
        step.getStyleClass().add("step-card");
        step.setAlignment(photo == null ? Pos.CENTER_LEFT : Pos.TOP_LEFT);
        return step;
    }

    // ------------------------------------------------------------------ comments (no vote needed)

    private Node comments() {
        List<Review> withText = reviews.stream().filter(Review::hasComment).toList();
        VBox card = Ui.card(22, 16);
        card.getChildren().addAll(Ui.row(10, Icons.icon("message-square", 20), Ui.label("Comments (" + withText.size() + ")", "h2")),
                composer());
        if (withText.isEmpty()) {
            card.getChildren().add(Ui.label("No comments yet. Ask a question or share a tip.", "muted"));
        }
        for (Review review : withText) {
            card.getChildren().add(commentRow(review));
        }
        return card;
    }

    private Node composer() {
        if (user() == null) {
            Button login = Ui.outline("Log in to comment", "user");
            login.setOnAction(e -> Nav.requireLogin("comment on guides"));
            return Ui.row(10, login, Ui.label("Anyone with an account can comment, with or without voting.", "muted", "small"));
        }
        if (!"PUBLISHED".equals(guide.getStatus())) {
            return Ui.wrap("Comments open once the guide is published.", "muted", "small");
        }
        Review mine = myReview();
        boolean editing = mine != null && mine.hasComment();
        TextArea box = new TextArea(editing ? mine.getComment() : "");
        box.setPromptText(isAuthor() ? "Answer a question or add a tip about your guide"
                : "Ask a question or share a tip. You don't need to vote to comment.");
        box.setWrapText(true);
        box.setPrefRowCount(3);
        Button post = Ui.primary(editing ? "Save comment" : "Post comment", "send");
        post.setOnAction(e -> {
            if (User.isBlank(box.getText())) {
                Ui.info("Write something first", "Type your comment, then post it.");
                return;
            }
            saveComment(box.getText());
        });
        HBox actions = Ui.row(10, Ui.label(editing ? "This is your comment. You can edit or delete it."
                : "Votes and comments are separate.", "faint", "small"), Ui.spacer());
        if (editing) {
            Button delete = Ui.button("Delete", "trash", "btn-text-danger");
            delete.setOnAction(e -> {
                if (Ui.confirm("Delete comment", "Delete your comment? Your vote stays as it is.")) {
                    saveComment("");
                }
            });
            actions.getChildren().add(delete);
        }
        actions.getChildren().add(post);
        VBox right = new VBox(8, box, actions);
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox row = new HBox(12, Ui.avatar(user().getInitials(), 36), right);
        row.getStyleClass().add("comment-composer");
        return row;
    }

    private void saveComment(String text) {
        User user = user();
        boolean delete = User.isBlank(text);
        Ui.async(() -> {
            user.comment(guide, text);
            return true;
        }, ok -> {
            Nav.toast(delete ? "Comment deleted." : "Comment posted.");
            load();
        });
    }

    private Node commentRow(Review review) {
        User who = review.getReviewer();
        String username = who.getUsername();
        boolean named = who.getFullName() != null && !who.getFullName().isBlank();
        HBox meta = Ui.row(8, Ui.label(named ? who.getFullName() : "@" + username, "bold"));
        if (named) {
            meta.getChildren().add(Ui.label("@" + username, "faint", "small"));
        }
        if (who.getUserId() == guide.getAuthor().getUserId()) {
            meta.getChildren().add(Ui.chip("Author", "pencil", "chip-green"));
        } else if ("EXPERT".equals(who.getRole())) {
            meta.getChildren().add(Ui.chip("Expert", "badge-check", "chip-blue"));
        } else if ("ADMIN".equals(who.getRole())) {
            meta.getChildren().add(Ui.chip("Admin", "shield", "chip-blue"));
        }
        if (review.hasVote()) {
            meta.getChildren().add(review.isApproved()
                    ? Ui.chip("Approves", "thumbs-up", "chip-green")
                    : Ui.chip("Disapproves", "thumbs-down", "chip-gray"));
        }
        meta.getChildren().add(Ui.label(Ui.date(review.getCreatedAt()), "faint", "tiny"));
        VBox text = new VBox(5, meta, Ui.wrap(review.getComment()));
        HBox.setHgrow(text, Priority.ALWAYS);
        text.setMinWidth(0);
        String initials = named ? who.getInitials() : username.substring(0, Math.min(2, username.length())).toUpperCase();
        HBox row = new HBox(12, Ui.avatar(initials, 36), text);
        row.getStyleClass().add("comment-row");
        return row;
    }

    // ------------------------------------------------------------------ right side panels

    private Node approvalCard() {
        int votes = guide.getVoteCount();
        int yes = guide.getApproveCount();
        int no = votes - yes;
        int percent = guide.getApprovalPercent();

        Label big = Ui.label(percent < 0 ? "--" : percent + "%");
        big.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: -green-dark;");
        Label from = Ui.label(votes == 0 ? "no votes yet" : "from " + votes + (votes == 1 ? " vote" : " votes"), "muted", "small");
        HBox rate = new HBox(10, big, from);
        rate.setAlignment(Pos.BASELINE_LEFT);

        HBox track = new HBox();
        track.getStyleClass().add("approval-track");
        track.setMinHeight(8);
        track.setPrefHeight(8);
        Region fill = new Region();
        fill.getStyleClass().add("approval-fill");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(votes == 0 ? 0 : yes / (double) votes));
        track.getChildren().add(fill);

        HBox counts = Ui.row(6, Icons.icon("thumbs-up", 14, "#2E7D32"), Ui.label(yes + " approvals", "small"),
                Ui.spacer(), Icons.icon("thumbs-down", 14, "#C62828"), Ui.label(no + " disapprovals", "small"));

        Review mine = myReview();
        Boolean myVote = mine == null || !mine.hasVote() ? null : mine.isApproved();
        boolean published = "PUBLISHED".equals(guide.getStatus());
        boolean canVote = published && !isAuthor();
        Button approve = Ui.outline(Boolean.TRUE.equals(myVote) ? "Approved" : "Approve", "thumbs-up");
        Button disapprove = Ui.outline(Boolean.FALSE.equals(myVote) ? "Disapproved" : "Disapprove", "thumbs-down");
        approve.setMaxWidth(Double.MAX_VALUE);
        disapprove.setMaxWidth(Double.MAX_VALUE);
        if (myVote != null) {
            (myVote ? approve : disapprove).getStyleClass().add("btn-outline-selected");
        }
        approve.setDisable(!canVote && user() != null);
        disapprove.setDisable(!canVote && user() != null);
        approve.setOnAction(e -> vote(Boolean.TRUE.equals(myVote) ? null : Boolean.TRUE));
        disapprove.setOnAction(e -> vote(Boolean.FALSE.equals(myVote) ? null : Boolean.FALSE));

        String note;
        if (user() == null) {
            note = "Log in to vote on this guide.";
        } else if (isAuthor()) {
            note = "You can't vote on your own guide, but you can answer comments below.";
        } else if (!published) {
            note = "Voting opens once the guide is published.";
        } else if (myVote != null) {
            note = "You " + (myVote ? "approved" : "disapproved") + " this guide. Click it again to take your vote back, "
                    + "or click the other button to change it.";
        } else {
            note = "One click to vote, no comment needed. Your vote also counts toward the author's expert rating.";
        }
        VBox card = Ui.card(20, 12);
        card.getChildren().addAll(Ui.label("Approval rate", "muted"), rate, track, counts, approve, disapprove,
                Ui.wrap(note, "muted", "small"));
        return card;
    }

    /** Approve (true), disapprove (false), or take the vote back (null). No comment needed. */
    private void vote(Boolean approve) {
        if (!Nav.requireLogin("vote on guides")) {
            return;
        }
        User user = user();
        Ui.async(() -> {
            user.vote(guide, approve);
            return true;
        }, ok -> {
            Nav.toast(approve == null ? "Your vote was taken back." : "Thanks for your vote!");
            load();
        });
    }

    private Node actionsCard() {
        VBox card = Ui.card(16, 10);
        if (isAuthor()) {
            Button edit = Ui.primary("Edit guide", "pencil");
            edit.setMaxWidth(Double.MAX_VALUE);
            edit.setOnAction(e -> Nav.builder(guide));
            card.getChildren().addAll(edit, Ui.wrap(
                    "Edits clear an earlier expert approval. High-risk guides go back to an expert.", "muted", "small"));
        } else {
            Button request = Ui.outline("Request Edit", "pencil");
            request.setMaxWidth(Double.MAX_VALUE);
            request.setOnAction(e -> report("EDIT_REQUEST"));
            Button flag = Ui.button("Report this guide", "flag", "btn-text-danger");
            flag.setOnAction(e -> report("REPORT"));
            HBox flagRow = new HBox(flag);
            flagRow.setAlignment(Pos.CENTER);
            card.getChildren().addAll(request,
                    Ui.wrap("Edit requests go to the admin, who works with the author.", "muted", "small"), flagRow);
        }
        if (Session.isAdmin() && !"DRAFT".equals(guide.getStatus())) {
            boolean hidden = "HIDDEN".equals(guide.getStatus());
            Button toggle = Ui.outline(hidden ? "Unhide guide" : "Hide guide", hidden ? "eye" : "eye-off");
            toggle.setMaxWidth(Double.MAX_VALUE);
            toggle.setOnAction(e -> {
                if (!Ui.confirm(hidden ? "Unhide guide" : "Hide guide",
                        hidden ? "Show this guide to everyone again?" : "Hide this guide from everyone?")) {
                    return;
                }
                User admin = user();
                Ui.async(() -> {
                    if (hidden) {
                        admin.unhideGuide(guide);
                    } else {
                        admin.hideGuide(guide);
                    }
                    return true;
                }, ok -> {
                    Nav.toast(hidden ? "Guide is visible again." : "Guide hidden.");
                    load();
                });
            });
            card.getChildren().add(toggle);
        }
        return card;
    }

    private void report(String type) {
        boolean edit = "EDIT_REQUEST".equals(type);
        if (!Nav.requireLogin(edit ? "request edits" : "report guides")) {
            return;
        }
        Optional<String> reason = Ui.askText(edit ? "Request an edit" : "Report this guide",
                edit ? "What should change in this guide?" : "What's wrong with this guide? An admin will check it.",
                edit ? "e.g. Step 2 should say which pin the buzzer uses." : "e.g. The wiring in step 3 is unsafe.", true);
        if (reason.isEmpty()) {
            return;
        }
        User user = user();
        Ui.async(() -> user.reportGuide(guide, type, reason.get()),
                r -> Nav.toast(edit ? "Edit request sent to the admin." : "Report sent. Thanks for keeping guides safe."));
    }

    private Node expertCard() {
        VBox card = Ui.card(20, 10);
        card.getChildren().add(Ui.row(8, Icons.icon("shield-check", 18, "#1D4FA8"), Ui.label("Expert review", "h3")));
        User expert = guide.getApprovedBy();
        String expertName = expert == null ? "an expert"
                : expert.getFullName() == null ? "@" + expert.getUsername() : expert.getFullName();
        if (guide.isExpertApproved()) {
            card.getChildren().addAll(Ui.wrap("Approved by " + expertName, "bold"),
                    Ui.label("Risk factor: " + Ui.capitalize(guide.getRiskFactor()) + "  ·  "
                            + Ui.date(guide.getApprovedAt()), "muted", "small"));
        } else if (guide.getApproval() == 'N') {
            card.getChildren().add(Ui.wrap("Returned to the author by " + expertName + ".", "muted"));
        } else if ("PENDING".equals(guide.getStatus())) {
            if (Session.isExpert() && !isAuthor()) {
                card.getChildren().add(expertDecision());
            } else {
                card.getChildren().add(Ui.wrap("Waiting for an expert. High-risk guides are published after an "
                        + "expert approves them.", "muted"));
            }
        } else if (guide.isHighRisk()) {
            card.getChildren().add(Ui.wrap("Written by an expert, so it was published without a separate check.", "muted"));
        } else {
            card.getChildren().add(Ui.wrap("No expert check needed. Low and moderate risk guides are published right away.",
                    "muted"));
        }
        return card;
    }

    /** Sequence 4: the expert sets the risk factor and decides Y or N. */
    private Node expertDecision() {
        ComboBox<String> risk = new ComboBox<>();
        risk.getItems().setAll("Low", "Moderate", "High");
        risk.getSelectionModel().select(Guide.RISK_FACTORS.indexOf(guide.getRiskFactor()));
        risk.setMaxWidth(Double.MAX_VALUE);
        Button yes = Ui.primary("Approve (Y)", "check");
        Button no = Ui.outline("Return to author (N)", "rotate-ccw");
        yes.setMaxWidth(Double.MAX_VALUE);
        no.setMaxWidth(Double.MAX_VALUE);
        yes.setOnAction(e -> decide(risk, true));
        no.setOnAction(e -> decide(risk, false));
        return new VBox(10, Ui.wrap("Read the guide, set the risk factor, then decide. The first expert decision counts.",
                "muted", "small"), Ui.label("Risk factor", "bold", "small"), risk, yes, no);
    }

    private void decide(ComboBox<String> riskBox, boolean approve) {
        String risk = Guide.RISK_FACTORS.get(riskBox.getSelectionModel().getSelectedIndex());
        if (!Ui.confirm(approve ? "Approve guide" : "Return guide",
                approve ? "Approve and publish this guide with a " + risk.toLowerCase() + " risk factor?"
                        : "Return this guide to the author as a draft?")) {
            return;
        }
        User expert = user();
        Ui.async(() -> {
            expert.approveGuide(guide, risk, approve);
            return true;
        }, ok -> {
            Nav.toast(approve ? "Guide approved and published." : "Guide returned to the author.");
            Nav.refreshShell();
            load();
        }, this::load);
    }
}
