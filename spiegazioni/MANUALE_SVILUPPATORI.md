# 🛠️ MANUALE PER SVILUPPATORI - SwapUnina

## 📚 Indice

1. [Setup Ambiente di Sviluppo](#setup-ambiente-di-sviluppo)
2. [Struttura del Codice](#struttura-del-codice)
3. [Convenzioni di Coding](#convenzioni-di-coding)
4. [Git Workflow](#git-workflow)
5. [Testing](#testing)
6. [Debugging](#debugging)
7. [Deploy](#deploy)

---

## 1. SETUP AMBIENTE DI SVILUPPO

### 1.1 Requisiti Software

```bash
# Verifica versioni
java -version     # >= 24
mvn -version       # >= 3.9
psql --version     # >= 15
git --version      # >= 2.x
```

### 1.2 IDE Consigliati

| IDE | Configurazione |
|-----|----------------|
| **IntelliJ IDEA** | Migliore per Java, supporto JavaFX integrato |
| **Eclipse** | Gratuito, richiede plugin e(fx) |
| **VS Code** | Leggero, richiede estensioni Java |
| **NetBeans** | Supporto JavaFX integrato |

### 1.3 Configurazione IntelliJ IDEA

1. **Apri progetto** come Maven project
2. **Configura SDK**: File → Project Structure → SDKs → Add JDK 24
3. **Abilita JavaFX**:
   - File → Project Structure → Libraries
   - Aggiungi JavaFX SDK dal path `librerie/javafx-sdk-24.0.1/lib`
4. **Configura VM Options** (Run → Edit Configurations):
   ```
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```

---

## 2. STRUTTURA DEL CODICE

### 2.1 Organizzazione Package

```
application/
├── Main.java                    # Punto di ingresso
│
├── DB/                          # Data Access Layer
│   ├── *DAO.java               # Data Access Objects (un per tabella)
│   ├── ConnessioneDB.java       # Gestione connessioni PostgreSQL
│   └── SessionManager.java      # Gestione sessioni utente (Singleton)
│
├── Classe/                      # Domain Entities
│   ├── *.java                  # Entità (Utente, Annuncio, Scambio...)
│   └── Segue naming: nomeClasse in lowercase o PascalCase
│
├── Enum/                        # Enumerazioni
│   └── *Enum.java              # Stati, categorie, tipologie
│
├── controls/                    # Controller UI
│   └── Controllo*.java         # Gestione form (Login, Registrazione)
│
└── CloudinaryImageService.java # Servizio upload immagini
```

### 2.2 Componenti UI (schermata/)

```
schermata/
├── Main.java                   # Finestra principale JavaFX
├── SchermataPrincipale.java    # Controller vista principale
├── TopBar.java                 # Barra di navigazione
├── ProductCard.java            # Card singolo annuncio
├── ProductGrid.java            # Griglia annunci
└── button/                     # Dialog e finestre secondarie
    ├── *Dialog.java            # Estendono javafx.scene.control.Dialog
    └── Segue pattern: NomeFunzioneDialog.java
```

### 2.3 Risorse CSS

```
src/main/resources/style/
├── application.css             # Stili globali applicazione
├── principali.css              # Stili componente principali
├── button.css                  # Stili pulsanti
├── carello.css                 # Stili carrello
└── chat.css                    # Stili chat
```

---

## 3. CONVENZIONI DI CODING

### 3.1 Naming Conventions

**Classi:** PascalCase
```java
public class AnnuncioDAO { }
public class PropostaScambioDialog { }
```

**Metodi:** camelCase
```java
public void creaAnnuncio() { }
public boolean verificaCredenziali() { }
```

**Costanti:** UPPER_SNAKE_CASE
```java
private static final String TABLE_NAME = "annuncio";
private static final int MAX_TITLE_LENGTH = 255;
```

**Variabili:** camelCase
```java
private String titolo;
private int venditoreId;
```

### 3.2 Documentazione

**JavaDoc per classi pubbliche:**
```java
/**
 * DAO per la gestione degli scambi tra utenti.
 *
 * <p>Questo DAO fornisce metodi per:</p>
 * <ul>
 *   <li>Proporre scambi</li>
 *   <li>Accettare/Rifiutare scambi</li>
 *   <li>Completare scambi</li>
 * </ul>
 *
 * @author SwapUnina Team
 * @version 1.0
 * @since 2026-02-03
 */
public class ScambioDAO { }
```

**Commenti per logica complessa:**
```java
// NOTA: Verifica che l'utente non possa proporre scambio per i propri annunci
if (annuncio.getVenditoreId() == utenteId) {
    throw new IllegalStateException("Non puoi scambiare con te stesso");
}
```

### 3.3 Error Handling

**SQLException:**
```java
try (Connection conn = ConnessioneDB.getConnessione();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    // Operazioni database
} catch (SQLException e) {
    LOGGER.log(Level.SEVERE, "Errore recupero annuncio {0}: {1}",
        new Object[]{id, e.getMessage()});
    throw new DAOException("Impossibile recuperare l'annuncio", e);
}
```

**Validazione Input:**
```java
public void setPrezzo(double prezzo) {
    if (prezzo < 0) {
        throw new IllegalArgumentException("Il prezzo non può essere negativo");
    }
    if (prezzo > 1000000) {
        throw new IllegalArgumentException("Il prezzo non può superare 1.000.000€");
    }
    this.prezzo = prezzo;
}
```

---

## 4. GIT WORKFLOW

### 4.1 Branch Model

```
main (prod)
├── develop (sviluppo)
    ├── feature/scambio-sistema
    ├── feature/miglioramenti-ui
    └── bugfix/login-error
```

### 4.2 Convenzioni Commit

```
<TIPO>(<ambito>): <descrizione>

<body>

OPZIONALI: dettagli sul commit

---

<Tipi>
feat:     nuova funzionalità
fix:      bug fix
docs:     documentazione
style:     formatting, missing semicolons
refactor: refactoring codice
test:     aggiunta/modifica test
chore:    modifiche minori, dipendenze
```

**Esempi:**
```bash
feat(scambio): implementa proposta scambio tra utenti

- Aggiunge ScambioDAO con metodi CRUD
- Crea PropostaScambioDialog per UI
- Aggiunge tabella scambio nel database

Closes #123

fix(login): risolve errore autenticazione password null

Il problema si verificava quando l'utente inseriva password
vuota. Aggiunto controllo preventivo.

Fixes #456
```

### 4.3 Pull Request Template

```markdown
## Descrizione
Breve descrizione delle modifiche

## Tipo di Cambiamento
- [ ] Bug fix
- [ ] Nuova feature
- [ ] Breaking change
- [ ] Documentazione

## Testing
- [ ] Unit tests passano
- [ ] Integration tests passano
- [ ] Testato manualmente

## Checklist
- [ ] Codice segue le convenzioni
- [ ] Self-review effettuato
- [ ] Commenti aggiunti dove necessario
- [ ] Documentazione aggiornata
- [ ] Nessun nuovo warning generato
```

---

## 5. TESTING

### 5.1 Unit Testing

**Struttura Test:**
```java
public class AnnuncioDAOTest {

    private AnnuncioDAO annuncioDAO;
    private Connection testConnection;

    @Before
    public void setUp() throws SQLException {
        // Setup database di test
        testConnection = TestDBUtil.getTestConnection();
        annuncioDAO = new AnnuncioDAO();
    }

    @Test
    public void testInserisciAnnuncio() {
        // Arrange
        Annuncio annuncio = creaAnnuncioTest();

        // Act
        int id = annuncioDAO.inserisciAnnuncioComplesivo(annuncio, 1);

        // Assert
        assertTrue(id > 0);
        Annuncio recuperato = annuncioDAO.getAnnuncioById(id);
        assertEquals(annuncio.getTitolo(), recuperato.getTitolo());
    }
}
```

### 5.2 Integration Testing

```java
@Test
public void testFlussoCompletoScambio() {
    // 1. Registra utenti
    int venditoreId = utentiDAO.salvaUtente(venditore);
    int acquirenteId = utentiDAO.salvaUtente(acquirente);

    // 2. Crea annunci
    int annuncioId = annuncioDAO.inserisciAnnuncioComplesso(
        creaAnnuncio(venditoreId)
    );

    // 3. Proponi scambio
    int scambioId = scambioDAO.proponiScambio(
        acquirenteId,
        annuncioId,
        null,
        "Test messaggio"
    );

    // 4. Accetta
    scambioDAO.accettaScambio(scambioId);

    // 5. Completa
    assertTrue(scambioDAO.completaScambio(scambioId));

    // 6. Verifica stato
    Annuncio a = annuncioDAO.getAnnuncioById(annuncioId);
    assertEquals("VENDUTO", a.getStato());
}
```

### 5.3 Esegui Test

```bash
# Tutti i test
mvn test

# Test specifici
mvn test -Dtest=AnnuncioDAOTest
mvn test -Dtest=ScambioDAOTest

# Con coverage
mvn clean test jacoco:report
```

---

## 6. DEBUGGING

### 6.1 Logging

**Livelli Log:**
```java
LOGGER.severe("Errore critico");
LOGGER.warning("Avviso");
LOGGER.info("Informazione");
LOGGER.fine("Debug dettagliato");
```

**Esempio logging:**
```java
private static final Logger LOGGER = Logger.getLogger(MiaClasse.class.getName());

public void metodo() {
    LOGGER.log(Level.INFO, "Inizio elaborazione");

    try {
        // operazione
        LOGGER.log(Level.INFO, "✅ Operazione completata: {0}", risultato);
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "❌ Errore: {0}", e.getMessage());
        throw e;
    }
}
```

### 6.2 Debug con IDE

**IntelliJ IDEA:**
1. Click su margine sinistro per设置 breakpoint
2. Right-click → Debug 'Main.main()'
3. Usa i controlli:
   - F8 - Step over
   - F7 - Step into
   - Shift+F8 - Step out
   - F9 - Resume

### 6.3 Debug Database

**Query Logging:**
```sql
-- Abilita log query PostgreSQL
ALTER DATABASE postgres SET log_min_duration_statement = 0;

-- Vedi log in tempo reale
tail -f /var/log/postgresql/postgresql.log
```

---

## 7. DEPLOY

### 7.1 Build Production

```bash
# Clean e package
mvn clean package -DskipTests

# Crea JAR standalone
mvn clean compile assembly:single -DskipTests
```

### 7.2 Database Migration

```bash
# Esegui migrazioni
psql -U postgres -d postgres -f spiegazioni/classi.sql

# Verifica tabelle
\dt
```

### 7.3 Configurazione Produzione

**application.properties:**
```properties
# Database
db.host=your-production-host
db.port=5432
db.name=swapunina-prod
db.user=swapunina_user
db.password=secure_password

# Cloudinary
cloudinary.url=cloudinary://api_key:api_secret@cloud_name

# Logging
logging.level=INFO
```

---

## 8. AGGIUNGERE NUOVE FUNZIONALITÀ

### 8.1 Pattern per Nuovo DAO

```java
/**
 * DAO per gestione NuovaEntita
 */
public class NuovaEntitaDAO {

    private static final Logger LOGGER = Logger.getLogger(NuovaEntitaDAO.class.getName());
    private static final String TABLE_NAME = "nuova_entita";

    /**
     * Crea una nuova entità
     */
    public int crea(NuovaEntita entita) {
        String sql = "INSERT INTO " + TABLE_NAME + " (...) VALUES (...) RETURNING id";

        try (Connection conn = ConnessioneDB.getConnessione();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Imposta parametri
            // ...

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore creazione: {0}", e.getMessage());
            return -1;
        }
    }

    /**
     * Recupera entità per ID
     */
    public NuovaEntita getById(int id) {
        // Implementazione...
    }

    /**
     * Aggiorna entità
     */
    public boolean aggiorna(NuovaEntita entita) {
        // Implementazione...
    }

    /**
     * Elimina entità
     */
    public boolean elimina(int id) {
        // Implementazione...
    }
}
```

### 8.2 Pattern per Nuovo Dialog

```java
/**
 * Dialog per nuova funzionalità
 */
public class NuovaFunzioneDialog extends Dialog<Risultato> {

    private final int id;

    public NuovaFunzioneDialog(int id) {
        this.id = id;
        setTitle("Titolo Dialog");
        inizializzaUI();
    }

    private void inizializzaUI() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Aggiungi componenti UI...

        getDialogPane().setContent(content);
        configuraPulsanti();
    }

    private void configuraPulsanti() {
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return eseguiAzione();
            }
            return null;
        });
    }

    private Risultato eseguiAzione() {
        // Logica...
        return new Risultato();
    }
}
```

---

## 9. TROUBLESHOOTING SVILUPPO

### Problema: JavaFX non si avvia

```bash
# Soluzione: Aggiungi VM options
--module-path "librerie/javafx-sdk-24.0.1/lib" --add-modules javafx.controls,javafx.fxml
```

### Problema: Database non si connette

```bash
# Verifica PostgreSQL
pg_ctl status

# Verifica connessione
psql -U postgres -d postgres
```

### Problema: Test falliscono

```bash
# Verifica database di test
mvn test -Dargumenst=-Dtest.db=true

# Salta test problematici
mvn test -DskipTests
```

---

## 10. RISORSE

### Documentazione Ufficiale
- [JavaFX Documentation](https://openjfx.io/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Maven Guide](https://maven.apache.org/guides/)

### Community
- [Stack Overflow - JavaFX](https://stackoverflow.com/questions/tagged/javafx)
- [PostgreSQL Community](https://www.postgresql.org/community/)

---

<div align="center">

### **🛠️ Buon coding!**

*Contribuisci a rendere SwapUnina sempre migliore*

Made with ❤️ by SwapUnina Development Team

</div>
