// com.tictactoe.game.Board
package com.tictactoe.game;

import com.tictactoe.Utils;
import java.io.Serializable;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private char[][] grid;
    private int size;

    public Board(int size) {
        this.size = size;
        this.grid = new char[size][size];

        // Initialize board with empty spaces
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = Utils.SYMBOL_EMPTY;
            }
        }
    }

    public boolean isValidMove(int row, int col) {
        // Check if position is within bounds
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }

        // Check if position is empty
        return grid[row][col] == Utils.SYMBOL_EMPTY;
    }

    public void makeMove(int row, int col, char symbol) {
        if (isValidMove(row, col)) {
            grid[row][col] = symbol;
        }
    }

    public char getSymbolAt(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return ' ';
        }
        return grid[row][col];
    }

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

    public boolean checkWin(char symbol) {
        // Check rows
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

        // Check columns
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

        // Check diagonals
        boolean diag1Win = true;
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