# AUDIT_SICUREZZA.md

## Password e Autenticazione

### UserDAO.java - Bug Critico Risolto (P0)

- **Problema**: metodo `login()` selezionava colonne `id, nome, cognome, email, matricola` senza includere `password`, poi tentava di leggere `rs.getString("password")` dal result set
- **Correzione**: query aggiornata a `SELECT id, nome, cognome, email, matricola, password FROM utente WHERE email = ?`
- **Verifica**: password mai esposta nel risultato restituito (mapResultSetToUtente imposta password a "")
- **Test**: test login corretto, password errata, utente inesistente, risposte senza dati sensibili

### UtentiDAO.java - Verifica Credenziali

- **Metodo `verificaCredenziali()`**: seleziona correttamente la colonna `password` e verifica con `BCrypt.checkpw(password, passwordHash)`
- **BCrypt rounds**: utilizza 12 rounds per hash generation (BCRYPT_ROUNDS = 12)
- **Password mai in chiaro**: storedHash letta dal database, mai restituita negli oggetti utente restituiti
- **Registrazione**: `createUser()` usa `BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12))`

### ConfigManager.java - Master Key e Password (P0)

- **Rimosso**: fallback hardcoded `SwapUnina2025!Secure` (linea 74)
- **Rimosso**: default password database "1234" in createDefaultConfig()
- **Fallimento chiaro**: lancio `RuntimeException` se SWAPUNINA_MASTER_KEY non configurata
- **Decrypt fallimento**: `decryptValue()` ora lancia eccezione invece di restituire ciphertext in fallback
- **Variabile richiesta**: SWAPUNINA_MASTER_KEY deve essere impostata all'avvio

### Error Handler API (P1)

- **Modifica**: `exception handler` in SwapUninaAPI.java ora restituisce "Errore interno del server" all'utente
- **Logging**: dettagli tecnici registrati solo su stderr server-side
- **Precedente**: `exception.getMessage()` esposto direttamente nella risposta JSON

## Autorizzazioni e Token

### SwapUninaAPI.java - CORS (P1)

- **Configurazione attuale**: `Access-Control-Allow-Origin: http://localhost:8081` con `Access-Control-Allow-Credentials: true`
- **Verifica necessaria**: confermare se questa configurazione è strettamente necessaria per l'app desktop JavaFX
- **Rischio**: `Access-Control-Allow-Credentials: true` con origine specifica (non wildcard) è accettabile, ma va verificato il traffico client

### Gestione Token (P1)

- **Token**: mappa in-memory `activeTokens` (token UUID → userId)
- **Login**: genera token `UUID.randomUUID().toString()`, lo memorizza in `activeTokens`
- **Logout**: rimuove token dai headers `Authorization: Bearer ...`
- **Limitationi**: nessun revoca automatica scadimento, token vivono fino a logout o restart app
- **Sicurezza**: i token non sono firmati (JWT), basati su sessione in-memory

### Endpoint Sensibili (P1)

- **aggiornaAnnuncio()**: restituisce "Aggiornamento non ancora implementato" (placeholder)
- **cancellaAnnuncio()**: restituisce "Cancellazione non ancora implementata" (placeholder)
- **getAnnunciUtente()**: restituisce lista vuota (non implementata)
- **getMessaggi()**: chiama `getConversazione(userId, 0)` - verifica se corretto o dovrebbe restituire messaggi utente
- **getInterlocutori()**: usa `mittenteId` e `destinatarioId` dal body senza piena verifica identità
- **Creazione offerta**: prende `venditoreId`, `userId`, `mittenteId` dal body senza verifica identità autorevole

## Token e CORS

### CORS

- **Origin attuale**: `http://localhost:8081`
- **Credentials**: `Access-Control-Allow-Credentials: true`
- **Verifica**: confermare client supportati e se necessari per app desktop
- **Regola**: non abilitare credentials con wildcard `*`

### Scadenza e Revoca Token

- Token attualmente senza scadenza formale
- Logout rimuove token dalla mappa in-memory
- Al riavvio applicazione, tutti i token diventano invalidi (puliti)
- **Valutazione**: per produzione valutare implementazione JWT con scadenza e refresh token

## Segreti e Configurazione

### ConfigManager Hardcoded Issues (P0 - Risolti)

1. **Master key hardcoded**: RIMOVENTO - non più presente nel codice
2. **Default db password "1234"**: RIMOVENTO - nessun valore di default per db.password
3. **Decrypt fallback ciphertext**: RIMOVENTO - eccezione lanciata invece di restituire testo cifrato

### Variabili d'Ambiente Richieste

- `SWAPUNINA_MASTER_KEY`: Master password per crittografia configurazione AES-256-GCM
- Senza questa variabile, l'applicazione non può avviarsi

## SQL Injection

### UtentiDAO.verificaEsistenzaCampo (P1 - Risolto)

- **Problema**: nome colonna concatenato direttamente nella query SQL: `"SELECT COUNT(*) FROM utente WHERE " + campo + " = ?"`
- **Pattern risk**: sebbene usato con costanti fisse (COLONNA_EMAIL, COLONNA_MATRICOLA), il pattern è rischioso
- **Correzione**: whitelist di colonne consentite: `Set<String> colonneConsentite = Set.of("email", "matricola")`
- **Verifica**: se colonna non nella whitelist, restituisce false e registra errore

### Altre Query

- Tutte le query UserDAO, AnnuncioDAO, CarrelloDAO, MessaggioDAO usano `PreparedStatement` (buona pratica)
- Nessuna concatenazione di input utente nelle query SQL principali

## Protezione Dati

### Crittografia Messaggi (MessaggioDAO)

- **AES-256 GCM**: tutti i messaggi crittografati prima del salvataggio
- **Componenti memorati**: id, mittente_id, destinatario_id, testo_encrypted (BYTEA), iv (BYTEA), data_invio, annuncio_id, algoritmo_encryption, key_id
- **Migrazione**: metodo `migraMessaggiToAESGCM()` disponibile per convertire messaggi vecchi da UTF-8
- **Integrità**: metodo `verificaIntegritaMessaggi()` per controllare stati di corruzione

### Configurazione Crittografata

- File `config.properties` con prefisso `ENC()` per proprietà sensibili
- AES-256-GCM per crittografia/decifratura valori
- Master key richiesta da variabile d'ambiente (NON più hardcoded)

## Stato dei Controlli Dopo le Correzioni

| Area | Stato | Azione |
|------|-------|--------|
| ConfigManager master key | ✅ Corretto | Rimosso fallback hardcoded |
| ConfigManager default password | ✅ Corretto | Rimosso "1234", richiede env var |
| ConfigManager decrypt fallback | ✅ Corretto | Eccezione invece di ciphertext |
| UserDAO login query | ✅ Corretto | Aggiunta colonna password |
| UtentiDAO SQL injection | ✅ Corretto | Whitelist colonne |
| API error handler | ✅ Corretto | Maschera dettagli tecnici |
| CORS configuration | ⚠️ Da verificare | Confermare necessità app desktop |
| Token management | ⚠️ Da migliorare | Valutare JWT con scadenza |
| SQL injection (verificaEsistenzaCampo) | ✅ Corretto | Whitelist implementata |