package application.Classe;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Versione estesa di Offerta con supporto per negoziazioni e contro-offerte
 * Questa classe estende le funzionalità della base Offerta per gestire
 * il ciclo completo di negoziazione tra acquirente e venditore
 */
public class OffertaNegoziazione {
    private int id;
    private int annuncioId;
    private int offerenteId;
    private int venditoreId;
    private double importo;
    private String messaggio;
    private StatoOfferta stato;
    private TipoOfferta tipo;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataRisposta;
    private Integer offertaPadreId; // Per contro-offerte
    private boolean notificata;

    public enum StatoOfferta {
        IN_ATTESA,      // In attesa di risposta
        ACCETTATA,      // Offerta accettata
        RIFIUTATA,      // Offerta rifiutata
        RITIRATA,       // Ritirata dall'offerente
        SCADUTA,        // Scaduta (tempo limite superato)
        CONTROFFERTA    // Contro-offerta pendente
    }

    public enum TipoOfferta {
        INIZIALE,       // Prima offerta
        CONTRO_OFFERTA  // Risposta a un'offerta
    }

    // Costruttori
    public OffertaNegoziazione() {
        this.dataCreazione = LocalDateTime.now();
        this.stato = StatoOfferta.IN_ATTESA;
        this.tipo = TipoOfferta.INIZIALE;
        this.notificata = false;
    }

    public OffertaNegoziazione(int annuncioId, int offerenteId, int venditoreId, double importo, String messaggio) {
        this();
        this.annuncioId = annuncioId;
        this.offerenteId = offerenteId;
        this.venditoreId = venditoreId;
        this.importo = importo;
        this.messaggio = messaggio;
    }

    /**
     * Crea un'OffertaNegoziazione da una Offerta esistente
     */
    public static OffertaNegoziazione fromOfferta(Offerta offerta) {
        OffertaNegoziazione negoziazione = new OffertaNegoziazione();
        negoziazione.setAnnuncioId(offerta.getAnnuncioId());
        negoziazione.setOfferenteId(offerta.getOfferenteId());
        negoziazione.setImporto(offerta.getImporto());
        negoziazione.setDataCreazione(offerta.getDataOra());
        negoziazione.setStato(offerta.isAccettata() ? StatoOfferta.ACCETTATA : StatoOfferta.IN_ATTESA);
        return negoziazione;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAnnuncioId() {
        return annuncioId;
    }

    public void setAnnuncioId(int annuncioId) {
        this.annuncioId = annuncioId;
    }

    public int getOfferenteId() {
        return offerenteId;
    }

    public void setOfferenteId(int offerenteId) {
        this.offerenteId = offerenteId;
    }

    public int getVenditoreId() {
        return venditoreId;
    }

    public void setVenditoreId(int venditoreId) {
        this.venditoreId = venditoreId;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public StatoOfferta getStato() {
        return stato;
    }

    public void setStato(StatoOfferta stato) {
        this.stato = stato;
    }

    public TipoOfferta getTipo() {
        return tipo;
    }

    public void setTipo(TipoOfferta tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public LocalDateTime getDataRisposta() {
        return dataRisposta;
    }

    public void setDataRisposta(LocalDateTime dataRisposta) {
        this.dataRisposta = dataRisposta;
    }

    public Integer getOffertaPadreId() {
        return offertaPadreId;
    }

    public void setOffertaPadreId(Integer offertaPadreId) {
        this.offertaPadreId = offertaPadreId;
    }

    public boolean isNotificata() {
        return notificata;
    }

    public void setNotificata(boolean notificata) {
        this.notificata = notificata;
    }

    // Metodi di utilità
    public boolean isInAttesa() {
        return stato == StatoOfferta.IN_ATTESA;
    }

    public boolean isAccettata() {
        return stato == StatoOfferta.ACCETTATA;
    }

    public boolean isRifiutata() {
        return stato == StatoOfferta.RIFIUTATA;
    }

    public boolean isControfferta() {
        return tipo == TipoOfferta.CONTRO_OFFERTA;
    }

    public boolean isScaduta() {
        return stato == StatoOfferta.SCADUTA ||
               (stato == StatoOfferta.IN_ATTESA && dataCreazione.plusDays(7).isBefore(LocalDateTime.now()));
    }

    /**
     * Accetta l'offerta
     */
    public void accetta() {
        this.stato = StatoOfferta.ACCETTATA;
        this.dataRisposta = LocalDateTime.now();
    }

    /**
     * Rifiuta l'offerta
     */
    public void rifiuta() {
        this.stato = StatoOfferta.RIFIUTATA;
        this.dataRisposta = LocalDateTime.now();
    }

    /**
     * Ritira l'offerta
     */
    public void ritira() {
        this.stato = StatoOfferta.RITIRATA;
        this.dataRisposta = LocalDateTime.now();
    }

    /**
     * Crea una contro-offerta basata su questa offerta
     */
    public OffertaNegoziazione creaControfferta(double nuovoImporto, String messaggio) {
        OffertaNegoziazione controfferta = new OffertaNegoziazione();
        controfferta.setAnnuncioId(this.annuncioId);
        // L'offerente della controfferta è il venditore dell'offerta originale
        controfferta.setOfferenteId(this.venditoreId);
        controfferta.setVenditoreId(this.offerenteId); // Il venditore della controfferta è l'offerente originale
        controfferta.setImporto(nuovoImporto);
        controfferta.setMessaggio(messaggio);
        controfferta.setTipo(TipoOfferta.CONTRO_OFFERTA);
        controfferta.setOffertaPadreId(this.id);
        return controfferta;
    }

    /**
     * Restituisce l'importo formattato in euro
     */
    public String getImportoFormattato() {
        return String.format("€%.2f", importo);
    }

    /**
     * Restituisce la data formattata
     */
    public String getDataFormattata() {
        return dataCreazione.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OffertaNegoziazione offerta = (OffertaNegoziazione) o;
        return id == offerta.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("OffertaNegoziazione{id=%d, importo=%.2f, stato=%s, tipo=%s, offerente=%d, venditore=%d}",
                id, importo, stato, tipo, offerenteId, venditoreId);
    }
}
