package schermata;

import application.Classe.Annuncio;
import application.Classe.Messaggio;
import application.Classe.ModificaAnnuncioDialog;
import application.Classe.Oggetto;
import application.Classe.utente;
import application.Enum.OrigineOggetto;
import application.Enum.Tipologia;
import application.utils.IconProvider;
import application.utils.AnimationConstants;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import schermata.button.CarrelloManager;
import schermata.button.MessaggiDialog;
import schermata.button.RecensioneDialog;
import application.DB.AnnuncioDAO;
import application.DB.MessaggioDAO;
import application.DB.RecensioneDAO;
import application.DB.SessionManager;
import application.DB.UserDAO;
import application.DB.ConnessioneDB;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * ProductCard - Componente per la visualizzazione di un singolo annuncio
 * Gestione completa delle interazioni utente: dettagli, acquisto, scambio, recensioni
 */
public class ProductCard extends VBox {
    
    // Costanti per configurazione
    private static final int IMAGE_WIDTH = 280;
    private static final int IMAGE_HEIGHT = 200;
    private static final int CARD_PADDING = 12;
    private static final int CONTENT_PADDING = 20;
    private static final int IMAGE_CONTAINER_PADDING = 10;

    // Componenti UI
    private final ImageView productImage = new ImageView();
    private final Label badge = new Label();
    private final Button contactButton = new Button();
    private final Label title = new Label();
    private final Text price = new Text();
    private final Label description = new Label();
    private final Button detailsButton = new Button("Dettagli");
    private final Button actionButton = new Button();
    private final Label vendutoBadge = new Label("VENDUTO");
    private final Label acquistatoBadge = new Label("ACQUISTATO - IN ATTESA RITIRO");
    private final Label consegnatoBadge = new Label("CONSEGNATO");
    private final Label ratingLabel = new Label(); // Punteggio medio venditore

    // Callback per azioni utente
    private Consumer<Annuncio> onDetailsAction;
    private Consumer<Annuncio> onAction;
    private Consumer<Annuncio> onFavoriteAction;
    private Consumer<Annuncio> onAnnuncioModificato;
    
    // Dati dell'annuncio
    private final int currentUserId = SessionManager.getCurrentUserId();
    private final Annuncio annuncio;
    private StackPane imageContainer;

    /**
     * Costruttore principale della ProductCard
     * @param annuncio L'annuncio da visualizzare nella card
     */
    public ProductCard(Annuncio annuncio) {
        super(10);
        this.annuncio = annuncio;
        
        initializeCard();
        setupImageSection();
        setupContentSection();
        setupEventHandlers();
        applyStyles();
        setupTooltips();
        
        checkStatoAnnuncio();
    }

    /**
     * Restituisce l'ID dell'annuncio visualizzato
     * @return ID univoco dell'annuncio
     */
    public int getAnnuncioId() {
        return annuncio.getId();
    }

    /**
     * Inizializza le proprietà base della card
     * Royal Purple Premium Design - Bordi più rotondi (24px)
     */
    private void initializeCard() {
        setPadding(new Insets(CARD_PADDING));
        setAlignment(Pos.TOP_CENTER);
        getStyleClass().add("product-card");
        // Inline style rimosso - gestito da CSS
        setPrefWidth(340);
    }

    /**
     * Verifica e gestisce lo stato dell'annuncio
     */
    private void checkStatoAnnuncio() {
        if ("CONSEGNATO".equalsIgnoreCase(annuncio.getStato())) {
            mostraStatoConsegnato();
        } else if ("VENDUTO".equalsIgnoreCase(annuncio.getStato())) {
            mostraStatoVenduto();
        } else if (isAcquistatoMaNonRitirato()) {
            mostraStatoAcquistato();
        }
    }

    /**
     * Mostra lo stato CONSEGNATO
     */
    private void mostraStatoConsegnato() {
        if (imageContainer != null) {
            consegnatoBadge.setVisible(true);
            StackPane.setAlignment(consegnatoBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(consegnatoBadge, new Insets(10));
            if (!imageContainer.getChildren().contains(consegnatoBadge)) {
                imageContainer.getChildren().add(consegnatoBadge);
            }
        }
        
        disableActionButtons();
        setupConsegnatoButton();
        applyConsegnatoStyle();
    }

    /**
     * Configura la sezione immagine della card
     */
    private void setupImageSection() {
        imageContainer = createImageContainer();
        loadProductImage();
        setupImageProperties();
        setupBadgeAndContact();
        setupStatoBadges();
        
        HBox badgeRow = createBadgeRow();
        imageContainer.getChildren().addAll(productImage, badgeRow);
        getChildren().add(imageContainer);
    }

    /**
     * Crea il container per l'immagine con bordi rotondi premium
     */
    private StackPane createImageContainer() {
        StackPane imageContainer = new StackPane();
        imageContainer.setAlignment(Pos.CENTER); // Centra l'immagine perfettamente
        imageContainer.getStyleClass().addAll("product-image-container", "image-container-premium");
        imageContainer.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        return imageContainer;
    }

    /**
     * Configura le proprietà dell'immagine
     * Metodo professionale: centra e riempie senza distorsione
     * - ImageView usa fitWidth/fitHeight per dimensioni
     * - Immagine centrata dentro ImageView
     * - preserveRatio mantiene qualità
     */
    private void setupImageProperties() {
        // Imposta dimensioni dell'ImageView
        productImage.setFitWidth(IMAGE_WIDTH);
        productImage.setFitHeight(IMAGE_HEIGHT);

        // ON preserveRatio per mantenere qualità (no sgranatura/distorzione)
        productImage.setPreserveRatio(true);
        productImage.setSmooth(true); // Anti-aliasing per qualità massima
        productImage.setCache(true); // Performance ottimizzata
        productImage.getStyleClass().add("product-image");

        // Applica clip con bordi rotondi coincidenti con la card (24px)
        Rectangle clip = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
        clip.setArcWidth(24); // Stesso arco della card
        clip.setArcHeight(24); // Stesso arco della card
        productImage.setClip(clip);

        // StackPane con CENTER centra ImageView nel container
        StackPane.setAlignment(productImage, Pos.CENTER);
    }

    /**
     * Configura badge e pulsante contatto
     */
    private void setupBadgeAndContact() {
        setupBadge();
        setupContactButton();
    }

    /**
     * Configura i badge di stato
     */
    private void setupStatoBadges() {
        setupVendutoBadge();
        setupAcquistatoBadge();
        setupConsegnatoBadge();
        setupRatingLabel();
    }

    /**
     * Configura il badge dell'origine dell'oggetto
     */
    private void setupBadge() {
        if (annuncio.getOggetto() != null) {
            badge.setText(annuncio.getOggetto().getOrigine().toString());
            badge.getStyleClass().add(getBadgeStyle(annuncio.getOggetto().getOrigine()));
        } else {
            badge.setText("N/D");
            badge.getStyleClass().add("badge-vendita");
        }
    }

    /**
     * Configura il pulsante di contatto
     */
    private void setupContactButton() {
        // Nascondi pulsante contatto se l'utente è il venditore
        if (currentUserId != -1 && currentUserId == annuncio.getVenditoreId()) {
            contactButton.setVisible(false);
            contactButton.setManaged(false);
            return;
        }

        setupContactButtonIcon();
        contactButton.getStyleClass().add("contact-button-large");
        contactButton.setTooltip(new Tooltip("Contatta venditore"));
    }

    /**
     * Configura l'icona del pulsante contatto con SVG premium
     */
    private void setupContactButtonIcon() {
        // Usa IconProvider per icona SVG Lucide-style
        SVGPath messageIcon = IconProvider.getIcon("message-circle");
        IconProvider.scaleIcon(messageIcon, IconProvider.IconSize.LG);
        messageIcon.setFill(Color.rgb(148, 163, 184)); // text-tertiary

        contactButton.setGraphic(messageIcon);
        contactButton.setText("");
        contactButton.getStyleClass().addAll("contact-button", "button-icon");
        contactButton.setTooltip(new Tooltip("Contatta venditore"));
    }

    /**
     * Configura il badge "VENDUTO" - Stile Royal Purple
     */
    private void setupVendutoBadge() {
        vendutoBadge.getStyleClass().addAll("venduto-badge", "badge-sold");
        vendutoBadge.setVisible(false);
    }

    /**
     * Configura il badge "ACQUISTATO" - Stile Royal Purple
     */
    private void setupAcquistatoBadge() {
        acquistatoBadge.getStyleClass().addAll("acquistato-badge", "badge-pending");
        acquistatoBadge.setVisible(false);
    }

    /**
     * Configura il badge "CONSEGNATO" - Stile Royal Purple
     */
    private void setupConsegnatoBadge() {
        consegnatoBadge.getStyleClass().addAll("consegnato-badge", "badge-delivered");
        consegnatoBadge.setVisible(false);
    }

    /**
     * Configura la label del rating del venditore (Vinted-style)
     */
    private void setupRatingLabel() {
        try {
            RecensioneDAO recensioneDAO = new RecensioneDAO();
            double punteggioMedio = recensioneDAO.getPunteggioMedioVenditore(annuncio.getVenditoreId());
            int numeroRecensioni = recensioneDAO.contaRecensioniVenditore(annuncio.getVenditoreId());

            if (numeroRecensioni > 0) {
                String stelle = generateStars(punteggioMedio);
                ratingLabel.setText(String.format("★ %.1f (%d)", punteggioMedio, numeroRecensioni));
                ratingLabel.getStyleClass().add("seller-rating");
                ratingLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 700; -fx-font-size: 13px;");
            } else {
                ratingLabel.setText("Nuovo venditore");
                ratingLabel.getStyleClass().add("seller-rating-new");
                ratingLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 12px;");
            }
        } catch (Exception e) {
            ratingLabel.setText("N/A");
            ratingLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        }
    }

    /**
     * Genera stelle per il punteggio medio
     */
    private String generateStars(double punteggioMedio) {
        int stellePiene = (int) Math.round(punteggioMedio);
        StringBuilder stelle = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stelle.append(i < stellePiene ? "★" : "☆");
        }
        return stelle.toString();
    }

    /**
     * Crea la riga contenente badge e pulsante contatto
     */
    private HBox createBadgeRow() {
        HBox badgeRow = new HBox(12, badge, new Region(), contactButton);
        HBox.setHgrow(badgeRow.getChildren().get(1), Priority.ALWAYS);
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        badgeRow.setPadding(new Insets(IMAGE_CONTAINER_PADDING));
        return badgeRow;
    }

    /**
     * Mostra lo stato venduto disabilitando i pulsanti e mostrando il badge
     */
    private void mostraStatoVenduto() {
        if (imageContainer != null) {
            vendutoBadge.setVisible(true);
            StackPane.setAlignment(vendutoBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(vendutoBadge, new Insets(10));
            if (!imageContainer.getChildren().contains(vendutoBadge)) {
                imageContainer.getChildren().add(vendutoBadge);
            }
        }
        
        disableActionButtons();
        setupVendutoButton();
        applyVendutoStyle();
    }

    /**
     * Mostra lo stato acquistato disabilitando i pulsanti e mostrando il badge
     */
    private void mostraStatoAcquistato() {
        if (imageContainer != null) {
            acquistatoBadge.setVisible(true);
            StackPane.setAlignment(acquistatoBadge, Pos.TOP_CENTER);
            StackPane.setMargin(acquistatoBadge, new Insets(10));
            if (!imageContainer.getChildren().contains(acquistatoBadge)) {
                imageContainer.getChildren().add(acquistatoBadge);
            }
        }
        
        disableActionButtons();
        setupAcquistatoButton();
        applyAcquistatoStyle();
    }

    /**
     * Disabilita tutti i pulsanti di azione
     */
    private void disableActionButtons() {
        actionButton.setDisable(true);
        detailsButton.setDisable(true);
        contactButton.setDisable(true);
    }

    /**
     * Configura il pulsante principale per stato venduto
     */
    private void setupVendutoButton() {
        actionButton.setText("Venduto");
        actionButton.getStyleClass().addAll("card-button", "card-button-disabled");
        setupPremiumIcon(actionButton, "check-circle");

        Tooltip.install(actionButton, new Tooltip("Questo articolo è stato venduto"));
        Tooltip.install(vendutoBadge, new Tooltip("Questo articolo è stato venduto"));
    }

    /**
     * Configura il pulsante principale per stato acquistato
     */
    private void setupAcquistatoButton() {
        actionButton.setText("In Attesa");
        actionButton.getStyleClass().addAll("card-button", "card-button-pending");
        setupPremiumIcon(actionButton, "clock");

        Tooltip.install(actionButton, new Tooltip("Questo articolo è stato acquistato e attende il ritiro"));
        Tooltip.install(acquistatoBadge, new Tooltip("Questo articolo è stato acquistato e attende il ritiro"));
    }

    /**
     * Configura il pulsante principale per stato consegnato
     */
    private void setupConsegnatoButton() {
        actionButton.setText("Consegnato");
        actionButton.getStyleClass().addAll("card-button", "card-button-disabled");
        setupPremiumIcon(actionButton, "check-circle");

        Tooltip.install(actionButton, new Tooltip("Questo articolo è stato consegnato"));
        Tooltip.install(consegnatoBadge, new Tooltip("Questo articolo è stato consegnato"));
    }

    /**
     * Aggiunge icona SVG premium a un bottone
     */
    private void setupPremiumIcon(Button button, String iconName) {
        try {
            SVGPath icon = IconProvider.getIcon(iconName);
            IconProvider.scaleIcon(icon, IconProvider.IconSize.SM);
            icon.setFill(Color.WHITE);
            button.setGraphic(icon);
        } catch (Exception e) {
            // Fallback se icona non trovata
            System.err.println("Icona non trovata: " + iconName);
        }
    }

    /**
     * Applica lo stile per annuncio venduto
     */
    private void applyVendutoStyle() {
        getStyleClass().add("product-card-sold");
        // Inline style rimosso - gestito da CSS
    }

    /**
     * Applica lo stile per annuncio acquistato
     */
    private void applyAcquistatoStyle() {
        getStyleClass().add("product-card-acquistato");
        // Inline style rimosso - gestito da CSS
    }

    /**
     * Applica lo stile per annuncio consegnato
     */
    private void applyConsegnatoStyle() {
        getStyleClass().add("product-card-consegnato");
        // Inline style rimosso - gestito da CSS
    }

    /**
     * Verifica se l'annuncio è stato acquistato ma non ancora ritirato
     * ✅ FIX: Spostato nel DAO layer per separazione responsabilità
     */
    private boolean isAcquistatoMaNonRitirato() {
        try {
            application.DB.CarrelloDAO carrelloDAO = new application.DB.CarrelloDAO();
            return carrelloDAO.isAnnuncioAcquistatoMaNonRitirato(annuncio.getId(), currentUserId);
        } catch (Exception e) {
            System.err.println("Errore nel verificare lo stato acquisto per annuncio " + annuncio.getId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Carica l'immagine del prodotto
     * Metodo professionale: carica con parametri ottimali per qualità massima
     */
    private void loadProductImage() {
        // Carica prima l'immagine di default
        loadDefaultImage();

        // Poi in un thread separato carica l'immagine reale se disponibile
        new Thread(() -> {
            try {
                AnnuncioDAO dao = new AnnuncioDAO();
                String imageUrl = dao.getImageUrlAnnuncio(annuncio.getId());

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Parametri Image JavaFX professionali:
                    // - width, height: dimensioni target
                    // - preserveRatio: TRUE per mantenere qualità (no sgranatura)
                    // - smooth: TRUE per anti-aliasing di qualità
                    // - backgroundLoading: TRUE per non bloccare UI
                    Image realImage = new Image(imageUrl, IMAGE_WIDTH, IMAGE_HEIGHT, true, true, true);

                    // Aggiorna l'immagine nella UI thread
                    javafx.application.Platform.runLater(() -> {
                        productImage.setImage(realImage);
                    });
                }
            } catch (Exception e) {
                System.err.println("Errore nel caricamento dell'immagine per annuncio " + annuncio.getId() + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * Carica l'immagine di default
     * Stesso approccio professionale delle immagini reali
     */
    private void loadDefaultImage() {
        try {
            InputStream defaultStream = getClass().getResourceAsStream("/application/img/default-product.png");
            if (defaultStream != null) {
                // preserveRatio: true per mantenere qualità
                productImage.setImage(new Image(defaultStream, IMAGE_WIDTH, IMAGE_HEIGHT, true, true));
            } else {
                productImage.setImage(new Image("https://via.placeholder.com/280x200.png?text=No+Image",
                    IMAGE_WIDTH, IMAGE_HEIGHT, true, true));
            }
        } catch (Exception e) {
            productImage.setImage(new Image("https://via.placeholder.com/280x200.png?text=No+Image",
                IMAGE_WIDTH, IMAGE_HEIGHT, true, true));
        }
    }

    /**
     * Configura la sezione contenuto della card
     * NOTA: Descrizione rimossa per mostrare solo in Dettagli
     */
    private void setupContentSection() {
        VBox content = createContentContainer();
        setupHeader(content);
        // setupDescription(content); // Rimosso - descrizione visibile solo in Dettagli
        setupMetaInfo(content);
        setupActionButtons(content);

        getChildren().add(content);
    }

    /**
     * Crea il container del contenuto
     */
    private VBox createContentContainer() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(CONTENT_PADDING));
        content.getStyleClass().add("product-content");
        return content;
    }

    /**
     * Configura l'header con titolo e prezzo
     */
    private void setupHeader(VBox content) {
        HBox header = createHeader();
        content.getChildren().add(header);
    }

    /**
     * Crea l'header con titolo e prezzo
     */
    private HBox createHeader() {
        setupTitle();
        setupPrice();
        
        HBox header = new HBox(title, new Region(), price);
        header.getStyleClass().add("product-header");
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /**
     * Configura il titolo dell'annuncio con wrapping per non farlo uscire
     */
    private void setupTitle() {
        String titoloAnnuncio = annuncio.getTitolo() != null ? annuncio.getTitolo() : "Senza titolo";

        title.setText(titoloAnnuncio);
        title.getStyleClass().addAll("product-title", "text-wrap"); // Aggiunge wrapping
        title.setWrapText(true); // Abilita il wrapping del testo
        title.setMaxWidth(IMAGE_WIDTH - 40); // Limita larghezza con margin
        title.setMinHeight(Region.USE_PREF_SIZE); // Permetti al label di crescere in altezza
    }

    /**
     * Configura il prezzo dell'annuncio
     */
    private void setupPrice() {
        String formattedPrice = annuncio.getPrezzo() > 0 ?
            annuncio.getPrezzoFormattato() : "Gratuito";
        price.setText(formattedPrice);
        price.getStyleClass().add("product-price");
        // Inline style rimosso - gestito da CSS
    }

    /**
     * Configura la descrizione con wrapping per non farla uscire
     */
    private void setupDescription(VBox content) {
        String descrizioneTesto = getDescrizioneTesto();
        description.setText(descrizioneTesto);
        description.getStyleClass().addAll("product-description", "text-wrap");
        description.setWrapText(true); // Abilita wrapping
        description.setMaxWidth(IMAGE_WIDTH - 40); // Limita larghezza
        // Inline style rimosso - gestito da CSS
        content.getChildren().add(description);
    }

    /**
     * Ottiene il testo della descrizione
     */
    private String getDescrizioneTesto() {
        if (annuncio.getOggetto() != null && annuncio.getOggetto().getDescrizione() != null) {
            return annuncio.getOggetto().getDescrizione();
        } else if (annuncio.getDescrizione() != null) {
            return annuncio.getDescrizione();
        }
        return "Nessuna descrizione";
    }

    /**
     * Configura le informazioni meta
     */
    private void setupMetaInfo(VBox content) {
        VBox metaInfo = createMetaInfo();
        metaInfo.getStyleClass().add("product-meta");
        content.getChildren().add(metaInfo);
    }

    /**
     * Crea le informazioni meta dell'annuncio
     */
    private VBox createMetaInfo() {
        VBox meta = new VBox(4);

        HBox row1 = new HBox(8,
                createIconText("📍", annuncio.getSedeConsegna() != null ? annuncio.getSedeConsegna() : "Non specificato"),
                new Text("•"),
                createIconText("🚚", annuncio.getModalitaConsegna() != null ? annuncio.getModalitaConsegna() : "Non specificata")
        );
        row1.getStyleClass().add("meta-row");

        // Row 2: Venditore + Rating + Data
        HBox row2 = new HBox(8,
                createIconText("👤", annuncio.getNomeUtenteVenditore() != null ? annuncio.getNomeUtenteVenditore() : "Venditore"),
                ratingLabel, // Rating del venditore (Vinted-style)
                new Text("•"),
                createIconText("📅", formatDate(annuncio.getDataPubblicazione()))
        );
        row2.getStyleClass().add("meta-row");

        meta.getChildren().addAll(row1, row2);
        return meta;
    }

    /**
     * Crea un elemento testo con icona
     */
    private HBox createIconText(String icon, String text) {
        Text iconText = new Text(icon);
        iconText.setStyle("-fx-font-size: 12px;");
        
        Text contentText = new Text(text);
        contentText.setStyle("-fx-font-size: 12px; -fx-fill: #7f8c8d;");
        
        HBox container = new HBox(4, iconText, contentText);
        container.setAlignment(Pos.CENTER_LEFT);
        return container;
    }

    /**
     * Configura i pulsanti di azione
     */
    private void setupActionButtons(VBox content) {
        // Solo se l'annuncio non è venduto, acquistato o consegnato
        if (!"VENDUTO".equalsIgnoreCase(annuncio.getStato()) && 
            !"CONSEGNATO".equalsIgnoreCase(annuncio.getStato()) && 
            !isAcquistatoMaNonRitirato()) {
            setupMainActionButton();
        }
        
        setupDetailsButton();
        HBox actions = createActionButtons();
        content.getChildren().add(actions);

        // Aggiungi pulsanti recensioni SOLO dopo acquisto (stato CONSEGNATO)
        if (currentUserId != -1 && "CONSEGNATO".equalsIgnoreCase(annuncio.getStato())) {
            HBox recensioniBox = createRecensioniButtons();
            content.getChildren().add(recensioniBox);
        }
    }

    /**
     * Configura il pulsante azione principale
     */
    private void setupMainActionButton() {
        if (currentUserId != -1 && currentUserId == annuncio.getVenditoreId()) {
            setupModificaButton();
        } else if (annuncio.getTipologia() == Tipologia.ASTA) {
            setupAstaButton();
        } else if (annuncio.getOggetto() != null) {
            setupOrigineButton();
        } else {
            setupDefaultButton();
        }
    }

    /**
     * Configura pulsante modifica per venditore
     */
    private void setupModificaButton() {
        actionButton.setText("Modifica Annuncio");
        actionButton.getStyleClass().addAll("card-button", "card-button-edit");
        setupPremiumIcon(actionButton, "edit");
    }

    /**
     * Configura pulsante per aste
     */
    private void setupAstaButton() {
        actionButton.setText("Fai Offerta");
        actionButton.getStyleClass().addAll("card-button", "card-button-auction");
        setupPremiumIcon(actionButton, "gavel");
    }

    /**
     * Configura pulsante in base all'origine dell'oggetto
     */
    private void setupOrigineButton() {
        switch (annuncio.getOggetto().getOrigine()) {
            case USATO:
                actionButton.setText("Aggiungi al Carrello");
                actionButton.getStyleClass().addAll("card-button", "card-button-success");
                setupPremiumIcon(actionButton, "shopping-cart");
                break;
            case SCAMBIO:
                actionButton.setText("Proponi Scambio");
                actionButton.getStyleClass().addAll("card-button", "card-button-info");
                setupPremiumIcon(actionButton, "repeat");
                break;
            case REGALO:
                actionButton.setText("Contatta");
                actionButton.getStyleClass().addAll("card-button", "card-button-accent");
                setupPremiumIcon(actionButton, "message-circle");
                break;
            default:
                setupDefaultButton();
        }
    }

    /**
     * Configura pulsante default
     */
    private void setupDefaultButton() {
        actionButton.setText("Contatta");
        actionButton.getStyleClass().addAll("card-button", "card-button-primary");
        setupPremiumIcon(actionButton, "message-circle");
    }

    /**
     * Configura il pulsante dettagli con stile premium uniforme
     */
    private void setupDetailsButton() {
        detailsButton.setText("Dettagli");
        detailsButton.getStyleClass().addAll("card-button", "card-button-secondary");
        // Rimuovi inline style - gestito da CSS
    }

    /**
     * Crea il container dei pulsanti azione con dimensioni uniformi
     * Royal Purple Premium - Bottoni stessi per grandezza e stile
     */
    private HBox createActionButtons() {
        HBox actions = new HBox(12, detailsButton, actionButton);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("product-actions");

        // Imposta dimensioni uniformi per entrambi i bottoni
        detailsButton.setPrefHeight(48);
        detailsButton.setPrefWidth(140);
        detailsButton.setMinHeight(48);
        detailsButton.setMinWidth(140);

        actionButton.setPrefHeight(48);
        actionButton.setPrefWidth(140);
        actionButton.setMinHeight(48);
        actionButton.setMinWidth(140);

        return actions;
    }

    /**
     * Crea i pulsanti per le recensioni
     */
    private HBox createRecensioniButtons() {
        HBox recensioniBox = new HBox(10);
        recensioniBox.setAlignment(Pos.CENTER);
        recensioniBox.getStyleClass().add("review-actions");
        
        Button recensioniBtn = createRecensioniButton();
        recensioniBox.getChildren().add(recensioniBtn);
        
        if (currentUserId != annuncio.getVenditoreId()) {
            Button lasciaRecensioneBtn = createLasciaRecensioneButton();
            recensioniBox.getChildren().add(lasciaRecensioneBtn);
        }
        
        return recensioniBox;
    }

    /**
     * Crea il pulsante visualizza recensioni
     */
    private Button createRecensioniButton() {
        Button recensioniBtn = new Button("⭐ Recensioni");
        recensioniBtn.getStyleClass().addAll("card-button", "card-button-info");
        recensioniBtn.setOnAction(e -> mostraRecensioniVenditore());
        return recensioniBtn;
    }

    /**
     * Crea il pulsante lascia recensione
     */
    private Button createLasciaRecensioneButton() {
        Button lasciaRecensioneBtn = new Button("✍️ Lascia Recensione");
        lasciaRecensioneBtn.getStyleClass().addAll("card-button", "card-button-accent");
        lasciaRecensioneBtn.setOnAction(e -> lasciaRecensione());
        return lasciaRecensioneBtn;
    }

    /**
     * Configura gli event handler della card
     */
    private void setupEventHandlers() {
        setupDetailsHandler();
        setupActionHandler();
        setupContactHandler();
    }

    /**
     * Configura l'handler per il pulsante dettagli
     */
    private void setupDetailsHandler() {
        detailsButton.setOnAction(e -> {
            if (onDetailsAction != null) onDetailsAction.accept(annuncio);
        });
    }

    /**
     * Configura l'handler per il pulsante azione principale
     */
    private void setupActionHandler() {
        actionButton.setOnAction(e -> {
            if ("VENDUTO".equalsIgnoreCase(annuncio.getStato()) || 
                "CONSEGNATO".equalsIgnoreCase(annuncio.getStato()) || 
                isAcquistatoMaNonRitirato()) {
                return;
            }
            
            if (currentUserId != -1 && currentUserId == annuncio.getVenditoreId()) {
                modificaAnnuncio();
                return;
            }
            
            handleUserAction();
        });
    }

    /**
     * Gestisce l'azione dell'utente in base al tipo di annuncio
     */
    private void handleUserAction() {
        String buttonText = actionButton.getText();
        if (buttonText.contains("Aggiungi al Carrello")) {
            aggiungiAlCarrello();
        } else if (buttonText.contains("Fai Offerta")) {
            faiOfferta();
        } else if (buttonText.contains("Proponi Scambio")) {
            proponiScambio();
        } else if (buttonText.contains("Contatta")) {
            contattaPerRegalo();
        } else {
            handleDefaultAction();
        }
    }

    /**
     * Configura l'handler per il pulsante contatto
     */
    private void setupContactHandler() {
        contactButton.setOnAction(e -> {
            if (!isUtenteLoggato()) {
                mostraMessaggio("Devi essere loggato per visualizzare le chat");
                return;
            }
            
            String messaggioIniziale = "Salve, sono interessato al suo articolo: " + annuncio.getTitolo();
            MessaggiDialog dialog = new MessaggiDialog(annuncio, currentUserId, messaggioIniziale);
            dialog.showAndWait();
        });
    }

    /**
     * Gestisce la modifica dell'annuncio da parte del venditore
     */
    private void modificaAnnuncio() {
        ModificaAnnuncioDialog dialog = new ModificaAnnuncioDialog(annuncio);
        
        dialog.showAndWait().ifPresent(annuncioModificato -> {
            try {
                AnnuncioDAO annuncioDAO = new AnnuncioDAO();
                boolean successo = annuncioDAO.aggiornaAnnuncioCompleto(annuncioModificato);
                
                if (successo) {
                    mostraMessaggio("Annuncio aggiornato con successo!");
                    
                    if (onAnnuncioModificato != null) {
                        onAnnuncioModificato.accept(annuncioModificato);
                    }
                } else {
                    mostraMessaggio("Errore durante l'aggiornamento dell'annuncio.");
                }
            } catch (Exception ex) {
                mostraMessaggio("Errore durante l'aggiornamento dell'annuncio: " + ex.getMessage());
            }
        });
    }

    /**
     * Gestisce l'azione di default
     */
    private void handleDefaultAction() {
        if (!annuncio.isDisponibile()) {
            mostraMessaggio("Questo annuncio non è più disponibile");
            return;
        }
        
        if (!isUtenteLoggato()) {
            mostraMessaggio("Devi essere loggato per effettuare questa azione");
            return;
        }
        
        if (currentUserId != -1 && currentUserId == annuncio.getVenditoreId()) {
            mostraMessaggio("Sei il venditore di questo annuncio");
            return;
        }
        
        if (annuncio.getTipologia() == Tipologia.ASTA) {
            faiOfferta();
        } else if (annuncio.getOggetto() != null) {
            handleByOrigine();
        } else {
            contattaPerRegalo();
        }
    }

    /**
     * Gestisce l'azione in base all'origine dell'oggetto
     */
    private void handleByOrigine() {
        switch (annuncio.getOggetto().getOrigine()) {
            case USATO:
                aggiungiAlCarrello();
                break;
            case SCAMBIO:
                proponiScambio();
                break;
            case REGALO:
                contattaPerRegalo();
                break;
        }
    }

    /**
     * Aggiunge l'annuncio al carrello
     */
    private void aggiungiAlCarrello() {
        try {
            CarrelloManager carrelloManager = CarrelloManager.getInstance();
            carrelloManager.aggiungiAlCarrello(annuncio);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aggiunto al Carrello");
            alert.setHeaderText(null);
            alert.setContentText(annuncio.getTitolo() + " è stato aggiunto al carrello!");
            alert.showAndWait();
            
        } catch (Exception e) {
            mostraMessaggio("Errore nell'aggiunta al carrello: " + e.getMessage());
        }
    }

    /**
     * Propone uno scambio per l'annuncio usando il dialog dedicato
     */
    private void proponiScambio() {
        try {
            schermata.button.PropostaScambioDialog dialog =
                new schermata.button.PropostaScambioDialog(annuncio.getId());

            Integer scambioId = dialog.showAndWait().orElse(null);

            if (scambioId != null) {
                mostraMessaggio("✅ Proposta di scambio inviata con successo!");

                // Invia anche un messaggio al venditore per notificarlo
                inviaNotificaScambio(scambioId);
            }
        } catch (IllegalStateException e) {
            mostraMessaggio(e.getMessage());
        } catch (Exception e) {
            mostraMessaggio("Errore durante la proposta di scambio: " + e.getMessage());
        }
    }

    /**
     * Invia una notifica di scambio al venditore
     */
    private void inviaNotificaScambio(int scambioId) {
        try {
            String messaggioTesto = "💫 Hai ricevuto una nuova proposta di scambio per il tuo articolo: " +
                                  annuncio.getTitolo() + "\n" +
                                  "Controlla la sezione Scambi per gestire la proposta.";

            Messaggio messaggio = new Messaggio(
                currentUserId,
                annuncio.getVenditoreId(),
                messaggioTesto,
                annuncio.getId()
            );

            MessaggioDAO messaggioDAO = new MessaggioDAO();
            messaggioDAO.inviaMessaggio(messaggio);
        } catch (Exception e) {
            System.err.println("Errore invio notifica: " + e.getMessage());
        }
    }

    /**
     * Invia il messaggio di proposta scambio
     */
    private void inviaMessaggioScambio(String proposta) {
        String messaggioTesto = "Proposta di scambio per il tuo articolo: " + annuncio.getTitolo() + 
                              "\nOffro in cambio: " + proposta;
        
        Messaggio messaggio = new Messaggio(
            currentUserId,
            annuncio.getVenditoreId(),
            messaggioTesto,
            annuncio.getId()
        );
        
        MessaggioDAO messaggioDAO = new MessaggioDAO();
        boolean successo = messaggioDAO.inviaMessaggio(messaggio);
        
        if (successo) {
            mostraMessaggio("Proposta di scambio inviata: " + proposta);
        } else {
            mostraMessaggio("Errore durante l'invio della proposta di scambio.");
        }
    }

    /**
     * Contatta per articolo in regalo
     */
    private void contattaPerRegalo() {
        String messaggioTesto = "Salve, sono interessato al tuo articolo in regalo: " + annuncio.getTitolo();
        
        Messaggio messaggio = new Messaggio(
            currentUserId,
            annuncio.getVenditoreId(),
            messaggioTesto,
            annuncio.getId()
        );
        
        MessaggioDAO messaggioDAO = new MessaggioDAO();
        boolean successo = messaggioDAO.inviaMessaggio(messaggio);
        
        if (successo) {
            mostraMessaggio("Richiesta inviata! Il venditore ti contatterà presto.");
        } else {
            mostraMessaggio("Errore durante l'invio della richiesta.");
        }
    }

    /**
     * Gestisce la creazione di un'offerta
     */
    private void faiOfferta() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Fai un'offerta");
        dialog.setHeaderText("Fai un'offerta per: " + annuncio.getTitolo());
        dialog.setContentText("Importo offerta (€):");

        dialog.showAndWait().ifPresent(importoStr -> {
            try {
                double importo = Double.parseDouble(importoStr);
                if (importo <= 0) {
                    mostraMessaggio("L'importo deve essere positivo");
                    return;
                }
                
                inviaOfferta(importo);
                
            } catch (NumberFormatException e) {
                mostraMessaggio("Inserisci un importo valido");
            }
        });
    }

    /**
     * Invia l'offerta al venditore
     */
    private void inviaOfferta(double importo) {
        String messaggioTesto = "Nuova offerta di €" + importo + " per il tuo articolo: " + annuncio.getTitolo();
        
        Messaggio messaggio = new Messaggio(
            currentUserId,
            annuncio.getVenditoreId(),
            messaggioTesto,
            annuncio.getId()
        );
        
        MessaggioDAO messaggioDAO = new MessaggioDAO();
        boolean successo = messaggioDAO.inviaMessaggio(messaggio);
        
        if (successo) {
            mostraMessaggio("Offerta di €" + importo + " inviata con successo!");
        } else {
            mostraMessaggio("Errore durante l'invio dell'offerta.");
        }
    }

    /**
     * Mostra le recensioni del venditore (Vinted-style)
     */
    private void mostraRecensioniVenditore() {
        try {
            RecensioneDAO recensioneDAO = new RecensioneDAO();
            UserDAO userDAO = new UserDAO();

            // Recupera il venditore
            utente venditore = userDAO.getUserById(annuncio.getVenditoreId());
            if (venditore == null) {
                mostraMessaggio("Impossibile recuperare le informazioni del venditore.");
                return;
            }

            // Recupera tutte le recensioni del venditore (non solo questo annuncio!)
            RecensioneDAO.StatisticheRecensioni risultato =
                recensioneDAO.getRecensioniEStatistichePerVenditore(venditore.getId());

            List<application.Classe.Recensioni> recensioni = risultato.getRecensioni();

            if (recensioni.isEmpty()) {
                String nomeVenditore = venditore.getNome() != null ? venditore.getNome() : "";
                String cognomeVenditore = venditore.getCognome() != null ? venditore.getCognome() : "";
                mostraMessaggio((nomeVenditore + " " + cognomeVenditore).trim() + " non ha ancora ricevuto recensioni.");
                return;
            }

            double punteggioMedio = risultato.getPunteggioMedio();
            RecensioneDialog dialog = new RecensioneDialog(venditore, recensioni, punteggioMedio);
            dialog.showAndWait();

        } catch (Exception e) {
            mostraMessaggio("Errore nel caricamento delle recensioni. Riprova più tardi.");
        }
    }
    
    /**
     * Gestisce l'invio di una recensione (Vinted-style: recensione tra persone)
     */
    private void lasciaRecensione() {
        if (!isUtenteLoggato()) {
            mostraMessaggio("Devi essere loggato per lasciare una recensione");
            return;
        }

        if (currentUserId == annuncio.getVenditoreId()) {
            mostraMessaggio("Non puoi lasciare una recensione a te stesso");
            return;
        }

        try {
            RecensioneDAO recensioneDAO = new RecensioneDAO();

            // Verifica se ha già recensito questo VENDITORE (non l'annuncio!)
            if (recensioneDAO.haGiaRecensitoVenditore(currentUserId, annuncio.getVenditoreId())) {
                mostraMessaggio("Hai già lasciato una recensione a questo venditore.");
                return;
            }

            utente venditore = recuperaVenditore(annuncio.getVenditoreId());

            if (venditore == null) {
                mostraMessaggio("Impossibile trovare le informazioni del venditore");
                return;
            }

            RecensioneDialog dialog = new RecensioneDialog(annuncio, venditore, currentUserId);
            
            boolean recensioneInviata = dialog.showAndWait().orElse(false);
            
            if (recensioneInviata) {
                mostraMessaggio("Recensione inviata con successo! Grazie per il feedback.");
            }

        } catch (Exception e) {
            mostraMessaggio("Errore durante l'invio della recensione: " + e.getMessage());
        }
    }

    /**
     * Recupera i dati del venditore
     */
    private utente recuperaVenditore(int venditoreId) {
        try {
            UserDAO userDAO = new UserDAO();
            return userDAO.getUserById(venditoreId);
        } catch (Exception e) {
            utente venditore = new utente();
            venditore.setId(venditoreId);
            venditore.setNome("Venditore");
            venditore.setCognome("#" + venditoreId);
            return venditore;
        }
    }

    /**
     * Verifica se l'utente è loggato
     */
    private boolean isUtenteLoggato() {
        return SessionManager.getCurrentUser() != null;
    }

    /**
     * Mostra un messaggio all'utente
     */
    private void mostraMessaggio(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    /**
     * Applica gli stili alla card
     */
    private void applyStyles() {
        getStyleClass().add("product-card");
        
        setupContactButtonHover();
    }

    /**
     * Configura l'effetto hover per il pulsante contatto
     * Gestito interamente da CSS - niente inline styles
     */
    private void setupContactButtonHover() {
        // L'hover è gestito da CSS - .contact-button:hover
        // setupContactButtonIcon() aggiunge già le classi CSS necessarie
    }

    /**
     * Configura i tooltips per i componenti
     */
    private void setupTooltips() {
        Tooltip.install(detailsButton, new Tooltip("Visualizza dettagli prodotto"));
        setupActionButtonTooltip();
        Tooltip.install(contactButton, new Tooltip("Contatta venditore"));
    }

    /**
     * Configura il tooltip per il pulsante azione
     */
    private void setupActionButtonTooltip() {
        String actionTooltip = getActionButtonTooltip();
        Tooltip.install(actionButton, new Tooltip(actionTooltip));
    }

    /**
     * Restituisce il tooltip appropriato per il pulsante azione
     */
    private String getActionButtonTooltip() {
        String buttonText = actionButton.getText();
        if (buttonText.contains("Aggiungi al Carrello")) {
            return "Aggiungi questo articolo al carrello per l'acquisto";
        } else if (buttonText.contains("Proponi Scambio")) {
            return "Proponi uno scambio per questo articolo";
        } else if (buttonText.contains("Contatta")) {
            return "Contatta per ricevere questo regalo";
        } else if (buttonText.contains("Fai Offerta")) {
            return "Fai un'offerta per questa asta";
        } else if (buttonText.contains("Modifica")) {
            return "Modifica il tuo annuncio";
        } else if (buttonText.contains("Venduto")) {
            return "Questo articolo è stato venduto";
        } else if (buttonText.contains("In Attesa")) {
            return "Questo articolo è stato acquistato e attende il ritiro";
        } else if (buttonText.contains("Consegnato")) {
            return "Questo articolo è stato consegnato";
        }
        return "";
    }

    /**
     * Formatta la data per la visualizzazione
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "Oggi";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Restituisce lo stile CSS per il badge in base all'origine
     */
    private String getBadgeStyle(OrigineOggetto origine) {
        if (origine == null) return "badge-vendita";
        switch (origine) {
            case USATO: return "badge-vendita";  
            case SCAMBIO: return "badge-scambio";
            case REGALO:  return "badge-regalo";
            default:      return "badge-vendita";
        }
    }

    // === SETTER PER I CALLBACK ===

    /**
     * Imposta il callback per l'azione dettagli
     */
    public void setOnDetailsAction(Consumer<Annuncio> handler) {
        this.onDetailsAction = handler;
    }

    /**
     * Imposta il callback per l'azione offerta
     */
    public void setOnOfferAction(Consumer<Annuncio> handler) {
        this.onAction = handler;
    }

    /**
     * Imposta il callback per l'azione preferiti
     */
    public void setOnFavoriteAction(Consumer<Annuncio> handler) {
        this.onFavoriteAction = handler;
    }

    /**
     * Imposta il callback per l'aggiornamento annuncio
     */
    public void setOnAnnuncioModificato(Consumer<Annuncio> handler) {
        this.onAnnuncioModificato = handler;
    }
}