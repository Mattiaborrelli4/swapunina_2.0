# File Icone Locali - SwapUnina

## Icone Locali (Emoji Fallback)

Invece di dipendere da CDN esterni, usiamo emoji Unicode come fallback.
Le icone sono configurate in `TopBar.java` con fallback automatici.

### Mappa Icone → Emoji

| Funzione | CDN URL | Emoji Fallback |
|----------|---------|----------------|
| **Search** | flaticon.com/512/54/54481.png | 🔍 |
| **Account** | flaticon.com/512/1077/1077063.png | 👤 |
| **Cart** | flaticon.com/512/263/263142.png | 🛒 |
| **Messages** | flaticon.com/512/542/542638.png | 💬 |
| **Scambi** | flaticon.com/512/32/32213.png | 🔄 |
| **Add** | flaticon.com/512/1828/1828817.png | ➕ |
| **Logo** | /icons/logo.png | 📦 |

### Vantaggi delle Emoji Locali

✅ **Nessuna dipendenza esterna** - Funziona offline
✅ **Performance migliori** - Nessuna richiesta HTTP
✅ **Supporto universale** - Tutti i sistemi moderni supportano emoji
✅ **Dimensioni ridotte** - Nessun file da scaricare
✅ **Disponibilità immediata** - App pronta all'uso

### Nota per il Futuro

Per icone personalizzate, aggiungere file SVG/PNG in questa directory e aggiornare
`TopBar.java` per caricarli con `getClass().getResourceAsStream()`.
