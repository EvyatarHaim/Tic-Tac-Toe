package com.tictactoe.game;

import com.tictactoe.Utils;
import java.io.Serializable;

public class Board implements Serializable {
    private static final long serialVersionUID = 1;

    private char[][] grid;  // Matrix that represents the game board
    private int size;   // size of the board (3x3, 4x4, or 5x5)

    // creates a new empty board with given size
    public Board(int size) {
        this.size = size;
        this.grid = new char[size][size];

        // fills the board cells with empty spaces
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = Utils.SYMBOL_EMPTY;
            }
        }
    }

    // checks if move at given position is valid
    public boolean isValidMove(int row, int col) {
        // check if position is within board boundaries
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }

        // check if position is empty
        return grid[row][col] == Utils.SYMBOL_EMPTY;
    }

    // places a symbol at the given position
    public void makeMove(int row, int col, char symbol) {
        if (isValidMove(row, col)) {
            grid[row][col] = symbol;
        }
    }

    // gets the symbol at a specific position
    public char getSymbolAt(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return ' ';
        }
        return grid[row][col];
    }

    // checks if the board is completely filled
    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == Utils.SYMBOL_EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    // checks if a player with given symbol has won
    public boolean checkWin(char symbol) {
        // check rows for win
        for (int i = 0; i < size; i++) {
            boolean rowWin = true;
            for (int j = 0; j < size; j++) {
                if (grid[i][j] != symbol) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin) return true;
        }

        // check columns for win
        for (int j = 0; j < size; j++) {
            boolean colWin = true;
            for (int i = 0; i < size; i++) {
                if (grid[i][j] != symbol) {
                    colWin = false;
                    break;
                }
            }
            if (colWin) return true;
        }

        // check diagonal from top-left to bottom-right
        boolean diag1Win = true;
        // check diagonal from top-right to bottom-left
        boolean diag2Win = true;
        for (int i = 0; i < size; i++) {
            if (grid[i][i] != symbol) {
                diag1Win = false;
            }
            if (grid[i][size - i - 1] != symbol) {
                diag2Win = false;
            }
        }

        return diag1Win || diag2Win;
    }

    public int getSize() {
        return size;
    }

    public char[][] getGrid() {
        return grid;
    }
}