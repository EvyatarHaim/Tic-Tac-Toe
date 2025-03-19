package com.tictactoe.server;

import com.tictactoe.Utils;
import com.tictactoe.game.Player;
import com.tictactoe.game.Game;

public class GameSession {
    private Game game;
    private Player player1;
    private Player player2;
    private ClientHandler player1Handler;
    private ClientHandler player2Handler;

    private long startTime;
    private boolean isGameOver;

    // creates a new game session
    public GameSession(int boardSize, Player player1, Player player2, ClientHandler player1Handler,
                       ClientHandler player2Handler) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;

        // assign symbols
        player1.setSymbol(Utils.SYMBOL_X);
        player2.setSymbol(Utils.SYMBOL_O);

        // create a new game with the board size
        this.game = new Game(boardSize, player1, player2);
        this.isGameOver = false;
    }

    // start the game session
    public void start() {
        System.out.println("Starting game session between " + player1.getName() + " and " + player2.getName());

        startTime = System.currentTimeMillis();

        // create GAME_START message for player 1
        Utils.Message player1Message = new Utils.Message(Utils.MessageType.GAME_START);
        player1Message.setData(Utils.Keys.PLAYER_SYMBOL, String.valueOf(player1.getSymbol()));
        player1Message.setData(Utils.Keys.OPPONENT_NAME, player2.getName());
        player1Message.setData(Utils.Keys.OPPONENT_SYMBOL, String.valueOf(player2.getSymbol()));
        player1Message.setData(Utils.Keys.BOARD_SIZE, game.getBoardSize());
        player1Message.setData(Utils.Keys.IS_YOUR_TURN, true);

        // create GAME_START message for player 2
        Utils.Message player2Message = new Utils.Message(Utils.MessageType.GAME_START);
        player2Message.setData(Utils.Keys.PLAYER_SYMBOL, String.valueOf(player2.getSymbol()));
        player2Message.setData(Utils.Keys.OPPONENT_NAME, player1.getName());
        player2Message.setData(Utils.Keys.OPPONENT_SYMBOL, String.valueOf(player1.getSymbol()));
        player2Message.setData(Utils.Keys.BOARD_SIZE, game.getBoardSize());
        player2Message.setData(Utils.Keys.IS_YOUR_TURN, false);

        player1Handler.sendMessage(player1Message);
        player2Handler.sendMessage(player2Message);
    }

    // processes a move from a player
    public synchronized boolean makeMove(Player player, int row, int col) {
        if (isGameOver || !game.isPlayerTurn(player) || !game.isValidMove(row, col)) {
            return false;
        }

        // make the move
        game.makeMove(row, col);

        // send MOVE_RESULT to both players
        Utils.Message moveMessage = new Utils.Message(Utils.MessageType.MOVE_RESULT);
        moveMessage.setData(Utils.Keys.ROW, row);
        moveMessage.setData(Utils.Keys.COL, col);
        moveMessage.setData(Utils.Keys.SYMBOL, String.valueOf(player.getSymbol()));
        moveMessage.setData(Utils.Keys.NEXT_TURN, game.getCurrentPlayer().getName());

        player1Handler.sendMessage(moveMessage);
        player2Handler.sendMessage(moveMessage);

        // check if the game is over
        if (game.isGameOver()) {
            isGameOver = true;
            long gameDuration = System.currentTimeMillis() - startTime;
            endGame(gameDuration);
        }

        return true;
    }

    // end the game and notifies both players
    private void endGame(long gameDuration) {
        Utils.Message gameOverMessage = new Utils.Message(Utils.MessageType.GAME_OVER);
        gameOverMessage.setData(Utils.Keys.GAME_DURATION, gameDuration);

        Player winner = game.getWinner();
        if (winner != null) {
            gameOverMessage.setData(Utils.Keys.RESULT, "win");
            gameOverMessage.setData(Utils.Keys.WINNER, winner.getName());
        }
        else {
            gameOverMessage.setData(Utils.Keys.RESULT, "tie");
        }

        player1Handler.sendMessage(gameOverMessage);
        player2Handler.sendMessage(gameOverMessage);
    }

    // checks if it is the given player turn
    public boolean isPlayerTurn(Player player) {
        return game.isPlayerTurn(player);
    }

    public Game getGame() {
        return game;
    }
}