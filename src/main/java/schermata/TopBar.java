package schermata;

import application.utils.IconProvider;
import application.utils.AnimationConstants;
import application.utils.LoggerUtil;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.util.Duration;

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

    // ✅ Royal Purple Icon System (Lucide-style SVG icons)
    private static final String ICON_ACCOUNT = "user";
    private static final String ICON_CART = "shopping-cart";
    private static final String ICON_MESSAGES = "message-circle";
    private static final String ICON_SCAMBI = "repeat";
    private static final String ICON_ADD = "plus-circle";
    private static final String ICON_SEARCH = "search";

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
     * Crea un logo di fallback con icona SVG premium realistica
     * Logo Marketplace MB: Design moderno con lettere MB
     */
    private Region createFallbackLogo() {
        // Crea un gruppo per contenere più elementi grafici
        Group logoGroup = new Group();

        // 1. Cerchio sfondo con gradiente
        Circle bgCircle = new Circle(18);
        bgCircle.setFill(Color.rgb(139, 92, 246)); // Royal Purple
        bgCircle.setStroke(Color.rgb(168, 85, 247));
        bgCircle.setStrokeWidth(2);

        // 2. Lettere "MB" stilizzate (Marketplace)
        Text mbLetters = new Text("MB");
        mbLetters.setFill(Color.rgb(255, 255, 255));
        mbLetters.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-font-family: 'Arial Black', sans-serif;");
        // Centra le lettere con micro-regolazione
        mbLetters.setX(-mbLetters.getLayoutBounds().getWidth() / 2 - 8);
        mbLetters.setY(7);

        // Assembla il gruppo (solo cerchio + MB, niente frecce)
        logoGroup.getChildren().addAll(bgCircle, mbLetters);

        StackPane fallbackContainer = new StackPane(logoGroup);
        fallbackContainer.setPrefSize(48, 48);
        fallbackContainer.setAlignment(Pos.CENTER);

        // Effetto glow premium
        fallbackContainer.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.6), 15px, 0, 0, 0px);"
        );

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
     * Configura il pulsante di ricerca con icona SVG premium
     */
    private void configureSearchButton() {
        // Usa IconProvider per ottenere icona Lucide-style
        SVGPath searchIcon = IconProvider.getIcon(ICON_SEARCH);
        IconProvider.scaleIcon(searchIcon, IconProvider.IconSize.LG);
        // Usa stroke nero per bordi visibili + fill viola trasparente
        searchIcon.setFill(Color.rgb(139, 92, 246, 0.3));
        searchIcon.setStroke(Color.rgb(0, 0, 0));
        searchIcon.setStrokeWidth(2.5);

        searchButton.setGraphic(searchIcon);
        searchButton.setText("");
        searchButton.setOnAction(e -> handleSearch());
        searchButton.setPrefHeight(SEARCH_FIELD_HEIGHT);
        searchButton.setPrefWidth(SEARCH_FIELD_HEIGHT);

        // Aggiungi animazione hover premium
        setupIconButtonAnimation(searchButton, searchIcon);

        // Imposta CSS class
        searchButton.getStyleClass().addAll("search-button", "icon-button");
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
     * Configura tutti i pulsanti di azione con icone SVG premium
     */
    private void configureActionButtons() {
        configureEmojiButton(inserisciAnnuncioButton, ICON_ADD, e -> handleInserisciAnnuncio());
        configureEmojiButton(scambiButton, ICON_SCAMBI, e -> handleScambi());
        configureEmojiButton(messagesButton, ICON_MESSAGES, e -> handleMessages());
        configureEmojiButton(cartButton, ICON_CART, e -> handleCart());

        // Configura il pulsante account con icona SVG premium
        configureAccountButton();

        applyButtonStyles();
    }

    /**
     * Configura un pulsante con icona SVG premium (Luice-style)
     * Sostituisce il vecchio sistema emoji
     */
    private void configureEmojiButton(Button button, String iconName, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        // Ottieni icona SVG da IconProvider
        SVGPath icon = IconProvider.getIcon(iconName);
        IconProvider.scaleIcon(icon, IconProvider.IconSize.LG);

        // Configura icone con stroke nero per migliore visibilità
        if ("plus-circle".equals(iconName) || "repeat".equals(iconName)) {
            // Per icone complesse, usa stroke nero + fill viola
            icon.setFill(Color.rgb(139, 92, 246, 0.4));
            icon.setStroke(Color.rgb(0, 0, 0));
            icon.setStrokeWidth(2.0);
        } else {
            icon.setFill(Color.rgb(148, 163, 184)); // text-tertiary color
        }

        // Configura bottone
        button.setGraphic(icon);
        button.setText("");
        button.setOnAction(handler);

        // Applica stile premium
        button.getStyleClass().add("action-button");

        // Aggiungi animazione hover premium
        setupIconButtonAnimation(button, icon);
    }

    /**
     * Configura animazione hover premium per icone
     * Transizione smooth da grigio a purple con spring physics
     */
    private void setupIconButtonAnimation(Button button, SVGPath icon) {
        button.hoverProperty().addListener((obs, wasHovering, isNowHovering) -> {
            // Colore target: purple-primary quando hover, text-tertiary quando non
            Color targetColor = isNowHovering ?
                Color.rgb(139, 92, 246) :  // purple-primary
                Color.rgb(148, 163, 184);   // text-tertiary

            // Animazione smooth 150ms con EASE_OUT_QUINT
            Timeline colorAnim = new Timeline(
                new KeyFrame(Duration.millis(AnimationConstants.DURATION_FAST),
                    new KeyValue(icon.fillProperty(), targetColor, AnimationConstants.EASE_OUT_QUINT)
                )
            );
            colorAnim.play();
        });
    }
    
    
    /**
     * Configura il pulsante account con icona SVG premium
     * Usa l'icona user di default, ma supporta anche immagine profilo personalizzata
     */
    private void configureAccountButton() {
        // Crea icona SVG user come default
        SVGPath userIcon = IconProvider.getIcon(ICON_ACCOUNT);
        IconProvider.scaleIcon(userIcon, IconProvider.IconSize.LG);
        // Usa stroke nero + fill bianco per visibilità su sfondo viola
        userIcon.setFill(Color.rgb(255, 255, 255));
        userIcon.setStroke(Color.rgb(0, 0, 0));
        userIcon.setStrokeWidth(1.5);

        accountButton.setGraphic(userIcon);
        accountButton.setText("");
        accountButton.setOnAction(e -> handleAccount());

        // Dimensioni leggermente maggiorate per il bottone account
        accountButton.setPrefHeight(48);
        accountButton.setPrefWidth(48);

        // Applica stile premium
        accountButton.getStyleClass().addAll("action-button", "account-button");

        // Aggiungi animazione hover
        setupIconButtonAnimation(accountButton, userIcon);

        // Rendi l'immagine circolare (sarà usata se c'è immagine profilo personalizzata)
        makeImageCircular(accountImageView);
    }
    
    
    /**
     * Applica gli stili CSS ai pulsanti (Royal Purple Premium)
     * Rimuove inline styles e usa solo classi CSS
     */
    private void applyButtonStyles() {
        // Icon buttons - stile premium già applicato in configureEmojiButton
        scambiButton.getStyleClass().add("action-button");
        messagesButton.getStyleClass().add("action-button");
        cartButton.getStyleClass().add("action-button");
        accountButton.getStyleClass().add("action-button");
        accountButton.getStyleClass().add("account-button");

        // Inserisci annuncio button - stile primary button con icona + testo
        inserisciAnnuncioButton.getStyleClass().add("button-primary");

        // Search button - già configurato in configureSearchButton
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
     * Applica gli stili CSS ai componenti principali
     */
    private void applyStyling() {
        // Top bar container
        root.getStyleClass().add("top-bar");

        // Search components
        searchField.getStyleClass().add("search-field");
        searchButton.getStyleClass().add("search-button");

        // Action buttons - le classi sono già state aggiunte in applyButtonStyles()
        scambiButton.getStyleClass().add("action-button");
        messagesButton.getStyleClass().add("action-button");
        cartButton.getStyleClass().add("action-button");
        accountButton.getStyleClass().add("action-button");
        accountButton.getStyleClass().add("account-button");

        // Primary button
        inserisciAnnuncioButton.getStyleClass().add("button-primary");
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

            // Se non c'è immagine, usa l'icona SVG user premium
            if (imageUrl == null || imageUrl.isEmpty()) {
                LoggerUtil.debug("TopBar - Nessuna immagine profilo, uso icona SVG user");
                SVGPath userIcon = IconProvider.getIcon(ICON_ACCOUNT);
                IconProvider.scaleIcon(userIcon, IconProvider.IconSize.LG);
                userIcon.setFill(Color.rgb(148, 163, 184));
                accountButton.setGraphic(userIcon);
                accountButton.setText("");
                return;
            }

            Image image;

            // Verifica se è un URL Cloudinary o un percorso locale valido
            if (imageUrl.contains("cloudinary.com") || imageUrl.startsWith("http")) {
                LoggerUtil.debug("TopBar - Caricamento da Cloudinary");
                image = new Image(imageUrl, true);
            } else {
                LoggerUtil.debug("TopBar - Caricamento da file locale: " + imageUrl);
                File file = new File(imageUrl);
                if (file.exists()) {
                    image = new Image(file.toURI().toString(), true);
                } else {
                    throw new Exception("File locale non trovato: " + imageUrl);
                }
            }

            if (!image.isError()) {
                // Aspetta che l'immagine sia caricata, poi applica cover
                image.progressProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.doubleValue() >= 1.0) {
                        javafx.application.Platform.runLater(() -> {
                            accountImageView.setImage(image);
                            applyObjectFitCoverToAvatar(accountImageView, image, 42);

                            accountButton.setGraphic(accountImageView);
                            accountButton.setText("");
                            accountButton.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
                        });
                    }
                });
                LoggerUtil.success("TopBar - Immagine profilo caricata");
            } else {
                throw new Exception("Errore nel caricamento immagine");
            }

        } catch (Exception e) {
            LoggerUtil.error("TopBar - Error loading account image", e);
            // Fallback a icona SVG user premium
            SVGPath userIcon = IconProvider.getIcon(ICON_ACCOUNT);
            IconProvider.scaleIcon(userIcon, IconProvider.IconSize.LG);
            userIcon.setFill(Color.rgb(148, 163, 184));
            accountButton.setGraphic(userIcon);
            accountButton.setText("");
        }
    }
    

    // Metodo per rendere l'immagine circolare con "object-fit: cover" (stile LinkedIn/Twitter)
    private void makeImageCircular(ImageView imageView) {
        double accountImageSize = 42;

        imageView.setSmooth(true);

        // Crea un clip circolare
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
            accountImageSize / 2.0,
            accountImageSize / 2.0,
            accountImageSize / 2.0
        );
        imageView.setClip(clip);

        // Se c'è già un'immagine, applica cover
        if (imageView.getImage() != null && !imageView.getImage().isError()) {
            applyObjectFitCoverToAvatar(imageView, imageView.getImage(), accountImageSize);
        }
    }

    /**
     * Applica "object-fit: cover" all'avatar circolare
     */
    private void applyObjectFitCoverToAvatar(ImageView imageView, Image image, double size) {
        if (image.isError() || image.getWidth() == 0 || image.getHeight() == 0) {
            // Fallback semplice
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            return;
        }

        imageView.setSmooth(true);

        double imageRatio = image.getWidth() / image.getHeight();
        double sizeRatio = 1.0;

        double fitWidth, fitHeight, x, y;

        if (imageRatio > sizeRatio) {
            fitHeight = size;
            fitWidth = fitHeight * imageRatio;
            x = (size - fitWidth) / 2;
            y = 0;
        } else {
            fitWidth = size;
            fitHeight = fitWidth / imageRatio;
            x = 0;
            y = (size - fitHeight) / 2;
        }

        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setPreserveRatio(false);
    }

    // Metodo per impostare l'immagine profilo
    public void setProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadAccountImage(imageUrl);
        } else {
            // Se non c'è immagine profilo, usa icona SVG user premium
            SVGPath userIcon = IconProvider.getIcon(ICON_ACCOUNT);
            IconProvider.scaleIcon(userIcon, IconProvider.IconSize.LG);
            userIcon.setFill(Color.rgb(148, 163, 184));
            accountButton.setGraphic(userIcon);
            accountButton.setText("");
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