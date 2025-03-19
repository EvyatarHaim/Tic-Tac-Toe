package com.tictactoe.ui;

import com.tictactoe.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;

public class LoginScreen {
    private App app;
    private Scene scene;

    @FXML private TextField nameField;
    @FXML private RadioButton size3x3;
    @FXML private RadioButton size4x4;
    @FXML private RadioButton size5x5;
    @FXML private ToggleGroup sizeGroup;
    @FXML private Button playButton;

    // create a new login screen
    public LoginScreen(App app) {
        this.app = app;
        createScene();
    }

    // create the login screen
    private void createScene() {
        try {
            // try to load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScreen.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            scene = new Scene(root, 300, 250);

            // set up play button action
            playButton.setOnAction(e -> handlePlayButton());

        } catch (IOException e) {
            e.printStackTrace();

            // create UI manually if FXML loading fails
            createManualLoginScreen();
        }
    }

    // create the login screen manually if FXML loading fails
    private void createManualLoginScreen() {
        // create UI components
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label nameLabel = new Label("Your Name:");
        nameField = new TextField();
        nameField.setPromptText("Enter your name");

        Label sizeLabel = new Label("Board Size:");
        sizeGroup = new ToggleGroup();

        size3x3 = new RadioButton("3x3");
        size3x3.setToggleGroup(sizeGroup);
        size3x3.setSelected(true);

        size4x4 = new RadioButton("4x4");
        size4x4.setToggleGroup(sizeGroup);

        size5x5 = new RadioButton("5x5");
        size5x5.setToggleGroup(sizeGroup);

        HBox sizeBox = new HBox(10, size3x3, size4x4, size5x5);

        playButton = new Button("Play");
        playButton.setPrefWidth(100);

        // create layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel,
                nameLabel,
                nameField,
                sizeLabel,
                sizeBox,
                playButton
        );

        // create scene
        scene = new Scene(layout, 300, 250);

        // set up play button action
        playButton.setOnAction(event -> handlePlayButton());
    }

    // handle play button click
    private void handlePlayButton() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter your name");
            return;
        }

        // determine selected board size
        int boardSize;
        if (size3x3.isSelected()) {
            boardSize = Utils.BOARD_SIZE_3X3;
        } else if (size4x4.isSelected()) {
            boardSize = Utils.BOARD_SIZE_4X4;
        } else {
            boardSize = Utils.BOARD_SIZE_5X5;
        }

        // send login request to the server
        app.getClient().login(name, boardSize);
    }

    // show error message
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}