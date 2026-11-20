package com.mycompany.mavenproject2;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * The JavaFX application: one window whose content changes as the user
 * moves between screens (see Nav).
 *
 * @author Luis Gabriel Santiago
 */
public class CasApp extends Application {

    private static HostServices hosts;

    /** Opens a web link (for example a guide's video) in the computer's browser. */
    public static void openLink(String url) {
        if (hosts != null && url != null) {
            hosts.showDocument(url);
        }
    }

    @Override
    public void start(Stage stage) {
        hosts = getHostServices();
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(Ui.stylesheet());
        stage.setTitle("Computer-Aided Solutions");
        for (int size : new int[] {32, 64, 256}) {
            var icon = CasApp.class.getResourceAsStream("app-icon-" + size + ".png");
            if (icon != null) {
                stage.getIcons().add(new javafx.scene.image.Image(icon));
            }
        }
        stage.setMinWidth(1100);
        stage.setMinHeight(680);
        stage.setScene(scene);
        Nav.init(stage, root);
        stage.show();
        Nav.showStartup();
    }

    @Override
    public void stop() {
        Ui.shutdown();
        Database.shutdown();
    }
}
