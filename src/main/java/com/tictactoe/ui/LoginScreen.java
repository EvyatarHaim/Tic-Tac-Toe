// com.tictactoe.ui.LoginScreen
package com.tictactoe.ui;

import com.tictactoe.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginScreen {
    private App app;
    private Scene scene;

    private TextField nameField;
    private RadioButton size3x3;
    private RadioButton size4x4;
    private RadioButton size5x5;
    private ToggleGroup sizeGroup;
    private Button playButton;
    private Button statsButton;

    public LoginScreen(App app) {
        this.app = app;
        createProgrammaticUI();
    }

    private void createProgrammaticUI() {
        System.out.println("Creating programmatic login UI");

        // Create UI components
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
        sizeBox.setAlignment(Pos.CENTER);

        playButton = new Button("Play");
        playButton.setPrefWidth(100);

        statsButton = new Button("Statistics");
        statsButton.setPrefWidth(100);

        // Set up button actions
        playButton.setOnAction(e -> handlePlay());
        statsButton.setOnAction(e -> {
            System.out.println("Stats button clicked");
            app.showStatsScreen();
        });

        // Create a button box with both buttons
        HBox buttonBox = new HBox(10, playButton, statsButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Create layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel,
                nameLabel,
                nameField,
                sizeLabel,
                sizeBox,
                buttonBox
        );

        // Create scene
        scene = new Scene(layout, 300, 250);

        System.out.println("Login UI created with Play and Statistics buttons");
    }

    private void handlePlay() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter your name.");
            return;
        }

        int boardSize;
        if (size3x3.isSelected()) {
            boardSize = Utils.BOARD_SIZE_3X3;
        } else if (size4x4.isSelected()) {
            boardSize = Utils.BOARD_SIZE_4X4;
        } else {
            boardSize = Utils.BOARD_SIZE_5X5;
        }

        // Send login request
        app.getClient().login(name, boardSize);
    }

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