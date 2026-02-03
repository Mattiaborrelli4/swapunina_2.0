# 📘 DOCUMENTAZIONE TECNICA COMPLETA - SwapUnina

## SOMMARIO EXECUTIVO

**SwapUnina** è una piattaforma di e-commerce universitaria progettata per facilitare lo scambio, la vendita e il commercio di oggetti tra studenti. Il sistema implementa un modello client-server con architettura three-tier, garantendo sicurezza, scalabilità e usabilità.

### Obiettivi del Progetto
- ✅ Creare un marketplace sicuro per studenti universitari
- ✅ Implementare sistema di scambio bidirezionale
- ✅ Garantire sicurezza delle transazioni e dei dati
- ✅ Fornire interfaccia intuitiva e moderna
- ✅ Supportare gestione distribuita con database cloud

---

## 1. ARCHITETTURA DEL SISTEMA

### 1.1 Architettura Three-Tier

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  JavaFX Stage & Scene                                 │   │
│  │  - TopBar: navigazione e ricerca                      │   │
│  │  - SchermataPrincipale: griglia prodotti              │   │
│  │  - ProductCard: visualizzazione singolo annuncio      │   │
│  │  - Dialog: gestione operazioni (scambi, acquisti)    │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      BUSINESS LOGIC LAYER                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controller & Services                                │   │
│  │  - ControlloLogin/Registrazione: autenticazione      │   │
│  │  - ScambioDAO: gestione scambi                        │   │
│  │  - CarrelloManager: gestione carrello                │   │
│  │  - CloudinaryImageService: upload immagini           │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      DATA ACCESS LAYER                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Data Access Objects (DAO)                           │   │
│  │  - UtentiDAO: CRUD utenti                            │   │
│  │  - AnnuncioDAO: CRUD annunci                         │   │
│  │  - ScambioDAO: gestione scambi                       │   │
│  │  - CarrelloDAO: gestione carrello                    │   │
│  │  - MessaggioDAO: messaggistica                        │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      DATABASE LAYER                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  PostgreSQL Database                                  │   │
│  │  - utente, annuncio, scambio                          │   │
│  │  - carrello_item, messaggio                           │   │
│  │  - recensione, transazione, ordine                   │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Pattern Architetturali Implementati

| Pattern | Utilizzo | Benefici |
|---------|----------|----------|
| **MVC** | Separa logica da interfaccia | Manutenibilità, testabilità |
| **DAO** | Astrazione accesso dati | Indipendenza dal database |
| **Singleton** | Gestori condivisi (CarrelloManager, SessionManager) | Unica istanza globale |
| **Factory** | Creazione Dialog e componenti UI | Flessibilità creazione oggetti |
| **Observer** | Notifiche e aggiornamenti UI | Reattività automatica |
| **Strategy** | Algoritmi di ricerca e filtro | Estensibilità |

---

## 2. DATABASE SCHEMA

### 2.1 Schema ER (Entity-Relationship)

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│    UTENTE     │         │   ANNUNCIO    │         │   OGGETTO    │
├──────────────┤         ├──────────────┤         ├──────────────┤
│ id (PK)      │────┐    │ id (PK)      │────┐    │ id (PK)      │
│ matricola    │    │    │ titolo       │    │    │ nome         │
│ nome         │    │    │ prezzo       │    │    │ descrizione  │
│ cognome      │    │    │ tipologia    │    │    │ categoria_id │
│ email        │    │    │ stato        │    └────│ origine      │
│ password     │    │    │ venditore_id │         │ image_url    │
└──────────────┘    │    └──────────────┘         └──────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐      ┌────────▼────────┐
│    SCAMBIO      │      │   CARRELLO_ITEM  │
├────────────────┤      ├─────────────────┤
│ id (PK)        │      │ id (PK)         │
│ richiedente_id │      │ utente_id       │
│ annuncio_...   │      │ annuncio_id     │
│ annuncio_...   │      │ quantita        │
│ stato          │      │ data_aggiunta   │
└────────────────┘      └─────────────────┘
        │
┌───────▼────────┐
│   MESSAGGIO    │
├────────────────┤
│ id (PK)        │
│ mittente_id    │
│ destinatario_id│
│ testo_encrypted│
└────────────────┘
```

### 2.2 Tabelle Principali

#### UTENTE
```sql
CREATE TABLE utente (
    id SERIAL PRIMARY KEY,
    matricola VARCHAR(20) UNIQUE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    data_registrazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### ANNUNCIO
```sql
CREATE TABLE annuncio (
    id SERIAL PRIMARY KEY,
    titolo VARCHAR(255) NOT NULL,
    oggetto_id INTEGER REFERENCES oggetto(id),
    prezzo DECIMAL(10,2) NOT NULL,
    tipologia VARCHAR(50) CHECK (tipologia IN ('VENDITA','SCAMBIO','REGALO','ASTA')),
    stato VARCHAR(50) DEFAULT 'ATTIVO' CHECK (stato IN ('ATTIVO','VENDUTO','RITIRATO')),
    venditore_id INTEGER REFERENCES utente(id),
    data_pubblicazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_url TEXT,
    descrizione TEXT
);
```

#### SCAMBIO (Tabella Nuova)
```sql
CREATE TABLE scambio (
    id SERIAL PRIMARY KEY,
    richiedente_id INTEGER NOT NULL REFERENCES utente(id),
    annuncio_richiesto_id INTEGER NOT NULL REFERENCES annuncio(id),
    annuncio_offerto_id INTEGER REFERENCES annuncio(id),
    stato VARCHAR(20) DEFAULT 'IN_ATTESA' CHECK (stato IN (
        'IN_ATTESA','ACCETTATO','RIFIUTATO','ANNULLATO','COMPLETATO'
    )),
    data_proposta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_accettazione TIMESTAMP,
    data_completamento TIMESTAMP,
    messaggio TEXT
);
```

### 2.3 Relazioni e Vincoli

| Relazione | Tipo | Descrizione |
|-----------|------|-------------|
| Utente → Annuncio | 1:N | Un utente ha molti annunci |
| Annuncio → Oggetto | 1:1 | Ogni annuncio ha un oggetto |
| Utente → Scambio | 1:N | Un utente può proporre molti scambi |
| Scambio → Annuncio | N:1 | Uno scambio coinvolge 1-2 annunci |
| Utente → CarrelloItem | 1:N | Un utente ha molti items nel carrello |

---

## 3. CLASSI JAVA PRINCIPALI

### 3.1 Package Structure

```
application/
├── Main.java                    # Entry Point
├── CloudinaryImageService.java # Gestione upload immagini
├── config.java                  # Configurazione Cloudinary
│
├── DB/                          # Data Access Layer
│   ├── ConnessioneDB.java       # Gestione connessioni DB
│   ├── SessionManager.java      # Gestione sessioni utente
│   ├── UtentiDAO.java           # CRUD Utenti
│   ├── AnnuncioDAO.java         # CRUD Annunci
│   ├── ScambioDAO.java          # Gestione Scambi
│   ├── CarrelloDAO.java         # Gestione Carrello
│   ├── MessaggioDAO.java        # Gestione Messaggi
│   └── ...
│
├── Classe/                      # Domain Models
│   ├── utente.java              # Entità Utente
│   ├── Annuncio.java            # Entità Annuncio
│   ├── Scambio.java             # Entità Scambio
│   ├── Oggetto.java             # Entità Oggetto
│   ├── Messaggio.java           # Entità Messaggio
│   └── ...
│
├── Enum/                        # Enumerazioni
│   ├── Categoria.java           # LIBRI, ELETTRONICA, etc.
│   ├── Tipologia.java           # VENDITA, SCAMBIO, REGALO, ASTA
│   ├── StatoScambio.java        # Stati scambio
│   └── OrigineOggetto.java      # NUOVO, USATO, etc.
│
└── controls/                    # UI Controllers
    ├── ControlloLogin.java      # Gestione login
    └── ControlloRegistrazione.java  # Gestione registrazione
```

### 3.2 Classi Dominio Principali

#### Annuncio.java
```java
public class Annuncio {
    private int id;
    private String titolo;
    private double prezzo;
    private Tipologia tipologia;
    private String stato;
    private int venditoreId;
    private LocalDateTime dataPubblicazione;
    private String imageUrl;
    private String descrizione;
    private Oggetto oggetto;
    private List<String> caratteristicheSpeciali;

    // Getters, Setters, Methods
}
```

#### Scambio.java (Nuova Classe)
```java
public class Scambio {
    private int id;
    private int richiedenteId;
    private int annuncioRichiestoId;
    private Integer annuncioOffertoId;  // null per scambio diretto
    private StatoScambio stato;
    private LocalDateTime dataProposta;
    private LocalDateTime dataAccettazione;
    private LocalDateTime dataCompletamento;
    private String messaggio;

    // Campi per visualizzazione (non salvati nel DB)
    private String titoloAnnuncioRichiesto;
    private String titoloAnnuncioOfferto;
    private String nomeRichiedente;
    private String nomeProprietario;
}
```

---

## 4. FUNZIONALITÀ IMPLEMENTATE

### 4.1 Sistema di Scambio

#### 4.1.1 Proposta Scambio
1. L'utente clicca "🔄 Proponi Scambio" su un annuncio
2. Si apre `PropostaScambioDialog`
3. L'utente può:
   - Selezionare un proprio annuncio da offrire
   - Lasciare vuoto per scambio diretto
   - Scrivere un messaggio
4. Il sistema crea un record nella tabella `scambio`

#### 4.1.2 Gestione Scambio
1. Il venditore riceve notifica
2. Apre "💫 Scambi" dalla TopBar
3. Può:
   - ✅ Accettare → Stato diventa ACCETTATO
   - 🚫 Rifiutare → Stato diventa RIFIUTATO
4. Se accettato:
   - Il richiedente può completare
   - Al completamento: annunci marcati come VENDUTI

#### 4.1.3 Stati Scambio

```java
public enum StatoScambio {
    IN_ATTESA,    // Proposta inviata, in attesa di risposta
    ACCETTATO,    // Scambio accettato, in attesa di completamento
    COMPLETATO,   // Scambio completato con successo
    RIFIUTATO,    // Scambio rifiutato dal destinatario
    ANNULLATO     // Proposta annullata dal richiedente
}
```

### 4.2 Gestione Carrello

```java
// Aggiunta al carrello
carrelloManager.aggiungiAlCarrello(annuncioId, quantita);

// Recupero items
List<CarrelloItem> items = carrelloDAO.getCarrelloItems(utenteId);

// Selezione per acquisto
carrelloManager.selezionaArticolo(annuncioId, true);

// Checkout
carrelloManager.acquistaSelezionati();
```

### 4.3 Messaggistica Criptata

```java
// Invio messaggio criptato
Messaggio msg = new Messaggio(
    mittenteId,
    destinatarioId,
    testoPlaintext,  // Viene criptato automaticamente
    annuncioId
);
messaggioDAO.inviaMessaggio(msg);
```

---

## 5. SICUREZZA

### 5.1 Autenticazione

**Password Hashing con BCrypt:**
```java
// Registrazione
String hashedPassword = BCrypt.hashpw(passwordPlain, BCrypt.gensalt(12));
utente.setPassword(hashedPassword);

// Login
boolean isValid = BCrypt.checkpw(passwordInput, utente.getPassword());
```

**Vantaggi BCrypt:**
- Salting automatico
- Resistente a Rainbow Tables
- Computazionalmente costoso (anti-brute force)

### 5.2 Messaggistica Criptata

**Implementazione AES-256:**
```java
// Criptaggio
byte[] iv = generateIV();
byte[] encrypted = cipher.doFinal(plaintext.getBytes());
PreparedStatement stmt.setBytes(1, encrypted);
stmt.setBytes(2, iv);

// Decriptaggio
byte[] decrypted = cipher.doFinal(encrypted);
String plaintext = new String(decrypted);
```

### 5.3 Validazione Input

**Lato Server:**
```java
// Validazione email
if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
    throw new ValidationException("Email non valida");
}

// Validazione prezzo
if (prezzo < 0 || prezzo > 1000000) {
    throw new ValidationException("Prezzo fuori range");
}
```

**Lato Client:**
```java
// TextField con vincoli
prezzoField.setTextFormatter(new TextFormatter<>(c -> {
    if (c.getControlNewText().matches("\\d*\\.?\\d*")) {
        return c;
    }
    return null;
}));
```

---

## 6. PERFORMANCE E OTTIMIZZAZIONE

### 6.1 Database Indexing

```sql
-- Indici per ricerche comuni
CREATE INDEX idx_annuncio_titolo ON annuncio(titolo);
CREATE INDEX idx_annuncio_prezzo ON annuncio(prezzo);
CREATE INDEX idx_annuncio_stato ON annuncio(stato);
CREATE INDEX idx_annuncio_venditore ON annuncio(venditore_id);
CREATE INDEX idx_annuncio_tipologia ON annuncio(tipologia);

-- Indici per scambi
CREATE INDEX idx_scambio_richiedente ON scambio(richiedente_id);
CREATE INDEX idx_scambio_stato ON scambio(stato);
```

### 6.2 Caching

**Cache annunci in memoria:**
```java
private List<Annuncio> tuttiGliAnnunci = new ArrayList<>();

// Refresh incrementale
public void aggiungiAnnunci(List<Annuncio> nuovi) {
    // Aggiunge solo nuovi annunci
    Set<Integer> idsPresenti = tuttiGliAnnunci.stream()
        .map(Annuncio::getId)
        .collect(Collectors.toSet());

    List<Annuncio> unici = nuovi.stream()
        .filter(a -> !idsPresenti.contains(a.getId()))
        .collect(Collectors.toList());

    tuttiGliAnnunci.addAll(unici);
}
```

### 6.3 Lazy Loading

```java
// Caricamento immagini lazy
productImage.setImage(new Image(imageUrl, true));  // true = background loading

// Paginazione risultati
public List<Annuncio> getAnnunciPaginati(int page, int size) {
    int offset = page * size;
    String sql = "SELECT * FROM annuncio LIMIT ? OFFSET ?";
}
```

---

## 7. DEPLOYMENT

### 7.1 Configurazione Database Remoto

**ElephantSQL (Consigliato):**
```bash
# Variabili d'ambiente
export SWAPUNINA_DB_HOST="your-host.db.elephantsql.com"
export SWAPUNINA_DB_PORT="5432"
export SWAPUNINA_DB_NAME="your-database"
export SWAPUNINA_DB_USER="your-username"
export SWAPUNINA_DB_PASSWORD="your-password"
```

**Supabase:**
- Interfaccia web intuitiva
- PostgreSQL 15+
- 500MB gratis
- Backup automatici

### 7.2 Build e Package

```bash
# Compile
mvn clean package

# Crea JAR eseguibile
java -jar target/swapunina-0.0.1-SNAPSHOT.jar
```

### 7.3 Distribuzione

**Opzioni:**
1. **JAR standalone** - Include tutte le dipendenze
2. **JavaWebStart** - Lancio da browser
3. **Installer Windows (exe)** - Con NSIS o IzPack
4. **macOS App Bundle** - Con javafx-maven-plugin

---

## 8. TESTING

### 8.1 Unit Testing

```java
@Test
public void testPropostaScambio() {
    // Arrange
    Scambio scambio = new Scambio(richiedenteId, annuncioId, offertoId, "Messaggio");

    // Act
    int scambioId = scambioDAO.proponiScambio(scambio);

    // Assert
    assertNotNull(scambioId);
    assertTrue(scambioId > 0);
}
```

### 8.2 Integration Testing

```java
@Test
public void testFlussoCompletoScambio() {
    // 1. Crea utenti
    utenteDAO.salvaUtente(venditore);
    utenteDAO.salvaUtente(richiedente);

    // 2. Crea annunci
    int annuncioId = annuncioDAO.inserisciAnnuncioComplesso(annuncio1);

    // 3. Proponi scambio
    int scambioId = scambioDAO.proponiScambio(richiedenteId, annuncioId, null, "Test");

    // 4. Accetta scambio
    assertTrue(scambioDAO.accettaScambio(scambioId));

    // 5. Completa scambio
    assertTrue(scambioDAO.completaScambio(scambioId));

    // 6. Verifica stato annunci
    Annuncio a = annuncioDAO.getAnnuncioById(annuncioId);
    assertEquals("VENDUTO", a.getStato());
}
```

---

## 9. MANUTENZIONE

### 9.1 Backup Database

```bash
# Backup automatico con pg_dump
pg_dump -h localhost -U postgres postgres > backup_$(date +%Y%m%d).sql
```

### 9.2 Monitoring

**Log Levels:**
- SEVERE - Errori critici
- WARNING - Avvisi
- INFO - Informazioni generali
- FINE - Debug dettagliato

### 9.3 Aggiornamenti

**Versioning Semantico:**
- MAJOR.MINOR.PATCH
- Es: 1.0.0 → 1.0.1 (bug fix)
- Es: 1.0.1 → 1.1.0 (new feature)
- Es: 1.1.0 → 2.0.0 (breaking changes)

---

## 10. APPENDICE

### 10.1 Codici Errore

| Codice | Descrizione |
|--------|-------------|
| DB_001 | Connessione database fallita |
| AUTH_001 | Credenziali non valide |
| SCAMBIO_001 | Scambio non più disponibile |
| CART_001 | Saldo insufficiente |

### 10.2 Metriche di Successo

- ⏱️ Tempo risposta < 2 secondi
- 🔒 Sicurezza password (min 8 caratteri)
- 💾 99.9% uptime database
- 📱 Supporto multi-device

### 10.3 Riferimenti

- [JavaFX Documentation](https://openjfx.io/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [BCrypt Documentation](https://github.com/patrickfav/bcrypt)
- [ElephantSQL](https://www.elephantsql.com/)

---

<div align="center">

### **📘 Documentazione Tecnica SwapUnina v2.0**

**Versione:** 2.0
**Data:** 2026-02-03
**Autore:** SwapUnina Development Team

Made with ❤️

</div>
