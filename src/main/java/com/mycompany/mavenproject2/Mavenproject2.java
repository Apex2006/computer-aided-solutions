package com.mycompany.mavenproject2;

import com.mycompany.mavenproject2.ui.CasApp;
import javafx.application.Application;

/**
 * Starts the Computer-Aided Solutions desktop app.
 *
 * This class does not extend Application on purpose: launching through a
 * plain main class lets NetBeans run the app with JavaFX on the classpath.
 *
 * @author Luis Gabriel Santiago
 */
public class Mavenproject2 {

    public static void main(String[] args) {
        Application.launch(CasApp.class, args);
    }
}
