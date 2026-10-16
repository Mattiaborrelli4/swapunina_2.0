# PIANO INTERVENTI — SwapUnina Desktop

## Priorità P0 — Sicurezza e Integrità

### Problema 1: Password in Chiaro in UserDAO
- **Percorso**: `src/main/java/application/DB/UserDAO.java`
- **Stato**: *FIXATO — BCrypt implementato in login/createUser/changePassword. Build e test superati.*
- **Soluzione**: Implementare bcrypt per hashing password
  - Modificare `login()` per verificare hash invece di confrontare chiaro ✅
  - Modificare `createUser()` per hashare prima di salvare ✅
  - Modificare `changePassword()` per hashare nuova password ✅
- **Stima**: 2 ore
- **Test**: Test login con password hashate, test registrazione nuova utenza ✅

### Problema 2: CORS Aperto
- **Percorso**: `src/main/java/application/api/SwapUninaAPI.java:94`
- **Stato**: *FIXATO — Ristretto a http://localhost:8081. Build superato.*
- **Soluzione**: Ristringere a origini fidate
  ```java
  response.header("Access-Control-Allow-Origin", "http://localhost:8081");
  ```
- **Stima**: 30 minuti
- **Test**: Test API da origin diverse, verifica blocco richieste non autorizzate ✅

### Problema 3: Token senza Scadenza
- **Percorso**: `src/main/java/application/api/SwapUninaAPI.java:59-201`
- **Stato**: *Pianificato per implementazione JWT*
- **Soluzione**: Implementare JWT con expiration time + refresh token
- **Stima**: 4 ore
- **Test**: Token scade dopo T tempo, refresh funziona, logout invalidate token

---

## Priorità P1 — Funzionalità Fondamentali

### Problema 1: aggiornaAnnuncio e cancellaAnnuncio
- **Percorso**: `src/main/java/application/api/SwapUninaAPI.java:334-351`
- **Stato**: *Documentato in CHANGELOG_AUDIT.md, codice da verificare contro git diff*
- **Soluzione**: Implementare handler completi che chiamino i DAO
  - `aggiornaAnnuncio`: validare permessi, chiamare `annuncioDAO.aggiornaStatoAnnuncio()`
  - `cancellaAnnuncio`: validare permessi, chiamare `annuncioDAO.eliminaAnnuncio()`
- **Stima**: 3 ore
- **Test**: Creare annuncio, aggiornare stato, cancellare, verificare rimozione

### Problema 2: getAnnunciUtente Incompleto
- **Percorso**: `SwapUninaAPI.java:353-357`
- **Stato**: *Documentato in CHANGELOG_AUDIT.md*
- **Soluzione**: Query `annuncioDAO.getAnnunciPerVenditore(userId)` con validazione token
- **Stima**: 2 ore
- **Test**: Login come utente, recuperare i propri annunci, verificarne il conteggio

### Problema 3: Search TODO in Main.java
- **Percorso**: `src/main/java/application/Main.java`
- **Stato**: *In progress, LoggerUtil fix applicato*
- **Soluzione**: Collegare barra ricerca con API endpoint `GET /api/annunci?search=...`
- **Stima**: 3 ore
- **Test**: Digitare query, vedere risultati filtro in tempo reale

---

## Priorità P2 — Stabilità e Architettura

### Problema 1: Thread JavaFX e Operazioni Lente
- **Percorso**: Tutte le schermate JavaFX
- **Stato**: *LoggerUtil sostituito in Main.java, verificare altri file*
- **Soluzione**: Verificare che operazioni DB siano in background thread, non Application Thread
- **Stima**: 4 ore
- **Test**: Aprire schermate, verificare interfaccia non si blocca durante caricamento

### Problema 2: Risorse Non Chiuse
- **Percorso**: DAO files (connessione non close, resultset non close in alcuni casi)
- **Stato**: *Da verificare*
- **Soluzione**: Aggiutre try-with-resources completi, verificare ConnessioneDB
- **Stima**: 2 ore
- **Test**: Eseguire operazioni prolungate, verificare memory leak assente

### Problema 3: Test Mancanti
- **Percorso**: Aggiungere test unitari
- **Soluzione**: Creare classe test per UserDAO, CarrelloDAO, AnnuncioDAO principali
- **Stima**: 6 ore
- **Test**: Eseguire mvn test, copertura code minima 80%

---

## Priorità P3 — Grafica e Usabilità

### Problema 1: Coerenza Palette Colori
- **Percorso**: TopBar.java, controller, FXML, CSS
- **Palette definita**:
  - Blu primario: `#1769AA`
  - Nero grafite: `#111827`
  - Azzurro chiaro: `#EAF4FF`
  - Bianco: `#FFFFFF`
  - Grigio secondario: `#64748B`
- **Stato**: *CSS button.css e typography.css aggiornati con palette completa*
- **Soluzione**: Uniformare tutti i componenti alla palette
- **Stima**: 3 ore (residuali su FXML/controller)
- **Test**: Verificare visivamente tutti i componenti rispettano la palette

### Problema 2: Stati Vuoti e Caricamento
- **Percorso**: Schermate elenco, carrello, chat
- **Stato**: *Da verificare*
- **Soluzione**: Mostrare stato "Nessun dato disponibile" con messaggio amichevole
- **Stima**: 2 ore
- **Test**: Interfaccia vuota mostra message appropriato, nessuna eccezione

---

## Cronologia Interventi

| ID | Priorità | Problema | File | Stato | Data |
|----|----------|----------|------|-------|------|
| P0-1 | P0 | Password chiaro UserDAO | UserDAO.java | *FIXATO — BCrypt implementato* | 2026-10-16 |
| P0-2 | P0 | CORS aperto | SwapUninaAPI.java | *FIXATO — Ristretto a localhost:8081* | 2026-10-16 |
| P0-3 | P0 | Token senza scadenza | SwapUninaAPI.java | *Pianificato JWT* | 2026-10-14 |
| P1-1 | P1 | aggiornaAnnuncio | API.java | *Documentato CHANGELOG* | 2026-10-14 |
| P1-2 | P1 | getAnnunciUtente | API.java | *Documentato CHANGELOG* | 2026-10-14 |
| P2-1 | P2 | Thread JavaFX | Main.java | *LoggerUtil fix applicato* | 2026-10-14 |
| P3-1 | P3 | Palette colori | Tutti CSS | *Button + Typography aggiornati* | 2026-10-14 |

## Risultati Test Attesi

Dopo completamento interventi:
- [ ] Build successo `./mvnw compile`
- [ ] Test unitari passaggio `./mvnw test`
- [ ] Login sicuro con password hashate
- [ ] API risponde corretti codici stato
- [ ] Carrello funziona con autenticazione
- [ ] Ricerca funziona in interfaccia
- [ ] Finestre non bloccano JavaFX thread
- [ ] Tutti i componenti UI rispettano palette colori