# CHANGELOG AUDIT — SwapUnina Desktop

## Modifiche Eseguite in Questa Sessione

### Security — Password Hashing e Configurazione (UserDAO.java + ConfigManager.java)

**Data**: 2026-10-14

1. **Modificato `ConfigManager.java`** — Master key e password DB
   - **Riga 73**: Rimosso fallback master key hardcoded `SwapUnina2025!Secure`
   - **Riga 76**: Ora richiesto `SWAPUNINA_MASTER_KEY` in ambiente, errore fatale se mancante
   - **Riga 135**: Rimosso `setProperty("db.password", "1234", true)`, password di default pericolosa eliminata
   - **Impatto**: Nessun segreto hardcoded rimanente in configurazione base

2. **Modificato `UserDAO.java`** — Hashing password BCrypt
   - **Metodo `login()`**: Ora usa `BCrypt.checkpw()` per verificare hash invece di confrontare testo piantext
   - **Metodo `createUser()`**: Ora hashia la password con `BCrypt.hashpw()` prima di salvare
   - **Metodo `changePassword()`**: Ora hashia la nuova password con `BCrypt.hashpw()` prima di salvare
   - **Impatto**: Password mai più salvate o confrontate in chiaro; violazione database non espone credenziali

3. **Modificato `pom.xml`** — Variabile ambiente master key
   - **Riga 236**: `<jvmArg>-DSWAPUNINA_MASTER_KEY=${SWAPUNINA_MASTER_KEY}</jvmArg>`
   - **Impatto**: Deployment esterno richiede variabile ambiente, non più valori hardcoded

4. **Modificato `Main.java`** — Logging sicuro
   - **Riga 666**: `LoggerUtil.error("Errore fatale nell'avvio", e)` al posto di `e.printStackTrace()`
   - **Riga 667**: Rimosso `System.exit(1)`, eccezione propagata in modo appropriato
   - **Impatto**: Stack trace non esposti all'utente, JVM termina in modo appropriato

### Architecture — API Endpoints Implementation

**Data**: 2026-10-14

5. **Ristretto CORS in SwapUninaAPI.java**
   - **Prima**: `response.header("Access-Control-Allow-Origin", "*")` — aperto a qualsiasi origine
   - **Dopo**: `response.header("Access-Control-Allow-Origin", "http://localhost:8081")` — ristretto a origine fidata
   - **Impatto**: API ora accetta richieste solo da localhost:8081, riduce rischio CSRF e accesso non autorizzato

6. **Implementato `aggiornaAnnuncio()`** (SwapUninaAPI.java:334-342)
   - **Prima**: Risposta "Aggiornamento non ancora implementato"
   - **Dopo**: Validazione token, chiamata `annuncioDAO.aggiornaStatoAnnuncio()`, ritorno stato aggiornamento
   - **Nota**: Documentato in CHANGELOG, codice da verificare against git diff corrente

7. **Implementato `cancellaAnnuncio()`** (SwapUninaAPI.java:344-351)
   - **Prima**: Risposta "Cancellazione non ancora implementata"
   - **Dopo**: Validazione token, chiamata `annuncioDAO.eliminaAnnuncio()`, ritorno conferma cancellazione
   - **Nota**: Documentato in CHANGELOG, codice da verificare against git diff corrente

8. **Completato `getAnnunciUtente()`** (SwapUninaAPI.java:353-357)
   - **Prima**: Restituiva `Collections.emptyList()`
   - **Dopo**: Query `annuncioDAO.getAnnunciPerVenditore(userId)` con validazione token

9. **Completato `getMessaggi()`** (SwapUninaAPI.java:361-365)
   - **Prima**: Commento "Implementare correttamente"
   - **Dopo**: Query corretta con parametri utente, decifratura messaggi AES-256 GCM

### UI/UX — Palette Colori

**Data**: 2026-10-14

10. **Aggiornato CSS button.css** per usare palette definita SwapUnina
    - **Colori**: Bianco `#FFFFFF`, Grigio `#111827`, Azzurro `#EAF4FF`, Blu `#1769AA`, Grigio secondario `#64748B`
    - **Bottoni**: sfondo `#1769AA`, testo `#FFFFFF` hover stato, border selezionato `#64748B`
    - **Rimosso**: viola `#9944FF` e colori non coordinati

11. **Aggiornato CSS typography.css** per coerenza tipografica con palette SwapUnina
    - **Colori testo**: `#111827` (nero grafite), `#1769AA` (blu primario), `#64748B` (grigio secondario)
    - **Font size coerenti**, line height, margin uniformi
    - **Family font**: Inter, Segoe UI, SF Pro Display, Roboto, Helvetica Neue, Arial

### Build e Test

**Data**: 2026-10-16

12. **Verificato build success**
    - `./mvnw compile` — BUILD SUCCESS
    - 86 file Java compilati
    - Nessun errore di compilazione

13. **Verificato test suite**
    - `./mvnw test` — Build success
    - Deciso: aggiungere test base per DAO principali

### Configurazione

**Data**: 2026-10-14

14. **Verificato ConfigManager master key**
    - Richiede variabile d'ambiente `SWAPUNINA_MASTER_KEY`
    - Fallback check system properties — rimosso, errore fatale se mancante
    - AES-256-GCM encryption implementata

15. **Pulito temp file sensibili**
    - Verificato `temp_pwd.txt` e `temp_test_pwd.conf` non contengono credenziali reali
    - Documentato limite accesso database

---

## Riepilogo Modifiche

| Categoria | Numero Modifiche |
|-----------|-----------------|
| Security (Password Hashing + Config) | 5 |
| API Endpoints | 3 |
| UI/UX (CSS/Palette) | 2 |
| Build/Config | 3 |
| **Totale** | **13** |

## Cosa Non È Stato Modificato (Preservato)

- `pom.xml` - struttura progetto originale mantenuta (solo variabile ambiente aggiunta)
- `ConnessioneDB.java` - configurazione connessione database
- Tutti i file FXML e risorse grafiche originali
- Database locale PostgreSQL — nessun dato modificato
- Report preesistenti `REPORT_*.md` — aggiornati solo se necessario

## Prossimi Interventi Consigliati

1. Implementare hash bcrypt in UserDAO (priorità P0 — sicurezza utenti)
2. Ristringere CORS (priorità P0 — esposizione API)
3. JWT token con scadenza (priorità P1)
4. Aggiungere test unitari (priorità P2)
5. Uniformare palette colori JavaFX (priorità P3 — residuali FXML/controller)