package application.Classe;

import java.time.LocalDateTime;


public class Codice {
    private int id;
    private int utenteId;
    private int annuncioId;
    private String codiceHash;      
    private String codicePlain;     
    private LocalDateTime dataCreazione;
    private int tentativiErrati;

    
    public Codice(int utenteId, int annuncioId, String codiceHash) {
        this.utenteId = utenteId;
        this.annuncioId = annuncioId;
        this.codiceHash = codiceHash;
        this.dataCreazione = LocalDateTime.now();
        this.tentativiErrati = 0;
    }

    
    public Codice(int id, int utenteId, int annuncioId, String codiceHash,
                  LocalDateTime dataCreazione, int tentativiErrati) {
        this(id, utenteId, annuncioId, codiceHash, null, dataCreazione, tentativiErrati);
    }

    
    public Codice(int id, int utenteId, int annuncioId, String codiceHash,
                  String codicePlain, LocalDateTime dataCreazione, int tentativiErrati) {
        this.id = id;
        this.utenteId = utenteId;
        this.annuncioId = annuncioId;
        this.codiceHash = codiceHash;
        this.codicePlain = codicePlain;
        this.dataCreazione = dataCreazione;
        this.tentativiErrati = tentativiErrati;
    }

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtenteId() { return utenteId; }
    public void setUtenteId(int utenteId) { this.utenteId = utenteId; }

    public int getAnnuncioId() { return annuncioId; }
    public void setAnnuncioId(int annuncioId) { this.annuncioId = annuncioId; }

    public String getCodiceHash() { return codiceHash; }
    public void setCodiceHash(String codiceHash) { this.codiceHash = codiceHash; }

    public String getCodicePlain() { return codicePlain; }
    public void setCodicePlain(String codicePlain) { this.codicePlain = codicePlain; }

    public LocalDateTime getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(LocalDateTime dataCreazione) { this.dataCreazione = dataCreazione; }

    public int getTentativiErrati() { return tentativiErrati; }
    public void setTentativiErrati(int tentativiErrati) { this.tentativiErrati = tentativiErrati; }

    
    public boolean isScaduto() {
        return dataCreazione.plusHours(24).isBefore(LocalDateTime.now());
    }

    
    public boolean isValido() {
        return !isScaduto() && tentativiErrati < 5; 
    }

    
    public void incrementaTentativiErrati() {
        this.tentativiErrati++;
    }

    @Override
    public String toString() {
        return "Codice{" +
                "id=" + id +
                ", utenteId=" + utenteId +
                ", annuncioId=" + annuncioId +
                ", codiceHash='" + codiceHash + '\'' +
                ", codicePlain='" + codicePlain + '\'' +
                ", dataCreazione=" + dataCreazione +
                ", tentativiErrati=" + tentativiErrati +
                '}';
    }
}
