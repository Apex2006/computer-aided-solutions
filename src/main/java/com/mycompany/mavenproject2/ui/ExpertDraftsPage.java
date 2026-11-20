package com.mycompany.mavenproject2.ui;

import com.mycompany.mavenproject2.backend.User;
import com.mycompany.mavenproject2.backend.Guide;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Expert Drafts (experts only): high-risk guides waiting for an expert's
 * decision (Sequence 4: load PENDING guides).
 *
 * @author Luis Gabriel Santiago
 */
public final class ExpertDraftsPage {

    private ExpertDraftsPage() {
    }

    public static Node create() {
        VBox page = new VBox();
        Node view = Cards.scroll(page);
        page.getChildren().addAll(new VBox(4, Ui.label("Expert Drafts", "h1"),
                Ui.wrap("High-risk guides waiting for an expert. Open one, set the risk factor, then approve (Y) "
                        + "or return it to the author (N). The first expert decision counts.", "muted")),
                Ui.loading());
        User me = Session.user();
        Ui.async(Guide::findPending, guides -> {
            page.getChildren().remove(1);
            if (guides.isEmpty()) {
                page.getChildren().add(Ui.empty("shield-check", "No guides waiting",
                        "When someone submits a high-risk guide, it shows up here."));
                return;
            }
            VBox card = Ui.card(8, 0);
            card.setPadding(new Insets(6, 20, 6, 20));
            for (Guide guide : guides) {
                boolean own = guide.getAuthor().getUserId() == me.getUserId();
                User author = guide.getAuthor();
                VBox text = new VBox(3, Ui.label(guide.getTitle(), "bold"),
                        Ui.label("by " + (author.getFullName() == null ? author.getUsername() : author.getFullName())
                                + "  ·  " + Guide.label(guide.getCategory())
                                + "  ·  submitted " + Ui.date(guide.getCreatedAt()), "muted", "small"));
                HBox.setHgrow(text, Priority.ALWAYS);
                Button review = own ? Ui.outline("Open", null) : Ui.primary("Review", "shield-check");
                review.setOnAction(e -> Nav.openGuide(guide.getGuideId()));
                HBox row = new HBox(14, Cards.categorySquare(guide.getCategory(), 46), text);
                if (own) {
                    row.getChildren().add(Ui.chip("Your guide", null, "chip-gray"));
                }
                row.getChildren().addAll(Ui.riskChip(guide.getRiskFactor()), review);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("parts-row");
                row.setPadding(new Insets(12, 0, 12, 0));
                card.getChildren().add(row);
            }
            page.getChildren().add(card);
        });
        return view;
    }
}
