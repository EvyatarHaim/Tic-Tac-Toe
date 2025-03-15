// com.tictactoe.ui.App
package com.tictactoe.ui;

import com.tictactoe.Utils;
import com.tictactoe.client.Client;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    private Client client;
    private Stage primaryStage;
    private LoginScreen loginScreen;
    private WaitingScreen waitingScreen;
    private GameScreen gameScreen;
    private GameOverScreen gameOverScreen;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.client = new Client();

        // Create screens
        this.loginScreen = new LoginScreen(this);

        // Set up client message handler
        client.setMessageHandler(message -> {
            switch (message.getType()) {
                case WAIT:
                    showWaitingScreen();
                    break;
                case GAME_START:
                    showGameScreen();
                    break;
                case MOVE_RESULT:
                    if (gameScreen != null) {
                        gameScreen.handleMoveResult(message);
                    }
                    break;
                case GAME_OVER:
                    showGameOverScreen(message);
                    break;
                case ERROR:
                    if (loginScreen != null) {
                        loginScreen.showError((String) message.getData(Utils.Keys.MESSAGE));
                    }
                    break;
            }
        });

        // Connect to server
        boolean connected = client.connect();
        if (!connected) {
            loginScreen.showError("Could not connect to server. Please try again later.");
        }

        // Show login screen
        primaryStage.setTitle("Tic Tac Toe");
        primaryStage.setScene(loginScreen.getScene());
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Show the waiting screen
     */
    public void showWaitingScreen() {
        if (waitingScreen == null) {
            waitingScreen = new WaitingScreen(this);
        }
        primaryStage.setScene(waitingScreen.getScene());
    }

    /**
     * Show the game screen
     */
    public void showGameScreen() {
        if (gameScreen == null || gameScreen.getBoardSize() != client.getBoardSize()) {
            gameScreen = new GameScreen(this, client.getBoardSize());
        }
        primaryStage.setScene(gameScreen.getScene());
        gameScreen.startGame();
    }

    /**
     * Show the game over screen
     */
    public void showGameOverScreen(Utils.Message message) {
        if (gameScreen != null) {
            gameScreen.stopGame();
        }
        gameOverScreen = new GameOverScreen(this, message);
        primaryStage.setScene(gameOverScreen.getScene());
    }

    /**
     * Show the login screen
     */
    public void showLoginScreen() {
        primaryStage.setScene(loginScreen.getScene());
    }

    /**
     * Get the client
     */
    public Client getClient() {
        return client;
    }

    @Override
    public void stop() {
        // Disconnect from server when application closes
        if (client.isConnected()) {
            client.disconnect();
        }

        // Stop the game if it's running
        if (gameScreen != null) {
            gameScreen.stopGame();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}