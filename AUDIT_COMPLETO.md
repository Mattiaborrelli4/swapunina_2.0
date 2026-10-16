# AUDIT COMPLETO — SwapUnina Desktop

## Inventario Codice

### Struttura Package

```
src/main/java/application/
├── api/              # SwapUninaAPI.java (618 linee) - REST API Spark Java
├── Classe/           # Modelli di dominio (Annuncio, Utente, Messaggio, Offerta, Oggetto, Ordine, Scambio, Recensioni, Conto, CarrelloItem)
├── config/           # ConfigManager.java - Gestione configurazione crittografata
├── controls/         # ControlloLogin.java, ControlloRegistrazione.java
├── DB/               # Tutti i DAO e connessione database
│   ├── AnnuncioDAO.java (783 linee)
│   ├── CarrelloDAO.java (343 linee)
│   ├── UserDAO.java (264 linee)
│   ├── MessaggioDAO.java (474 linee)
│   ├── OffertaDAO.java
│   ├── OggettoDAO.java
│   └── ...
├── Enum/             # Categoria, StatoAnnuncio, Tipologia, ModalitaConsegna, OrigineOggetto, StatoOfferta, StatoScambio
├── messagistica/     # MessageEncryptionService.java
└── Main.java         # Punto ingresso applicazione JavaFX
```

### Totale File Java: 86 sorgenti

### Modifiche Verificate (ultima sessione)

- `pom.xml`: Master key sostituito da variabile ambiente `SWAPUNINA_MASTER_KEY`, nessun valore hardcoded
- `src/main/java/application/Main.java`: `LoggerUtil.error()` al posto di `e.printStackTrace()`, nessun `System.exit`
- `src/main/java/application/config/ConfigManager.java`: Password DB rimossa, richiede `SWAPUNINA_MASTER_KEY` in ambiente
- `src/main/resources/style/button.css`: Palette colori SwapUnina completamente applicata
- `src/main/resources/style/typography.css`: Colori palette integrati (Blu `#1769AA`, Grigio `#111827`)

### Report Preesistenti

- `REPORT_FUNZIONALITA_VERIFICATE.md` - Funzionalità verificate in sessioni precedenti
- `REPORT_SICUREZZA_MIGLIORATA.md` - Miglioramenti sicurezza precedenti
- `REPORT_VERIFICA_COMPLETA.md` - Verifica completa precedente
- `ROYAL_PURPLE_REDESIGN_REPORT.md` - Report redesign palette viola

### Componenti Chiave

1. **SwapUninaAPI.java** - Endpoint REST:
   - Auth: login, register, logout (token-based da implementare con JWT)
   - Annunci: get, crea, aggiorna (parzialmente implementato), cancella (parzialmente implementato)
   - Messaggi: get conversazione, invia messaggio (AES-256 GCM)
   - Carrello: get, aggiungi, rimuovi, aggiorna quantità
   - Offerte: riceve, crea, accetta, rifiuta, controfferta

2. **DAO Layer**:
   - AnnuncioDAO: CRUD completo, ricerca per titolo/categoria, trigger stato
   - UserDAO: CRUD utenti, login con password in chiaro (PROBLEMA SICUREZZA - documentato, fix pendente)
   - CarrelloDAO: gestione carrello con upsert quantity
   - MessaggioDAO: crittografia AES-256 GCM completa

3. **Modelli**:
   - Classe: Annuncio, Utente, Messaggio, Offerta, Oggetto, Ordine, Scambio, Recensioni, Conto, CarrelloItem
   - Enum: Categoria, StatoAnnuncio, Tipologia, ModalitaConsegna, OrigineOggetto, StatoOfferta, StatoScambio

4. **Configurazione**:
   - pom.xml - Maven, Java 24, JavaFX, PostgreSQL JDBC
   - ConfigManager - AES-256-GCM encrypted properties, richiede variabile ambiente SWAPUNINA_MASTER_KEY
   - config.properties - configurazione database locale (password rimossa da configurazione predefinita)

### Nuove Scoperte

- `ConfigManager.java:140` - Fallback master key rimosso, ora genera errore fatale se `SWAPUNINA_MASTER_KEY` non impostata
- `pom.xml:236` - jvmArg `-DSWAPUNINA_MASTER_KEY=${SWAPUNINA_MASTER_KEY}` per deployment esterno
- `Main.java:666` - `LoggerUtil.error()` sostituisce `printStackTrace()` per non esporre dettagli interni