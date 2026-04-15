package schermata;

import application.Classe.Annuncio;
import application.Classe.AzioneAnnuncioHandler;
import application.Classe.Messaggio;
import application.DB.MessaggioDAO;
import application.DB.SessionManager;
import application.Enum.Tipologia;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import schermata.button.MessaggiDialog;


public class DettagliProdottoView {
    
    /** Percorso immagine di default per prodotti senza immagine */
    private static final String PERCORSO_IMMAGINE_DEFAULT = "/images/default-product.png";
    
    /** Dimensioni finestra principale - Design premium responsive */
    private static final int LARGHEZZA_FINESTRA = 700;
    private static final int ALTEZZA_FINESTRA = 550;

    /** Dimensioni immagine prodotto - Ottimizzata per responsive */
    private static final int LARGHEZZA_IMMAGINE = 280;
    private static final int ALTEZZA_IMMAGINE = 220;
    
    /** Dimensioni minime finestra */
    private static final int LARGHEZZA_MINIMA = 400;
    private static final int ALTEZZA_MINIMA = 400;
    
    
    /** Cache delle icone per evitare ricaricamenti multipli e migliorare performance */
    private static final Image ICONA_CARRELLO = caricaIcona("/icons/cart.png");
    private static final Image ICONA_SCAMBIO = caricaIcona("/icons/exchange.png");
    private static final Image ICONA_MESSAGGIO = caricaIcona("/icons/message.png");
    private static final Image ICONA_ASTA = caricaIcona("/icons/auction.png");
    
    /** Stili CSS predefiniti per i badge di tipologia */
    private static final String STILE_BADGE_VENDITA = "badge-sale";
    private static final String STILE_BADGE_SCAMBIO = "badge-exchange";
    private static final String STILE_BADGE_REGALO = "badge-gift";
    private static final String STILE_BADGE_ASTA = "badge-auction";
    
    /** L'annuncio di cui visualizzare i dettagli */
    private final Annuncio annuncio;
    
    /** Stage della finestra modale */
    private final Stage stage = new Stage();
    
    /** Componenti dell'interfaccia grafica */
    private final ImageView visualizzatoreImmagine = new ImageView();
    private final Text testoNomeProdotto = new Text();
    private final Text testoPrezzoProdotto = new Text();
    private final Text testoDescrizioneProdotto = new Text();
    private final Button pulsanteAzione = new Button();

    /** Layout containers for responsive switching */
    private javafx.scene.layout.Pane contenutoResponsive;
    private VBox sezioneImmagine;
    private VBox sezioneInformazioni;
    
    /**
     * Costruttore principale della vista dettagli prodotto
     * 
     * @param annuncio L'annuncio di cui visualizzare i dettagli
     * @throws IllegalArgumentException Se l'annuncio è null
     */
    public DettagliProdottoView(Annuncio annuncio) {
        if (annuncio == null) {
            throw new IllegalArgumentException("L'annuncio non può essere null");
        }

        this.annuncio = annuncio;
        inizializzaStage();
        configuraInterfacciaUtente();
        setupDialogImageCover();
        setupResponsiveDialog();
    }
    
    /**
     * Inizializza le proprietà della finestra modale
     * Configura il titolo, modalità e dimensioni della stage
     * Centra la finestra allo schermo
     */
    private void inizializzaStage() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Dettagli Prodotto - " + ottieniNomeProdotto());
        stage.setMinWidth(LARGHEZZA_MINIMA);
        stage.setMinHeight(ALTEZZA_MINIMA);

        // Centra la finestra sullo schermo
        stage.centerOnScreen();
    }
    
    /**
     * Configura l'interfaccia utente principale
     * Layout responsive professionale:
     * - Contenuto allineato a SINISTRA (tranne bottoni centrati)
     * - Spaziatura MINIMA per spostare tutto a sinistra
     */
    private void configuraInterfacciaUtente() {
        // Container principale con contenuto left-aligned
        VBox layoutPrincipale = new VBox(25);
        layoutPrincipale.setPadding(new Insets(30, 30, 30, 0)); // Padding sinistro a 0
        layoutPrincipale.getStyleClass().add("product-detail-container");
        layoutPrincipale.setAlignment(Pos.TOP_LEFT); // Contenuto a sinistra
        layoutPrincipale.setMaxWidth(Double.MAX_VALUE);
        layoutPrincipale.setFillWidth(true);

        // Contenitore responsive per immagine + info
        contenutoResponsive = new HBox(8); // Spazio minimo 8
        ((HBox)contenutoResponsive).setAlignment(Pos.TOP_LEFT); // Allineato a sinistra
        contenutoResponsive.getStyleClass().add("responsive-content");
        contenutoResponsive.setMaxWidth(Double.MAX_VALUE);

        sezioneImmagine = creaSezioneImmagine();
        sezioneInformazioni = creaSezioneInformazioni();

        contenutoResponsive.getChildren().addAll(sezioneImmagine, sezioneInformazioni);

        HBox sezioneAzioni = creaSezioneAzioni(); // Bottoni restano centrati

        layoutPrincipale.getChildren().addAll(contenutoResponsive, sezioneAzioni);

        // WRAP in ScrollPane per responsive design
        ScrollPane scrollPane = new ScrollPane(layoutPrincipale);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("detail-scroll-pane");
        scrollPane.setPannable(true);

        Scene scena = new Scene(scrollPane, LARGHEZZA_FINESTRA, ALTEZZA_FINESTRA);
        applicaStiliScena(scena);
        stage.setScene(scena);
    }
    
    /**
     * Applica gli stili CSS alla scena
     * 
     * @param scena La scena a cui applicare gli stili
     */
    private void applicaStiliScena(Scene scena) {
        try {
            scena.getStylesheets().add(getClass().getResource("/styles/dettagli-prodotto.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Errore nel caricamento fogli di stile: " + e.getMessage());
        }
    }
    
    /**
     * Crea la sezione intestazione con immagine e informazioni prodotto
     * Design professionale con allineamento perfetto
     *
     * @return HBox contenente la sezione immagine e informazioni
     */
    private HBox creaSezioneIntestazione() {
        HBox intestazione = new HBox(30); // Spazio aumentato per呼吸
        intestazione.setAlignment(Pos.TOP_CENTER); // Allineamento center per entrambi
        intestazione.setPadding(new Insets(10, 0, 10, 0));

        VBox sezioneImmagine = creaSezioneImmagine();
        VBox sezioneInformazioni = creaSezioneInformazioni();

        intestazione.getChildren().addAll(sezioneImmagine, sezioneInformazioni);
        return intestazione;
    }
    
    /**
     * Crea la sezione dedicata all'immagine del prodotto
     * 
     * @return VBox configurata con l'immagine del prodotto
     */
    private VBox creaSezioneImmagine() {
        VBox sezioneImmagine = new VBox();
        sezioneImmagine.setAlignment(Pos.CENTER);
        sezioneImmagine.setMinWidth(LARGHEZZA_IMMAGINE);
        
        configuraVisualizzatoreImmagine();
        sezioneImmagine.getChildren().add(visualizzatoreImmagine);
        return sezioneImmagine;
    }
    
    /**
     * Crea la sezione informazioni con tutti i dettagli del prodotto
     * Design professionale con contenuto allineato a SINISTRA
     *
     * @return VBox contenente tutti i dettagli informativi
     */
    private VBox creaSezioneInformazioni() {
        VBox sezioneInformazioni = new VBox(18);
        sezioneInformazioni.setAlignment(Pos.TOP_LEFT); // Allineato a sinistra
        sezioneInformazioni.setPadding(new Insets(5, 0, 0, 0));
        sezioneInformazioni.setMaxWidth(400); // Larghezza max per readability

        configuraInformazioniProdotto();
        Label badgeTipologia = creaBadgeTipologia();
        VBox informazioniConsegna = creaInformazioniConsegna();
        VBox informazioniVenditore = creaInformazioniVenditore();

        sezioneInformazioni.getChildren().addAll(
            testoNomeProdotto,
            testoPrezzoProdotto,
            badgeTipologia,
            testoDescrizioneProdotto,
            informazioniConsegna,
            informazioniVenditore
        );

        return sezioneInformazioni;
    }
    
    /**
     * Crea la sezione azioni con i pulsanti
     * Design professionale con 3 bottoni: Chiudi, Info (chat), Azione
     * Tutti centrati con dimensioni uniformi
     *
     * @return HBox contenente i pulsanti
     */
    private HBox creaSezioneAzioni() {
        HBox sezioneAzioni = new HBox(15); // Spazio tra bottoni
        sezioneAzioni.setAlignment(Pos.CENTER); // Bottoni rimangono centrati
        sezioneAzioni.getStyleClass().add("action-section");
        sezioneAzioni.setPadding(new Insets(15, 0, 5, 0));

        Button pulsanteChiudi = creaPulsanteChiudi();
        Button pulsanteInfo = creaPulsanteInfo(); // Nuovo bottone Info
        configuraPulsanteAzione();

        // Dimensioni uniformi per tutti i bottoni
        pulsanteChiudi.setPrefWidth(140);
        pulsanteChiudi.setPrefHeight(50);
        pulsanteInfo.setPrefWidth(140);
        pulsanteInfo.setPrefHeight(50);
        pulsanteAzione.setPrefWidth(200);
        pulsanteAzione.setPrefHeight(50);

        sezioneAzioni.getChildren().addAll(pulsanteChiudi, pulsanteInfo, pulsanteAzione);
        return sezioneAzioni;
    }
    
    /**
     * Configura le informazioni testuali del prodotto
     * Imposta titolo, prezzo formattato e descrizione con wrapping responsive
     */
    private void configuraInformazioniProdotto() {
        testoNomeProdotto.setText(ottieniNomeProdotto());
        testoNomeProdotto.getStyleClass().add("product-detail-title");

        testoPrezzoProdotto.setText(annuncio.getPrezzoFormattato());
        testoPrezzoProdotto.getStyleClass().add("product-detail-price");

        testoDescrizioneProdotto.setText(ottieniDescrizioneProdotto());
        testoDescrizioneProdotto.setWrappingWidth(350); // Aumentato per migliore responsive
        testoDescrizioneProdotto.getStyleClass().add("product-detail-description");
    }
    
    /**
     * Configura il visualizzatore immagine del prodotto
     * Imposta dimensioni, stile e carica l'immagine
     */
    private void configuraVisualizzatoreImmagine() {
        visualizzatoreImmagine.setFitWidth(LARGHEZZA_IMMAGINE);
        visualizzatoreImmagine.setFitHeight(ALTEZZA_IMMAGINE);
        visualizzatoreImmagine.setPreserveRatio(true);
        visualizzatoreImmagine.getStyleClass().add("product-detail-image");

        // Image loading handled by setupDialogImageCover() in constructor
    }
    
    /**
     * Crea e configura il pulsante di chiusura
     *
     * @return Button configurato per chiudere la finestra
     */
    private Button creaPulsanteChiudi() {
        Button pulsanteChiudi = new Button("Chiudi");
        pulsanteChiudi.getStyleClass().add("close-button");
        pulsanteChiudi.setOnAction(e -> stage.close());

        // Tooltip informativo
        Tooltip.install(pulsanteChiudi, new Tooltip("Chiudi questa finestra"));

        return pulsanteChiudi;
    }

    /**
     * Crea e configura il pulsante Info per aprire la chat con il venditore
     * Permette di chiedere informazioni aggiuntive sul prodotto
     *
     * @return Button configurato per aprire la chat
     */
    private Button creaPulsanteInfo() {
        Button pulsanteInfo = new Button("💬 Info");
        pulsanteInfo.getStyleClass().add("info-button");
        pulsanteInfo.setOnAction(e -> apriChatPerInfo());

        // Tooltip informativo
        Tooltip.install(pulsanteInfo, new Tooltip("Contatta il venditore per informazioni"));

        return pulsanteInfo;
    }

    /**
     * Apre la chat con il venditore per richiedere informazioni
     */
    private void apriChatPerInfo() {
        try {
            int currentUserId = SessionManager.getCurrentUserId();

            // Verifica se l'utente è loggato
            if (currentUserId == -1) {
                mostraAlert("Devi essere loggato per contattare il venditore");
                return;
            }

            // Verifica che non sia il venditore stesso
            if (currentUserId == annuncio.getVenditoreId()) {
                mostraAlert("Sei il venditore di questo annuncio");
                return;
            }

            // Crea messaggio iniziale per informazioni
            String messaggioIniziale = "Salve, vorrei delle informazioni in più su: " + annuncio.getTitolo();

            // Apre il dialog chat
            MessaggiDialog dialog = new MessaggiDialog(annuncio, currentUserId, messaggioIniziale);
            dialog.showAndWait();

        } catch (Exception e) {
            System.err.println("Errore nell'apertura della chat: " + e.getMessage());
            mostraAlert("Errore nell'apertura della chat. Riprova più tardi.");
        }
    }

    /**
     * Mostra un alert all'utente
     */
    private void mostraAlert(String messaggio) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    /**
     * Configura il pulsante di azione principale in base alla tipologia annuncio
     * Imposta testo, icona, tooltip e gestore eventi appropriati
     */
    private void configuraPulsanteAzione() {
        ConfigurazionePulsanteAzione config = ottieniConfigurazionePulsante();
        
        pulsanteAzione.setText(config.testo());
        
        if (config.icona() != null) {
            ImageView iconaView = new ImageView(config.icona());
            iconaView.setFitWidth(16);
            iconaView.setFitHeight(16);
            pulsanteAzione.setGraphic(iconaView);
        }
        
        pulsanteAzione.getStyleClass().add("action-button");
        pulsanteAzione.setOnAction(e -> gestisciAzionePrincipale());
        
        // Tooltip contestuale
        Tooltip.install(pulsanteAzione, new Tooltip(config.tooltip()));
    }
    
    /**
     * Crea il badge della tipologia con stile appropriato
     * 
     * @return Label configurata come badge di tipologia
     */
    private Label creaBadgeTipologia() {
        Label badgeTipologia = new Label(annuncio.getTipologia().toString());
        badgeTipologia.getStyleClass().addAll("badge", ottieniStileBadge(annuncio.getTipologia()));
        return badgeTipologia;
    }
    
    /**
     * Crea la sezione informazioni di consegna
     * Contenuto allineato a sinistra
     *
     * @return VBox con dettagli località e metodo consegna
     */
    private VBox creaInformazioniConsegna() {
        VBox informazioniConsegna = new VBox(5);
        informazioniConsegna.setAlignment(Pos.TOP_LEFT); // Allineato a sinistra
        informazioniConsegna.getStyleClass().add("delivery-info");

        Text titolo = creaTitoloSezione("Modalità di consegna:");
        Text localita = new Text("📍 " + ottieniLocalitaConsegna());
        Text metodo = new Text("🚚 " + ottieniMetodoConsegna());

        informazioniConsegna.getChildren().addAll(titolo, localita, metodo);
        return informazioniConsegna;
    }

    /**
     * Crea la sezione informazioni venditore
     * Contenuto allineato a sinistra
     *
     * @return VBox con dettagli identificativi venditore
     */
    private VBox creaInformazioniVenditore() {
        VBox informazioniVenditore = new VBox(5);
        informazioniVenditore.setAlignment(Pos.TOP_LEFT); // Allineato a sinistra
        informazioniVenditore.getStyleClass().add("seller-info");

        Text titolo = creaTitoloSezione("Informazioni venditore:");
        Text nome = new Text("👤 " + annuncio.getNomeUtenteVenditore());

        informazioniVenditore.getChildren().addAll(titolo, nome);
        return informazioniVenditore;
    }
    
    /**
     * Crea un titolo di sezione con stile predefinito
     * 
     * @param testo Il testo da visualizzare come titolo
     * @return Text configurato come titolo di sezione
     */
    private Text creaTitoloSezione(String testo) {
        Text titolo = new Text(testo);
        titolo.getStyleClass().add("section-title");
        return titolo;
    }
    
    /**
     * Carica un'icona in modo sicuro con gestione errori
     * 
     * @param percorso Il percorso della risorsa icona
     * @return L'immagine caricata o null in caso di errore
     */
    private static Image caricaIcona(String percorso) {
        try {
            return new Image(DettagliProdottoView.class.getResourceAsStream(percorso));
        } catch (Exception e) {
            System.err.println("Errore nel caricamento icona: " + percorso + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Carica l'immagine del prodotto con gestione robusta degli errori
     * Utilizza l'immagine di default in caso di problemi
     */
    private void caricaImmagineProdotto() {
        try {
            String urlImmagine = annuncio.getImageUrlSafe();
            
            if (urlImmagine != null && !urlImmagine.isEmpty() && !urlImmagine.equals("null")) {
                caricaImmagineDaUrl(urlImmagine);
            } else {
                usaImmagineDefault();
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento immagine prodotto: " + e.getMessage());
            usaImmagineDefault();
        }
    }
    
    /**
     * Carica un'immagine da URL con supporto per diversi formati
     *
     * @param urlImmagine L'URL o percorso dell'immagine
     */
    private void caricaImmagineDaUrl(String urlImmagine) {
        try {
            Image image;
            if (urlImmagine.startsWith("file:")) {
                // URL file system diretto
                image = new Image(urlImmagine);
            } else if (urlImmagine.startsWith("/")) {
                // Percorso assoluto file system
                image = new Image("file:" + urlImmagine);
            } else {
                // Tentativo come risorsa interna
                image = new Image(getClass().getResourceAsStream(urlImmagine));
            }

            if (!image.isError()) {
                applyObjectFitCover(visualizzatoreImmagine, image, LARGHEZZA_IMMAGINE, ALTEZZA_IMMAGINE);
            } else {
                usaImmagineDefault();
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento immagine da URL: " + urlImmagine);
            usaImmagineDefault();
        }
    }
    
    /**
     * Utilizza l'immagine di default come fallback
     */
    private void usaImmagineDefault() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream(PERCORSO_IMMAGINE_DEFAULT));
            applyObjectFitCover(visualizzatoreImmagine, defaultImage, LARGHEZZA_IMMAGINE, ALTEZZA_IMMAGINE);
        } catch (Exception e) {
            System.err.println("Errore critico: impossibile caricare l'immagine default");
        }
    }

    /**
     * Apply object-fit: cover effect to dialog image
     * Ensures image covers entire container without distortion
     */
    private void applyObjectFitCover(javafx.scene.image.ImageView imageView, Image image, double containerWidth, double containerHeight) {
        if (image.isError() || image.getWidth() == 0 || image.getHeight() == 0) {
            imageView.setImage(image);
            imageView.setFitWidth(containerWidth);
            imageView.setFitHeight(containerHeight);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return;
        }

        imageView.setImage(image);
        imageView.setSmooth(true);

        double imageRatio = image.getWidth() / image.getHeight();
        double containerRatio = containerWidth / containerHeight;

        if (imageRatio > containerRatio) {
            imageView.setFitHeight(containerHeight);
            double scaledWidth = containerHeight * imageRatio;
            imageView.setFitWidth(scaledWidth);
            double x = (containerWidth - scaledWidth) / 2;
            imageView.setX(x);
            imageView.setY(0);
        } else {
            imageView.setFitWidth(containerWidth);
            double scaledHeight = containerWidth / imageRatio;
            imageView.setFitHeight(scaledHeight);
            double y = (containerHeight - scaledHeight) / 2;
            imageView.setX(0);
            imageView.setY(y);
        }

        imageView.setPreserveRatio(false);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(containerWidth, containerHeight);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);
    }

    /**
     * Setup dialog image with object-fit: cover
     * Ensures large product image covers entire area
     */
    private void setupDialogImageCover() {
        String urlImmagine = annuncio.getImageUrlSafe();

        if (urlImmagine != null && !urlImmagine.isEmpty() && !urlImmagine.equals("null")) {
            try {
                Image image;
                if (urlImmagine.startsWith("file:")) {
                    image = new Image(urlImmagine);
                } else if (urlImmagine.startsWith("/")) {
                    image = new Image("file:" + urlImmagine);
                } else {
                    image = new Image(getClass().getResourceAsStream(urlImmagine));
                }

                if (!image.isError()) {
                    applyObjectFitCover(visualizzatoreImmagine, image, LARGHEZZA_IMMAGINE, ALTEZZA_IMMAGINE);
                } else {
                    usaImmagineDefault();
                }
            } catch (Exception e) {
                usaImmagineDefault();
            }
        } else {
            usaImmagineDefault();
        }
    }
    
    /**
     * Gestisce l'azione principale del pulsante in base alla tipologia
     * Delegà all'handler appropriato e chiude la finestra
     */
    private void gestisciAzionePrincipale() {
        try {
            AzioneAnnuncioHandler handler = new AzioneAnnuncioHandler();
            handler.gestisciAzione(annuncio);
            stage.close();
        } catch (Exception e) {
            System.err.println("Errore durante l'esecuzione dell'azione: " + e.getMessage());
            // Qui potresti mostrare un alert all'utente
        }
    }
    
    /**
     * Ottiene la configurazione del pulsante in base alla tipologia annuncio
     * 
     * @return ConfigurazionePulsanteAzione appropriata
     */
    private ConfigurazionePulsanteAzione ottieniConfigurazionePulsante() {
        return switch (annuncio.getTipologia()) {
            case VENDITA -> new ConfigurazionePulsanteAzione(
                "Acquista", 
                ICONA_CARRELLO, 
                "Procedi all'acquisto diretto del prodotto"
            );
            case SCAMBIO -> new ConfigurazionePulsanteAzione(
                "Proponi scambio", 
                ICONA_SCAMBIO, 
                "Proponi un oggetto in scambio per questo prodotto"
            );
            case REGALO -> new ConfigurazionePulsanteAzione(
                "Contatta", 
                ICONA_MESSAGGIO, 
                "Contatta il donatore per ricevere il prodotto"
            );
            case ASTA -> new ConfigurazionePulsanteAzione(
                "Partecipa all'asta", 
                ICONA_ASTA, 
                "Partecipa all'asta per questo prodotto"
            );
            default -> new ConfigurazionePulsanteAzione(
                "Azione", 
                null, 
                "Azione principale per questo annuncio"
            );
        };
    }
    
    /**
     * Restituisce lo stile CSS per il badge in base alla tipologia
     * 
     * @param tipologia La tipologia dell'annuncio
     * @return Il nome della classe CSS appropriata
     */
    private String ottieniStileBadge(Tipologia tipologia) {
        return switch (tipologia) {
            case VENDITA -> STILE_BADGE_VENDITA;
            case SCAMBIO -> STILE_BADGE_SCAMBIO;
            case REGALO -> STILE_BADGE_REGALO;
            case ASTA -> STILE_BADGE_ASTA;
            default -> STILE_BADGE_VENDITA;
        };
    }
    
    /**
     * Ottiene il nome del prodotto in modo sicuro con fallback
     * 
     * @return Il nome del prodotto o testo default
     */
    private String ottieniNomeProdotto() {
        if (annuncio.getTitolo() != null && !annuncio.getTitolo().isEmpty()) {
            return annuncio.getTitolo();
        } else if (annuncio.getOggetto() != null && annuncio.getOggetto().getNome() != null) {
            return annuncio.getOggetto().getNome();
        } else {
            return "Prodotto senza nome";
        }
    }
    
    /**
     * Ottiene la descrizione del prodotto in modo sicuro con fallback
     * 
     * @return La descrizione del prodotto o testo default
     */
    private String ottieniDescrizioneProdotto() {
        if (annuncio.getDescrizione() != null && !annuncio.getDescrizione().isEmpty()) {
            return annuncio.getDescrizione();
        } else if (annuncio.getOggetto() != null && annuncio.getOggetto().getDescrizione() != null) {
            return annuncio.getOggetto().getDescrizione();
        } else {
            return "Nessuna descrizione disponibile per questo prodotto.";
        }
    }
    
    /**
     * Ottiene la località di consegna in modo sicuro con fallback
     * 
     * @return La località di consegna o testo default
     */
    private String ottieniLocalitaConsegna() {
        if (annuncio.getSedeConsegna() != null && !annuncio.getSedeConsegna().isEmpty()) {
            return annuncio.getSedeConsegna();
        } else if (annuncio.getCitta() != null && !annuncio.getCitta().isEmpty()) {
            return annuncio.getCitta();
        } else {
            return "Località non specificata";
        }
    }
    
    /**
     * Ottiene il metodo di consegna in modo sicuro con fallback
     * 
     * @return Il metodo di consegna o testo default
     */
    private String ottieniMetodoConsegna() {
        if (annuncio.getModalitaConsegna() != null && !annuncio.getModalitaConsegna().isEmpty()) {
            return annuncio.getModalitaConsegna();
        } else {
            return "Da concordare con il venditore";
        }
    }
    
    /**
     * Mostra la finestra modale dei dettagli prodotto
     */
    public void mostra() {
        if (!stage.isShowing()) {
            stage.show();
        }
    }
    
    /**
     * Chiude la finestra modale
     */
    public void chiudi() {
        if (stage.isShowing()) {
            stage.close();
        }
    }
    
    /**
     * Verifica se la finestra è attualmente visibile
     * 
     * @return true se la finestra è visibile, false altrimenti
     */
    public boolean isVisibile() {
        return stage.isShowing();
    }
    
    /**
     * Restituisce l'annuncio associato a questa vista
     *
     * @return L'annuncio visualizzato
     */
    public Annuncio getAnnuncio() {
        return annuncio;
    }

    /**
     * Setup responsive dialog layout based on window width
     * Mobile (<768px): vertical stack, Desktop (≥768px): horizontal
     */
    private void setupResponsiveDialog() {
        stage.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();

            if (width < 768 && !(contenutoResponsive instanceof VBox)) {
                // Mobile: switch to vertical layout
                switchToVerticalLayout();
            } else if (width >= 768 && !(contenutoResponsive instanceof HBox)) {
                // Desktop: switch to horizontal layout
                switchToHorizontalLayout();
            }
        });
    }

    /**
     * Switch to vertical layout for mobile
     */
    private void switchToVerticalLayout() {
        VBox parent = (VBox) contenutoResponsive.getParent();
        int index = parent.getChildren().indexOf(contenutoResponsive);

        VBox verticalLayout = new VBox(16);
        verticalLayout.setAlignment(Pos.TOP_LEFT);
        verticalLayout.getStyleClass().add("responsive-content");
        verticalLayout.setMaxWidth(Double.MAX_VALUE);

        verticalLayout.getChildren().addAll(sezioneImmagine, sezioneInformazioni);

        parent.getChildren().set(index, verticalLayout);
        contenutoResponsive = verticalLayout;
    }

    /**
     * Switch to horizontal layout for desktop
     */
    private void switchToHorizontalLayout() {
        VBox parent = (VBox) contenutoResponsive.getParent();
        int index = parent.getChildren().indexOf(contenutoResponsive);

        HBox horizontalLayout = new HBox(8);
        horizontalLayout.setAlignment(Pos.TOP_LEFT);
        horizontalLayout.getStyleClass().add("responsive-content");
        horizontalLayout.setMaxWidth(Double.MAX_VALUE);

        horizontalLayout.getChildren().addAll(sezioneImmagine, sezioneInformazioni);

        parent.getChildren().set(index, horizontalLayout);
        contenutoResponsive = horizontalLayout;
    }
    
    /**
     * Record per la configurazione del pulsante di azione principale
     * 
     * @param testo Il testo visualizzato sul pulsante
     * @param icona L'icona associata al pulsante (può essere null)
     * @param tooltip Il testo del tooltip esplicativo
     */
    private record ConfigurazionePulsanteAzione(String testo, Image icona, String tooltip) {}
}