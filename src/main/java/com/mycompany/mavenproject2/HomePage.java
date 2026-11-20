package com.mycompany.mavenproject2;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Home: a greeting with the user's identity and niches, edit requests on
 * their guides, recommended guides from their niches, the 8 categories and
 * the newest guides.
 *
 * @author Luis Gabriel Santiago
 */
public final class HomePage {

    private record Data(List<Guide> recommended, List<Guide> newest, Map<String, Integer> counts,
            List<Report> editRequests) {
    }

    private HomePage() {
    }

    public static Node create() {
        VBox page = new VBox();
        Node view = Cards.scroll(page);
        page.getChildren().add(Ui.loading());
        User user = Session.user();
        Ui.async(() -> {
            List<Guide> published = Guide.findPublished();
            List<Guide> mine = user == null ? published : Guide.findForInterests(user);
            List<Guide> recommended = Cards.ranked(mine).stream().limit(4).toList();
            List<Guide> newest = published.stream().limit(4).toList();
            List<Report> requests = user == null ? List.of() : Report.findOpenEditRequestsFor(user);
            return new Data(recommended, newest, Guide.countByCategory(), requests);
        }, data -> fill(page, user, data));
        return view;
    }

    private static void fill(VBox page, User user, Data data) {
        page.getChildren().clear();
        page.getChildren().add(header(user, data.editRequests()));

        String subtitle = user == null
                ? "Top guides, ranked by approval rate and expert badges"
                : "Based on your niches and identity, ranked by approval rate and expert badges";
        page.getChildren().addAll(
                Cards.section(user == null ? "Popular Guides" : "Recommended for You", subtitle,
                        "See all", () -> Nav.browse(null)),
                Cards.guideGrid(data.recommended(), "Nothing in your niches yet",
                        "No published guides match your niches yet. Try Browse Categories or create the first one."));

        List<Node> tiles = new ArrayList<>();
        for (String category : Guide.CATEGORIES) {
            tiles.add(Cards.categoryTile(category, data.counts().get(category), false, () -> Nav.browse(category)));
        }
        page.getChildren().addAll(
                Cards.section("Browse by Category", "Eight everyday niches", "All categories", () -> Nav.browse(null)),
                Ui.grid(tiles, 4),
                Cards.section("Newest Guides", "Fresh fixes from the community", null, null),
                Cards.guideGrid(data.newest(), "No guides yet", "Be the first to publish a guide."));
    }

    private static Node header(User user, List<Report> requests) {
        VBox left = new VBox(10);
        if (user == null) {
            left.getChildren().addAll(Ui.label("Welcome, guest", "h1"),
                    Ui.wrap("You're browsing as a guest. Log in to vote on guides, comment, and publish your own.",
                            "muted"));
            Button login = Ui.primary("Log in", "user");
            login.setOnAction(e -> Nav.showLogin());
            Button register = Ui.outline("Create an account", null);
            register.setOnAction(e -> Nav.showRegister(null));
            left.getChildren().add(Ui.row(10, login, register));
            return left;
        }
        HBox greeting = Ui.row(12, Ui.label(greeting() + ", " + LoginController.firstName(user), "h1"),
                Ui.chip(User.identityLabel(user.getIdentity()), Cards.identityIcon(user.getIdentity()), "chip-gray"));
        if (user.isExpert()) {
            greeting.getChildren().add(Ui.chip("Expert", "badge-check", "chip-blue"));
        }
        if (user.isAdmin()) {
            greeting.getChildren().add(Ui.chip("Admin", "shield", "chip-blue"));
        }
        FlowPane niches = new FlowPane(8, 8);
        niches.setAlignment(Pos.CENTER_LEFT);
        niches.setPrefWrapLength(900);
        niches.getChildren().add(Ui.label("Your niches:", "muted", "small"));
        for (String niche : user.getInterests()) {
            niches.getChildren().add(Ui.chip(Guide.label(niche), null, "chip-green"));
        }
        if (user.getInterests().isEmpty()) {
            niches.getChildren().add(Ui.label("none yet", "faint", "small"));
        }
        Button edit = Ui.button("Edit", "pencil", "btn-text");
        edit.setOnAction(e -> Nav.profile());
        niches.getChildren().add(edit);
        left.getChildren().addAll(greeting, niches);

        HBox header = new HBox(20, left, Ui.spacer());
        header.setAlignment(Pos.TOP_LEFT);
        if (!requests.isEmpty()) {
            header.getChildren().add(editRequestCard(requests));
        }
        return header;
    }

    private static Node editRequestCard(List<Report> requests) {
        StackPane icon = new StackPane(Icons.icon("pencil", 18, "#A15C00"));
        icon.setStyle("-fx-background-color: #FFF1D6; -fx-background-radius: 999;");
        icon.setMinSize(42, 42);
        icon.setMaxSize(42, 42);
        int n = requests.size();
        VBox text = new VBox(2, Ui.label(n + (n == 1 ? " edit request waiting" : " edit requests waiting"), "bold"),
                Ui.label("Other users asked to improve your guides.", "muted", "small"));
        Button review = Ui.outline("Review", null);
        review.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            for (Report request : requests) {
                sb.append(request.getGuide().getTitle()).append("\n   \"").append(request.getReason())
                        .append("\" from @").append(request.getReporter().getUsername()).append("\n\n");
            }
            sb.append("The admin reviews each request. Open your guide from My Guides to make the change.");
            Ui.info("Edit requests on your guides", sb.toString());
        });
        HBox card = new HBox(14, icon, text, review);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setMinWidth(HBox.USE_PREF_SIZE);
        return card;
    }

    private static String greeting() {
        int hour = LocalTime.now().getHour();
        return hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
    }
}
