// com.tictactoe.ui.LoginScreen
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

    public LoginScreen(App app) {
        this.app = app;
        createScene();
    }

    private void createScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScreen.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            scene = new Scene(root, 300, 250);

            // Handle play button click
            playButton.setOnAction(e -> {
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
            });
        } catch (IOException e) {
            e.printStackTrace();

            // Fallback to programmatic UI creation
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

            playButton = new Button("Play");
            playButton.setPrefWidth(100);

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
                    playButton
            );

            // Create scene
            scene = new Scene(layout, 300, 250);

            // Handle play button click
            playButton.setOnAction(event -> {
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
            });
        }
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