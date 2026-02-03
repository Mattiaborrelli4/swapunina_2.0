package schermata.button;

import application.DB.AnnuncioDAO;
import application.DB.ScambioDAO;
import application.DB.SessionManager;
import application.Classe.Annuncio;
import application.Classe.Scambio;
import application.Classe.utente;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog per proporre uno scambio per un annuncio.
 *
 * @author SwapUnina Development Team
 * @version 1.0
 */
public class PropostaScambioDialog extends Dialog<Integer> {

    private static final Logger LOGGER = Logger.getLogger(PropostaScambioDialog.class.getName());

    private final int annuncioRichiestoId;
    private final AnnuncioDAO annuncioDAO;
    private final ScambioDAO scambioDAO;
    private final int utenteId;

    private ComboBox<Annuncio> mieiAnnunciCombo;
    private TextArea messaggioArea;

    /**
     * Crea un nuovo dialog per proporre uno scambio
     *
     * @param annuncioRichiestoId ID dell'annuncio che si vuole ottenere
     */
    public PropostaScambioDialog(int annuncioRichiestoId) {
        this.annuncioRichiestoId = annuncioRichiestoId;
        this.annuncioDAO = new AnnuncioDAO();
        this.scambioDAO = new ScambioDAO();
        this.utenteId = SessionManager.getCurrentUser() != null ?
                SessionManager.getCurrentUser().getId() : -1;

        if (utenteId == -1) {
            throw new IllegalStateException("Nessun utente loggato");
        }

        // Verifica se l'utente può proporre lo scambio
        if (!scambioDAO.puoProporreScambio(utenteId, annuncioRichiestoId)) {
            throw new IllegalArgumentException("Non puoi proporre uno scambio per i tuoi annunci");
        }

        setTitle("💫 Proponi Scambio");
        setHeaderText("Seleziona un annuncio da offrire in scambio");

        inizializzaUI();
        configuraPulsanti();
    }

    /**
     * Inizializza i componenti dell'interfaccia
     */
    private void inizializzaUI() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Informazioni
        Label infoLabel = new Label("Puoi offrire uno dei tuoi annunci in scambio, o lasciare vuoto per uno scambio diretto:");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-text-fill: #666;");

        // Combo per selezionare i propri annunci
        Label labelMieiAnnunci = new Label("I miei annunci (opzionale):");
        labelMieiAnnunci.setStyle("-fx-font-weight: bold;");

        mieiAnnunciCombo = new ComboBox<>();
        mieiAnnunciCombo.setPromptText("Nessun annuncio selezionato (scambio diretto)");
        mieiAnnunciCombo.setPrefWidth(400);

        // Carica i propri annunci
        caricaMieiAnnunci();

        // Area messaggio
        Label labelMessaggio = new Label("Messaggio per il venditore (opzionale):");
        labelMessaggio.setStyle("-fx-font-weight: bold;");

        messaggioArea = new TextArea();
        messaggioArea.setPromptText("Scrivi un messaggio per presentarti...");
        messaggioArea.setPrefRowCount(4);
        messaggioArea.setWrapText(true);

        content.getChildren().addAll(
                infoLabel,
                labelMieiAnnunci,
                mieiAnnunciCombo,
                labelMessaggio,
                messaggioArea
        );

        getDialogPane().setContent(content);
    }

    /**
     * Configura i pulsanti del dialog
     */
    private void configuraPulsanti() {
        ButtonType proponiButtonType = new ButtonType("💫 Proponi Scambio", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaButtonType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        getDialogPane().getButtonTypes().addAll(proponiButtonType, annullaButtonType);

        setResultConverter(dialogButton -> {
            if (dialogButton == proponiButtonType) {
                return proponiScambio();
            }
            return null;
        });
    }

    /**
     * Carica gli annunci dell'utente nella combo
     */
    private void caricaMieiAnnunci() {
        try {
            List<Annuncio> mieiAnnunci = annuncioDAO.getAnnunciPerVenditore(utenteId);

            // Aggiungi solo annunci attivi e non venduti
            mieiAnnunci.stream()
                    .filter(a -> "ATTIVO".equals(a.getStato()))
                    .forEach(a -> mieiAnnunciCombo.getItems().add(a));

            LOGGER.log(Level.INFO, "Caricati {0} annunci per lo scambio", mieiAnnunciCombo.getItems().size());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento annunci: {0}", e.getMessage());
            mostraAlert(Alert.AlertType.ERROR, "Errore", "Impossibile caricare i tuoi annunci");
        }
    }

    /**
     * Propongono lo scambio
     *
     * @return ID dello scambio creato, null se fallisce
     */
    private Integer proponiScambio() {
        Annuncio annuncioOfferto = mieiAnnunciCombo.getValue();
        String messaggio = messaggioArea.getText();

        if (messaggio != null && messaggio.trim().isEmpty()) {
            messaggio = null;
        }

        Integer annuncioOffertoId = null;
        if (annuncioOfferto != null) {
            annuncioOffertoId = annuncioOfferto.getId();
        }

        try {
            int scambioId = scambioDAO.proponiScambio(
                    utenteId,
                    annuncioRichiestoId,
                    annuncioOffertoId,
                    messaggio
            );

            if (scambioId != -1) {
                String tipoScambio = annuncioOffertoId != null ? "con scambio reciproco" : "diretto";
                LOGGER.log(Level.INFO, "✅ Scambio {0} proposto con successo", tipoScambio);

                Platform.runLater(() -> {
                    mostraAlert(Alert.AlertType.INFORMATION,
                            "Scambio Proposto!",
                            "La tua proposta di scambio è stata inviata al venditore.");
                });

                return scambioId;
            } else {
                mostraAlert(Alert.AlertType.ERROR,
                        "Errore",
                        "Impossibile proporre lo scambio. Riprova.");
                return null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore proposta scambio: {0}", e.getMessage());
            mostraAlert(Alert.AlertType.ERROR,
                    "Errore",
                    "Si è verificato un errore: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mostra un alert
     */
    private void mostraAlert(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
