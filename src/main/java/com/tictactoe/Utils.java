package com.tictactoe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    // Network constants
    public static final int SERVER_PORT = 1234;
    public static final String SERVER_HOST = "localhost";

    // Game constants
    public static final int BOARD_SIZE_3X3 = 3;
    public static final int BOARD_SIZE_4X4 = 4;
    public static final int BOARD_SIZE_5X5 = 5;

    // Symbols
    public static final char SYMBOL_X = 'X';
    public static final char SYMBOL_O = 'O';
    public static final char SYMBOL_EMPTY = ' ';

    // Protocol message types
    public enum MessageType {
        LOGIN,           // Client logs in with name and board size
        WAIT,            // Server tells client to wait for opponent
        GAME_START,      // Server notifies clients that game has started
        MOVE,            // Client makes a move
        MOVE_RESULT,     // Server sends result of a move
        GAME_OVER,       // Server notifies game is over
        ERROR,           // Error message
        QUIT             // Client quits the game
    }

    // Message keys
    public static class Keys {
        // Login message keys
        public static final String PLAYER_NAME = "playerName";
        public static final String BOARD_SIZE = "boardSize";

        // Game start message keys
        public static final String PLAYER_SYMBOL = "playerSymbol";
        public static final String OPPONENT_NAME = "opponentName";
        public static final String OPPONENT_SYMBOL = "opponentSymbol";
        public static final String IS_YOUR_TURN = "isYourTurn";

        // Move message keys
        public static final String ROW = "row";
        public static final String COL = "col";
        public static final String SYMBOL = "symbol";
        public static final String NEXT_TURN = "nextTurn";

        // Game over message keys
        public static final String RESULT = "result";
        public static final String WINNER = "winner";
        public static final String GAME_DURATION = "gameDuration";

        // Error message key
        public static final String MESSAGE = "message";
    }

    // Message class for client-server communication
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;

        private MessageType type;
        private Map<String, Object> data;

        public Message(MessageType type) {
            this.type = type;
            this.data = new HashMap<>();
        }

        public MessageType getType() {
            return type;
        }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public Object getData(String key) {
            return data.get(key);
        }

        public boolean hasData(String key) {
            return data.containsKey(key);
        }

        @Override
        public String toString() {
            return "Message{type=" + type + ", data=" + data + "}";
        }
    }
}