# 🎓 SwapUnina 2.0 - Marketplace Universitario

<div align="center">

![Java](https://img.shields.io/badge/Java-24-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-24.0.1-blue)
![Maven](https://img.shields.io/badge/Maven-3.9+-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue)
![License](https://img.shields.io/badge/License-MIT-green)

**Piattaforma di scambio e compravendita per studenti universitari**

[Features](#-caratteristiche) • [Installazione](#-installazione) • [Utilizzo](#-utilizzo) • [Stack Tecnologico](#-stack-tecnologico)

</div>

---

## 📖 Descrizione

**SwapUnina** è un'applicazione desktop JavaFX che permette agli studenti universitari di comprare, vendere e scambiare libri, appunti, elettronica e molto altro. La piattaforma facilita gli scambi tra studenti della stessa università, offrendo un sistema sicuro con profili utente, carrello, messaggistica e recensioni.

### 🎯 Obiettivo

Semplificare la vita degli studenti universitari permettendo loro di:
- 💰 Rivendere libri di testo e materiale didattico
- 📚 Acquistare libri usati a prezzi vantaggiosi
- 🤝 Scambiare direttamente con altri studenti
- ⭐ Valutare venditori e acquirenti attraverso recensioni

---

## ✨ Caratteristiche

### 👤 Gestione Utente
- ✅ Registrazione con email istituzionale (@studenti.unina.it)
- 🔐 Autenticazione sicura con jBCrypt
- 👤 Profilo personalizzabile con immagine
- 🖼️ Supporto avatar generati automaticamente o caricati

### 📦 Annunci e Prodotti
- 📝 Pubblicazione annunci con foto (Cloudinary)
- 🔍 Ricerca avanzata per categoria e testo
- 🏷️ Categorie: Libri, Elettronica, Appunti, Altro
- 📷 Gestione immagini multiple per prodotto
- 🔄 Stati: Attivo, Venduto, Ritirato, Consegnato

### 🛒 Carrello e Acquisti
- ➕ Aggiunta/rimozione prodotti dal carrello
- 📊 Gestione quantità
- 💳 Checkout semplificato
- 📦 Tracciamento stato ordine

### 💬 Messaggistica
- 💬 Chat tra venditore e acquirente
- 🔒 Crittografia messaggi
- 📋 Lista conversazioni
- 🔔 Notifiche nuovi messaggi

### ⭐ Recensioni
- 🌟 Sistema valutazione 1-5 stelle
- 📝 Commenti testuali
- 👥 Recensioni venditori e acquirenti
- 📊 Media valutazioni visibile

### 🎨 Interfaccia Utente
- 🌙 Design moderno con sfondo gradiente scuro
- 📱 Layout responsive (550x780 minimo)
- 🎨 Badge stati (VENDUTO, CONSEGNATO, ACQUISTATO)
- 🔍 Icone SVG personalizzate
- ✨ Animazioni fluide

---

## 🚀 Installazione

### Prerequisiti

- **Java JDK 24** o superiore
  - [Download](https://adoptium.net/)
- **Maven 3.9+** (opzionale, mvnw incluso)
  - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 15+** con database configurato
  - [Download](https://www.postgresql.org/download/)

### Setup Rapido

1. **Clona il repository**
   ```bash
   git clone https://github.com/Mattiaborrelli4/swapunina_2.0.git
   cd swapunina_2.0
   ```

2. **Configura il database**
   - Crea un database PostgreSQL chiamato `swapunina`
   - Configura le credenziali nel file di configurazione

3. **Compila il progetto**
   ```bash
   mvn clean install
   ```

4. **Avvia l'applicazione**
   ```bash
   # Windows
   run_swapunina.bat

   # Oppure con Maven
   mvn javafx:run
   ```

### 📦 Dipendenze

Tutte le librerie sono gestite automaticamente da Maven:

- **JavaFX 24.0.1** - Interfaccia grafica
- **PostgreSQL Driver 42.6.0** - Connessione database
- **jBCrypt 0.4** - Crittografia password
- **Cloudinary 1.38.0** - Hosting immagini
- **Apache Commons** - Utilità varie
- **JSON 20230227** - Parsing JSON

---

## 📖 Utilizzo

### Prima Connessione

1. **Registrati** con email @studenti.unina.it
2. **Compila il profilo** con nome e cognome
3. **Carica una foto profilo** (opzionale)

### Pubblicare un Annuncio

1. Clicca su **"Inserisci Annuncio"** nella TopBar
2. Compila i dettagli:
   - Titolo e descrizione
   - Prezzo
   - Categoria
   - Foto (caricate su Cloudinary)
3. Pubblica l'annuncio

### Acquistare Prodotti

1. **Naviga** tra gli annunci disponibili
2. **Usa i filtri** per categoria o cerca per testo
3. Clicca **"Aggiungi al Carrello"**
4. Procedi al **checkout**
5. Contatta il venditore via chat per il ritiro

### Gestire il Carrello

- Accesso dalla icona carrello in TopBar
- Modifica quantità
- Rimuovi prodotti
- Visualizza totale

### Valutare Venditori

Dopo aver ricevuto il prodotto:
1. Vai nei tuoi ordini
2. Clicca su **"Lascia Recensione"**
3. Valuta da 1 a 5 stelle
4. Scrivi un commento

---

## 🏗️ Stack Tecnologico

### Backend
- **Java 24** - Linguaggio principale
- **JavaFX 24.0.1** - Framework GUI
- **PostgreSQL** - Database relazionale
- **JDBC** - Connessione database

### Security
- **jBCrypt** - Hash password
- **Crittografia AES** - Messaggi chat
- **PreparedStatement** - SQL Injection protection

### Cloud & Storage
- **Cloudinary** - CDN immagini
- **Upload automatico** - Ottimizzazione immagini

### Librerie Utility
- **Apache Commons Codec** - Encoding/decoding
- **Apache Commons IO** - Operazioni I/O
- **Apache HttpClient** - Richieste HTTP
- **org.json** - Parsing JSON

### Build Tool
- **Maven 3.9+** - Dependency management
- **JavaFX Maven Plugin 0.0.8** - Package applicazione

---

## 📂 Struttura Progetto

```
swapunina_2.0/
├── src/main/
│   ├── java/
│   │   ├── application/
│   │   │   ├── Main.java                 # Entry point applicazione
│   │   │   ├── DB/                       # DAO e Database layer
│   │   │   │   ├── ConnessioneDB.java
│   │   │   │   ├── AnnuncioDAO.java
│   │   │   │   ├── UtentiDAO.java
│   │   │   │   ├── CarrelloDAO.java
│   │   │   │   └── ...
│   │   │   ├── Classe/                   # Entity classes
│   │   │   │   ├── Annuncio.java
│   │   │   │   ├── utente.java
│   │   │   │   ├── Messaggio.java
│   │   │   │   └── ...
│   │   │   ├── Enum/                     # Enumerations
│   │   │   ├── controls/                 # Controllers
│   │   │   └── messagistica/             # Chat system
│   │   └── schermata/                    # UI Screens
│   │       ├── SchermataPrincipale.java
│   │       ├── TopBar.java
│   │       ├── ProductCard.java
│   │       └── button/                   # Dialogs
│   └── resources/
│       ├── style/                        # CSS stylesheets
│       │   ├── application.css
│       │   └── principali.css
│       └── application/icons/            # SVG icons
├── pom.xml                               # Maven configuration
├── run_swapunina.bat                     # Windows launcher
└── README.md                             # This file
```

---

## 🗄️ Database Schema

### Tabelle Principali

- **utente** - Informazioni utenti
- **annuncio** - Annunci di vendita
- **oggetto** - Dettagli prodotti
- **carrello** - Carrelli utenti
- **messaggio** - Chat messaggi
- **recensione** - Valutazioni utenti
- **transazione** - Storico acquisti
- **scambio** - Gestione scambi

### Trigger Automatici

- Aggiornamento stato annuncio (VENDUTO/RITIRATO)
- Gestione automatica scadenze
- Statistiche vendite

---

## 🔧 Configurazione

### Database Connection

Modifica `ConnessioneDB.java` per configurare:

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/swapunina";
private static final String DB_USER = "postgres";
private static final String DB_PASSWORD = "tua_password";
```

### Cloudinary API

Imposta la variabile ambiente `CLOUDINARY_URL`:

```bash
export CLOUDINARY_URL="cloudinary://API_KEY:API_SECRET@CLOUD_NAME"
```

Oppure configurala in `CloudinaryService.java`.

---

## 🤝 Contribuire

Contributi benvenuti! Per favore:

1. Fai fork del progetto
2. Crea un branch feature (`git checkout -b feature/NuovaFunzionalita`)
3. Commit le modifiche (`git commit -m 'Aggiunge nuova funzionalità'`)
4. Push al branch (`git push origin feature/NuovaFunzionalita`)
5. Apri una Pull Request

### Linee Guida

- Segui lo stile di codice esistente
- Aggiungi commenti JavaDoc
- Testa le modifiche
- Aggiorna questo README se necessario

---

## 📋 Roadmap

### Versione 2.1 (Prossima)
- [ ] Notifiche desktop per nuovi messaggi
- [ ] Filtri avanzati ricerca
- [ ] Statistiche personali vendite
- [ ] Export PDF storico acquisti

### Versione 3.0 (Future)
- [ ] App mobile companion
- [ ] Sistema di offerte automatiche
- [ ] Integrazione pagamento elettronico
- [ ] geolocalizzazione per ritiro

---

## 📝 Licenza

Questo progetto è rilasciato sotto la Licenza MIT. Vedi il file [LICENSE](LICENSE) per dettagli.

---

## 👨‍💻 Autori

- **Mattia Borrelli** - Sviluppatore principale
  - GitHub: [@Mattiaborrelli4](https://github.com/Mattiaborrelli4)

---

## 📧 Contatti

Per domande, suggerimenti o segnalazioni bug:

- 📧 Email: [mattia.borrelli@studenti.unina.it](mailto:mattia.borrelli@studenti.unina.it)
- 🐛 Issues: [GitHub Issues](https://github.com/Mattiaborrelli4/swapunina_2.0/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/Mattiaborrelli4/swapunina_2.0/discussions)

---

<div align="center">

**Costruito con ❤️ per studenti universitari**

[⬆ Torna su](#-swapunina-20---marketplace-universitario)

</div>
