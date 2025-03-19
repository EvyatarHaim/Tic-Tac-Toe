package com.tictactoe.game;

import java.io.Serializable;

public class GameResult implements Serializable {
    private static final long serialVersionUID = 1;

    // possible game results
    public enum Result {
        WIN, LOSE, TIE
    }
    private Result result;
    private Player winner;
    private long gameDuration;

    // create a new game result
    public GameResult(Result result, Player winner, long gameDuration) {
        this.result = result;
        this.winner = winner;
        this.gameDuration = gameDuration;
    }

    // get the result type
    public Result getResult() {
        return result;
    }


    public Player getWinner() {
        return winner;
    }

    // in milliseconds
    public long getGameDuration() {
        return gameDuration;
    }
}