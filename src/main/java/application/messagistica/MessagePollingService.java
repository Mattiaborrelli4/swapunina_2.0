package application.messagistica;

import application.DB.MessaggioDAO;
import application.Classe.Messaggio;
import application.Classe.utente;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Servizio di polling periodico per nuovi messaggi
 * Controlla automaticamente se ci sono nuovi messaggi ogni N secondi
 *
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Polling configurabile (default 5 secondi)</li>
 *   <li>ThreadPoolExecutor per operazioni asincrone</li>
 *   <li>Callback per nuovi messaggi</li>
 *   <li>Gestione efficiente delle risorse</li>
 *   <li>Supporto per utenti multipli</li>
 *   <li>Statistiche sul polling</li>
 * </ul>
 * </p>
 *
 * <p><b>Flusso di polling:</b>
 * <pre>
 * Scheduler esegue ogni N secondi
 *    ↓
 * Query database per ultimi messaggi
 *    ↓
 * Confronta con ultimo messaggio noto
 *    ↓
 * Se nuovi messaggi → notifica listeners
 * </pre>
 * </p>
 */
public class MessagePollingService {
    private static volatile MessagePollingService instance;

    // Configurazione polling
    private static final int DEFAULT_POLLING_INTERVAL_SECONDS = 5;
    private static final int INITIAL_DELAY_SECONDS = 2;

    // Scheduler e executor
    private final ScheduledExecutorService scheduler;
    private final ExecutorService callbackExecutor;

    // Stato del servizio
    private final AtomicBoolean running;
    private final Map<Integer, LocalDateTime> ultimoMessaggioPerUtente;
    private final Map<Integer, Set<Integer>> interlocutoriPerUtente;

    // Listener per nuovi messaggi
    private final Map<Integer, List<Consumer<List<Messaggio>>>> messageListeners;
    private final Map<Integer, List<Consumer<utente>>> newConversationListeners;

    // DAO
    private final MessaggioDAO messaggioDAO;

    // Statistiche
    private final AtomicInteger totalePolls;
    private final AtomicInteger nuoviMessaggiTrovati;
    private final AtomicInteger erroriPolling;

    private MessagePollingService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MessagePollingService");
            t.setDaemon(true);
            return t;
        });

        this.callbackExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "MessageCallbackExecutor");
            t.setDaemon(true);
            return t;
        });

        this.running = new AtomicBoolean(false);
        this.ultimoMessaggioPerUtente = new ConcurrentHashMap<>();
        this.interlocutoriPerUtente = new ConcurrentHashMap<>();

        this.messageListeners = new ConcurrentHashMap<>();
        this.newConversationListeners = new ConcurrentHashMap<>();

        this.messaggioDAO = new MessaggioDAO();

        this.totalePolls = new AtomicInteger(0);
        this.nuoviMessaggiTrovati = new AtomicInteger(0);
        this.erroriPolling = new AtomicInteger(0);

        System.out.println("🔄 MessagePollingService inizializzato");
    }

    /**
     * Ottiene l'istanza singleton
     */
    public static MessagePollingService getInstance() {
        if (instance == null) {
            synchronized (MessagePollingService.class) {
                if (instance == null) {
                    instance = new MessagePollingService();
                }
            }
        }
        return instance;
    }

    /**
     * Avvia il servizio di polling per un utente specifico
     *
     * @param userId ID dell'utente
     */
    public void startPollingForUser(int userId) {
        if (running.get()) {
            System.out.println("⚠️  Polling già attivo per l'utente " + userId);
            return;
        }

        running.set(true);

        // Inizializza l'ultimo messaggio noto
        inizializzaUltimoMessaggio(userId);

        // Avvia il task periodico
        scheduler.scheduleWithFixedDelay(
            () -> pollForNewMessages(userId),
            INITIAL_DELAY_SECONDS,
            DEFAULT_POLLING_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );

        System.out.println("✅ Polling avviato per utente " + userId + " (intervallo: " +
                          DEFAULT_POLLING_INTERVAL_SECONDS + "s)");
    }

    /**
     * Avvia il polling con un intervallo personalizzato
     *
     * @param userId ID dell'utente
     * @param intervalSeconds Intervallo in secondi
     */
    public void startPollingForUser(int userId, int intervalSeconds) {
        if (running.get()) {
            System.out.println("⚠️  Polling già attivo");
            stopPolling();
        }

        running.set(true);
        inizializzaUltimoMessaggio(userId);

        scheduler.scheduleWithFixedDelay(
            () -> pollForNewMessages(userId),
            INITIAL_DELAY_SECONDS,
            intervalSeconds,
            TimeUnit.SECONDS
        );

        System.out.println("✅ Polling avviato per utente " + userId + " (intervallo: " +
                          intervalSeconds + "s)");
    }

    /**
     * Ferma il servizio di polling
     */
    public void stopPolling() {
        if (!running.get()) {
            return;
        }

        running.set(false);
        System.out.println("⏹️  Polling fermato");
    }

    /**
     * Inizializza l'ultimo messaggio noto per un utente
     */
    private void inizializzaUltimoMessaggio(int userId) {
        try {
            List<utente> interlocutori = messaggioDAO.getInterlocutoriUtenti(userId);

            for (utente interlocutore : interlocutori) {
                int interlocutoreId = interlocutore.getId();
                List<Messaggio> messaggi = messaggioDAO.getConversazione(userId, interlocutoreId);

                if (!messaggi.isEmpty()) {
                    Messaggio ultimo = messaggi.get(messaggi.size() - 1);
                    LocalDateTime oraUltimo = ultimo.getDataInvio();

                    // Salva il timestamp più recente per questa conversazione
                    ultimoMessaggioPerUtente.compute(userId, (k, v) -> {
                        if (v == null || oraUltimo.isAfter(v)) {
                            return oraUltimo;
                        }
                        return v;
                    });
                }
            }

            System.out.println("📍 Inizializzato ultimo messaggio per utente " + userId);

        } catch (Exception e) {
            System.err.println("⚠️  Errore inizializzazione ultimo messaggio: " + e.getMessage());
        }
    }

    /**
     * Esegue un ciclo di polling per nuovi messaggi
     */
    private void pollForNewMessages(int userId) {
        if (!running.get()) {
            return;
        }

        totalePolls.incrementAndGet();

        try {
            // Recupera gli interlocutori
            List<utente> interlocutori = messaggioDAO.getInterlocutoriUtenti(userId);

            // Controlla nuove conversazioni
            Set<Integer> vecchiInterlocutori = interlocutoriPerUtente.get(userId);
            if (vecchiInterlocutori == null) {
                vecchiInterlocutori = new HashSet<>();
                interlocutoriPerUtente.put(userId, vecchiInterlocutori);
            }

            for (utente interlocutore : interlocutori) {
                int interlocutoreId = interlocutore.getId();

                // Nuova conversazione?
                if (!vecchiInterlocutori.contains(interlocutoreId)) {
                    vecchiInterlocutori.add(interlocutoreId);
                    notificaNuovaConversazione(userId, interlocutore);
                }

                // Controlla nuovi messaggi in questa conversazione
                List<Messaggio> messaggi = messaggioDAO.getConversazione(userId, interlocutoreId);
                List<Messaggio> nuoviMessaggi = filtraNuoviMessaggi(userId, messaggi);

                if (!nuoviMessaggi.isEmpty()) {
                    nuoviMessaggiTrovati.addAndGet(nuoviMessaggi.size());
                    notificaNuoviMessaggi(userId, nuoviMessaggi);
                }
            }

        } catch (Exception e) {
            erroriPolling.incrementAndGet();
            System.err.println("❌ Errore durante polling: " + e.getMessage());
        }
    }

    /**
     * Filtra solo i messaggi più recenti rispetto all'ultimo polling
     */
    private List<Messaggio> filtraNuoviMessaggi(int userId, List<Messaggio> tuttiMessaggi) {
        LocalDateTime ultimoNoto = ultimoMessaggioPerUtente.get(userId);
        List<Messaggio> nuovi = new ArrayList<>();

        for (Messaggio msg : tuttiMessaggi) {
            if (ultimoNoto == null || msg.getDataInvio().isAfter(ultimoNoto)) {
                nuovi.add(msg);

                // Aggiorna l'ultimo messaggio noto
                ultimoMessaggioPerUtente.compute(userId, (k, v) -> {
                    if (v == null || msg.getDataInvio().isAfter(v)) {
                        return msg.getDataInvio();
                    }
                    return v;
                });
            }
        }

        return nuovi;
    }

    /**
     * Notifica i listener di nuovi messaggi
     */
    private void notificaNuoviMessaggi(int userId, List<Messaggio> messaggi) {
        List<Consumer<List<Messaggio>>> listeners = messageListeners.get(userId);

        if (listeners != null && !listeners.isEmpty()) {
            for (Consumer<List<Messaggio>> listener : listeners) {
                callbackExecutor.submit(() -> {
                    try {
                        listener.accept(messaggi);
                    } catch (Exception e) {
                        System.err.println("⚠️  Errore nel listener di nuovi messaggi: " + e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * Notifica i listener di nuove conversazioni
     */
    private void notificaNuovaConversazione(int userId, utente nuovoInterlocutore) {
        List<Consumer<utente>> listeners = newConversationListeners.get(userId);

        if (listeners != null && !listeners.isEmpty()) {
            for (Consumer<utente> listener : listeners) {
                callbackExecutor.submit(() -> {
                    try {
                        listener.accept(nuovoInterlocutore);
                    } catch (Exception e) {
                        System.err.println("⚠️  Errore nel listener di nuova conversazione: " + e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * Aggiunge un listener per nuovi messaggi
     *
     * @param userId ID dell'utente
     * @param listener Callback da chiamare quando arrivano nuovi messaggi
     */
    public void addMessageListener(int userId, Consumer<List<Messaggio>> listener) {
        messageListeners.computeIfAbsent(userId, k -> new ArrayList<>()).add(listener);
        System.out.println("📝 Listener messaggi aggiunto per utente " + userId);
    }

    /**
     * Rimuove un listener per nuovi messaggi
     */
    public void removeMessageListener(int userId, Consumer<List<Messaggio>> listener) {
        List<Consumer<List<Messaggio>>> listeners = messageListeners.get(userId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Aggiunge un listener per nuove conversazioni
     *
     * @param userId ID dell'utente
     * @param listener Callback da chiamare quando inizia una nuova conversazione
     */
    public void addNewConversationListener(int userId, Consumer<utente> listener) {
        newConversationListeners.computeIfAbsent(userId, k -> new ArrayList<>()).add(listener);
        System.out.println("📝 Listener nuove conversazioni aggiunto per utente " + userId);
    }

    /**
     * Rimuove un listener per nuove conversazioni
     */
    public void removeNewConversationListener(int userId, Consumer<utente> listener) {
        List<Consumer<utente>> listeners = newConversationListeners.get(userId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Rimuove tutti i listener per un utente
     */
    public void removeAllListenersForUser(int userId) {
        messageListeners.remove(userId);
        newConversationListeners.remove(userId);
    }

    /**
     * Forza un controllo immediato per nuovi messaggi
     */
    public void forcePollNow(int userId) {
        if (!running.get()) {
            System.out.println("⚠️  Impossibile forzare polling: servizio non attivo");
            return;
        }

        callbackExecutor.submit(() -> pollForNewMessages(userId));
        System.out.println("🔄 Polling forzato eseguito per utente " + userId);
    }

    /**
     * Resetta lo stato dell'ultimo messaggio per ricominciare da capo
     */
    public void resetUltimoMessaggio(int userId) {
        ultimoMessaggioPerUtente.remove(userId);
        System.out.println("♻️  Reset ultimo messaggio per utente " + userId);
    }

    /**
     * Chiude il servizio e rilascia le risorse
     */
    public void shutdown() {
        System.out.println("🛑 Chiusura MessagePollingService...");

        stopPolling();

        scheduler.shutdown();
        callbackExecutor.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!callbackExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                callbackExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            callbackExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        messageListeners.clear();
        newConversationListeners.clear();
        ultimoMessaggioPerUtente.clear();
        interlocutoriPerUtente.clear();

        System.out.println("✅ MessagePollingService chiuso");
    }

    // ========== METODI STATISTICHE ==========

    /**
     * Verifica se il polling è attivo
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Restituisce il numero totale di polling eseguiti
     */
    public int getTotalePolls() {
        return totalePolls.get();
    }

    /**
     * Restituisce il numero di nuovi messaggi trovati
     */
    public int getNuoviMessaggiTrovati() {
        return nuoviMessaggiTrovati.get();
    }

    /**
     * Restituisce il numero di errori durante il polling
     */
    public int getErroriPolling() {
        return erroriPolling.get();
    }

    /**
     * Resetta le statistiche
     */
    public void resetStatistiche() {
        totalePolls.set(0);
        nuoviMessaggiTrovati.set(0);
        erroriPolling.set(0);
        System.out.println("📊 Statistiche polling resettate");
    }

    /**
     * Restituisce un report delle statistiche
     */
    public String getReportStatistiche() {
        return String.format(
            "📊 Statistiche Polling - Eseguiti: %d, Nuovi messaggi: %d, Errori: %d, Attivo: %s",
            getTotalePolls(),
            getNuoviMessaggiTrovati(),
            getErroriPolling(),
            isRunning() ? "✅" : "❌"
        );
    }
}
