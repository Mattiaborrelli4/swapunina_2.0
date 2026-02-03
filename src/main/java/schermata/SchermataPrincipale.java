package schermata;

import application.DB.AnnuncioDAO;
import application.Classe.Annuncio;
import application.Classe.utente;
import application.Enum.Categoria;
import application.Enum.Tipologia;
import application.messagistica.ChatListDialog;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.EventHandler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import application.DB.FilterManager;
import application.DB.SessionManager;

/**
 * SchermataPrincipale - Schermata principale OTTIMIZZATA con sistema di cache e refresh incrementale
 */
public class SchermataPrincipale extends BorderPane {

    // ========== COMPONENTI UI ==========
    private final TopBar topBar;
    private final CategoryMenu categoryMenu = new CategoryMenu();
    private final ProductGrid productGrid = new ProductGrid();
    private final FilterBar filterBar = new FilterBar();
    private Label statusLabel;
    private Pane productGridNode;
    private ProgressIndicator loadingIndicator;

    // ========== GESTORI DATI ==========
    private final AnnuncioDAO annuncioDAO = new AnnuncioDAO();
    private List<Annuncio> tuttiGliAnnunci = new ArrayList<>();
    private List<Annuncio> annunciFiltrati = new ArrayList<>();

    // ========== STATO APPLICAZIONE ==========
    private Stage palcoscenicoPrincipale;
    private Categoria categoriaSelezionata;
    private Tipologia tipologiaSelezionata;
    private String ordinamento = "recent";
    private String queryRicerca = "";

    // ========== SISTEMA DI CARICAMENTO INCREMENTALE ==========
    private final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("Background-Loader");
        return t;
    });
    
    private final Service<List<Annuncio>> annunciLoader = new Service<List<Annuncio>>() {
        @Override
        protected Task<List<Annuncio>> createTask() {
            return new Task<List<Annuncio>>() {
                @Override
                protected List<Annuncio> call() throws Exception {
                    updateMessage("🔄 Caricamento annunci in corso...");

                    // Simula un piccolo ritardo per mostrare l'indicatore di caricamento
                    Thread.sleep(500);

                    // ✅ FIX: Carica TUTTI gli annunci dal database senza deduplicazione locale
                    // Questo garantisce la sincronizzazione tra dispositivi
                    List<Annuncio> tuttiAnnunciDalDB = annuncioDAO.getAnnunciAttivi();

                    System.out.println("📡 Caricati " + tuttiAnnunciDalDB.size() + " annunci dal database");

                    return tuttiAnnunciDalDB;
                }
            };
        }
    };
    
    private Timer refreshTimer;
    private boolean isLoading = false;

    // ========== CACHE ANNUNCI ==========
    private final Map<Integer, Annuncio> cacheAnnunci = new ConcurrentHashMap<>();
    private final Set<Integer> annunciVisualizzati = ConcurrentHashMap.newKeySet();

    /**
     * Costruttore principale della schermata
     */
    public SchermataPrincipale(String matricolaUtente, Stage palcoscenicoPrincipale, TopBar topBar) {
        this.palcoscenicoPrincipale = palcoscenicoPrincipale;
        this.topBar = topBar;
        initializeUI();
        setupEventHandlers();
        setupAnnunciLoader();
        startIncrementalLoading();
    }

    // ========== INIZIALIZZAZIONE UI ==========

    /**
     * Inizializza l'interfaccia utente principale
     */
    private void initializeUI() {
        getStyleClass().add("main-layout");

        VBox contentBox = new VBox(10);
        contentBox.getStyleClass().add("content-area");
        
        // Barra di stato con refresh
        HBox statusBar = createStatusBar();
        
        // Crea la griglia di prodotti
        productGridNode = productGrid.creaProductGrid();
        
        contentBox.getChildren().addAll(
            createHeader(),
            statusBar,
            filterBar.getRoot(),
            productGridNode
        );

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-scroll");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setLeft(categoryMenu.getView());
        setCenter(scrollPane);
    }

    /**
     * Crea la barra di stato con indicatore di caricamento
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        statusLabel = new Label("Pronto");
        statusLabel.getStyleClass().add("status-label");
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(16, 16);
        loadingIndicator.getStyleClass().add("loading-indicator");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusBar.getChildren().addAll(loadingIndicator, statusLabel, spacer);
        return statusBar;
    }

    /**
     * Crea l'header della schermata principale
     */
    private Node createHeader() {
        VBox header = new VBox(8);
        header.getStyleClass().add("header-section");

        Text title = new Text("Marketplace Universitario");
        title.getStyleClass().add("main-title");

        Text subtitle = new Text("Scambia libri, dispositivi e materiale didattico");
        subtitle.getStyleClass().add("main-subtitle");

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    // ========== CONFIGURAZIONE CARICATORE ANNUNCI ==========

    /**
     * Configura il servizio di caricamento annunci
     */
    private void setupAnnunciLoader() {
        annunciLoader.setOnRunning(e -> {
            isLoading = true;
            loadingIndicator.setVisible(true);
            statusLabel.setText("Caricamento annunci...");
        });

        annunciLoader.setOnSucceeded(e -> {
            isLoading = false;
            loadingIndicator.setVisible(false);

            List<Annuncio> annunciDalDB = annunciLoader.getValue();
            if (annunciDalDB != null) {
                handleRefreshCompleto(annunciDalDB);
            } else {
                statusLabel.setText("Nessun annuncio disponibile");
            }
        });

        annunciLoader.setOnFailed(e -> {
            isLoading = false;
            loadingIndicator.setVisible(false);
            statusLabel.setText("Errore nel caricamento");
            showError("Errore nel caricamento degli annunci: " +
                     annunciLoader.getException().getMessage());
        });
    }

    // ========== GESTIONE EVENTI ==========

    /**
     * Configura tutti gli event handler della schermata
     */
    private void setupEventHandlers() {
        // Search handler con debounce
        topBar.setOnSearch(query -> {
            this.queryRicerca = query;
            scheduleFilterWithDebounce();
        });
        
        topBar.setOnMessages(this::handleMessages);
        
        // Category filter
        categoryMenu.setOnCategorySelected(categoria -> {
            this.categoriaSelezionata = categoria;
            scheduleFilterWithDebounce();
        });

        // Type filter
        filterBar.setOnTypeChange(tipologia -> {
            this.tipologiaSelezionata = tipologia;
            scheduleFilterWithDebounce();
        });

        // Sort handler
        filterBar.setOnSortChange(ordinamento -> {
            this.ordinamento = ordinamento;
            scheduleFilterWithDebounce();
        });

        // Product actions
        productGrid.setOnDetailsAction(this::showProductDetails);
        productGrid.setOnOfferAction(this::handleOffer);
        
        // Callback per l'aggiornamento degli annunci modificati
        productGrid.setOnAnnuncioModificato(this::handleAnnuncioModificato);

        // Insert ad handler
        topBar.setOnInserisciAnnuncio(this::handleInsertAd);
    }

    // ========== SISTEMA DI CARICAMENTO INCREMENTALE ==========

    /**
     * Avvia il caricamento incrementale iniziale
     */
    private void startIncrementalLoading() {
        // Carica i primi annunci
        loadInitialAnnunci();
        
        // Avvia il timer per aggiornamenti periodici
        startAutoRefreshTimer();
    }

    /**
     * Carica gli annunci iniziali
     */
    private void loadInitialAnnunci() {
        backgroundExecutor.submit(() -> {
            try {
                // ✅ FIX: Resetta le cache prima del caricamento
                cacheAnnunci.clear();
                annunciVisualizzati.clear();

                List<Annuncio> annunciIniziali = annuncioDAO.getAnnunciAttivi();

                Platform.runLater(() -> {
                    // Sostituisci completamente le liste
                    tuttiGliAnnunci = new ArrayList<>(annunciIniziali);
                    annunciFiltrati = new ArrayList<>(annunciIniziali);

                    // Aggiorna cache
                    for (Annuncio annuncio : annunciIniziali) {
                        cacheAnnunci.put(annuncio.getId(), annuncio);
                    }

                    // Mostra nella griglia
                    productGrid.aggiornaAnnunci(annunciIniziali);

                    // Aggiorna stato
                    statusLabel.setText(annunciIniziali.size() + " annunci disponibili");
                    filterBar.updateCount(annunciIniziali.size());

                    System.out.println("🚀 Caricamento iniziale completato: " + annunciIniziali.size() + " annunci");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Errore nel caricamento iniziale");
                    showError("Errore nel caricamento iniziale: " + e.getMessage());
                });
            }
        });
    }

    /**
     * ✅ FIX: Gestisce il refresh completo degli annunci dal database
     * Questo metodo sostituisce completamente le liste locali garantendo la sincronizzazione
     */
    private void handleRefreshCompleto(List<Annuncio> annunciDalDB) {
        Platform.runLater(() -> {
            // Svuota e ripopola la cache
            cacheAnnunci.clear();
            for (Annuncio annuncio : annunciDalDB) {
                cacheAnnunci.put(annuncio.getId(), annuncio);
            }

            // ✅ SOSTITUISCI completamente le liste (non aggiungere)
            tuttiGliAnnunci = new ArrayList<>(annunciDalDB);

            // Applica i filtri correnti
            List<Annuncio> annunciFiltratiNuovi = FilterManager.applicaFiltri(
                tuttiGliAnnunci, categoriaSelezionata, tipologiaSelezionata, queryRicerca, ordinamento
            );

            annunciFiltrati = annunciFiltratiNuovi;

            // Aggiorna la griglia
            if (!annunciFiltrati.isEmpty()) {
                productGrid.aggiornaAnnunci(annunciFiltrati);
                statusLabel.setText(annunciFiltrati.size() + " annunci trovati su " + tuttiGliAnnunci.size() + " totali");
            } else {
                productGrid.mostraStatoVuoto();
                statusLabel.setText("Nessun annuncio corrisponde ai filtri");
            }

            filterBar.updateCount(annunciFiltrati.size());

            System.out.println("✅ Refresh completato: " + tuttiGliAnnunci.size() + " annunci totali, " +
                             annunciFiltrati.size() + " filtrati");
        });
    }

    /**
     * Avvia il timer per il refresh automatico
     */
    private void startAutoRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!isLoading && !annunciLoader.isRunning()) {
                        // ✅ FIX: Resetta la cache per forzare il reload completo dal database
                        System.out.println("⏰ Refresh automatico: reset cache e reload...");
                        cacheAnnunci.clear();
                        annunciVisualizzati.clear();
                        annunciLoader.restart();
                    }
                });
            }
        }, 60000, 60000); // Ogni 60 secondi
    }

    // ========== FILTRI E RICERCA ==========

    private void scheduleFilterWithDebounce() {
        // Annulla eventuali task in corso
        if (backgroundExecutor != null) {
            backgroundExecutor.submit(() -> {
                try {
                    Thread.sleep(300); // Debounce di 300ms
                    
                    Platform.runLater(() -> {
                        applyFilters();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    /**
     * Applica i filtri correnti agli annunci
     */
    private void applyFilters() {
        if (tuttiGliAnnunci.isEmpty()) return;
        
        backgroundExecutor.submit(() -> {
            List<Annuncio> risultati = FilterManager.applicaFiltri(
                tuttiGliAnnunci, categoriaSelezionata, 
                tipologiaSelezionata, queryRicerca, ordinamento
            );
            
            Platform.runLater(() -> {
                annunciFiltrati = risultati;
                
                if (risultati.isEmpty()) {
                    productGrid.mostraStatoVuoto();
                    statusLabel.setText("Nessun annuncio corrisponde ai filtri");
                } else {
                    productGrid.aggiornaAnnunci(risultati);
                    statusLabel.setText(risultati.size() + " annunci trovati");
                }
                
                filterBar.updateCount(risultati.size());
            });
        });
    }

    // ========== NOTIFICHE E FEEDBACK ==========

    /**
     * Mostra una notifica per nuovi annunci
     */
    private void showNewAnnunciNotification(int nuovi) {
        Platform.runLater(() -> {
            Label toast = new Label("📢 " + nuovi + " nuovo" + (nuovi > 1 ? "i" : "") + " annunci" + (nuovi > 1 ? "o" : ""));
            toast.getStyleClass().add("toast-notification");
            
            StackPane rootPane = (StackPane) getScene().getRoot();
            rootPane.getChildren().add(toast);
            
            // Posiziona in alto
            toast.setTranslateY(50);
            
            // Animazione fade out
            FadeTransition fade = new FadeTransition(Duration.seconds(3), toast);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> rootPane.getChildren().remove(toast));
            fade.play();
        });
    }

    // ========== HANDLER FUNCTIONS ==========

    private void handleAnnuncioModificato(Annuncio annuncioModificato) {
        // Aggiorna nella cache
        cacheAnnunci.put(annuncioModificato.getId(), annuncioModificato);
        
        // Aggiorna nella lista
        for (int i = 0; i < tuttiGliAnnunci.size(); i++) {
            if (tuttiGliAnnunci.get(i).getId() == annuncioModificato.getId()) {
                tuttiGliAnnunci.set(i, annuncioModificato);
                break;
            }
        }
        
        // Aggiorna la card specifica
        productGrid.aggiornaCardAnnuncio(annuncioModificato);
        
        System.out.println("🔄 Annuncio " + annuncioModificato.getId() + " aggiornato");
    }

    private void handleInsertAd() {
        try {
            utente currentUser = SessionManager.getCurrentUser();
            if (currentUser == null || currentUser.getId() <= 0) {
                showError("Utente non trovato! Effettua il login.");
                return;
            }

            schermata.button.InserisciAnnuncioDialog dialog =
                new schermata.button.InserisciAnnuncioDialog(currentUser.getId());
            Optional<Annuncio> result = dialog.showAndWait();

            if (result.isPresent()) {
                Annuncio nuovoAnnuncio = result.get();

                backgroundExecutor.submit(() -> {
                    try {
                        int resultId = annuncioDAO.inserisciAnnuncioComplessivo(nuovoAnnuncio, currentUser.getId());

                        Platform.runLater(() -> {
                            if (resultId > 0) {
                                // ✅ FIX: Refresh completo per sincronizzare con il database
                                showSuccess("Annuncio pubblicato con successo!");
                                System.out.println("✅ Nuovo annuncio inserito con ID: " + resultId);

                                // Forza refresh completo dal database
                                cacheAnnunci.clear();
                                annunciVisualizzati.clear();
                                annunciLoader.restart();
                            } else {
                                showError("Errore durante la pubblicazione dell'annuncio");
                            }
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showError("Errore: " + e.getMessage());
                        });
                    }
                });
            }
        } catch (Exception e) {
            showError("Errore nel recupero dell'utente: " + e.getMessage());
        }
    }

    private void showProductDetails(Annuncio annuncio) {
        try {
            DettagliProdottoView dettagliView = new DettagliProdottoView(annuncio);
            dettagliView.mostra();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(annuncio.getTitolo() != null ? annuncio.getTitolo() : "Dettagli Prodotto");
            alert.setHeaderText("Prezzo: " + annuncio.getPrezzoFormattato());
            
            String descrizione = annuncio.getDescrizione() != null ? 
                annuncio.getDescrizione() : "Nessuna descrizione disponibile";
                
            alert.setContentText(
                "Categoria: " + (annuncio.getOggetto() != null && annuncio.getOggetto().getCategoria() != null ? 
                    annuncio.getOggetto().getCategoria().toString() : "Non specificata") + "\n" +
                "Tipologia: " + annuncio.getTipologia().toString() + "\n\n" +
                "Descrizione:\n" + descrizione
            );
            alert.showAndWait();
        }
    }

    private void handleOffer(Annuncio annuncio) {
        showSuccess("Hai inviato un'offerta per: " + 
                   (annuncio.getTitolo() != null ? annuncio.getTitolo() : 
                    annuncio.getOggetto() != null ? annuncio.getOggetto().getNome() : "Annuncio"));
    }

    private void handleMessages() {
        try {
            if (SessionManager.getCurrentUserId() == -1) {
                showAlert("Accesso richiesto", "Devi effettuare l'accesso per visualizzare le chat");
                return;
            }
            
            ChatListDialog chatListDialog = new ChatListDialog();
            chatListDialog.show();
        } catch (Exception e) {
            showError("Impossibile aprire le chat: " + e.getMessage());
        }
    }

    // ========== UTILITY UI FUNCTIONS ==========

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successo");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStyleClass().add("custom-alert");
            alert.showAndWait();
        });
    }

    // ========== PUBLIC API ==========

    /**
     * Forza il refresh degli annunci dal database
     */
    public void refresh() {
        if (!isLoading && !annunciLoader.isRunning()) {
            annunciLoader.restart();
        }
    }

    public Categoria getCategoria() {
        return categoriaSelezionata;
    }

    public int getTotalAnnunci() {
        return tuttiGliAnnunci.size();
    }

    public int getAnnunciFiltrati() {
        return annunciFiltrati.size();
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
        
        if (annunciLoader != null && annunciLoader.isRunning()) {
            annunciLoader.cancel();
        }
        
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdownNow();
        }
        
        System.out.println("🧹 Pulizia risorse SchermataPrincipale completata");
    }

    private void gestisciAggiornamentoAnnuncio(int annuncioId, String nuovoStato) {
        Platform.runLater(() -> {
            if ("VENDUTO".equals(nuovoStato)) {
                rimuoviAnnuncioDalleListe(annuncioId);
                showSuccess("Annuncio venduto con successo!");
            }
        });
    }

    private void rimuoviAnnuncioDalleListe(int annuncioId) {
        // Rimuovi dalla cache
        cacheAnnunci.remove(annuncioId);
        
        // Rimuovi dalle liste
        tuttiGliAnnunci.removeIf(annuncio -> annuncio.getId() == annuncioId);
        annunciFiltrati.removeIf(annuncio -> annuncio.getId() == annuncioId);
        
        // Rimuovi dalla vista
        productGrid.rimuoviAnnuncio(annuncioId);
        
        // Aggiorna contatori
        statusLabel.setText(tuttiGliAnnunci.size() + " annunci disponibili");
        filterBar.updateCount(annunciFiltrati.size());
    }

    public void notificaAnnuncioVenduto(int annuncioId) {
        gestisciAggiornamentoAnnuncio(annuncioId, "VENDUTO");
    }
}