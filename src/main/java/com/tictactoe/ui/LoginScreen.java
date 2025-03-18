// com.tictactoe.ui.LoginScreen
package com.tictactoe.ui;

import com.tictactoe.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;

public class LoginScreen {
    private App app;
    private Scene scene;

    @FXML private TextField nameField;
    @FXML private RadioButton size3x3;
    @FXML private RadioButton size4x4;
    @FXML private RadioButton size5x5;
    @FXML private ToggleGroup sizeGroup;
    @FXML private Button playButton;
    @FXML private Button statsButton; // Add this field for the statistics button

    public LoginScreen(App app) {
        this.app = app;
        createScene();
    }

    private void createScene() {
        try {
            // Check if resource exists
            URL resourceUrl = getClass().getResource("/LoginScreen.fxml");
            System.out.println("Login screen FXML URL: " + resourceUrl);

            FXMLLoader loader = new FXMLLoader(resourceUrl);
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

            // Create and add the stats button programmatically
            Platform.runLater(() -> {
                System.out.println("Adding statistics button programmatically");
                try {
                    // Create a new stats button
                    Button newStatsButton = new Button("Statistics");
                    newStatsButton.setPrefWidth(100);
                    newStatsButton.setOnAction(e -> {
                        System.out.println("Stats button clicked");
                        app.showStatsScreen();
                    });

                    // Find the parent of the play button
                    Parent parent = playButton.getParent();
                    System.out.println("Play button parent type: " + parent.getClass().getName());

                    if (parent instanceof VBox) {
                        // Play button is directly in a VBox
                        VBox vbox = (VBox) parent;
                        int index = vbox.getChildren().indexOf(playButton);

                        // Remove the play button temporarily
                        vbox.getChildren().remove(playButton);

                        // Create a horizontal box for both buttons
                        HBox buttonBox = new HBox(10);
                        buttonBox.setAlignment(Pos.CENTER);
                        buttonBox.getChildren().addAll(playButton, newStatsButton);

                        // Add the button box back to the same position
                        vbox.getChildren().add(index, buttonBox);

                        System.out.println("Added stats button in an HBox to VBox at position " + index);
                    } else if (parent instanceof HBox) {
                        // Play button is already in an HBox
                        HBox hbox = (HBox) parent;
                        hbox.getChildren().add(newStatsButton);
                        System.out.println("Added stats button to existing HBox");
                    } else if (parent instanceof Pane) {
                        // Generic pane, just add button alongside
                        Pane pane = (Pane) parent;
                        pane.getChildren().add(newStatsButton);

                        // Position it next to the play button
                        newStatsButton.setLayoutX(playButton.getLayoutX() + playButton.getWidth() + 10);
                        newStatsButton.setLayoutY(playButton.getLayoutY());

                        System.out.println("Added stats button to generic Pane");
                    } else {
                        System.out.println("Unknown parent type, can't add stats button: " + parent.getClass().getName());
                    }
                } catch (Exception ex) {
                    System.err.println("Error adding stats button: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

        } catch (IOException e) {
            System.err.println("Error loading LoginScreen.fxml: " + e.getMessage());
            e.printStackTrace();

            // Fallback to programmatic UI creation
            System.out.println("Falling back to programmatic login UI creation");

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

            Button statsButton = new Button("Statistics");
            statsButton.setPrefWidth(100);
            statsButton.setOnAction(event -> {
                System.out.println("Stats button clicked from fallback UI");
                app.showStatsScreen();
            });

            // Create a button box
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