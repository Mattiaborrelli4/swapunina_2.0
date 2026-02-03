package application.Classe;

import application.Enum.StatoScambio;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rappresenta una proposta di scambio tra utenti.
 *
 * <p>Un utente può proporre di scambiare il proprio annuncio con quello di un altro utente.</p>
 *
 * @author SwapUnina Development Team
 * @version 1.0
 */
public class Scambio {

    private int id;
    private int richiedenteId;
    private int annuncioRichiestoId;
    private Integer annuncioOffertoId;  // null se lo scambio è diretto senza contropartita
    private StatoScambio stato;
    private LocalDateTime dataProposta;
    private LocalDateTime dataAccettazione;
    private LocalDateTime dataCompletamento;
    private String messaggio;

    // Campi aggiuntivi per visualizzazione (non salvati nel DB)
    private String titoloAnnuncioRichiesto;
    private String titoloAnnuncioOfferto;
    private String nomeRichiedente;
    private String nomeProprietario;

    /**
     * Costruttore di default
     */
    public Scambio() {
        this.stato = StatoScambio.IN_ATTESA;
        this.dataProposta = LocalDateTime.now();
    }

    /**
     * Costruttore completo
     */
    public Scambio(int richiedenteId, int annuncioRichiestoId, Integer annuncioOffertoId, String messaggio) {
        this();
        this.richiedenteId = richiedenteId;
        this.annuncioRichiestoId = annuncioRichiestoId;
        this.annuncioOffertoId = annuncioOffertoId;
        this.messaggio = messaggio;
    }

    // ========== GETTER E SETTER ==========

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRichiedenteId() {
        return richiedenteId;
    }

    public void setRichiedenteId(int richiedenteId) {
        this.richiedenteId = richiedenteId;
    }

    public int getAnnuncioRichiestoId() {
        return annuncioRichiestoId;
    }

    public void setAnnuncioRichiestoId(int annuncioRichiestoId) {
        this.annuncioRichiestoId = annuncioRichiestoId;
    }

    public Integer getAnnuncioOffertoId() {
        return annuncioOffertoId;
    }

    public void setAnnuncioOffertoId(Integer annuncioOffertoId) {
        this.annuncioOffertoId = annuncioOffertoId;
    }

    public StatoScambio getStato() {
        return stato;
    }

    public void setStato(StatoScambio stato) {
        this.stato = stato;
    }

    public LocalDateTime getDataProposta() {
        return dataProposta;
    }

    public void setDataProposta(LocalDateTime dataProposta) {
        this.dataProposta = dataProposta;
    }

    public LocalDateTime getDataAccettazione() {
        return dataAccettazione;
    }

    public void setDataAccettazione(LocalDateTime dataAccettazione) {
        this.dataAccettazione = dataAccettazione;
    }

    public LocalDateTime getDataCompletamento() {
        return dataCompletamento;
    }

    public void setDataCompletamento(LocalDateTime dataCompletamento) {
        this.dataCompletamento = dataCompletamento;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public String getTitoloAnnuncioRichiesto() {
        return titoloAnnuncioRichiesto;
    }

    public void setTitoloAnnuncioRichiesto(String titoloAnnuncioRichiesto) {
        this.titoloAnnuncioRichiesto = titoloAnnuncioRichiesto;
    }

    public String getTitoloAnnuncioOfferto() {
        return titoloAnnuncioOfferto;
    }

    public void setTitoloAnnuncioOfferto(String titoloAnnuncioOfferto) {
        this.titoloAnnuncioOfferto = titoloAnnuncioOfferto;
    }

    public String getNomeRichiedente() {
        return nomeRichiedente;
    }

    public void setNomeRichiedente(String nomeRichiedente) {
        this.nomeRichiedente = nomeRichiedente;
    }

    public String getNomeProprietario() {
        return nomeProprietario;
    }

    public void setNomeProprietario(String nomeProprietario) {
        this.nomeProprietario = nomeProprietario;
    }

    // ========== METODI DI UTILITY ==========

    /**
     * Verifica se lo scambio è in attesa
     */
    public boolean isInAttesa() {
        return stato == StatoScambio.IN_ATTESA;
    }

    /**
     * Verifica se lo scambio è stato accettato
     */
    public boolean isAccettato() {
        return stato == StatoScambio.ACCETTATO;
    }

    /**
     * Verifica se lo scambio è stato completato
     */
    public boolean isCompletato() {
        return stato == StatoScambio.COMPLETATO;
    }

    /**
     * Verifica se lo scambio è stato rifiutato
     */
    public boolean isRifiutato() {
        return stato == StatoScambio.RIFIUTATO;
    }

    /**
     * Verifica se lo scambio è uno scambio reciproco (con contropartita)
     */
    public boolean isScambioReciproco() {
        return annuncioOffertoId != null;
    }

    /**
     * Restituisce una descrizione testuale dello stato
     */
    public String getDescrizioneStato() {
        switch (stato) {
            case IN_ATTESA:
                return "⏳ In attesa di risposta";
            case ACCETTATO:
                return "✅ Accettato";
            case COMPLETATO:
                return "🎉 Completato";
            case ANNULLATO:
                return "❌ Annullato";
            case RIFIUTATO:
                return "🚫 Rifiutato";
            default:
                return stato.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scambio scambio = (Scambio) o;
        return id == scambio.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Scambio{" +
                "id=" + id +
                ", richiedenteId=" + richiedenteId +
                ", annuncioRichiestoId=" + annuncioRichiestoId +
                ", annuncioOffertoId=" + annuncioOffertoId +
                ", stato=" + stato +
                ", dataProposta=" + dataProposta +
                '}';
    }
}
