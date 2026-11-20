package com.mycompany.mavenproject2;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * My Guides: the user's drafts, guides waiting for an expert, published and
 * hidden guides, with quick links to open or edit each one.
 *
 * @author Luis Gabriel Santiago
 */
public final class MyGuidesPage {

    private static final String[][] FILTERS = {
        {null, "All"}, {"DRAFT", "Drafts"}, {"PENDING", "Waiting for expert"},
        {"PUBLISHED", "Published"}, {"HIDDEN", "Hidden"},
    };

    private MyGuidesPage() {
    }

    public static Node create() {
        VBox page = new VBox();
        Node view = Cards.scroll(page);
        Button create = Ui.primary("Create Guide", "plus");
        create.setOnAction(e -> Nav.builder(null));
        HBox header = new HBox(16, new VBox(4, Ui.label("My Guides", "h1"),
                Ui.label("Your drafts, guides waiting for an expert, and your published guides.", "muted")),
                Ui.spacer(), create);
        header.setAlignment(Pos.TOP_LEFT);
        page.getChildren().addAll(header, Ui.loading());
        User user = Session.user();
        Ui.async(() -> Guide.findByAuthor(user), guides -> {
            page.getChildren().remove(1);
            if (guides.isEmpty()) {
                page.getChildren().add(Ui.empty("book-open", "No guides yet",
                        "Share your first computer-aided fix. Click Create Guide to start."));
                return;
            }
            VBox list = new VBox();
            HBox filters = new HBox(8);
            ToggleGroup group = new ToggleGroup();
            for (String[] filter : FILTERS) {
                long count = guides.stream().filter(g -> filter[0] == null || filter[0].equals(g.getStatus())).count();
                if (filter[0] != null && count == 0) {
                    continue;
                }
                ToggleButton chip = new ToggleButton(filter[1] + "  " + count);
                chip.getStyleClass().add("toggle-chip");
                chip.setToggleGroup(group);
                chip.setOnAction(e -> {
                    if (!chip.isSelected()) {
                        chip.setSelected(true);
                    }
                    showRows(list, guides, filter[0]);
                });
                if (filter[0] == null) {
                    chip.setSelected(true);
                }
                filters.getChildren().add(chip);
            }
            showRows(list, guides, null);
            page.getChildren().addAll(filters, list);
        });
        return view;
    }

    private static void showRows(VBox list, List<Guide> guides, String status) {
        VBox card = Ui.card(8, 0);
        card.setPadding(new Insets(6, 20, 6, 20));
        for (Guide guide : guides) {
            if (status != null && !status.equals(guide.getStatus())) {
                continue;
            }
            card.getChildren().add(row(guide));
        }
        list.getChildren().setAll(card);
    }

    private static Node row(Guide guide) {
        VBox text = new VBox(3, Ui.label(guide.getTitle(), "bold"),
                Ui.label(Guide.label(guide.getCategory()) + "  ·  " + Ui.capitalize(guide.getDifficulty())
                        + "  ·  " + Ui.date(guide.getCreatedAt()), "muted", "small"));
        HBox.setHgrow(text, Priority.ALWAYS);
        int percent = guide.getApprovalPercent();
        String votes = "PUBLISHED".equals(guide.getStatus())
                ? (percent < 0 ? "No votes yet" : percent + "% approval  ·  " + guide.getVoteCount() + " votes")
                : "";
        Button open = Ui.outline("Open", null);
        open.setOnAction(e -> Nav.openGuide(guide.getGuideId()));
        Button edit = Ui.button("Edit", "pencil", "btn-text");
        edit.setOnAction(e -> Ui.async(() -> Guide.findById(guide.getGuideId()), Nav::builder));
        HBox row = new HBox(14, Cards.categorySquare(guide.getCategory(), 46), text, Ui.label(votes, "muted", "small"),
                Ui.statusChip(guide.getStatus()), open, edit);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("parts-row");
        row.setPadding(new Insets(12, 0, 12, 0));
        return row;
    }
}
