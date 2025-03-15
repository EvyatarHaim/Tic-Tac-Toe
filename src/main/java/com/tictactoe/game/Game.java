// com.tictactoe.game.Game
package com.tictactoe.game;

import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private Board board;
    private Player player1; // X
    private Player player2; // O
    private Player currentPlayer;
    private Player winner;
    private boolean gameOver;

    public Game(int boardSize, Player player1, Player player2) {
        this.board = new Board(boardSize);
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1; // X always goes first
        this.gameOver = false;
        this.winner = null;
    }

    public boolean isValidMove(int row, int col) {
        return !gameOver && board.isValidMove(row, col);
    }

    public void makeMove(int row, int col) {
        if (isValidMove(row, col)) {
            board.makeMove(row, col, currentPlayer.getSymbol());

            // Check if the game is over
            if (board.checkWin(currentPlayer.getSymbol())) {
                gameOver = true;
                winner = currentPlayer;
            } else if (board.isFull()) {
                gameOver = true;
            } else {
                // Switch players
                currentPlayer = (currentPlayer == player1) ? player2 : player1;
            }
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isPlayerTurn(Player player) {
        return !gameOver && currentPlayer == player;
    }

    public int getBoardSize() {
        return board.getSize();
    }

    public Board getBoard() {
        return board;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}