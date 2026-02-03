package schermata.button;

import application.DB.ScambioDAO;
import application.DB.SessionManager;
import application.Classe.Scambio;
import application.Classe.utente;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog per visualizzare e gestire gli scambi dell'utente.
 *
 * Mostra tutti gli scambi (proposti e ricevuti) con la possibilità di:
 * - Accettare o rifiutare scambi ricevuti
 * - Annullare scambi proposti
 * - Completare scambi accettati
 *
 * @author SwapUnina Development Team
 * @version 1.0
 */
public class GestioneScambiDialog extends Dialog<Void> {

    private static final Logger LOGGER = Logger.getLogger(GestioneScambiDialog.class.getName());

    private final ScambioDAO scambioDAO;
    private final int utenteId;
    private VBox content;

    /**
     * Crea un nuovo dialog per gestire gli scambi
     */
    public GestioneScambiDialog() {
        this.scambioDAO = new ScambioDAO();
        this.utenteId = SessionManager.getCurrentUserId();

        if (utenteId == -1) {
            throw new IllegalStateException("Nessun utente loggato");
        }

        setTitle("💫 I Miei Scambi");
        setHeaderText("Gestisci le tue proposte di scambio");

        // Crea tabella se non esiste
        scambioDAO.creaTabellaSeMancante();

        inizializzaUI();
        caricaScambi();
    }

    /**
     * Inizializza i componenti dell'interfaccia
     */
    private void inizializzaUI() {
        content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(700);
        content.setPrefHeight(500);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        getDialogPane().setContent(scrollPane);

        ButtonType chiudiButtonType = new ButtonType("Chiudi", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(chiudiButtonType);
    }

    /**
     * Carica e visualizza gli scambi dell'utente
     */
    private void caricaScambi() {
        content.getChildren().clear();

        Text titolo = new Text("I tuoi scambi");
        titolo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        content.getChildren().add(titolo);

        List<Scambio> scambi = scambioDAO.getScambiPerUtente(utenteId);

        if (scambi.isEmpty()) {
            Text nessunScambio = new Text("Nessuno scambio presente");
            nessunScambio.setStyle("-fx-font-size: 14px; -fx-fill: #666;");
            content.getChildren().add(new Separator());
            content.getChildren().add(nessunScambio);
            return;
        }

        // Separa scambi proposti e ricevuti
        for (Scambio scambio : scambi) {
            content.getChildren().add(creaCardScambio(scambio));
            content.getChildren().add(new Separator());
        }
    }

    /**
     * Crea una card per visualizzare uno scambio
     */
    private VBox creaCardScambio(Scambio scambio) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 8px;");

        // Titolo con stato
        HBox header = new HBox(15);
        header.setStyle("-fx-alignment: center-left;");

        Text tipo = new Text();
        boolean isProprietario = scambio.getNomeProprietario() != null &&
                                 SessionManager.getCurrentUser() != null &&
                                 SessionManager.getCurrentUser().getNome().equals(scambio.getNomeProprietario());

        if (isProprietario) {
            tipo.setText("📥 RICEVUTO da " + (scambio.getNomeRichiedente() != null ? scambio.getNomeRichiedente() : "Utente"));
        } else {
            tipo.setText("📤 PROPOSTO a " + (scambio.getNomeProprietario() != null ? scambio.getNomeProprietario() : "Utente"));
        }
        tipo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Text stato = new Text(scambio.getDescrizioneStato());
        stato.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        header.getChildren().addAll(tipo, stato);
        card.getChildren().add(header);

        // Dettagli scambio
        StringBuilder dettagli = new StringBuilder();
        dettagli.append("📦 Articolo richiesto: ").append(scambio.getTitoloAnnuncioRichiesto() != null ?
                scambio.getTitoloAnnuncioRichiesto() : "N/D").append("\n");

        if (scambio.getTitoloAnnuncioOfferto() != null) {
            dettagli.append("📦 Articolo offerto: ").append(scambio.getTitoloAnnuncioOfferto()).append("\n");
        }

        if (scambio.getMessaggio() != null && !scambio.getMessaggio().isEmpty()) {
            dettagli.append("💬 Messaggio: ").append(scambio.getMessaggio()).append("\n");
        }

        dettagli.append("📅 Proposta il: ").append(scambio.getDataProposta().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        Text dettagliText = new Text(dettagli.toString());
        dettagliText.setStyle("-fx-font-size: 12px;");
        card.getChildren().add(dettagliText);

        // Pulsanti azione
        HBox pulsanti = creaPulsantiAzione(scambio, isProprietario);
        if (pulsanti != null) {
            card.getChildren().add(pulsanti);
        }

        return card;
    }

    /**
     * Crea i pulsanti di azione in base allo stato dello scambio
     */
    private HBox creaPulsantiAzione(Scambio scambio, boolean isProprietario) {
        HBox pulsanti = new HBox(10);
        pulsanti.setStyle("-fx-alignment: center-left;");

        switch (scambio.getStato()) {
            case IN_ATTESA:
                if (isProprietario) {
                    // Il proprietario dell'annuncio richiesto può accettare/rifiutare
                    Button accettaButton = new Button("✅ Accetta");
                    accettaButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                    accettaButton.setOnAction(e -> gestisciAccettazione(scambio));

                    Button rifiutaButton = new Button("🚫 Rifiuta");
                    rifiutaButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    rifiutaButton.setOnAction(e -> gestisciRifiuto(scambio));

                    pulsanti.getChildren().addAll(accettaButton, rifiutaButton);
                } else {
                    // Il richiedente può annullare
                    Button annullaButton = new Button("❌ Annulla Proposta");
                    annullaButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                    annullaButton.setOnAction(e -> gestisciAnnullamento(scambio));

                    pulsanti.getChildren().add(annullaButton);
                }
                break;

            case ACCETTATO:
                if (isProprietario) {
                    // Il proprietario può completare lo scambio
                    Button completaButton = new Button("🎉 Completa Scambio");
                    completaButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    completaButton.setOnAction(e -> gestisciCompletamento(scambio));

                    pulsanti.getChildren().add(completaButton);
                } else {
                    Text info = new Text("⏳ In attesa che l'altro utente completi lo scambio");
                    info.setStyle("-fx-font-style: italic; -fx-fill: #666;");
                    pulsanti.getChildren().add(info);
                }
                break;

            case COMPLETATO:
                Text completatoText = new Text("✅ Scambio completato!");
                completatoText.setStyle("-fx-font-weight: bold; -fx-fill: #27ae60;");
                pulsanti.getChildren().add(completatoText);
                break;

            case RIFIUTATO:
            case ANNULLATO:
                Text chiusoText = new Text("🚫 " + (scambio.getStato() == application.Enum.StatoScambio.RIFIUTATO ? "Scambio rifiutato" : "Scambio annullato"));
                chiusoText.setStyle("-fx-font-style: italic; -fx-fill: #e74c3c;");
                pulsanti.getChildren().add(chiusoText);
                break;
        }

        return pulsanti;
    }

    /**
     * Gestisce l'accettazione di uno scambio
     */
    private void gestisciAccettazione(Scambio scambio) {
        boolean confermato = mostraConferma(
                "Accettare Scambio?",
                "Vuoi accettare lo scambio proposto?\n\n" +
                        "Articolo richiesto: " + scambio.getTitoloAnnuncioRichiesto() +
                        (scambio.getTitoloAnnuncioOfferto() != null ?
                                "\nArticolo offerto: " + scambio.getTitoloAnnuncioOfferto() : "")
        );

        if (confermato) {
            boolean successo = scambioDAO.accettaScambio(scambio.getId());
            if (successo) {
                mostraMessaggio("✅ Scambio accettato!");
                caricaScambi(); // Ricarica la lista
            } else {
                mostraMessaggio("❌ Impossibile accettare lo scambio");
            }
        }
    }

    /**
     * Gestisce il rifiuto di uno scambio
     */
    private void gestisciRifiuto(Scambio scambio) {
        boolean confermato = mostraConferma(
                "Rifiutare Scambio?",
                "Sei sicuro di voler rifiutare questa proposta di scambio?"
        );

        if (confermato) {
            boolean successo = scambioDAO.rifiutaScambio(scambio.getId());
            if (successo) {
                mostraMessaggio("🚫 Scambio rifiutato");
                caricaScambi(); // Ricarica la lista
            } else {
                mostraMessaggio("❌ Impossibile rifiutare lo scambio");
            }
        }
    }

    /**
     * Gestisce l'annullamento di uno scambio
     */
    private void gestisciAnnullamento(Scambio scambio) {
        boolean confermato = mostraConferma(
                "Annullare Proposta?",
                "Sei sicuro di voler annullare la tua proposta di scambio?"
        );

        if (confermato) {
            boolean successo = scambioDAO.annullaScambio(scambio.getId());
            if (successo) {
                mostraMessaggio("❌ Proposta annullata");
                caricaScambi(); // Ricarica la lista
            } else {
                mostraMessaggio("❌ Impossibile annullare la proposta");
            }
        }
    }

    /**
     * Gestisce il completamento di uno scambio
     */
    private void gestisciCompletamento(Scambio scambio) {
        boolean confermato = mostraConferma(
                "Completare Scambio?",
                "Confermi di aver effettuato lo scambio?\n\n" +
                        "Gli annunci coinvolti verranno marcati come VENDUTI."
        );

        if (confermato) {
            boolean successo = scambioDAO.completaScambio(scambio.getId());
            if (successo) {
                mostraMessaggio("🎉 Scambio completato con successo!");
                caricaScambi(); // Ricarica la lista
            } else {
                mostraMessaggio("❌ Impossibile completare lo scambio");
            }
        }
    }

    /**
     * Mostra un dialog di conferma
     */
    private boolean mostraConferma(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Mostra un messaggio informativo
     */
    private void mostraMessaggio(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
