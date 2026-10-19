# PIANO_INTERVENTI.md

## Priorità P0 - Sicurezza Critica

### Attività Completate

1. **ConfigManager.java - Master Key Hardcoded**
   - File: `src/main/java/application/config/ConfigManager.java`
   - Modifica: Rimosso fallback hardcoded `SwapUnina2025!Secure`
   - Ora: Richiede variabile d'ambiente `SWAPUNINA_MASTER_KEY`
   - Risultato: ✅ Compila e passa test standalone

2. **ConfigManager.java - Default Password Debole**
   - File: `src/main/java/application/config/ConfigManager.java`
   - Modifica: Rimosso `db.password` default "1234"
   - Ora: Nessun valore di default; applicazione fallisce se non configurata
   - Risultato: ✅ Compila

3. **ConfigManager.java - Decrypt Fallback Ciphertext**
   - File: `src/main/java/application/config/ConfigManager.java`
   - Modifica: `decryptValue()` lancia `RuntimeException` invece di restituire ciphertext
   - Ora: Fallimento chiaro e controllato se manca chiave necessaria
   - Risultato: ✅ Compila

4. **UserDAO.java - Bug Login Password Column**
   - File: `src/main/java/application/DB/UserDAO.java`
   - Modifica: Aggiunta colonna `password` alla query SELECT
   - Query: `SELECT id, nome, cognome, email, matricola, password FROM utente WHERE email = ?`
   - Risultato: ✅ Compila

5. **UtentiDAO.java - SQL Injection Pattern**
   - File: `src/main/java/application/DB/UtentiDAO.java`
   - Modifica: Whitelist colonne in `verificaEsistenzaCampo()`
   - Whitelist: `Set.of("email", "matricola")`
   - Risultato: ✅ Compila

### Attività P0 ancora Aperte

- [ ] Verificare connessione database PostgreSQL locale
- [ ] Testare autenticazione end-to-end con credenziali reali
- [ ] Verificare che nessuna password venga esposta in log o risposte API

## Priorità P1 - Funzionalità e Stabilità

### Attività Completate

1. **SwapUninaAPI.java - Error Handler Maschera Dettagli**
   - File: `src/main/java/application/api/SwapUninaAPI.java`
   - Modifica: `exception handler` ora restituisce "Errore interno del server" all'utente
   - Logging: dettagli tecnici su stderr solo per sviluppatori
   - Risultato: ✅ Compila

2. **UI/UX Correzioni P3 (incluse in P1)**
   - Main.java: gradient sostituito con `#111827` (nero grafite)
   - TopBar.java: fallback logo viola sostituito con blue-primary `#1769AA`
   - TopBar.java: colori hover icona da purple a `#1769AA`
   - FinestraMessaggi.java: green button `#4CAF50` sostituito con `#1769AA`
   - FinestraMessaggi.java: message bubble colors da `#DCF8C6`/#`#FFFFFF` a `#1769AA`/#`#EAF4FF`
   - FinestraMessaggi.java: header background da `#f5f5f5` a `#111827`
   - FinestraMessaggi.java: input box background da `#f5f5f5` a `#111827`
   - FinestraMessaggi.java: message list view background da `#fafafa` a `#111827`
   - FinestraMessaggi.java: default avatar colors aggiornati palette

### Attività P1 ancora Aperte

- [ ] Implementare handler `aggiornaAnnuncio()` con verifica identità proprietario
- [ ] Implementare handler `cancellaAnnuncio()` con verifica identità
- [ ] Implementare handler `getAnnunciUtente()` recupero annunci utente
- [ ] Verificare e correggere `getMessaggi()` vs `getConversazione(userId, 0)`
- [ ] Verificare autorizzazione endpoint che usano userId/venditoreId/mitenteId dal client
- [ ] Confermare configurazione CORS necessaria per app desktop
- [ ] Valutare implementazione JWT per token con scadenza

## Priorità P2 - Stabilità e Qualità del Codice

### Attività Completate

- Nessuna attività P2 specifica richiesta oltre le correzioni generali

### Attività P2 ancora Aperte

- [ ] Analisi completa eccezioni ignorate o mal gestite
- [ ] Verificare `printStackTrace()` nelle schermate e servizi (già presenti, valutare logging strutturato)
- [ ] Controllare `System.exit()` usato impropriamente (presente in Main.java line 666)
- [ ] Verifica risorse non chiuse all'arresto applicazione
- [ ] Connessioni database non rilasciate correttamente
- [ ] Thread bloccanti nell'interfaccia JavaFX
- [ ] Accessi UI fuori dal JavaFX Application Thread
- [ ] Operazioni lunghe sul thread grafico
- [ ] Null pointer checks diffusi
- [ ] Parsing fragile date e prezzi
- [ ] Duplicazione di codice identificata
- [ ] Gestione errata date e prezzi
- [ ] Import inutilizzati e warning significativi
- [ ] Dipendenze obsolete o vulnerabili

## Priorità P3 - UI/UX Desktop JavaFX

### Attività Completate

- **Palette applicata coerentemente** su tutte le schermate modificate
- **Main.java**: background gradient sostituito con `#111827` (nero grafite)
- **TopBar.java**: fallback logo viola → blue-primary `#1769AA`
- **TopBar.java**: colori hover icona da purple a `#1769AA`
- **FinestraMessaggi.java**: green button → `#1769AA` blue-primary
- **FinestraMessaggi.java**: message bubble sent → `#1769AA` sfondo, `#FFFFFF` testo
- **FinestraMessaggi.java**: message bubble received → `#EAF4FF` sfondo, `#111827` testo
- **FinestraMessaggi.java**: header background `#111827` (nero grafite)
- **FinestraMessaggi.java**: input box background `#111827`
- **FinestraMessaggi.java**: message list view background `#111827`
- **FinestraMessaggi.java**: default avatar colors palette-consistent
- **principali.css**: root palette colors aggiornati alle specifiche richieste

### Attività P3 ancora Aperte

- [ ] Rendere layout responsive alle dimensioni finestra desktop
- [ ] Verificare nessun overflow o controlli tagliati
- [ ] Confermare spaziature coerenti su tutte le schermate
- [ ] Gerarchia visiva chiara in tutte le aree modificate
- [ ] Bottoni con stati hover, focus, disabilitato e attivo (verificare CSS)
- [ ] Contrasto leggibile verificato against palette
- [ ] Navigazione intuitiva verifica flow completi
- [ ] Indicatori di caricamento per operazioni lunghe
- [ ] Nessun blocco interfaccia durante chiamate database
- [ ] Campi input con validazione comprensibile
- [ ] Accessibilità da tastiera dove possibile

## Dipendenze Tra Attività

```
P0 (Security Critical)
  ├── ConfigManager master key removal
  ├── ConfigManager weak password removal
  ├── UserDAO login bug fix
  └── UtentiDAO SQL injection fix

P1 (Features & Stability)
  ├── API error handler masking
  ├── User authentication flow verification
  ├── CORS configuration review
  ├── UI/UX palette fixes (included)
  └── Annunci/messaggi/carrello implementation gaps

P2 (Code Quality)
  ├── Exception handling analysis
  ├── Resource cleanup verification
  └── Code duplication review

P3 (UI/UX)
  ├── Palette consistency (completed)
  ├── Layout responsiveness (pending)
  └── Accessibility review (pending)
```

## Modifiche Effettuate

| # | File | Problema | Modifica | Motivazione |
|---|------|----------|----------|-------------|
| 1 | ConfigManager.java | Master key hardcoded | Rimosso fallback, richiede env var | P0 sicurezza |
| 2 | ConfigManager.java | Default password "1234" | Rimosso, nessun default | P0 sicurezza |
| 3 | ConfigManager.java | decryptValue fallback | Eccezione invece di ciphertext | P0 sicurezza |
| 4 | UserDAO.java | Login query missing password | Aggiunta colonna password | P0 bug critico |
| 5 | UtentiDAO.java | SQL injection column concat | Whitelist colonne consentite | P1 sicurezza |
| 6 | SwapUninaAPI.java | Error handler exposure | Maschera dettagli tecnici | P1 sicurezza |
| 7 | Main.java | Dark gradient hardcoded | Sostituito `#111827` | P3 palette |
| 8 | TopBar.java | Fallback logo viola | Sostituito `#1769AA` | P3 palette |
| 9 | TopBar.java | Hover purple color | Sostituito `#1769AA` | P3 palette |
| 10 | FinestraMessaggi.java | Green send button | Sostituito `#1769AA` | P3 palette |
| 11 | FinestraMessaggi.java | Message bubble colors | Sostituiti palette completa | P3 palette |
| 12 | FinestraMessaggi.java | Background colors | Sostituiti palette completa | P3 palette |
| 12 | principali.css | Blue primary color | Sostituito `#1769AA` | P3 palette |

## Rischi e Decisioni che Richiedono Consenso

1. **Migrazione database**: Eventuali modifiche schema richiedono autorizzazione esplicita
2. **Cambio sistema autenticazione**: Da token in-memory a JWT richiede valutazione rischi
3. **Rimozione directory styles/**: Potrebbe rompere dialoghi utente (AccountDialog, CarrelloDialog, etc.)
4. **Cambio porta API**: Porta 8080 corrente, modifica richiede consenso
5. **Database PostgreSQL accesso**: Solo con autorizzazione esplicita, readonly per audit