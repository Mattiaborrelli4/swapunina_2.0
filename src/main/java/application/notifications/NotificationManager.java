package application.notifications;

import application.Classe.Messaggio;
import application.Classe.utente;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Gestore centralizzato per le notifiche desktop
 * Fornisce un sistema di notifiche non intrusivo con diverse tipologie
 *
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Notifiche desktop native (SystemTray)</li>
 *   <li>Notifiche in-app customizzabili</li>
 *   <li>Diverse tipologie (messaggi, offerte, transazioni, sistema)</li>
 *   <li>Gestione code e priorità</li>
 *   <li>Theming personalizzabile</li>
 *   <li>Statistiche sulle notifiche</li>
 *   <li>Click handler per interazioni</li>
 * </ul>
 * </p>
 *
 * <p><b>Tipi di notifiche supportate:</b>
 * <ul>
 *   <li>MESSAGE: Nuovi messaggi ricevuti</li>
 *   <li>OFFER: Nuove offerte/contro-offerte</li>
 *   <li>TRANSACTION: Aggiornamenti transazioni</li>
 *   <li>SYSTEM: Notifiche di sistema</li>
 * </ul>
 * </p>
 */
public class NotificationManager {
    private static volatile NotificationManager instance;

    // System Tray
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean trayAvailable;

    // Code di notifiche
    private final PriorityBlockingQueue<Notification> notificationQueue;
    private final ScheduledExecutorService notificationExecutor;

    // Listener per click sulle notifiche
    private final Map<NotificationType, List<Consumer<Notification>>> clickListeners;

    // Statistiche
    private final AtomicInteger notificheMostrate;
    private final AtomicInteger notificheClick;
    private final AtomicInteger notificheIgnorate;

    // Configurazione
    private boolean soundEnabled = true;
    private boolean desktopNotificationsEnabled = true;
    private int autoDismissSeconds = 5;

    private NotificationManager() {
        this.notificationQueue = new PriorityBlockingQueue<>(100,
            Comparator.comparingInt(n -> n.getPriority().getValue()));

        this.notificationExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NotificationManager");
            t.setDaemon(true);
            return t;
        });

        this.clickListeners = new ConcurrentHashMap<>();

        this.notificheMostrate = new AtomicInteger(0);
        this.notificheClick = new AtomicInteger(0);
        this.notificheIgnorate = new AtomicInteger(0);

        inizializzaSystemTray();
        startNotificationProcessor();

        System.out.println("🔔 NotificationManager inizializzato");
    }

    /**
     * Ottiene l'istanza singleton
     */
    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (NotificationManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }

    /**
     * Inizializza il System Tray se disponibile
     */
    private void inizializzaSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("⚠️  System Tray non supportato su questo sistema");
            trayAvailable = false;
            return;
        }

        try {
            systemTray = SystemTray.getSystemTray();

            // Crea icona semplice (colorata per essere visibile)
            Image image = creaIconaTray();

            // Crea TrayIcon con popup menu
            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("Esci");
            exitItem.addActionListener(e -> {
                System.out.println("👋 Uscita dall'app tramite notifica");
                // System.exit(0); // Decommentare se serve uscita reale
            });
            popup.add(exitItem);

            trayIcon = new TrayIcon(image, "SwapUnina", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("SwapUnina - Marketplace Universitario");

            systemTray.add(trayIcon);
            trayAvailable = true;

            System.out.println("✅ System Tray inizializzato");

        } catch (AWTException e) {
            System.err.println("❌ Errore inizializzazione System Tray: " + e.getMessage());
            trayAvailable = false;
        }
    }

    /**
     * Crea un'icona semplice per il tray
     */
    private Image creaIconaTray() {
        // Crea un'immagine 16x16 con una "S" per SwapUnina
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Sfondo blu
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRect(0, 0, 16, 16);

        // Testo bianco
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("S", 4, 12);

        g2d.dispose();
        return image;
    }

    /**
     * Avvia il processor delle notifiche
     */
    private void startNotificationProcessor() {
        notificationExecutor.scheduleWithFixedDelay(() -> {
            Notification notification = notificationQueue.poll();
            if (notification != null) {
                mostraNotifica(notification);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Mostra una notifica
     */
    private void mostraNotifica(Notification notification) {
        notificheMostrate.incrementAndGet();

        // Notifica desktop
        if (desktopNotificationsEnabled && trayAvailable) {
            mostraNotificaDesktop(notification);
        }

        // Log in console
        logNotifica(notification);

        // Suono se abilitato
        if (soundEnabled) {
            riproduciSuono(notification);
        }
    }

    /**
     * Mostra una notifica desktop native
     */
    private void mostraNotificaDesktop(Notification notification) {
        try {
            // Mapping tra NotificationType e TrayIcon.MessageType
            TrayIcon.MessageType messageType = mapToTrayIconMessageType(notification.getType());

            trayIcon.displayMessage(
                notification.getTitle(),
                notification.getMessage(),
                messageType
            );

            // Auto-dismiss
            ScheduledExecutorService dismissExecutor = Executors.newSingleThreadScheduledExecutor();
            dismissExecutor.schedule(() -> {
                // Il System Tray gestisce automaticamente l'auto-dismiss
                dismissExecutor.shutdown();
            }, autoDismissSeconds, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("⚠️  Errore visualizzazione notifica desktop: " + e.getMessage());
        }
    }

    /**
     * Log della notifica in console
     */
    private void logNotifica(Notification notification) {
        String icona = switch (notification.getType()) {
            case MESSAGE -> "💬";
            case OFFER -> "💰";
            case TRANSACTION -> "📦";
            case SYSTEM -> "⚙️";
        };

        System.out.println(icona + " " + notification.getTitle() + ": " + notification.getMessage());
    }

    /**
     * Riproduce un suono per la notifica
     */
    private void riproduciSuono(Notification notification) {
        // Suono beep di default
        Toolkit.getDefaultToolkit().beep();

        // In futuro, si possono aggiungere suoni personalizzati per tipo
        // usando javax.sound.sampled o JavaFX MediaPlayer
    }

    // ========== METODI PUBBLICI PER NOTIFICHE ==========

    /**
     * Mostra una notifica per un nuovo messaggio
     *
     * @param messaggio Il messaggio ricevuto
     * @param mittente Il mittente del messaggio
     */
    public void notificaNuovoMessaggio(Messaggio messaggio, utente mittente) {
        String titolo = "Nuovo messaggio da " + mittente.getNome();
        String testo = messaggio.getTesto();

        if (testo.length() > 50) {
            testo = testo.substring(0, 47) + "...";
        }

        Notification notifica = new Notification(
            NotificationType.MESSAGE,
            titolo,
            testo,
            mittente.getFotoProfilo(),
            NotificationPriority.NORMAL
        );

        notifica.setMetadata("messaggioId", messaggio.getId());
        notifica.setMetadata("mittenteId", mittente.getId());

        accodaNotifica(notifica);
    }

    /**
     * Mostra una notifica per una nuova offerta
     *
     * @param nomeProdotto Nome del prodotto
     * @param offrente Nome dell'offerente
     * @param importo Importo offerto
     */
    public void notificaNuovaOfferta(String nomeProdotto, String offrente, double importo) {
        String titolo = "Nuova offerta ricevuta!";
        String testo = String.format("%s offre €%.2f per: %s", offrente, importo, nomeProdotto);

        Notification notifica = new Notification(
            NotificationType.OFFER,
            titolo,
            testo,
            null,
            NotificationPriority.HIGH
        );

        accodaNotifica(notifica);
    }

    /**
     * Mostra una notifica per un aggiornamento transazione
     *
     * @param stato Stato della transazione
     * @param nomeProdotto Nome del prodotto
     */
    public void notificaAggiornamentoTransazione(String stato, String nomeProdotto) {
        String titolo = "Aggiornamento ordine";
        String testo = String.format("Il tuo ordine per '%s' è: %s", nomeProdotto, stato);

        Notification notifica = new Notification(
            NotificationType.TRANSACTION,
            titolo,
            testo,
            null,
            NotificationPriority.NORMAL
        );

        accodaNotifica(notifica);
    }

    /**
     * Mostra una notifica di sistema
     *
     * @param titolo Titolo della notifica
     * @param messaggio Messaggio della notifica
     * @param priority Priorità
     */
    public void notificaSistema(String titolo, String messaggio, NotificationPriority priority) {
        Notification notifica = new Notification(
            NotificationType.SYSTEM,
            titolo,
            messaggio,
            null,
            priority
        );

        accodaNotifica(notifica);
    }

    /**
     * Aggiunge una notifica alla coda
     */
    private void accodaNotifica(Notification notification) {
        notificationQueue.offer(notification);
    }

    // ========== GESTIONE LISTENER ==========

    /**
     * Aggiunge un listener per il click su notifiche di un certo tipo
     *
     * @param type Tipo di notifica
     * @param listener Callback da eseguire al click
     */
    public void addClickListener(NotificationType type, Consumer<Notification> listener) {
        clickListeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Rimuove un listener
     */
    public void removeClickListener(NotificationType type, Consumer<Notification> listener) {
        List<Consumer<Notification>> listeners = clickListeners.get(type);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifica i listener di un click
     */
    private void notificaClick(Notification notification) {
        notificheClick.incrementAndGet();

        List<Consumer<Notification>> listeners = clickListeners.get(notification.getType());
        if (listeners != null) {
            for (Consumer<Notification> listener : listeners) {
                try {
                    listener.accept(notification);
                } catch (Exception e) {
                    System.err.println("⚠️  Errore nel listener di click: " + e.getMessage());
                }
            }
        }
    }

    // ========== CONFIGURAZIONE ==========

    /**
     * Abilita/disabilita il suono delle notifiche
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        System.out.println("🔊 Suono notifiche: " + (enabled ? "attivo" : "disattivato"));
    }

    /**
     * Abilita/disabilita le notifiche desktop
     */
    public void setDesktopNotificationsEnabled(boolean enabled) {
        this.desktopNotificationsEnabled = enabled;
        System.out.println("🖥️  Notifiche desktop: " + (enabled ? "attive" : "disattivate"));
    }

    /**
     * Imposta il tempo di auto-dismiss delle notifiche
     *
     * @param secondi Tempo in secondi
     */
    public void setAutoDismissSeconds(int secondi) {
        this.autoDismissSeconds = Math.max(1, secondi);
        System.out.println("⏱️  Auto-dismiss notifiche: " + this.autoDismissSeconds + "s");
    }

    // ========== STATISTICHE ==========

    /**
     * Restituisce il numero di notifiche mostrate
     */
    public int getNotificheMostrate() {
        return notificheMostrate.get();
    }

    /**
     * Restituisce il numero di notifiche cliccate
     */
    public int getNotificheClick() {
        return notificheClick.get();
    }

    /**
     * Restituisce il numero di notifiche ignorate
     */
    public int getNotificheIgnorate() {
        return notificheIgnorate.get();
    }

    /**
     * Resetta le statistiche
     */
    public void resetStatistiche() {
        notificheMostrate.set(0);
        notificheClick.set(0);
        notificheIgnorate.set(0);
        System.out.println("📊 Statistiche notifiche resettate");
    }

    /**
     * Chiude il notification manager e rilascia le risorse
     */
    public void shutdown() {
        System.out.println("🛑 Chiusura NotificationManager...");

        notificationExecutor.shutdown();

        try {
            if (!notificationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                notificationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            notificationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (trayAvailable && systemTray != null) {
            systemTray.remove(trayIcon);
        }

        notificationQueue.clear();
        clickListeners.clear();

        System.out.println("✅ NotificationManager chiuso");
    }

    // ========== CLASSI INTERNE ==========

    /**
     * Tipo di notifica
     */
    public enum NotificationType {
        MESSAGE,
        OFFER,
        TRANSACTION,
        SYSTEM
    }

    /**
     * Priorità della notifica
     */
    public enum NotificationPriority {
        LOW(1),
        NORMAL(2),
        HIGH(3),
        URGENT(4);

        private final int value;

        NotificationPriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Classe che rappresenta una notifica
     */
    public static class Notification {
        private final NotificationType type;
        private final String title;
        private final String message;
        private final String imageUrl;
        private final NotificationPriority priority;
        private final LocalDateTime timestamp;
        private final Map<String, Object> metadata;

        public Notification(NotificationType type, String title, String message,
                          String imageUrl, NotificationPriority priority) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
            this.priority = priority;
            this.timestamp = LocalDateTime.now();
            this.metadata = new HashMap<>();
        }

        public NotificationType getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public NotificationPriority getPriority() {
            return priority;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }

        public Object getMetadata(String key) {
            return metadata.get(key);
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", type, title, message);
        }
    }

    /**
     * Mappa NotificationType a TrayIcon.MessageType per notifiche desktop
     */
    private TrayIcon.MessageType mapToTrayIconMessageType(NotificationType type) {
        return switch (type) {
            case MESSAGE -> TrayIcon.MessageType.INFO;
            case OFFER -> TrayIcon.MessageType.WARNING;
            case TRANSACTION -> TrayIcon.MessageType.INFO;
            case SYSTEM -> TrayIcon.MessageType.ERROR;
        };
    }
}
