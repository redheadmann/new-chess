package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    // Maps gameIDs to sets of Connection objects, which relate users in a game to their individual sessions
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> gameSessions = new ConcurrentHashMap<>();


    public void add(Integer gameID, String username, Session session) {
        // Create new entry in hashmap if gameID missing
        ConcurrentHashMap<String, Connection> connections = gameSessions.get(gameID);
        if (connections == null) {
            connections = new ConcurrentHashMap<>();
            connections.put(username, new Connection(username, session));
        } else {
            connections.put(username, new Connection(username, session));
        }
        gameSessions.put(gameID, connections);
    }

    public void remove(Integer gameID, String username) {
        ConcurrentHashMap<String, Connection> connections = gameSessions.get(gameID);
        if (connections == null) {
            return;
        }
        // If the user is the last one viewing the game, remove the entire game from the hashmap
        if (connections.size() == 1) {
            gameSessions.remove(gameID);
        } else { // otherwise, update the hashmap of users to Connection objects
            connections.remove(username);
        }
    }

    private void cleanupDeadConnections() {
        HashMap<Integer, String> removalList = new HashMap<>();

        // Find connections which are no longer open and close them
        for (Integer gameID : gameSessions.keySet()) {
            var gameConnections = gameSessions.get(gameID);
            for (String username : gameConnections.keySet()) {
                Connection connection = gameConnections.get(username);
                if (!connection.session.isOpen()) {
                    removalList.put(gameID, username);
                }
            }
        }
        // Close without issues brought by closing during iteration
        for (Integer gameID : removalList.keySet()) {
            String username = removalList.get(gameID);
            remove(gameID, username);
        }
    }

    // Tells broadcast method whether to send to all, one, or all but one
    public enum SendType {
        ALL,
        ONE,
        EXCLUDE_ONE
    }

    public void broadcast(Integer gameID, String specialUsername, ServerMessage notification,
                          SendType sendType) {
        // Collect games and respective users to remove if they no longer have connections
        HashMap<Integer, String> removalList = new HashMap<>();


        // Broadcast to users in the game except the one we exclude
        ConcurrentHashMap<String, Connection> gameConnections = gameSessions.get(gameID);
        for (String username : gameConnections.keySet()) {
            Connection connection = gameConnections.get(username);
            // Skip closed connections
            if (!connection.session.isOpen()) {
                continue;
            }

            // Send based on type
            boolean shouldSend = switch (sendType) {
                case ALL -> true;
                case ONE -> connection.username.equals(specialUsername);
                case EXCLUDE_ONE -> !connection.username.equals(specialUsername);
            };
            if (shouldSend) {
                try {
                    connection.send(notification.toString());
                } catch (IOException ignore) { // ignore failed sends
                    // log this in the future
                }
            }
        }

        // Clean up any connections that are no longer open
        cleanupDeadConnections();
    }


}
