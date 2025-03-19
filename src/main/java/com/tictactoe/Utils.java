package com.tictactoe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static final int SERVER_PORT = 1234;
    public static final String SERVER_HOST = "localhost";

    // game
    public static final int BOARD_SIZE_3X3 = 3;
    public static final int BOARD_SIZE_4X4 = 4;
    public static final int BOARD_SIZE_5X5 = 5;

    public static final char SYMBOL_X = 'X';
    public static final char SYMBOL_O = 'O';
    public static final char SYMBOL_EMPTY = ' ';

    // message types for client server communication
    public enum MessageType {
        LOGIN,           // client logs in with name and board size
        WAIT,            // server tells client to wait for opponent
        GAME_START,      // server notifies clients that game has started
        MOVE,            // client makes a move
        MOVE_RESULT,     // server sends result of a move
        GAME_OVER,       // server notifies game is over
        ERROR,           // error message
        QUIT             // client quits the game
    }

    // keys for message data
    public static class Keys {
        // login message keys
        public static final String PLAYER_NAME = "playerName";
        public static final String BOARD_SIZE = "boardSize";

        // game start message keys
        public static final String PLAYER_SYMBOL = "playerSymbol";
        public static final String OPPONENT_NAME = "opponentName";
        public static final String OPPONENT_SYMBOL = "opponentSymbol";
        public static final String IS_YOUR_TURN = "isYourTurn";

        // move message keys
        public static final String ROW = "row";
        public static final String COL = "col";
        public static final String SYMBOL = "symbol";
        public static final String NEXT_TURN = "nextTurn";

        // game over message keys
        public static final String RESULT = "result";
        public static final String WINNER = "winner";
        public static final String GAME_DURATION = "gameDuration";

        // error message key
        public static final String MESSAGE = "message";
    }

    // message class for client server communication
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1;

        // message type and data
        private MessageType type;
        private Map<String, Object> data;

        // creates a new message with the given type
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

        // checks if key exists
        public boolean hasData(String key) {
            return data.containsKey(key);
        }

        @Override
        public String toString() {
            return "Message{type=" + type + ", data=" + data + "}";
        }
    }
}