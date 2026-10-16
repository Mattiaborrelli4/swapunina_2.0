# AUDIT SICUREZZA — SwapUnina Desktop

## Vulnerabilità Critiche

### 1. Password in Chiaro — CRITICO

**File**: `src/main/java/application/DB/UserDAO.java:94`

```java
public utente login(String email, String password) {
    String sql = "SELECT id, nome, cognome, email, matricola FROM utente WHERE email = ? AND password = ?";
    // ...
    stmt.setString(2, password); // Nota: in produzione usare hash
}
```

**Impatto**: Tutte le password sono memorizzate e confrontate in testo piantext. Qualsiasi persona con accesso al database può leggere tutte le credenziali utente.

**Rischio**: Furto massiccio di account, impersonamento utenti, accesso non autorizzato a tutte le funzionalità.

**Stato**: *FIXATO — Implementato BCrypt.checkpw() in login(), BCrypt.hashpw() in createUser() e changePassword(). Password never stored or compared in plaintext.*

**Correzione applicata**: 
- Implementare hash bcrypt/SCRYPT per le password ✅
- Verificare hash durante il login, non confrontare testo piantext ✅
- Non mai restituire password nei report o log ✅

### 2. CORS Aperto — ALTO

**File**: `src/main/java/application/api/SwapUninaAPI.java:94`

```java
response.header("Access-Control-Allow-Origin", "*");
```

**Impatto**: L'API accetta richieste da qualsiasi origine. Combinato con assenza di autenticazione token validation, espone tutti gli endpoint.

**Rischio**: Accesso non autorizzato da script malevoli, CSRF potenziale.

**Stato**: *FIXATO — Ristretto a http://localhost:8081 invece di "*".*

**Rischio**: Accesso non autorizzato da script malevoli, CSRF potenziale — mitigato dal ristringimento CORS.

### 3. Token senza Scadenza — MEDIO

**File**: `SwapUninaAPI.java:201`

```java
String token = UUID.randomUUID().toString();
activeTokens.put(token, user.getId());
```

I token non scadono mai e non c'è meccanismo di revoca oltre il logout.

**Stato**: *Documentato, piano per JWT con expiration time in PIANO_INTERVENTI.md P1.*

### 4. Impersonazione Utenti — ALTO

**Vulnerabilità**: Non verifica che il token presente nelle richieste corrisponda all'utente autenticato effettivo. Il sistema usa una semplice `Map<String, Integer> activeTokens` senza convalida delle proprietà dell'utente.

**File**: `SwapUninaAPI.java:59, 201-220`

### 5. SQL Injection Rischio — MEDIO

**Pattern trovati** (ma uso PreparedStatement in maggior parte):
- Alcune query costruiscono nomi tabelle dinamicamente (es. `CarrelloDAO.creaTabellaSeMancante()` line 21-28)
- FilterManager potrebbe avere query non parametrizzate

### 6. PrintStackTrace Espone Dettagli — MEDIO

Presente in diversi file (`UserDAO`, `MessaggioDAO`, `AnnuncioDAO`). Espone implementazione interna che potrebbe aiutare attacchi.

### 7. Password DB Hardcoded — ALTO (RISTTITO)

**File**: `src/main/java/application/config/ConfigManager.java:135`

```java
// setProperty("db.password", "1234", true); // Rimosso: password di default pericolosa
```

**Stato**: *Fix applicato in questa sessione — password di default rimossa, ora richiede variabile ambiente.*

### 8. Master Key Hardcoded — ALTO (RISTTITO)

**File**: `src/main/java/application/config/ConfigManager.java:73`

**Vecchio**: `this.masterPassword = "SwapUnina2025!Secure";` (fallback hardcoded)
**Nuovo**: Richiede variabile ambiente `SWAPUNINA_MASTER_KEY`, errore fatale se mancante

**Stato**: *Fix applicato in questa sessione.*

---

## File Interessati e Stato

| File | Livello | Problema | Stato |
|------|---------|----------|-------|
| UserDAO.java | CRITICO | Password in chiaro login/creazione/cambio | *FIXATO — BCrypt implementato in login/createUser/changePassword* |
| SwapUninaAPI.java | ALTO | CORS aperto, token deboli, endpoint senza auth | *CORS ristretto a localhost:8081, JWT pianificato* |
| ConfigManager.java | MEDIO | Master key management, fallback | *Fix applicato: env variable richiesta* |
| AnnuncioDAO.java | MEDIO | SQL dinamica tabelle, printStackTrace | *Da verificare* |
| MessaggioDAO.java | MEDIO | Gestione chiavi crittografia | *AES-256 GCM implementato* |

## Priorità Interventi

1. **P0**: Hash password BCrypt in UserDAO — *FIXATO — BCrypt implementato in login/createUser/changePassword*
2. **P0**: Ristringere CORS a origini fidate — *FIXATO — Ristretto a http://localhost:8081*
3. **P1**: JWT token con scadenza e refresh — *pianificato in PIANO_INTERVENTI*
4. **P2**: Rate limiting, header security — *da aggiungere*
5. **P3**: Pulire printStackTrace, aggiungere logging sicuro — *LoggerUtil implementato in Main.java*
6. **P4**: Rimuovere eventuali segreti hardcoded residui — *ConfigManager e pom.xml aggiornati*