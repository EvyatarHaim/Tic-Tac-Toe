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

    // player info
    private String playerName;
    private int boardSize;
    private boolean connected;
    private boolean inGame;
    private boolean myTurn;
    private char playerSymbol;
    private String opponentName;
    private char opponentSymbol;

    // store received messages
    private BlockingQueue<Utils.Message> messageQueue;

    // thread to listen for coming messages
    private Thread listenerThread;

    // message handler
    private MessageHandler messageHandler;

    // interface for handling messages
    public interface MessageHandler {
        void handleMessage(Utils.Message message);
    }

    // creates a new client
    public Client() {
        this.messageQueue = new LinkedBlockingQueue<>();
        this.connected = false;
        this.inGame = false;
    }

    // connects to the server
    public boolean connect() {
        try {
            socket = new Socket(Utils.SERVER_HOST, Utils.SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            connected = true;

            // start listening for messages
            startMessageListener();

            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    // disconnects from the server
    public void disconnect() {
        if (connected) {
            try {
                // send quit message
                sendMessage(new Utils.Message(Utils.MessageType.QUIT));

                // stop the listener thread
                if (listenerThread != null) {
                    listenerThread.interrupt();
                }

                // close streams and socket
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

    // starts listening for server messages
    private void startMessageListener() {
        listenerThread = new Thread(() -> {
            try {
                while (connected) {
                    Utils.Message message = (Utils.Message) inputStream.readObject();

                    // process the message based on the type
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

                    // add message to the queue
                    messageQueue.add(message);

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

    // sends login request to server
    public boolean login(String playerName, int boardSize) {
        if (!connected) {
            return false;
        }

        this.playerName = playerName;
        this.boardSize = boardSize;

        // create login message
        Utils.Message loginMessage = new Utils.Message(Utils.MessageType.LOGIN);
        loginMessage.setData(Utils.Keys.PLAYER_NAME, playerName);
        loginMessage.setData(Utils.Keys.BOARD_SIZE, boardSize);

        // send login message
        return sendMessage(loginMessage);
    }

    // makes a move on the board
    public boolean makeMove(int row, int col) {
        if (!connected || !inGame || !myTurn) {
            return false;
        }

        // create move message
        Utils.Message moveMessage = new Utils.Message(Utils.MessageType.MOVE);
        moveMessage.setData(Utils.Keys.ROW, row);
        moveMessage.setData(Utils.Keys.COL, col);

        // send move message
        boolean sent = sendMessage(moveMessage);

        // set my turn to false until move result is received
        if (sent) {
            myTurn = false;
        }

        return sent;
    }

    // sends a message to the server
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

    // handles game start message
    private void handleGameStart(Utils.Message message) {
        playerSymbol = message.getData(Utils.Keys.PLAYER_SYMBOL).toString().charAt(0);
        opponentName = (String) message.getData(Utils.Keys.OPPONENT_NAME);
        opponentSymbol = message.getData(Utils.Keys.OPPONENT_SYMBOL).toString().charAt(0);
        myTurn = (boolean) message.getData(Utils.Keys.IS_YOUR_TURN);
        inGame = true;

        System.out.println("Game started! You are playing as " + playerSymbol +
                " against " + opponentName + " (" + opponentSymbol + ")");
    }

    // handles move result message
    private void handleMoveResult(Utils.Message message) {
        String nextTurn = (String) message.getData(Utils.Keys.NEXT_TURN);
        myTurn = nextTurn.equals(playerName);
    }

    // handles game over message
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

    // formats duration from milliseconds to minutes:seconds
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    // sets message handler
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    // gets the next message from the queue
    public Utils.Message getNextMessage() {
        return messageQueue.poll();
    }

    // waits for a specific message type
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