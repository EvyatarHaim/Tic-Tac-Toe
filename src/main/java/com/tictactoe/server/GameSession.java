package com.tictactoe.server;

import com.tictactoe.Utils;
import com.tictactoe.db.DatabaseManager;
import com.tictactoe.game.Game;
import com.tictactoe.game.GameResult;
import com.tictactoe.game.Player;

public class GameSession {
    private Game game;
    private Player player1;
    private Player player2;
    private ClientHandler player1Handler;
    private ClientHandler player2Handler;
    private long startTime;
    private boolean isGameOver;

    public GameSession(int boardSize, Player player1, Player player2,
                       ClientHandler player1Handler, ClientHandler player2Handler) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;

        // Assign player symbols
        player1.setSymbol(Utils.SYMBOL_X);
        player2.setSymbol(Utils.SYMBOL_O);

        // Create a new game with the specified board size
        this.game = new Game(boardSize, player1, player2);
        this.isGameOver = false;
    }

    /**
     * Start the game by notifying both players
     */
    public void start() {
        System.out.println("Starting game session between " + player1.getName() + " and " + player2.getName());

        startTime = System.currentTimeMillis();

        // Create game start message with player info
        Utils.Message player1Message = new Utils.Message(Utils.MessageType.GAME_START);
        player1Message.setData(Utils.Keys.PLAYER_SYMBOL, String.valueOf(player1.getSymbol()));
        player1Message.setData(Utils.Keys.OPPONENT_NAME, player2.getName());
        player1Message.setData(Utils.Keys.OPPONENT_SYMBOL, String.valueOf(player2.getSymbol()));
        player1Message.setData(Utils.Keys.BOARD_SIZE, game.getBoardSize());
        player1Message.setData(Utils.Keys.IS_YOUR_TURN, true);  // X always goes first

        Utils.Message player2Message = new Utils.Message(Utils.MessageType.GAME_START);
        player2Message.setData(Utils.Keys.PLAYER_SYMBOL, String.valueOf(player2.getSymbol()));
        player2Message.setData(Utils.Keys.OPPONENT_NAME, player1.getName());
        player2Message.setData(Utils.Keys.OPPONENT_SYMBOL, String.valueOf(player1.getSymbol()));
        player2Message.setData(Utils.Keys.BOARD_SIZE, game.getBoardSize());
        player2Message.setData(Utils.Keys.IS_YOUR_TURN, false);

        // Send messages to players
        player1Handler.sendMessage(player1Message);
        player2Handler.sendMessage(player2Message);
    }

    /**
     * Process a move from a player
     * @param player The player making the move
     * @param row Row index of the move
     * @param col Column index of the move
     * @return true if the move was valid, false otherwise
     */
    public synchronized boolean makeMove(Player player, int row, int col) {
        if (isGameOver || !game.isPlayerTurn(player) || !game.isValidMove(row, col)) {
            return false;
        }

        // Make the move
        game.makeMove(row, col);

        // Send move result to both players
        Utils.Message moveMessage = new Utils.Message(Utils.MessageType.MOVE_RESULT);
        moveMessage.setData(Utils.Keys.ROW, row);
        moveMessage.setData(Utils.Keys.COL, col);
        moveMessage.setData(Utils.Keys.SYMBOL, String.valueOf(player.getSymbol()));
        moveMessage.setData(Utils.Keys.NEXT_TURN, game.getCurrentPlayer().getName());

        player1Handler.sendMessage(moveMessage);
        player2Handler.sendMessage(moveMessage);

        // Check if game is over
        if (game.isGameOver()) {
            isGameOver = true;
            long gameDuration = System.currentTimeMillis() - startTime;
            endGame(gameDuration);
        }

        return true;
    }

    /**
     * Check if it's the given player's turn
     */
    public boolean isPlayerTurn(Player player) {
        return game.isPlayerTurn(player);
    }

    /**
     * Get the current game state
     */
    public Game getGame() {
        return game;
    }

    /**
     * End the game and notify both players
     */
    private void endGame(long gameDuration) {
        Utils.Message gameOverMessage = new Utils.Message(Utils.MessageType.GAME_OVER);
        gameOverMessage.setData(Utils.Keys.GAME_DURATION, gameDuration);

        Player winner = game.getWinner();
        if (winner != null) {
            gameOverMessage.setData(Utils.Keys.RESULT, "win");
            gameOverMessage.setData(Utils.Keys.WINNER, winner.getName());
        } else {
            gameOverMessage.setData(Utils.Keys.RESULT, "tie");
        }

        player1Handler.sendMessage(gameOverMessage);
        player2Handler.sendMessage(gameOverMessage);

        // Save game result to the database
        GameResult.Result resultEnum;
        if (winner != null) {
            resultEnum = GameResult.Result.WIN;
        } else {
            resultEnum = GameResult.Result.TIE;
        }

        GameResult gameResult = new GameResult(resultEnum, winner, gameDuration);

        // Save game result in a separate thread to avoid blocking
        new Thread(() -> {
            try {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                dbManager.saveGameResult(player1, player2, gameResult, game.getBoardSize());
            } catch (Exception e) {
                System.err.println("Error saving game result to database: " + e.getMessage());
            }
        }).start();
    }
}