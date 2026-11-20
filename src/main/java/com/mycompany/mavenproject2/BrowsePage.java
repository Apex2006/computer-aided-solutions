package com.mycompany.mavenproject2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Browse Categories: pick a niche, then filter by difficulty and risk and
 * sort by approval, as in the Figma. Also shows search results.
 *
 * @author Luis Gabriel Santiago
 */
public final class BrowsePage {

    private final String query;
    private String category;
    private List<Guide> all = List.of();
    private Map<String, Integer> counts = Map.of();

    private final VBox page = new VBox();
    private final VBox tiles = new VBox();
    private final VBox results = new VBox(18);
    private final TextField filterField = new TextField();
    private final ComboBox<String> difficultyBox = new ComboBox<>();
    private final ComboBox<String> riskBox = new ComboBox<>();
    private final ComboBox<String> sortBox = new ComboBox<>();

    private BrowsePage(String category, String query) {
        this.category = category;
        this.query = query;
    }

    public static Node create(String category, String query) {
        return new BrowsePage(category, query).build();
    }

    private Node build() {
        Node view = Cards.scroll(page);
        page.getChildren().add(Ui.loading());
        Ui.async(() -> {
            List<Guide> guides = query == null ? Guide.findPublished() : Guide.search(query);
            return new Object[]{guides, Guide.countByCategory()};
        }, data -> {
            @SuppressWarnings("unchecked")
            List<Guide> guides = (List<Guide>) data[0];
            @SuppressWarnings("unchecked")
            Map<String, Integer> numbers = (Map<String, Integer>) data[1];
            all = guides;
            counts = numbers;
            layout();
        });
        return view;
    }

    private void layout() {
        page.getChildren().clear();
        VBox title = new VBox(4,
                Ui.label(query == null ? "Browse Categories" : "Search results for \"" + query + "\"", "h1"),
                Ui.label(query == null
                        ? "Find computer-aided solutions by the everyday problem you want to solve."
                        : all.size() + (all.size() == 1 ? " guide matches" : " guides match") + " every word you typed.",
                        "muted"));
        HBox filterBox = new HBox(filterField);
        filterBox.getStyleClass().add("input-box");
        filterBox.getChildren().add(0, Icons.icon("search", 16));
        filterBox.setPrefWidth(300);
        filterBox.setMaxWidth(300);
        HBox.setHgrow(filterField, javafx.scene.layout.Priority.ALWAYS);
        filterField.textProperty().addListener((o, a, b) -> showResults());
        HBox header = new HBox(16, title, Ui.spacer(), filterBox);
        header.setAlignment(Pos.TOP_LEFT);

        difficultyBox.getItems().setAll("Difficulty: All", "Beginner", "Intermediate", "Advanced");
        riskBox.getItems().setAll("Risk: All levels", "Low risk", "Moderate risk", "High risk");
        sortBox.getItems().setAll("Sort: Highest approval", "Sort: Newest", "Sort: Lowest cost");
        difficultyBox.getSelectionModel().select(0);
        riskBox.getSelectionModel().select(0);
        sortBox.getSelectionModel().select(0);
        difficultyBox.setOnAction(e -> showResults());
        riskBox.setOnAction(e -> showResults());
        sortBox.setOnAction(e -> showResults());

        page.getChildren().addAll(header, tiles, results);
        showTiles();
        showResults();
    }

    private void showTiles() {
        List<Node> list = new ArrayList<>();
        for (String c : Guide.CATEGORIES) {
            list.add(Cards.categoryTile(c, counts.getOrDefault(c, 0), c.equals(category), () -> {
                category = c.equals(category) ? null : c;
                showTiles();
                showResults();
            }));
        }
        tiles.getChildren().setAll(Ui.grid(list, 4));
    }

    private void showResults() {
        filterField.setPromptText(category == null ? "Search in all guides" : "Search in " + Guide.label(category));
        List<Guide> shown = new ArrayList<>();
        String text = filterField.getText() == null ? "" : filterField.getText().trim().toLowerCase(Locale.ROOT);
        String difficulty = difficultyBox.getSelectionModel().getSelectedIndex() <= 0 ? null
                : Guide.DIFFICULTIES.get(difficultyBox.getSelectionModel().getSelectedIndex() - 1);
        String risk = riskBox.getSelectionModel().getSelectedIndex() <= 0 ? null
                : Guide.RISK_FACTORS.get(riskBox.getSelectionModel().getSelectedIndex() - 1);
        for (Guide guide : all) {
            if (category != null && !category.equals(guide.getCategory())) {
                continue;
            }
            if (difficulty != null && !difficulty.equals(guide.getDifficulty())) {
                continue;
            }
            if (risk != null && !risk.equals(guide.getRiskFactor())) {
                continue;
            }
            if (!text.isEmpty() && !(guide.getTitle() + " " + guide.getSummary() + " " + guide.getPartsList())
                    .toLowerCase(Locale.ROOT).contains(text)) {
                continue;
            }
            shown.add(guide);
        }
        switch (sortBox.getSelectionModel().getSelectedIndex()) {
            case 1 -> {
                // already newest first
            }
            case 2 -> shown.sort(Comparator.comparingDouble(g -> g.getEstimatedCost() <= 0 ? Double.MAX_VALUE : g.getEstimatedCost()));
            default -> shown = Cards.ranked(shown);
        }
        String heading = category == null ? "All guides" : Guide.label(category) + " guides";
        String detail = shown.size() + (shown.size() == 1 ? " guide" : " guides")
                + (category == null ? "" : "  ·  " + Categories.style(category).tagline());
        VBox titles = new VBox(3, Ui.label(heading, "h2"), Ui.label(detail, "muted", "small"));
        HBox bar = new HBox(10, titles, Ui.spacer(), difficultyBox, riskBox, sortBox);
        bar.setAlignment(Pos.CENTER_LEFT);
        results.getChildren().setAll(bar, Cards.guideGrid(shown, "No guides found",
                "Try another category, clear the filters, or search with fewer words."));
    }
}
