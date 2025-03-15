// com.tictactoe.client.TicTacToeClient
package com.tictactoe.client;

import com.tictactoe.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String playerName;
    private int boardSize;
    private boolean connected;
    private boolean inGame;
    private boolean myTurn;
    private char playerSymbol;
    private String opponentName;
    private char opponentSymbol;

    // Queue for received messages
    private BlockingQueue<Utils.Message> messageQueue;

    // Message listener thread
    private Thread listenerThread;

    // Message handler callback
    private MessageHandler messageHandler;

    // Interface for handling messages
    public interface MessageHandler {
        void handleMessage(Utils.Message message);
    }

    public Client() {
        this.messageQueue = new LinkedBlockingQueue<>();
        this.connected = false;
        this.inGame = false;
    }

    /**
     * Connect to the server
     * @return true if connection successful, false otherwise
     */
    public boolean connect() {
        try {
            socket = new Socket(Utils.SERVER_HOST, Utils.SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            connected = true;

            // Start message listener
            startMessageListener();

            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (connected) {
            try {
                // Send quit message
                sendMessage(new Utils.Message(Utils.MessageType.QUIT));

                // Stop the listener thread
                if (listenerThread != null) {
                    listenerThread.interrupt();
                }

                // Close streams and socket
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null) socket.close();

                connected = false;
                inGame = false;
            } catch (IOException e) {
                System.err.println("Error disconnecting from server: " + e.getMessage());
            }
        }
    }

    /**
     * Start the message listener thread
     */
    private void startMessageListener() {
        listenerThread = new Thread(() -> {
            try {
                while (connected) {
                    Utils.Message message = (Utils.Message) inputStream.readObject();

                    // Process message according to type
                    switch (message.getType()) {
                        case GAME_START:
                            handleGameStart(message);
                            break;
                        case MOVE_RESULT:
                            handleMoveResult(message);
                            break;
                        case GAME_OVER:
                            handleGameOver(message);
                            break;
                    }

                    // Add message to queue
                    messageQueue.add(message);

                    // Notify message handler
                    if (messageHandler != null) {
                        messageHandler.handleMessage(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("Error receiving message: " + e.getMessage());
                    disconnect();
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Send login request to the server
     * @param playerName Player name
     * @param boardSize Board size (3, 4, or 5)
     * @return true if the login message was sent successfully
     */
    public boolean login(String playerName, int boardSize) {
        if (!connected) {
            return false;
        }

        this.playerName = playerName;
        this.boardSize = boardSize;

        // Create login message
        Utils.Message loginMessage = new Utils.Message(Utils.MessageType.LOGIN);
        loginMessage.setData(Utils.Keys.PLAYER_NAME, playerName);
        loginMessage.setData(Utils.Keys.BOARD_SIZE, boardSize);

        // Send login message
        return sendMessage(loginMessage);
    }

    /**
     * Make a move on the board
     * @param row Row index
     * @param col Column index
     * @return true if the move message was sent successfully
     */
    public boolean makeMove(int row, int col) {
        if (!connected || !inGame || !myTurn) {
            return false;
        }

        // Create move message
        Utils.Message moveMessage = new Utils.Message(Utils.MessageType.MOVE);
        moveMessage.setData(Utils.Keys.ROW, row);
        moveMessage.setData(Utils.Keys.COL, col);

        // Send move message
        boolean sent = sendMessage(moveMessage);

        // Temporarily set turn to false until move result is received
        if (sent) {
            myTurn = false;
        }

        return sent;
    }

    /**
     * Send a message to the server
     * @param message Message to send
     * @return true if the message was sent successfully
     */
    private boolean sendMessage(Utils.Message message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * Handle game start message
     * @param message Game start message
     */
    private void handleGameStart(Utils.Message message) {
        playerSymbol = message.getData(Utils.Keys.PLAYER_SYMBOL).toString().charAt(0);
        opponentName = (String) message.getData(Utils.Keys.OPPONENT_NAME);
        opponentSymbol = message.getData(Utils.Keys.OPPONENT_SYMBOL).toString().charAt(0);
        myTurn = (boolean) message.getData(Utils.Keys.IS_YOUR_TURN);
        inGame = true;

        System.out.println("Game started! You are playing as " + playerSymbol +
                " against " + opponentName + " (" + opponentSymbol + ")");
    }

    /**
     * Handle move result message
     * @param message Move result message
     */
    private void handleMoveResult(Utils.Message message) {
        String nextTurn = (String) message.getData(Utils.Keys.NEXT_TURN);
        myTurn = nextTurn.equals(playerName);
    }

    /**
     * Handle game over message
     * @param message Game over message
     */
    private void handleGameOver(Utils.Message message) {
        inGame = false;
        myTurn = false;

        String result = (String) message.getData(Utils.Keys.RESULT);
        String winner = (String) message.getData(Utils.Keys.WINNER);
        long gameDuration = (long) message.getData(Utils.Keys.GAME_DURATION);

        if (result.equals("win")) {
            if (winner.equals(playerName)) {
                System.out.println("Congratulations! You won!");
            } else {
                System.out.println("You lost. Better luck next time!");
            }
        } else {
            System.out.println("The game ended in a tie.");
        }

        System.out.println("Game duration: " + formatDuration(gameDuration));
    }

    /**
     * Format duration in milliseconds to a readable string
     * @param durationMs Duration in milliseconds
     * @return Formatted duration string
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Set message handler
     * @param handler Message handler
     */
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    /**
     * Get the next message from the queue
     * @return Next message or null if queue is empty
     */
    public Utils.Message getNextMessage() {
        return messageQueue.poll();
    }

    /**
     * Wait for a specific message type
     * @param type Message type to wait for
     * @param timeoutMs Timeout in milliseconds
     * @return The message, or null if timeout
     */
    public Utils.Message waitForMessage(Utils.MessageType type, long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < endTime) {
            Utils.Message message = getNextMessage();
            if (message != null && message.getType() == type) {
                return message;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return null;
    }

    // Getters for client state
    public boolean isConnected() {
        return connected;
    }

    public boolean isInGame() {
        return inGame;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public char getPlayerSymbol() {
        return playerSymbol;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public char getOpponentSymbol() {
        return opponentSymbol;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getBoardSize() {
        return boardSize;
    }
}