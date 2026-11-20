package com.mycompany.mavenproject2;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

/**
 * The green welcome panel on the left of the login and sign-up screens,
 * with the plant-and-sensor illustration from the Figma design.
 *
 * @author Luis Gabriel Santiago
 */
public final class BrandPanel extends VBox {

    public BrandPanel() {
        getStyleClass().add("brand-panel");
        setPrefWidth(500);
        setMinWidth(420);
        setMaxWidth(500);
        setPadding(new Insets(40, 40, 32, 40));
        setSpacing(18);

        StackPane illustration = new StackPane(drawIllustration());
        illustration.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(illustration, new Insets(40, 0, 10, 0));

        getChildren().addAll(
                logo(true),
                illustration,
                Ui.wrap("Everyday problems, solved with a little tech.", "brand-title"),
                Ui.wrap("Find step-by-step DIY guides for sensors, Arduino, and smart home builds. "
                        + "Written by the community, rated by users, and approved by experts.", "muted"),
                Ui.spacer(),
                Ui.wrap("Guides for gardening, cooking, energy saving, home security, and more.", "muted", "small"));
    }

    /** The app logo: a green tile with a chip icon and the app name. */
    public static HBox logo(boolean onDark) {
        StackPane tile = new StackPane(Icons.icon("cpu", 22));
        tile.getStyleClass().add("logo-box");
        tile.setMinSize(38, 38);
        tile.setMaxSize(38, 38);
        if (onDark) {
            tile.setStyle("-fx-background-color: rgba(255,255,255,0.16);");
        }
        VBox text = new VBox(1, Ui.label("Computer-Aided Solutions", "h3"),
                Ui.label("Everyday Problems Repository", onDark ? "muted" : "muted", "small"));
        text.setMinWidth(Region.USE_PREF_SIZE);
        HBox logo = new HBox(12, tile, text);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setMinWidth(Region.USE_PREF_SIZE);
        return logo;
    }

    /** A plant in a pot with a soil sensor, a water drop card and a Wi-Fi board. */
    private static Node drawIllustration() {
        Pane pane = new Pane();
        pane.setPrefSize(330, 230);
        pane.setMinSize(330, 230);
        pane.setMaxSize(330, 230);

        Circle glow = new Circle(150, 118, 92, Color.web("#2B7A35"));

        Rectangle stem = new Rectangle(147, 78, 6, 62);
        stem.setFill(Color.web("#2E5E2F"));
        Ellipse leftLeaf = new Ellipse(127, 82, 26, 12);
        leftLeaf.setFill(Color.web("#8BC58F"));
        leftLeaf.setRotate(-28);
        Ellipse rightLeaf = new Ellipse(173, 78, 24, 11);
        rightLeaf.setFill(Color.web("#A9D6AC"));
        rightLeaf.setRotate(24);
        Ellipse topLeaf = new Ellipse(152, 62, 10, 18);
        topLeaf.setFill(Color.web("#6DB574"));

        Rectangle rim = new Rectangle(102, 138, 96, 16);
        rim.setArcWidth(8);
        rim.setArcHeight(8);
        rim.setFill(Color.web("#EBA055"));
        Polygon pot = new Polygon(108, 152, 192, 152, 180, 210, 120, 210);
        pot.setFill(Color.web("#E08A3C"));
        Rectangle potLine = new Rectangle(118, 168, 64, 3);
        potLine.setFill(Color.web("#F2B57A"));

        Rectangle sensor = new Rectangle(156, 104, 20, 14);
        sensor.setArcWidth(4);
        sensor.setArcHeight(4);
        sensor.setFill(Color.web("#1565C0"));
        Circle led = new Circle(162, 111, 2.5, Color.web("#7CE08A"));

        CubicCurve wire = new CubicCurve(176, 111, 200, 104, 214, 128, 236, 128);
        wire.setFill(null);
        wire.setStroke(Color.web("#DCEFDD"));
        wire.setStrokeWidth(2);

        Rectangle board = new Rectangle(236, 112, 56, 36);
        board.setArcWidth(6);
        board.setArcHeight(6);
        board.setFill(Color.web("#1F8A8A"));
        Rectangle chip = new Rectangle(252, 121, 20, 16);
        chip.setFill(Color.web("#1C2B1E"));
        Arc wifiSmall = wifiArc(266, 104, 10);
        Arc wifiBig = wifiArc(266, 104, 18);

        Rectangle card = new Rectangle(14, 40, 92, 34);
        card.setArcWidth(10);
        card.setArcHeight(10);
        card.setFill(Color.WHITE);
        Rectangle line1 = new Rectangle(42, 50, 54, 5);
        line1.setArcWidth(5);
        line1.setArcHeight(5);
        line1.setFill(Color.web("#2E7D32"));
        Rectangle line2 = new Rectangle(42, 60, 40, 5);
        line2.setArcWidth(5);
        line2.setArcHeight(5);
        line2.setFill(Color.web("#C9CFC6"));

        pane.getChildren().addAll(glow, wire, stem, leftLeaf, rightLeaf, topLeaf, rim, pot, potLine,
                sensor, led, board, chip, wifiSmall, wifiBig, card, line1, line2,
                drop(20, 46, 0.55), drop(52, 116, 1.0), drop(40, 158, 0.6));
        return pane;
    }

    private static Arc wifiArc(double x, double y, double radius) {
        Arc arc = new Arc(x, y, radius, radius, 50, 80);
        arc.setType(ArcType.OPEN);
        arc.setFill(null);
        arc.setStroke(Color.WHITE);
        arc.setStrokeWidth(2.5);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        return arc;
    }

    private static Node drop(double x, double y, double scale) {
        SVGPath drop = new SVGPath();
        drop.setContent("M10 0 C10 0 0 12 0 18 A10 10 0 0 0 20 18 C20 12 10 0 10 0 Z");
        drop.setFill(Color.web("#64B5F6"));
        drop.setLayoutX(x);
        drop.setLayoutY(y);
        drop.setScaleX(scale);
        drop.setScaleY(scale);
        return drop;
    }
}
