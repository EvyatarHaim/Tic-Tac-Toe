package com.tictactoe.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;

public class WaitingScreen {
    private App app;
    private Scene scene;

    @FXML private Label waitingLabel;
    @FXML private ProgressIndicator progressIndicator;

    // create a new waiting screen
    public WaitingScreen(App app) {
        this.app = app;
        createScene();
    }

    // create the waiting screen
    private void createScene() {
        try {
            // try to load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WaitingScreen.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            scene = new Scene(root, 300, 200);
        } catch (IOException e) {
            e.printStackTrace();

            // create UI manually if FXML loading fails
            createManualWaitingScreen();
        }
    }

    // create the waiting screen manually if FXML loading fails
    private void createManualWaitingScreen() {
        // create UI components
        waitingLabel = new Label("Waiting for another player...");
        waitingLabel.setFont(Font.font("Arial", 16));

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        // create layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(waitingLabel, progressIndicator);

        // create scene
        scene = new Scene(layout, 300, 200);
    }

    public Scene getScene() {
        return scene;
    }
}