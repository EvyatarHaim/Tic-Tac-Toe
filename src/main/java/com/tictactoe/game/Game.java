package com.tictactoe.game;

import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 1;

    private Board board;
    private Player player1; // X player
    private Player player2; // O player
    private Player currentPlayer;
    private Player winner;
    private boolean gameOver;

    // creates a new game with the given board size and players
    public Game(int boardSize, Player player1, Player player2) {
        this.board = new Board(boardSize);
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1; // X always goes first
        this.gameOver = false;
        this.winner = null;
    }

    // checks if a move is valid
    public boolean isValidMove(int row, int col) {
        return !gameOver && board.isValidMove(row, col);
    }

    // makes a move on the board
    public void makeMove(int row, int col) {
        if (isValidMove(row, col)) {
            board.makeMove(row, col, currentPlayer.getSymbol());

            // check if the game is over
            if (board.checkWin(currentPlayer.getSymbol())) {
                gameOver = true;
                winner = currentPlayer;
            }
            else if (board.isFull()) {
                gameOver = true;
            }
            else {
                // switch players
                if (currentPlayer == player1) {
                    currentPlayer = player2;
                }
                else {
                    currentPlayer = player1;
                }
            }
        }
    }

    // checks if the game is over
    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // checks if it is the player turn
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