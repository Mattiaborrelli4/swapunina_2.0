# RIPRESA_LAVORI.md

## Stato Git Iniziale

- **Branch**: main
- **Remote**: origin https://github.com/Mattiaborrelli4/swapunina_2.0.git (fetch/push)
- **Commit Current**: db41d04 Miglioramenti sulla sicurezza
- **Last 5 Commits**:
  - db41d04 miglioramenti sulla sicurezza
  - 6935858 Merge branch 'main' of https://github.com/Mattiaborrelli4/swapunina_2.0
  - e852c9f Merge branch 'feature/marketplace-layout-improvements'
  - 9240db6 test(finale): all layouts compiled successfully
  - 5025e9d style(typography): standardize font sizes to scale
- **Git Status**: Clean (nessuna modifica locale non commitata)

## Struttura del Progetto

- **Cartella locale**: C:\Users\matti\Desktop\SwapUnina
- **Tecnologie**: Java 24, Maven 3.9.11, JavaFX 24
- **Architettura a 5 livelli**:
  1. UI Layer: JavaFX schermate e CSS
  2. Application Layer: Controllori e gestione flussi
  3. API Layer: SwapUninaAPI (Spark Java, port 8080)
  4. DAO Layer: Database access object (UserDAO, UtentiDAO, AnnuncioDAO, MessaggioDAO, CarrelloDAO)
  5. Database Layer: PostgreSQL con HikariCP connection pooling

## Tecnologie e Dipendenze Principali

- **BCrypt** (0.4): Hashing password
- **Cloudinary** (1.38.0): Gestione immagini
- **Gson** (2.10.1): JSON processing
- **Spark Java** (2.9.4): REST API framework
- **PostgreSQL** (42.6.0): Database driver
- **HikariCP** (5.1.0): Connection pool
- **JavaFX** (24.0.1): Desktop UI framework
- **Maven Surefire**: Testing framework

## Lavoro Già Presentato

- Project compiles successfully with `./mvnw compile`
- Standalone tests available: `TestFunzionalita.java`, `TestSicurezzaStandalone.java`
- AES-256-GCM encryption implemented in ConfigManager and MessaggioDAO
- HikariCP pool configured with connection validation
- BCrypt hashing with 12 rounds for passwords
- CSS styling system with multiple themes (Royal Purple, WhatsApp, Amazon, Dark Mode)

## Attività Riprese

- Audit di sicurezza P0: ConfigManager hardcoded key removed, weak default password removed, decryptValue fix implemented
- Bug fix P0: UserDAO.login() query now includes password column
- Sicurezza P1: UtentiDAO SQL injection pattern fixed in verificaEsistenzaCampo
- Stabilità P1: SwapUninaAPI error handler now masks technical details
- UI/UX P3: Main.java gradient fixed, TopBar fallback logo corrected to blue-primary, FinestraMessaggi green button replaced with blue-primary, message bubble colors corrected

## Problemi di Ambiente

- Variabile d'ambiente SWAPUNINA_MASTER_KEY richiesta per l'avvio (rimossa fallback hardcoded)
- Database PostgreSQL locale richiede configurazione credenziali esplicita
- Nessun test JUnit standard rilevato (test sono main-class in src/main/java/)
- Risorse CSS duplicate: style/ e styles/ directory con file sovrapposti (dettagli-prodotto.css, finestre-utente.css, inserisci-annuncio.css)
- Alcune risorse immagine potrebbero richiedere Cloudinary service configurato

## Avvertenze

- Non rimuovere la cartella styles/ senza verificare la dipendenza dai file dialogali
- Le modifiche a ConfigManager richiedono SWAPUNINA_MASTER_KEY variabile d'ambiente
- Non collegarsi a database produzione senza autorizzazione esplicita