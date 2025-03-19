package com.tictactoe.game;

public class Player {
    private String name;
    private char symbol;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // symbol (X or O)
    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}