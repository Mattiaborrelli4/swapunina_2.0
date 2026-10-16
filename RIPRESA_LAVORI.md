# RIPRESA LAVORI — SwapUnina Desktop

## Stato Attuale del Repository

- **Percorso**: `C:\Users\matti\Desktop\SwapUnina`
- **Branch**: main (up to date with origin/main)
- **Ultimo commit**: 6935858 Merge branch 'main' of https://github.com/Mattiaborrelli4/swapunina_2.0
- **Modifiche locali non committate**:
  - `pom.xml` - master key ora usa variabile ambiente SWAPUNINA_MASTER_KEY
  - `src/main/java/application/Main.java` - sostituito printStackTrace con LoggerUtil
  - `src/main/java/application/config/ConfigManager.java` - rimosso hardcoded db password, richiede variabile ambiente
  - `src/main/resources/style/button.css` - palette colori SwapUnina applicata
  - `src/main/resources/style/typography.css` - palette colori integrata

## Attività da Riprendere

1. **Sicurezza P0**: 
   - Risolvere stored password in chiaro in UserDAO — *FIXATO — BCrypt implementato in login/createUser/changePassword* ✅
   - Verificare e correggere eventuali segreti hardcoded — *in corso (ConfigManager fix applicato)*
   - Implementare autenticazione token-based corretta — *da pianificare*

2. **Funzionalità P1**:
   - Implementare `aggiornaAnnuncio` e `cancellaAnnuncio` in SwapUninaAPI — *documentato in CHANGELOG, codice verificato* ✅
   - Completare `getAnnunciUtente` — *documentato in CHANGELOG, implementato* ✅
   - Implementare ricerca funzionante in Main.java — *in progress*

3. **Stabilità P2**:
   - Verificare thread JavaFX — *LoggerUtil fix applicato in Main.java*
   - Risolvere risorse non chiuse — *da verificare nei DAO*
   - Aggiungere test unitari — *nessun test esistente, da aggiungere*

4. **UI/UX P3**:
   - Uniformare palette colori (Blu: `#1769AA`, Grigio: `#111827`, Azzurro: `#EAF4FF`, Bianco: `#FFFFFF`, Grigio secondario: `#64748B`) — *CSS button.css e typography.css aggiornati*
   - Verificare coerenza interfaccia — *da completare su tutte le schermate*
   - Migliorare reattività finestre — *da verificare*

## Report Generati

- `AUDIT_COMPLETO.md` - Inventario dettagliato
- `AUDIT_SICUREZZA.md` - Vulnerabilità e correzioni
- `PIANO_INTERVENTI.md` - Piano prioritizzato
- `CHANGELOG_AUDIT.md` - Modifiche effettuate
- `VERIFICA_FINALE.md` - Verifica conclusiva