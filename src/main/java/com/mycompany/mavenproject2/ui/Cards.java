package com.mycompany.mavenproject2.ui;

import com.mycompany.mavenproject2.backend.Guide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Pieces shared by the pages: guide cards, category tiles, section headers
 * and the scrolling page container.
 *
 * @author Luis Gabriel Santiago
 */
public final class Cards {

    private Cards() {
    }

    /** A page body that scrolls. Add content to the returned page's VBox. */
    public static ScrollPane scroll(VBox page) {
        page.getStyleClass().add("page");
        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    /** A guide card like the Figma: colored cover, category, title, badges, approval and risk. */
    public static VBox guide(Guide guide) {
        Categories.Style style = Categories.style(guide.getCategory());
        StackPane cover = new StackPane(Icons.icon(style.icon(), 30, style.iconColor()));
        cover.getStyleClass().add("cover");
        cover.setStyle("-fx-background-color: " + style.cover() + ";");
        cover.setMinHeight(118);
        cover.setPrefHeight(118);

        VBox body = new VBox(6);
        body.setPadding(new Insets(14, 16, 16, 16));
        Label title = Ui.wrap(guide.getTitle(), "h3");
        body.getChildren().addAll(Ui.label(Guide.label(guide.getCategory()), "green-text", "small"), title);
        if (guide.isExpertApproved()) {
            body.getChildren().add(Ui.expertApprovedChip());
        }
        String meta = Ui.capitalize(guide.getDifficulty())
                + (guide.getEstimatedCost() > 0 ? "  ·  " + Ui.peso(guide.getEstimatedCost()) : "");
        body.getChildren().add(Ui.label(meta, "muted", "small"));
        VBox.setVgrow(body, Priority.ALWAYS);

        int percent = guide.getApprovalPercent();
        HBox votes = Ui.row(6, Icons.icon("thumbs-up", 15, "#2E7D32"),
                Ui.label(percent < 0 ? "No votes yet" : percent + "%  (" + guide.getVoteCount() + ")", "small"));
        votes.setMinWidth(Region.USE_PREF_SIZE);
        HBox bottom = Ui.row(8, votes, Ui.spacer(), Ui.riskChip(guide.getRiskFactor()));
        VBox.setMargin(bottom, new Insets(8, 0, 0, 0));
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        body.getChildren().addAll(spacer, bottom);

        VBox card = new VBox(cover, body);
        card.getStyleClass().addAll("card", "card-click");
        card.setOnMouseClicked(e -> Nav.openGuide(guide.getGuideId()));
        card.setMaxHeight(Double.MAX_VALUE);
        return card;
    }

    /** A category tile: icon, name and number of guides. */
    public static HBox categoryTile(String category, int count, boolean selected, Runnable onClick) {
        VBox text = new VBox(2, Ui.wrap(Guide.label(category), "bold"),
                Ui.label(count + (count == 1 ? " guide" : " guides"), "muted", "small"));
        HBox tile = new HBox(14, Ui.iconCircle(Categories.style(category).icon(), 42, selected), text);
        tile.setAlignment(Pos.CENTER_LEFT);
        if (selected) {
            tile.getChildren().addAll(Ui.spacer(), Icons.icon("circle-check", 18, "#2E7D32"));
        }
        tile.getStyleClass().addAll("card", "card-click");
        if (selected) {
            tile.getStyleClass().add("card-selected");
        }
        tile.setPadding(new Insets(16, 18, 16, 18));
        tile.setMinHeight(78);
        tile.setOnMouseClicked(e -> onClick.run());
        return tile;
    }

    /** A section title with an optional link on the right, like "Recommended for You ... See all". */
    public static HBox section(String title, String subtitle, String linkText, Runnable onLink) {
        VBox text = new VBox(3, Ui.label(title, "h2"));
        if (subtitle != null) {
            text.getChildren().add(Ui.label(subtitle, "muted", "small"));
        }
        HBox row = new HBox(text, Ui.spacer());
        row.setAlignment(Pos.BOTTOM_LEFT);
        if (linkText != null) {
            Button link = new Button(linkText);
            link.getStyleClass().add("btn-text");
            link.setGraphic(Icons.icon("chevron-right", 15));
            link.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
            link.setOnAction(e -> onLink.run());
            row.getChildren().add(link);
        }
        return row;
    }

    /** Cards for the given guides in 4 columns, or a friendly empty message. */
    public static Node guideGrid(List<Guide> guides, String emptyTitle, String emptyText) {
        if (guides.isEmpty()) {
            return Ui.empty("search", emptyTitle, emptyText);
        }
        List<Node> cards = new ArrayList<>();
        for (Guide guide : guides) {
            cards.add(guide(guide));
        }
        return Ui.grid(cards, 4);
    }

    /** Best guides first: expert approved, then highest approval, then most votes. */
    public static List<Guide> ranked(List<Guide> guides) {
        List<Guide> sorted = new ArrayList<>(guides);
        sorted.sort(Comparator.comparing(Guide::isExpertApproved).reversed()
                .thenComparing(Comparator.comparingInt(Guide::getApprovalPercent).reversed())
                .thenComparing(Comparator.comparingInt(Guide::getVoteCount).reversed()));
        return sorted;
    }

    public static String identityIcon(String identity) {
        return switch (identity == null ? "" : identity) {
            case "STUDENT" -> "graduation-cap";
            case "PROFESSIONAL" -> "briefcase";
            case "HOBBYIST" -> "hammer";
            case "EDUCATOR" -> "presentation";
            default -> "user";
        };
    }

    /** A small square icon for list rows, in the category's colors. */
    public static StackPane categorySquare(String category, double size) {
        Categories.Style style = Categories.style(category);
        StackPane square = new StackPane(Icons.icon(style.icon(), size * 0.45, style.iconColor()));
        square.setStyle("-fx-background-color: " + style.cover() + "; -fx-background-radius: 10;");
        square.setMinSize(size, size);
        square.setMaxSize(size, size);
        return square;
    }
}
