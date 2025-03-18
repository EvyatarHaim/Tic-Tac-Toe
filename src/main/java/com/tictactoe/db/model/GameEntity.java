package com.tictactoe.db.model;

import java.util.Date;
/**
 * GameEntity represents a completed game in the database
 */
public class GameEntity extends BaseEntity {
    private int player1Id;
    private int player2Id;
    private int winnerId;  // 0 if a tie
    private String result; // "WIN", "LOSE", "TIE"
    private int boardSize;
    private long duration; // in milliseconds
    private Date playedAt;

    // References to related entities
    private PlayerEntity player1;
    private PlayerEntity player2;
    private PlayerEntity winner;

    public GameEntity() {
        // Default constructor
    }

    // Getters and Setters
    public int getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(int player2Id) {
        this.player2Id = player2Id;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Date getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Date playedAt) {
        this.playedAt = playedAt;
    }

    public PlayerEntity getPlayer1() {
        return player1;
    }

    public void setPlayer1(PlayerEntity player1) {
        this.player1 = player1;
        if (player1 != null) {
            this.player1Id = player1.getId();
        }
    }

    public PlayerEntity getPlayer2() {
        return player2;
    }

    public void setPlayer2(PlayerEntity player2) {
        this.player2 = player2;
        if (player2 != null) {
            this.player2Id = player2.getId();
        }
    }

    public PlayerEntity getWinner() {
        return winner;
    }

    public void setWinner(PlayerEntity winner) {
        this.winner = winner;
        if (winner != null) {
            this.winnerId = winner.getId();
        } else {
            this.winnerId = 0; // 0 indicates a tie
        }
    }

    @Override
    public String toString() {
        return "GameEntity{" +
                "id=" + id +
                ", player1Id=" + player1Id +
                ", player2Id=" + player2Id +
                ", winnerId=" + winnerId +
                ", result='" + result + '\'' +
                ", boardSize=" + boardSize +
                ", duration=" + duration +
                ", playedAt=" + playedAt +
                '}';
    }
}