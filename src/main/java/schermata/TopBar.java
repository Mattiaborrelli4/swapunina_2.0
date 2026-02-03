package schermata;

import application.utils.LoggerUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.function.Consumer;

import application.DB.ConnessioneDB;
import application.DB.SessionManager;
import application.messagistica.ChatListDialog;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TopBar - Barra superiore dell'applicazione con funzionalità principali
 * Gestione: ricerca, messaggi, carrello, account e inserimento annunci
 */
public class TopBar {
    // Componenti UI
    private final HBox root = new HBox();
    private final TextField searchField = new TextField();
    private final Button searchButton = new Button();
    private final Button accountButton = new Button();
    private final Button cartButton = new Button();
    private final Button messagesButton = new Button();
    private final Button scambiButton = new Button();
    private final Button inserisciAnnuncioButton = new Button("Inserisci Annuncio");
    private final ImageView accountImageView = new ImageView();
    private String profileImageUrl;

    // Handler per le azioni
    private Consumer<String> searchHandler;
    private Runnable accountHandler;
    private Runnable cartHandler;
    private Runnable messagesHandler;
    private Runnable scambiHandler;
    private Runnable inserisciAnnuncioHandler;
    
    // Dati utente
    private String nomeUtente;
    private String emailUtente;
    
    
    // Costanti per configurazione
    private static final int PADDING = 10;
    private static final int SPACING = 16;
    private static final int SEARCH_FIELD_HEIGHT = 44;
    private static final int BUTTON_ICON_SIZE = 24;
    private static final int LOGO_SIZE = 36;
    private static final String LOGO_PATH = "/application/icons/logo.png";
    private static final String DEFAULT_SEARCH_PROMPT = "Cerca prodotti...";
    private static final String DEFAULT_ACCOUNT_ICON_URL = "https://cdn-icons-png.flaticon.com/512/1077/1077063.png";
    
    // ✅ Icone con Emoji Unicode (funzionano perfettamente, senza dipendenze)
    private static final String ACCOUNT_EMOJI = "👤";
    private static final String CART_EMOJI = "🛒";
    private static final String MESSAGES_EMOJI = "💬";
    private static final String SCAMBI_EMOJI = "🔄";
    private static final String ADD_EMOJI = "➕";

    /**
     * Costruttore principale della TopBar
     */
    public TopBar() {
        initializeComponents();
        setupLayout();
        applyStyling();
    }
    
    /**
     * Inizializza tutti i componenti della TopBar
     */
    private void initializeComponents() {
        configureRoot();
        setupSearchSection();
        setupActionButtons();
        setupTooltips();
    }
    
    /**
     * Configura il container principale
     */
    private void configureRoot() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(PADDING, 24, PADDING, 24));
        root.setSpacing(SPACING);
        root.getStyleClass().add("top-bar");
    }
    
    /**
     * Configura la sezione di ricerca
     */
    private void setupSearchSection() {
        configureSearchField();
        configureSearchButton();
    }
    
    /**
     * Configura i pulsanti di azione
     */
    private void setupActionButtons() {
        configureActionButtons();
    }
    
    /**
     * Configura il layout completo della TopBar
     */
    private void setupLayout() {
        Region logo = createLogo();
        Region leftSpacer = createSpacer();
        Region rightSpacer = createSpacer();
        HBox searchContainer = createSearchContainer();
        HBox actionsContainer = createActionsContainer();

        root.getChildren().addAll(logo, leftSpacer, searchContainer, rightSpacer, actionsContainer);
    }
    
    /**
     * Crea il logo dell'applicazione
     */
    private Region createLogo() {
        try {
            ImageView logoImage = loadLogoImage();
            if (logoImage != null) {
                return createLogoContainer(logoImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }
        
        return createFallbackLogo();
    }
    
    /**
     * Carica l'immagine del logo dal percorso specificato
     */
    private ImageView loadLogoImage() {
        InputStream logoStream = getClass().getResourceAsStream(LOGO_PATH);
        if (logoStream != null) {
            ImageView logoImage = new ImageView(new Image(logoStream));
            logoImage.setFitHeight(LOGO_SIZE);
            logoImage.setPreserveRatio(true);
            return logoImage;
        }
        return null;
    }
    
    /**
     * Crea un container per il logo
     */
    private Region createLogoContainer(ImageView logoImage) {
        StackPane container = new StackPane(logoImage);
        container.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        return container;
    }
    
    /**
     * Crea un logo di fallback con emoji
     */
    private Region createFallbackLogo() {
        Text emojiLogo = new Text("📦");
        emojiLogo.setFont(Font.font(24));
        
        HBox fallbackContainer = new HBox(emojiLogo);
        fallbackContainer.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        fallbackContainer.setAlignment(Pos.CENTER);
        return fallbackContainer;
    }
    
    /**
     * Crea uno spacer flessibile per il layout
     */
    private Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
    
    /**
     * Crea il container per la sezione di ricerca
     */
    private HBox createSearchContainer() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(700);
        HBox.setHgrow(container, Priority.ALWAYS);
        
        // Configura il campo di ricerca per espandersi
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        container.getChildren().addAll(searchField, searchButton);
        container.setSpacing(8);
        return container;
    }

    /**
     * Configura il campo di ricerca
     */
    private void configureSearchField() {
        searchField.setPromptText(DEFAULT_SEARCH_PROMPT);
        searchField.setOnAction(e -> handleSearch());
        searchField.setPrefHeight(SEARCH_FIELD_HEIGHT);
        searchField.setPrefWidth(400);
        
        // Listener per ricerca in tempo reale
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() && searchHandler != null) {
                searchHandler.accept("");
            }
        });
    }
    
    /**
     * Configura il pulsante di ricerca
     */
    private void configureSearchButton() {
        // Crea l'icona di ricerca usando SVGPath
        javafx.scene.shape.SVGPath searchIcon = new javafx.scene.shape.SVGPath();
        searchIcon.setContent("M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z");
        searchIcon.setFill(javafx.scene.paint.Color.BLACK);

        // Ridimensiona l'icona per adattarla al pulsante
        searchIcon.setScaleX(0.035);
        searchIcon.setScaleY(0.035);

        // Usa un Group per centrare automaticamente l'icona
        javafx.scene.Group iconGroup = new javafx.scene.Group(searchIcon);
        searchButton.setGraphic(iconGroup);
        searchButton.setText("");
        searchButton.setOnAction(e -> handleSearch());
        searchButton.setPrefHeight(SEARCH_FIELD_HEIGHT);
        searchButton.setPrefWidth(SEARCH_FIELD_HEIGHT);
    }

    /**
     * Crea il container per i pulsanti azione
     */
    private HBox createActionsContainer() {
        HBox container = new HBox();
        container.setSpacing(12);
        container.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().addAll(inserisciAnnuncioButton, scambiButton, messagesButton, cartButton, accountButton);

        return container;
    }
    
    /**
     * Configura tutti i pulsanti di azione
     */
    private void configureActionButtons() {
        configureEmojiButton(inserisciAnnuncioButton, ADD_EMOJI, e -> handleInserisciAnnuncio());
        configureEmojiButton(scambiButton, SCAMBI_EMOJI, e -> handleScambi());
        configureEmojiButton(messagesButton, MESSAGES_EMOJI, e -> handleMessages());
        configureEmojiButton(cartButton, CART_EMOJI, e -> handleCart());

        // Configura il pulsante account con gestione immagine profilo
        configureAccountButton();

        applyButtonStyles();
    }

    /**
     * Configura un pulsante con emoji
     */
    private void configureEmojiButton(Button button, String emoji, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        button.setText(emoji);
        button.setOnAction(handler);
        button.setStyle("-fx-background-radius: 8px; -fx-cursor: hand; -fx-font-size: 18px; -fx-text-fill: black;");
    }
    
    
    /**
     * Configura un pulsante con icona da URL
     */
    private void configureAccountButton() {
        accountButton.setText(ACCOUNT_EMOJI);
        accountButton.setOnAction(e -> handleAccount());
        accountButton.setPrefHeight(SEARCH_FIELD_HEIGHT);
        accountButton.setPrefWidth(SEARCH_FIELD_HEIGHT);
        accountButton.setStyle("-fx-background-radius: 8px; -fx-cursor: hand; -fx-font-size: 18px; -fx-text-fill: black;");

        // Rendi l'immagine circolare (sarà usata se c'è immagine profilo personalizzata)
        makeImageCircular(accountImageView);
    }
    
    
    /**
     * Applica gli stili ai pulsanti
     */
    private void applyButtonStyles() {
        String iconButtonStyle = "-fx-background-color: transparent; -fx-padding: 10px; " +
                               "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8px;";
        messagesButton.setStyle(messagesButton.getStyle() + iconButtonStyle);
        cartButton.setStyle(cartButton.getStyle() + iconButtonStyle);
        accountButton.setStyle(accountButton.getStyle() + iconButtonStyle);

        String primaryButtonStyle = "-fx-background-color: #3b82f6; " +
                                   "-fx-font-weight: bold; -fx-padding: 8px 16px;";
        inserisciAnnuncioButton.setStyle(inserisciAnnuncioButton.getStyle() + primaryButtonStyle);
    }
    /**
     * Configura i tooltip per accessibilità
     */
    private void setupTooltips() {
        setupButtonTooltip(searchButton, "Cerca prodotti");
        setupButtonTooltip(messagesButton, "Messaggi e chat");
        setupButtonTooltip(cartButton, "Carrello acquisti");
        setupButtonTooltip(accountButton, "Account utente");
        setupButtonTooltip(inserisciAnnuncioButton, "Inserisci nuovo annuncio");
    }
    
    /**
     * Configura un tooltip per un pulsante
     */
    private void setupButtonTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(javafx.util.Duration.millis(500));
        Tooltip.install(button, tooltip);
    }
    
    /**
     * Applica gli stili CSS ai componenti
     */
    private void applyStyling() {
        root.getStyleClass().add("top-bar");
        searchField.getStyleClass().add("search-field");
        searchButton.getStyleClass().add("search-button");
        accountButton.getStyleClass().add("action-button");
        cartButton.getStyleClass().add("action-button");
        messagesButton.getStyleClass().add("action-button");
        inserisciAnnuncioButton.getStyleClass().add("inserisci-annuncio-button");
        
        // Stili inline per garantire il funzionamento
        root.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        searchField.setStyle("-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 0 12px;");
        searchButton.setStyle("-fx-min-width: 44px; -fx-min-height: 44px; -fx-background-radius: 8px;");
    }

    /**
     * Gestisce l'azione di ricerca
     */
    private void handleSearch() {
        if (searchHandler != null) {
            String searchText = searchField.getText().trim();
            searchHandler.accept(searchText);
        }
    }
    
    /**
     * Gestisce l'azione dell'account
     */
    private void handleAccount() {
        if (accountHandler != null) {
            accountHandler.run();
        } else {
            showAlert("Account", "Funzionalità account non configurata");
        }
    }
    
    /**
     * Gestisce l'azione del carrello
     */
    private void handleCart() {
        if (cartHandler != null) {
            cartHandler.run();
        } else {
            showAlert("Carrello", "Funzionalità carrello non configurata");
        }
    }
    
    /**
     * Gestisce l'azione dei messaggi con controllo accesso
     */
    private void handleMessages() {
        try {
            if (!isUtenteLoggato()) {
                showAlert("Accesso richiesto", "Devi effettuare l'accesso per visualizzare le chat");
                return;
            }
            
            if (messagesHandler != null) {
                messagesHandler.run();
            } else {
                // Implementazione di default
                if (haChatDisponibili()) {
                    openChatList();
                } else {
                    showNoChatsMessage();
                }
            }
        } catch (Exception e) {
            showError("Impossibile aprire le chat: " + e.getMessage());
        }
    }
    
    /**
     * Gestisce l'azione di inserimento annuncio
     */
    private void handleInserisciAnnuncio() {
        if (inserisciAnnuncioHandler != null) {
            inserisciAnnuncioHandler.run();
        } else {
            showAlert("Inserisci Annuncio", "Funzionalità inserimento annuncio non configurata");
        }
    }

    /**
     * Gestisce l'azione degli scambi
     */
    private void handleScambi() {
        if (!isUtenteLoggato()) {
            showAlert("Accesso richiesto", "Devi effettuare l'accesso per gestire gli scambi");
            return;
        }

        try {
            if (scambiHandler != null) {
                scambiHandler.run();
            } else {
                // Implementazione di default - apre il dialog di gestione scambi
                schermata.button.GestioneScambiDialog dialog = new schermata.button.GestioneScambiDialog();
                dialog.showAndWait();
            }
        } catch (Exception e) {
            showError("Impossibile aprire gli scambi: " + e.getMessage());
        }
    }

    // ==================== METODI DI SUPPORTO ====================

    /**
     * Verifica se l'utente è attualmente loggato
     */
    private boolean isUtenteLoggato() {
        return SessionManager.getCurrentUserId() != -1;
    }

    /**
     * Verifica se ci sono chat disponibili per l'utente
     */
    private boolean haChatDisponibili() {
        try (Connection connection = ConnessioneDB.getConnessione()) {
            String query = "SELECT COUNT(*) FROM messaggio WHERE mittente_id = ? OR destinatario_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            int userId = SessionManager.getCurrentUserId();
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nel verificare le chat: " + e.getMessage());
            return false;
        }
    }

    /**
     * Apre la lista delle chat
     */
    private void openChatList() {
        try {
            ChatListDialog chatListDialog = new ChatListDialog();
            chatListDialog.show();
        } catch (Exception e) {
            showError("Errore nell'apertura delle chat: " + e.getMessage());
        }
    }
    
    /**
     * Mostra il messaggio per nessuna chat disponibile
     */
    private void showNoChatsMessage() {
        showAlert("Nessuna chat", 
            "Non hai ancora nessuna conversazione. Contatta un venditore per iniziare una chat!");
    }

    /**
     * Restituisce il nodo radice della TopBar
     */
    public HBox getRoot() {
        return root;
    }
    
    /**
     * Imposta l'handler per la ricerca
     */
    public void setOnSearch(Consumer<String> handler) {
        this.searchHandler = handler;
    }
    
    /**
     * Imposta l'handler per l'account
     */
    public void setOnAccount(Runnable handler) {
        this.accountHandler = handler;
    }
    
    /**
     * Imposta l'handler per il carrello
     */
    public void setOnCart(Runnable handler) {
        this.cartHandler = handler;
    }
    
    /**
     * Imposta l'handler per i messaggi
     */
    public void setOnMessages(Runnable handler) {
        this.messagesHandler = handler;
    }
    
    /**
     * Imposta l'handler per l'inserimento annuncio
     */
    public void setOnInserisciAnnuncio(Runnable handler) {
        this.inserisciAnnuncioHandler = handler;
    }
    
    /**
     * Pulisce il campo di ricerca
     */
    public void clearSearch() {
        searchField.clear();
    }

    /**
     * Imposta i dati dell'utente corrente
     */
    public void setDatiUtente(String nome, String email, String profileImageUrl) {
        this.nomeUtente = nome;
        this.emailUtente = email;
        this.profileImageUrl = profileImageUrl;
        updateUIWithUserData();
    }
    
    /**
     * Aggiorna l'UI con i dati utente
     */
    private void updateUIWithUserData() {
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            accountButton.setTooltip(new Tooltip("Account: " + nomeUtente));
        }
        
        // Carica l'immagine profilo se disponibile
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            setProfileImage(profileImageUrl);
        }
    }
    
    /**
     * Restituisce il nome dell'utente corrente
     */
    public String getNomeUtente() {
        return nomeUtente;
    }
    
    /**
     * Restituisce l'email dell'utente corrente
     */
    public String getEmailUtente() {
        return emailUtente;
    }

    /**
     * Mostra un alert informativo
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = createAlert(Alert.AlertType.INFORMATION, title, message);
            alert.showAndWait();
        });
    }
    
    /**
     * Mostra un alert di errore
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = createAlert(Alert.AlertType.ERROR, "Errore", message);
            alert.showAndWait();
        });
    }
    
    /**
     * Crea un alert con stili consistenti
     */
    private Alert createAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("custom-alert");
        
        return alert;
    }
    
    /**
     * Imposta il testo di ricerca programmaticamente
     */
    public void setSearchText(String text) {
        searchField.setText(text);
    }
    
    /**
     * Restituisce il testo attuale di ricerca
     */
    public String getSearchText() {
        return searchField.getText();
    }
    
    /**
     * Focus sul campo di ricerca
     */
    public void focusSearchField() {
        Platform.runLater(() -> searchField.requestFocus());
    }
    
    /**
     * Disabilita tutti i pulsanti (utile durante il loading)
     */
    public void setButtonsDisabled(boolean disabled) {
        searchButton.setDisable(disabled);
        accountButton.setDisable(disabled);
        cartButton.setDisable(disabled);
        messagesButton.setDisable(disabled);
        inserisciAnnuncioButton.setDisable(disabled);
    }
    
    /**
     * Aggiorna lo stato della TopBar in base all'autenticazione
     */
    public void refreshAuthState() {
        boolean isLoggedIn = isUtenteLoggato();
        
        // Disabilita alcune funzionalità se non loggato
        messagesButton.setDisable(!isLoggedIn);
        inserisciAnnuncioButton.setDisable(!isLoggedIn);
        
        // Aggiorna tooltip per utenti non loggati
        if (!isLoggedIn) {
            setupButtonTooltip(messagesButton, "Accedi per visualizzare i messaggi");
            setupButtonTooltip(inserisciAnnuncioButton, "Accedi per inserire annunci");
        } else {
            setupButtonTooltip(messagesButton, "Messaggi");
            setupButtonTooltip(inserisciAnnuncioButton, "Inserisci nuovo annuncio");
        }
        
        // Aggiorna stili visivi
        updateButtonStylesForAuthState(isLoggedIn);
    }
    
    /**
     * Aggiorna gli stili dei pulsanti in base allo stato di autenticazione
     */
    private void updateButtonStylesForAuthState(boolean isLoggedIn) {
        if (!isLoggedIn) {
            String disabledStyle = "-fx-opacity: 0.6;";
            messagesButton.setStyle(messagesButton.getStyle() + disabledStyle);
            inserisciAnnuncioButton.setStyle(inserisciAnnuncioButton.getStyle() + disabledStyle);
        } else {
            // Rimuovi stili di disabilitazione
            messagesButton.setStyle(messagesButton.getStyle().replace("-fx-opacity: 0.6;", ""));
            inserisciAnnuncioButton.setStyle(inserisciAnnuncioButton.getStyle().replace("-fx-opacity: 0.6;", ""));
        }
    }
    
    /**
     * Mostra un indicatore di caricamento sulla barra
     */
    public void showLoading(boolean loading) {
        setButtonsDisabled(loading);

        if (loading) {
            // Cambia l'icona con un indicatore di caricamento
            searchButton.setText("⏳");
            searchButton.setGraphic(null);
            searchField.setDisable(true);
        } else {
            // Ripristina l'icona SVG di ricerca
            restoreSearchIcon();
            searchField.setDisable(false);
        }
    }

    /**
     * Ripristina l'icona SVG di ricerca
     */
    private void restoreSearchIcon() {
        javafx.scene.shape.SVGPath searchIcon = new javafx.scene.shape.SVGPath();
        searchIcon.setContent("M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z");
        searchIcon.setFill(javafx.scene.paint.Color.BLACK);

        // Ridimensiona l'icona per adattarla al pulsante
        searchIcon.setScaleX(0.035);
        searchIcon.setScaleY(0.035);

        // Usa un Group per centrare automaticamente l'icona
        javafx.scene.Group iconGroup = new javafx.scene.Group(searchIcon);
        searchButton.setGraphic(iconGroup);
        searchButton.setText("");
    }
    
    
    private void loadAccountImage(String imageUrl) {
        try {
            LoggerUtil.debug("TopBar - Tentativo di caricamento immagine: " + imageUrl);

            // Se non c'è immagine, mantieni l'emoji
            if (imageUrl == null || imageUrl.isEmpty()) {
                LoggerUtil.debug("TopBar - Nessuna immagine profilo, uso emoji");
                accountButton.setText(ACCOUNT_EMOJI);
                accountButton.setGraphic(null);
                return;
            }

            Image image;

            // Verifica se è un URL Cloudinary o un percorso locale valido
            if (imageUrl.contains("cloudinary.com") || imageUrl.startsWith("http")) {
                // URL Cloudinary
                LoggerUtil.debug("TopBar - Caricamento da Cloudinary");
                image = new Image(imageUrl, BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, true, true, true);
            } else {
                // Percorso locale - converti in URL file
                LoggerUtil.debug("TopBar - Caricamento da file locale: " + imageUrl);
                File file = new File(imageUrl);
                if (file.exists()) {
                    image = new Image(file.toURI().toString(), BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, true, true, true);
                } else {
                    throw new Exception("File locale non trovato: " + imageUrl);
                }
            }

            if (!image.isError()) {
                accountImageView.setImage(image);
                accountButton.setGraphic(accountImageView);
                accountButton.setText(""); // Rimuovi emoji se c'è immagine
                LoggerUtil.success("TopBar - Immagine profilo caricata");
            } else {
                throw new Exception("Errore nel caricamento immagine");
            }

        } catch (Exception e) {
            LoggerUtil.error("TopBar - Error loading account image", e);
            // Fallback a emoji
            accountButton.setText(ACCOUNT_EMOJI);
            accountButton.setGraphic(null);
        }
    }
    

    // Nuovo metodo per rendere l'immagine circolare
    private void makeImageCircular(ImageView imageView) {
        imageView.setFitWidth(BUTTON_ICON_SIZE);
        imageView.setFitHeight(BUTTON_ICON_SIZE);
        imageView.setPreserveRatio(true);
        
        // Crea un clip circolare
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
            BUTTON_ICON_SIZE / 2.0, 
            BUTTON_ICON_SIZE / 2.0, 
            BUTTON_ICON_SIZE / 2.0
        );
        imageView.setClip(clip);
        
        // Aggiungi un bordo sottile
        imageView.setStyle("-fx-border-radius: " + (BUTTON_ICON_SIZE / 2) + "px; " +
                          "-fx-border-color: #e2e8f0; -fx-border-width: 1px;");
    }

    // Nuovo metodo per impostare l'immagine profilo
    public void setProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadAccountImage(imageUrl);
        } else {
            // ✅ FIX: Se non c'è immagine profilo, usa emoji
            accountButton.setText(ACCOUNT_EMOJI);
            accountButton.setGraphic(null);
        }
    }

    // Nuovo metodo per ottenere l'URL dell'immagine profilo
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void updateProfileImage(String newImageUrl) {
        setProfileImage(newImageUrl);
    }
}