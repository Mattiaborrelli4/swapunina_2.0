# 📚 INDICE DOCUMENTAZIONE COMPLETA - SwapUnina

<div align="center">

## 🎓 SwapUnina - Piattaforma Universitaria Marketplace

### Documentazione Tecnica e Utente Completa

**Versione 2.0** | **Data: 2026-02-03**

</div>

---

## 📋 INDICE DEI DOCUMENTI

| # | Documento | Descrizione | Target |
|---|-----------|-------------|--------|
| 1 | [README.md](../README.md) | Guida principale del progetto | Tutti |
| 2 | [GUIDA_UTENTE.md](GUIDA_UTENTE.md) | Manuale utente completo | Utenti Finali |
| 3 | [DOCUMENTAZIONE_COMPLETA.md](DOCUMENTAZIONE_COMPLETA.md) | Documentazione tecnica architetturale | Sviluppatori |
| 4 | [MANUALE_SVILUPPATORI.md](MANUALE_SVILUPPATORI.md) | Guida per contributori | Sviluppatori |
| 5 | [ISTRUZIONI_DATABASE_REMOTO.md](../ISTRUZIONI_DATABASE_REMOTO.md) | Configurazione database cloud | DevOps |
| 6 | [classi.sql](classi.sql) | Schema database completo | DBA |
| 7 | [Diagramma swapunina.drawio](Diagramma%20swapunina.drawio) | Diagramma classi UML | Architetti |
| 8 | [ER.mdj](ER.mdj) | Diagramma Entity-Relationship | DBA |

---

## 📖 PER CHI INIZIARE?

### 👨‍💻 Sei uno Sviluppatore

**Leggi in questo ordine:**
1. 📘 **[README.md](../README.md)** - Panoramica progetto e setup rapido
2. 🛠️ **[MANUALE_SVILUPPATORI.md](MANUALE_SVILUPPATORI.md)** - Convenzioni e workflow
3. 📚 **[DOCUMENTAZIONE_COMPLETA.md](DOCUMENTAZIONE_COMPLETA.md)** - Architettura tecnica
4. 🗄️ **[classi.sql](classi.sql)** - Schema database

### 👤 Sei un Utente

**Leggi in questo ordine:**
1. 📘 **[README.md](../README.md)** - Cos'è SwapUnina
2. 📖 **[GUIDA_UTENTE.md](GUIDA_UTENTE.md)** - Come usare l'applicazione

### 🗄️ Sei un Database Administrator

**Leggi in questo ordine:**
1. 🗄️ **[classi.sql](classi.sql)** - Script creazione tabelle
2. 📚 **[DOCUMENTAZIONE_COMPLETA.md](DOCUMENTAZIONE_COMPLETA.md)** - Sezione Database Schema
3. 🌐 **[ISTRUZIONI_DATABASE_REMOTO.md](../ISTRUZIONI_DATABASE_REMOTO.md)** - Configurazione remota

---

## 📊 DOCUMENTAZIONE PER ARGOMENTO

### 🎯 Funzionalità Implementate

#### 💫 Sistema di Scambio
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 4.1](DOCUMENTAZIONE_COMPLETA.md#41-sistema-di-scambio)
- **Schema DB:** [classi.sql - Tabella SCAMBIO](classi.sql#tabella-scambio)
- **Guida Utente:** [GUIDA_UTENTE.md - Scambio Oggetti](GUIDA_UTENTE.md#-scambio-oggetti)

#### 🛒 Carrello e Acquisti
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 4.2](DOCUMENTAZIONE_COMPLETA.md#42-gestione-carrello)
- **Guida Utente:** [GUIDA_UTENTE.md - Acquisti](GUIDA_UTENTE.md#-️-acquisti-e-carrello)

#### 💬 Messaggistica
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 4.3](DOCUMENTAZIONE_COMPLETA.md#43-messaggistica-criptata)
- **Schema DB:** [classi.sql - Tabella MESSAGGIO](classi.sql#tabella-messaggio)

### 🏗️ Architettura e Design

#### Architettura Three-Tier
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 1](DOCUMENTAZIONE_COMPLETA.md#1-architettura-del-sistema)
- **Diagrammi:** [Diagramma swapunina.drawio](Diagramma%20swapunina.drawio)

#### Database Schema
- **SQL Schema:** [classi.sql](classi.sql)
- **ER Diagram:** [ER.mdj](ER.mdj)
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 2](DOCUMENTAZIONE_COMPLETA.md#2-database-schema)

#### Pattern Design
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 1.2](DOCUMENTAZIONE_COMPLETA.md#12-pattern-architetturali-implementati)

### 🔒 Sicurezza

#### Autenticazione
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 5.1](DOCUMENTAZIONE_COMPLETA.md#51-autenticazione)
- **Guida Utente:** [GUIDA_UTENTE.md - Consigli Sicurezza](GUIDA_UTENTE.md#⚠️-consigli-di-sicurezza)

#### Criptaggio
- **Documentazione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 5.2](DOCUMENTAZIONE_COMPLETA.md#52-messaggistica-criptata)

### 🚀 Deployment e Configurazione

#### Setup Database
- **Guida Rapida:** [README.md - Configurazione Database](../README.md#️-configurazione-database)
- **Database Remoto:** [ISTRUZIONI_DATABASE_REMOTO.md](../ISTRUZIONI_DATABASE_REMOTO.md)
- **ElephantSQL:** [ISTRUZIONI_DATABASE_REMOTO.md - Opzione 1](../ISTRUZIONI_DATABASE_REMOTO.md#opzione-1-database-postgresql-cloud-gratuito-consigliato)

#### Build e Deploy
- **Sviluppatori:** [MANUALE_SVILUPPATORI.md - Deploy](MANUALE_SVILUPPATORI.md#7-deploy)
- **Produzione:** [DOCUMENTAZIONE_COMPLETA.md - Sezione 7](DOCUMENTAZIONE_COMPLETA.md#7-deployment)

---

## 🎓 QUICK REFERENCE

### Comandi Maven Essenziali

```bash
# Compila
mvn clean compile

# Esegui
mvn exec:java -Dexec.mainClass="application.Main"

# Test
mvn test

# Package
mvn clean package
```

### Connessione Database

```bash
# Locale (default)
Host: localhost
Port: 5432
Database: postgres
User: postgres
Password: 1234

# Remoto (da configurare)
# Vedi ISTRUZIONI_DATABASE_REMOTO.md
```

### Struttura Progetto

```
swapunina-main/
├── src/                    # Codice sorgente
├── spiegazioni/            # Documentazione tecnica
├── librerie/               # Librerie esterne
└── README.md              # Guida principale
```

---

## 📚 PER APPROFONDIRE

### Per Sviluppatori Java
- [Oracle Java Tutorial](https://docs.oracle.com/javase/tutorial/)
- [JavaFX Documentation](https://openjfx.io/)
- [PostgreSQL JDBC Docs](https://jdbc.postgresql.org/documentation/)

### Per Database Administrator
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [ElephantSQL Documentation](https://www.elephantsql.com/docs/)
- [pgAdmin Documentation](https://www.pgadmin.org/docs/)

### Per UI/UX Design
- [Material Design Guidelines](https://material.io/design)
- [JavaFX CSS Reference](https://openjfx.io/javadoc/17/javafx.scene/javafx-scene/DocStub.html)

---

## 🔄 AGGIORNAMENTI DOCUMENTAZIONE

### Versionamento

La documentazione segue il versionamento del progetto:
- **v1.0** - Release iniziale
- **v2.0** - Aggiunto sistema di scambio
- **v2.1** - Miglioramenti UI e fix

### Contribuire

Per contribuire alla documentazione:
1. Leggi [MANUALE_SVILUPPATORI.md](MANUALE_SVILUPPATORI.md)
2. Segui le convenzioni di scrittura
3. Fai pull request con descrizione chiara

---

## 📞 SUPPORTO E CONTATTI

### Per Problemi Tecnici
- 📧 **Email Dev:** dev@swapunina.it
- 🐛 **Bug Report:** [Issues GitHub](https://github.com/swapunina/issues)

### Per Supporto Utente
- 📧 **Email Support:** support@swapunina.it
- 💬 **Wiki:** [Documentazione Online](https://github.com/swapunina/wiki)

---

<div align="center">

## 📚 SwapUnina - Documentazione Completa

### **Documentazione professionale per utenti, sviluppatori e amministratori**

**Versione:** 2.0
**Data:** 2026-02-03
**Team:** SwapUnina Development Team

---

#### 🎓 Trova la documentazione giusta per te!

| Se sei... | Leggi prima... |
|-----------|----------------|
| 👨‍💻 Sviluppatore | [MANUALE_SVILUPPATORI.md](MANUALE_SVILUPPATORI.md) |
| 👤 Utente | [GUIDA_UTENTE.md](GUIDA_UTENTE.md) |
| 🗄️ DBA | [classi.sql](classi.sql) |
| 🏗️ Architetto | [DOCUMENTAZIONE_COMPLETA.md](DOCUMENTAZIONE_COMPLETA.md) |

Made with ❤️ by SwapUnina Development Team

</div>
