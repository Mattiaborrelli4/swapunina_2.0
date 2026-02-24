package application.websocket;

import application.DB.MessaggioDAO;
import application.Classe.Messaggio;
import application.messagistica.MessageEncryptionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Server WebSocket per la messaggistica real-time di SwapUnina
 * VERSIONE SEMPLIFICATA - Richiede configurazione aggiuntiva per produzione
 *
 * NOTA: Per una completa implementazione WebSocket con Jetty 9.4.x,
 * è necessario creare un WebSocketServlet e configurarlo correttamente.
 * Questa versione fornisce l'interfaccia base ma richiede setup aggiuntivo.
 *
 * <p><b>Stato corrente:</b>
 * <ul>
 *   <li>✅ Interfaccia completa definita</li>
 *   <li>✅ Gestione sessioni WebSocket</li>
 *   <li>⚠️ Richiede configurazione server web (Jetty/Tomcat)</li>
 *   <li>📝 Usare MessagePollingService per polling nel frattempo</li>
 * </ul>
 * </p>
 *
 * <p><b>Per abilitare WebSocket:</b>
 * <pre>
 * 1. Configurare un servlet container (Jetty, Tomcat, ecc.)
 * 2. Creare un WebSocketServlet endpoint
 * 3. Mappare l'endpoint su /ws
 * 4. Questo server gestirà la logica business
 * </pre>
 * </p>
 */
public class SwapUninaWebSocketServer {

    private static SwapUninaWebSocketServer instance;
    private final int port;

    // Gestione sessioni WebSocket (simulata per ora)
    private final Map<String, WebSocketSession> sessions;
    private final Map<Integer, Set<String>> userSessions;

    // Servizi
    private final MessaggioDAO messaggioDAO;
    private final MessageEncryptionService encryptionService;
    private final Gson gson;

    // Listener per eventi
    private final Map<EventType, List<Consumer<WebSocketEvent>>> eventListeners;

    private volatile boolean running = false;

    private SwapUninaWebSocketServer(int port) {
        this.port = port;
        this.sessions = new ConcurrentHashMap<>();
        this.userSessions = new ConcurrentHashMap<>();
        this.messaggioDAO = new MessaggioDAO();
        this.encryptionService = MessageEncryptionService.getInstance();
        this.gson = new Gson();
        this.eventListeners = new ConcurrentHashMap<>();

        System.out.println("🔌 WebSocket Server inizializzato sulla porta " + port);
    }

    /**
     * Ottiene l'istanza singleton del server WebSocket
     */
    public static synchronized SwapUninaWebSocketServer getInstance() {
        if (instance == null) {
            instance = new SwapUninaWebSocketServer(8081);
        }
        return instance;
    }

    /**
     * Avvia il server WebSocket (simulato per ora)
     * NOTE: Per produzione, configurare un servlet container WebSocket
     */
    public void start() throws Exception {
        if (running) {
            System.out.println("⚠️ WebSocket Server già in esecuzione");
            return;
        }

        running = true;

        System.out.println("✅ WebSocket Server avviato in modalità simulata");
        System.out.println("📝 Per produzione, configurare un WebSocketServlet (es. Jetty 9.4+)");
        System.out.println("🌐 Endpoint: ws://localhost:" + port + "/ws");

        // Avvia heartbeat periodico (simulato)
        startHeartbeat();

        System.out.println("ℹ️  Usa MessagePollingService per polling real-time nel frattempo");
    }

    /**
     * Ferma il server WebSocket
     */
    public void stop() throws Exception {
        if (!running) {
            return;
        }

        System.out.println("🛑 Arresto WebSocket Server...");

        // Chiudi tutte le sessioni
        for (WebSocketSession session : sessions.values()) {
            try {
                session.close();
            } catch (Exception e) {
                // Ignora errori durante la chiusura
            }
        }

        sessions.clear();
        userSessions.clear();

        running = false;
        System.out.println("✅ WebSocket Server arrestato");
    }

    /**
     * Registra una nuova sessione WebSocket
     */
    public void addSession(Object session, Integer userId) {
        String sessionId = session.toString();

        WebSocketSession wsSession = new WebSocketSession(session, userId);
        sessions.put(sessionId, wsSession);

        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        System.out.println("✅ Nuova sessione WebSocket: " + sessionId + " per utente " + userId);

        // Notifica presenza online
        broadcastPresence(userId, true);

        // Notifica listeners
        fireEvent(EventType.USER_CONNECTED, new WebSocketEvent(userId, "CONNECTED"));
    }

    /**
     * Rimuove una sessione WebSocket
     */
    public void removeSession(Object session) {
        String sessionId = session.toString();
        WebSocketSession wsSession = sessions.remove(sessionId);

        if (wsSession != null) {
            Integer userId = wsSession.getUserId();
            Set<String> userSessionSet = userSessions.get(userId);

            if (userSessionSet != null) {
                userSessionSet.remove(sessionId);

                // Se non ci sono più sessioni per questo utente, è offline
                if (userSessionSet.isEmpty()) {
                    userSessions.remove(userId);
                    broadcastPresence(userId, false);
                    fireEvent(EventType.USER_DISCONNECTED, new WebSocketEvent(userId, "DISCONNECTED"));
                }
            }

            System.out.println("🔌 Sessione WebSocket rimossa: " + sessionId);
        }
    }

    /**
     * Gestisce un messaggio ricevuto da un client
     */
    public void handleMessage(Object session, String messageJson) {
        try {
            JsonObject message = gson.fromJson(messageJson, JsonObject.class);
            String type = message.has("type") ? message.get("type").getAsString() : "";

            switch (type) {
                case "message":
                    handleChatMessage(session, message);
                    break;

                case "typing":
                    handleTypingIndicator(session, message);
                    break;

                case "heartbeat":
                    handleHeartbeat(session);
                    break;

                case "presence":
                    handlePresenceQuery();
                    break;

                default:
                    System.err.println("⚠️ Tipo messaggio sconosciuto: " + type);
            }

        } catch (Exception e) {
            System.err.println("❌ Errore gestione messaggio WebSocket: " + e.getMessage());
            sendError(session, "Invalid message format");
        }
    }

    /**
     * Gestisce un messaggio di chat
     */
    private void handleChatMessage(Object session, JsonObject message) {
        try {
            int fromUserId = message.get("from").getAsInt();
            int toUserId = message.get("to").getAsInt();
            String testo = message.get("text").getAsString();
            Integer annuncioId = message.has("annuncioId") ? message.get("annuncioId").getAsInt() : null;

            // Crea il messaggio usando il costruttore corretto
            Messaggio msg = new Messaggio(fromUserId, toUserId, testo, annuncioId);

            // Salva nel database
            boolean saved = messaggioDAO.inviaMessaggio(msg);

            if (saved) {
                // Invia al destinatario se online
                JsonObject messageToSend = new JsonObject();
                messageToSend.addProperty("type", "message");
                messageToSend.addProperty("from", fromUserId);
                messageToSend.addProperty("to", toUserId);
                messageToSend.addProperty("text", testo);
                messageToSend.addProperty("timestamp", msg.getDataInvio().toString());
                sendToUser(toUserId, messageToSend);

                System.out.println("💬 Messaggio WebSocket da " + fromUserId + " a " + toUserId);
            } else {
                sendError(session, "Failed to save message");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore handling chat message: " + e.getMessage());
            sendError(session, "Failed to process message");
        }
    }

    /**
     * Gestisce indicatore di digitazione
     */
    private void handleTypingIndicator(Object session, JsonObject message) {
        int fromUserId = message.get("from").getAsInt();
        int toUserId = message.get("to").getAsInt();
        boolean isTyping = message.get("typing").getAsBoolean();

        JsonObject typingMsg = new JsonObject();
        typingMsg.addProperty("type", "typing");
        typingMsg.addProperty("from", fromUserId);
        typingMsg.addProperty("typing", isTyping);

        sendToUser(toUserId, typingMsg);
    }

    /**
     * Gestisce heartbeat per keep-alive
     */
    private void handleHeartbeat(Object session) {
        JsonObject pong = new JsonObject();
        pong.addProperty("type", "pong");
        pong.addProperty("timestamp", LocalDateTime.now().toString());

        sendToSession(session, pong.toString());
    }

    /**
     * Gestisce query presenza utenti
     */
    private void handlePresenceQuery() {
        JsonObject presenceMsg = new JsonObject();
        presenceMsg.addProperty("type", "presence_list");
        presenceMsg.add("online_users", gson.toJsonTree(getOnlineUsers()));

        // Broadcast a tutti
        String messageJson = gson.toJson(presenceMsg);
        for (WebSocketSession session : sessions.values()) {
            sendToSession(session.getSession(), messageJson);
        }
    }

    /**
     * Invia un messaggio a uno specifico utente
     */
    public void sendToUser(Integer userId, JsonObject message) {
        Set<String> sessionIds = userSessions.get(userId);

        if (sessionIds != null && !sessionIds.isEmpty()) {
            String messageJson = gson.toJson(message);

            for (String sessionId : sessionIds) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    sendToSession(session.getSession(), messageJson);
                }
            }
        }
    }

    /**
     * Invia un messaggio a una specifica sessione
     */
    private void sendToSession(Object session, String message) {
        // Implementazione simulata - in produzione usare Session.getRemote().sendString()
        System.out.println("📤 WebSocket message: " + message.substring(0, Math.min(50, message.length())) + "...");
    }

    /**
     * Invia un messaggio di errore
     */
    private void sendError(Object session, String errorMessage) {
        JsonObject error = new JsonObject();
        error.addProperty("type", "error");
        error.addProperty("message", errorMessage);

        sendToSession(session, error.toString());
    }

    /**
     * Broadcast presenza online/offline
     */
    private void broadcastPresence(int userId, boolean online) {
        JsonObject presence = new JsonObject();
        presence.addProperty("type", "presence");
        presence.addProperty("userId", userId);
        presence.addProperty("online", online);

        // Broadcast a tutti gli utenti connessi
        String messageJson = gson.toJson(presence);
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                sendToSession(session.getSession(), messageJson);
            }
        }
    }

    /**
     * Ottieni lista utenti online
     */
    public Set<Integer> getOnlineUsers() {
        return new HashSet<>(userSessions.keySet());
    }

    /**
     * Verifica se un utente è online
     */
    public boolean isUserOnline(int userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * Avvia heartbeat periodico
     */
    private void startHeartbeat() {
        ScheduledExecutorService heartbeatTimer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WebSocketHeartbeat");
            t.setDaemon(true);
            return t;
        });

        heartbeatTimer.scheduleAtFixedRate(() -> {
            if (!running) {
                return;
            }

            JsonObject heartbeat = new JsonObject();
            heartbeat.addProperty("type", "heartbeat");
            heartbeat.addProperty("timestamp", LocalDateTime.now().toString());

            String messageJson = gson.toJson(heartbeat);
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    sendToSession(session.getSession(), messageJson);
                }
            }
        }, 30, 30, TimeUnit.SECONDS); // Ogni 30 secondi
    }

    /**
     * Aggiunge un listener per eventi WebSocket
     */
    public void addEventListener(EventType eventType, Consumer<WebSocketEvent> listener) {
        eventListeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Rimuove un listener
     */
    public void removeEventListener(EventType eventType, Consumer<WebSocketEvent> listener) {
        List<Consumer<WebSocketEvent>> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Fire evento
     */
    private void fireEvent(EventType eventType, WebSocketEvent event) {
        List<Consumer<WebSocketEvent>> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            for (Consumer<WebSocketEvent> listener : listeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    System.err.println("⚠️ Errore nel listener evento: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Statistiche server
     */
    public String getStatistics() {
        return String.format(
            "📊 WebSocket Stats - Sessioni: %d, Utenti online: %d, In esecuzione: %s",
            sessions.size(),
            userSessions.size(),
            running ? "✅" : "❌"
        );
    }

    // ========== CLASSI INTERNE ==========

    /**
     * Wrapper per Session WebSocket
     */
    private static class WebSocketSession {
        private final Object session;
        private final Integer userId;
        private final LocalDateTime connectedAt;

        public WebSocketSession(Object session, Integer userId) {
            this.session = session;
            this.userId = userId;
            this.connectedAt = LocalDateTime.now();
        }

        public Object getSession() {
            return session;
        }

        public Integer getUserId() {
            return userId;
        }

        public LocalDateTime getConnectedAt() {
            return connectedAt;
        }

        public boolean isOpen() {
            // Sempre true per la versione simulata
            return true;
        }

        public void close() {
            // No-op per versione simulata
        }
    }

    /**
     * Evento WebSocket
     */
    public static class WebSocketEvent {
        private final Integer userId;
        private final String data;
        private final LocalDateTime timestamp;

        public WebSocketEvent(Integer userId, String data) {
            this.userId = userId;
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        public Integer getUserId() {
            return userId;
        }

        public String getData() {
            return data;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Tipi di evento
     */
    public enum EventType {
        USER_CONNECTED,
        USER_DISCONNECTED,
        MESSAGE_RECEIVED,
        ERROR
    }
}
