package com.tictactoe.server;

import com.tictactoe.Utils;
import com.tictactoe.game.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    // server components
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService threadPool;
    private Map<Integer, ClientHandler> waitingPlayers;

    // creates a new server
    public Server() {
        this.waitingPlayers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
    }

    // starts the server
    public void start() {
        try {
            serverSocket = new ServerSocket(Utils.SERVER_PORT);
            running = true;
            System.out.println("TicTacToe Server started on port " + Utils.SERVER_PORT);

            // accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // create new client handler and execute it in thread pool
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

    // stop the server
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

    // try to match with a player with another waiting player
    public synchronized boolean matchPlayer(ClientHandler clientHandler, Player player, int boardSize) {
        // check if there is a player waiting for the same board size
        ClientHandler waitingHandler = waitingPlayers.get(boardSize);

        if (waitingHandler == null) {
            // no waiting player for this board size, so add this player to the waiting list
            waitingPlayers.put(boardSize, clientHandler);
            return false;
        } else {
            // found a waiting player, remove from waiting list
            waitingPlayers.remove(boardSize);

            // get waiting player
            Player waitingPlayer = waitingHandler.getPlayer();

            // create a new game session
            GameSession gameSession = new GameSession(boardSize, waitingPlayer, player, waitingHandler, clientHandler);
            waitingHandler.setGameSession(gameSession);
            clientHandler.setGameSession(gameSession);

            // start the game
            gameSession.start();
            return true;
        }
    }

    // removes a player from the waiting list
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