package com.tictactoe.server;

import com.tictactoe.Utils;
import com.tictactoe.game.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Server server;
    private Player player;
    private GameSession gameSession;
    private boolean running;
    private int requestedBoardSize;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.running = true;

        try {
            // Create input/output streams for communication with client
            this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
            closeConnection();
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                // Read message from client
                Utils.Message message = (Utils.Message) inputStream.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            // Clean up
            if (player != null && gameSession == null) {
                // If player was waiting but didn't get matched
                server.removeWaitingPlayer(this, requestedBoardSize);
            }
            closeConnection();
        }
    }

    /**
     * Process a message from the client
     */
    private void processMessage(Utils.Message message) {
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;
            case MOVE:
                handleMove(message);
                break;
            case QUIT:
                running = false;
                break;
            default:
                sendError("Unsupported message type: " + message.getType());
        }
    }

    /**
     * Handle login message from client
     */
    private void handleLogin(Utils.Message message) {
        String playerName = (String) message.getData(Utils.Keys.PLAYER_NAME);
        int boardSize = (int) message.getData(Utils.Keys.BOARD_SIZE);

        // Validate board size
        if (boardSize != Utils.BOARD_SIZE_3X3 &&
                boardSize != Utils.BOARD_SIZE_4X4 &&
                boardSize != Utils.BOARD_SIZE_5X5) {
            sendError("Invalid board size: " + boardSize);
            return;
        }

        // Create player
        this.player = new Player(playerName);
        this.requestedBoardSize = boardSize;

        // Try to match with another player
        boolean matched = server.matchPlayer(this, player, boardSize);

        if (!matched) {
            // Send waiting message to client
            Utils.Message waitMessage = new Utils.Message(Utils.MessageType.WAIT);
            waitMessage.setData(Utils.Keys.BOARD_SIZE, boardSize);
            sendMessage(waitMessage);
        }
    }

    /**
     * Handle move message from client
     */
    private void handleMove(Utils.Message message) {
        if (gameSession == null || player == null) {
            sendError("Game not started yet");
            return;
        }

        int row = (int) message.getData(Utils.Keys.ROW);
        int col = (int) message.getData(Utils.Keys.COL);

        // Try to make the move
        boolean moveResult = gameSession.makeMove(player, row, col);

        if (!moveResult) {
            // Send error if move was invalid
            Utils.Message errorMessage = new Utils.Message(Utils.MessageType.ERROR);
            errorMessage.setData(Utils.Keys.MESSAGE, "Invalid move");
            sendMessage(errorMessage);
        }
    }

    /**
     * Send an error message to the client
     */
    private void sendError(String errorMessage) {
        Utils.Message error = new Utils.Message(Utils.MessageType.ERROR);
        error.setData(Utils.Keys.MESSAGE, errorMessage);
        sendMessage(error);
    }

    /**
     * Send a message to the client
     */
    public void sendMessage(Utils.Message message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            running = false;
        }
    }

    /**
     * Close the connection with the client
     */
    private void closeConnection() {
        running = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Set the game session for this client
     */
    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    /**
     * Get the player associated with this client
     */
    public Player getPlayer() {
        return player;
    }
}