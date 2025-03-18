package com.tictactoe.server;

import com.tictactoe.Utils;
import com.tictactoe.db.DatabaseManager;
import com.tictactoe.game.Player;
import com.tictactoe.game.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService threadPool;
    private Map<Integer, ClientHandler> waitingPlayers;

    public Server() {
        this.waitingPlayers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            // Initialize database with MySQL configuration
            DatabaseManager.getInstance();
            System.out.println("Database initialized successfully");

            serverSocket = new ServerSocket(Utils.SERVER_PORT);
            running = true;
            System.out.println("TicTacToe Server started on port " + Utils.SERVER_PORT);

            // Main server loop - accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Create new client handler and execute it in thread pool
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Tries to match a player with another waiting player for the same board size
     * @param clientHandler The client handler for the player to match
     * @param player The player to match
     * @param boardSize The requested board size
     * @return true if matched with another player and game started, false if player is waiting
     */
    public synchronized boolean matchPlayer(ClientHandler clientHandler, Player player, int boardSize) {
        // Check if there's a player waiting for the same board size
        ClientHandler waitingHandler = waitingPlayers.get(boardSize);

        if (waitingHandler == null) {
            // No waiting player for this board size, so add this player to waiting list
            waitingPlayers.put(boardSize, clientHandler);
            return false;
        } else {
            // Found a waiting player, remove from waiting list
            waitingPlayers.remove(boardSize);

            // Get waiting player
            Player waitingPlayer = waitingHandler.getPlayer();

            // Create a new game session
            GameSession gameSession = new GameSession(boardSize, waitingPlayer, player, waitingHandler, clientHandler);

            // Set game session for both client handlers
            waitingHandler.setGameSession(gameSession);
            clientHandler.setGameSession(gameSession);

            // Start the game
            gameSession.start();
            return true;
        }
    }

    /**
     * Remove a player from the waiting list if they disconnect while waiting
     * @param clientHandler The client handler for the player
     * @param boardSize The board size the player was waiting for
     */
    public synchronized void removeWaitingPlayer(ClientHandler clientHandler, int boardSize) {
        ClientHandler waitingHandler = waitingPlayers.get(boardSize);
        if (waitingHandler == clientHandler) {
            waitingPlayers.remove(boardSize);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}