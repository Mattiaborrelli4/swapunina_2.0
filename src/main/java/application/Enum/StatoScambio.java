package application.Enum;

/**
 * Stati possibili di una proposta di scambio.
 *
 * @author SwapUnina Development Team
 * @version 1.0
 */
public enum StatoScambio {

    /**
     * Scambio proposto ma non ancora gestito dal destinatario
     */
    IN_ATTESA,

    /**
     * Scambio accettato dal destinatario, in attesa di completamento
     */
    ACCETTATO,

    /**
     * Scambio rifiutato dal destinatario
     */
    RIFIUTATO,

    /**
     * Scambio annullato dal richiedente prima dell'accettazione
     */
    ANNULLATO,

    /**
     * Scambio completato con successo, oggetti scambiati
     */
    COMPLETATO
}
