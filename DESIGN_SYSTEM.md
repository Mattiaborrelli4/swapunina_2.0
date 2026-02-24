# SwapUniNa - Design System

## Indice
1. [Sistema Tipografico](#sistema-tipografico)
2. [Sistema Spaziature](#sistema-spaziature)
3. [Sistema Colori](#sistema-colori)
4. [Border Radius](#border-radius)
5. [Classi Utilità](#classi-utilità)

---

## Sistema Tipografico

### Font Family
```css
-fx-font-family: "Segoe UI", "System", "Roboto", "Arial", sans-serif;
```

### Scala Font Size

| Classe | Dimensione | Uso |
|--------|-----------|-----|
| `.text-display-xl` | 36px | Hero principale, titolo hero |
| `.text-display-lg` | 32px | Titolo pagina principale |
| `.text-h1` | 28px | Titolo di sezione |
| `.text-h2` | 24px | Sottotitolo di sezione |
| `.text-h3` | 20px | Titolo card/componente |
| `.text-h4` | 18px | Sottotitolo card |
| `.text-body-lg` | 16px | Testo importante, descrizioni |
| `.text-body` | 14px | Testo standard (default) |
| `.text-body-sm` | 13px | Testo secondario |
| `.text-caption` | 12px | Caption, metadata |
| `.text-overline` | 11px | Label, overline (uppercase) |

### Font Weight

| Classe | Weight | Uso |
|--------|--------|-----|
| `.fw-light` | 300 | Testo leggero |
| `.fw-regular` | 400 | Testo normale |
| `.fw-medium` | 500 | Testo semi-grassetto |
| `.fw-semibold` | 600 | Testo semi-grassetto forte |
| `.fw-bold` | 700 | Titoli, testo importante |

### Esempi di Utilizzo in JavaFX

```java
// Titolo principale
Label title = new Label("Benvenuto su SwapUniNa");
title.getStyleClass().addAll("text-h1", "fw-bold");

// Descrizione
Label description = new Label("Scopri tutti gli annunci...");
description.getStyleClass().add("text-body-lg");

// Metadata
Label meta = new Label("Pubblicato 2 ore fa");
meta.getStyleClass().addAll("text-caption", "fw-medium");
```

---

## Sistema Spaziature

### Base Unit: 8px

Tutte le spaziature sono multipli di 4px, con base su 8px.

| Token CSS | Valore | Uso |
|-----------|--------|-----|
| `-fx-space-xs` | 4px | Spaziatura minima |
| `-fx-space-sm` | 8px | Spaziatura piccola |
| `-fx-space-md` | 12px | Spaziatura media |
| `-fx-space-lg` | 16px | Spaziatura standard |
| `-fx-space-xl` | 20px | Spaziatura larga |
| `-fx-space-2xl` | 24px | Spaziatura extra larga |
| `-fx-space-3xl` | 32px | Spaziatura section |
| `-fx-space-4xl` | 40px | Spaziatura grande |
| `-fx-space-5xl` | 48px | Spaziatura molto grande |
| `-fx-space-6xl` | 64px | Spaziatura hero |

### Linee Guida per Padding

| Componente | Padding | Note |
|-----------|---------|------|
| Button piccolo | `8px 16px` | Icone, azioni secondarie |
| Button standard | `12px 20px` | Azioni principali |
| Button grande | `12px 24px` | CTA principali |
| Card | `20px` | Padding interno card |
| Input field | `8px 12px` | Campi form |
| Container | `24px` | Container principale |

### Linee Guida per Gap/Spacing

| Contesto | Gap | Note |
|----------|-----|------|
| Grid items | `24px` | Spaziatura tra card |
| Form fields | `12px` | Tra campi form |
| Button group | `8px` | Bottoni affiancati |
| Stack elements | `12px` | Elementi in vertical box |

---

## Sistema Colori

### Background

| Token | Valore | Uso |
|-------|--------|-----|
| `-fx-color-bg-primary` | `#f8fafc` | Sfondo principale |
| `-fx-color-bg-secondary` | `#f1f5f9` | Sfondo secondario |
| `-fx-color-bg-tertiary` | `#e2e8f0` | Border, divider |
| `-fx-color-bg-white` | `#ffffff` | Card, container |

### Text

| Token | Valore | Uso |
|-------|--------|-----|
| `-fx-color-text-primary` | `#1e293b` | Testo principale |
| `-fx-color-text-secondary` | `#64748b` | Testo secondario |
| `-fx-color-text-tertiary` | `#94a3b8` | Placeholder, disabilitato |
| `-fx-color-text-inverse` | `#ffffff` | Testo su sfondo scuro |

### Brand

| Token | Valore | Uso |
|-------|--------|-----|
| `-fx-color-brand-primary` | `#3b82f6` | Brand principale |
| `-fx-color-brand-dark` | `#2563eb` | Hover stato |
| `-fx-color-brand-light` | `#60a5fa` | Stato attivo/focus |
| `-fx-color-accent` | `#10b981` | CTA, azioni positive |
| `-fx-color-accent-dark` | `#059669` | Hover CTA |

### Semantic

| Token | Valore | Uso |
|-------|--------|-----|
| `-fx-color-success-bg` | `#dcfce7` | Badge successo |
| `-fx-color-success-text` | `#166534` | Testo successo |
| `-fx-color-warning-bg` | `#fef3c7` | Badge warning |
| `-fx-color-warning-text` | `#92400e` | Testo warning |
| `-fx-color-error-bg` | `#fee2e2` | Badge errore |
| `-fx-color-error-text` | `#dc2626` | Testo errore |
| `-fx-color-info-bg` | `#dbeafe` | Badge info |
| `-fx-color-info-text` | `#1e40af` | Testo info |

---

## Border Radius

| Token | Valore | Uso |
|-------|--------|-----|
| `-fx-radius-sm` | 4px | Piccoli elementi |
| `-fx-radius-md` | 8px | Button standard, input |
| `-fx-radius-lg` | 12px | Button grandi, card |
| `-fx-radius-xl` | 16px | Card principali |
| `-fx-radius-2xl` | 20px | Container arrotondati |
| `-fx-radius-full` | 9999px | Pill, circle |

---

## Classi Utilità

### Spacing

```css
/* Margin */
.m-0 { -fx-margin: 0; }
.m-xs { -fx-margin: 4px; }
.m-sm { -fx-margin: 8px; }
.m-md { -fx-margin: 12px; }
.m-lg { -fx-margin: 16px; }
.m-xl { -fx-margin: 20px; }
.m-2xl { -fx-margin: 24px; }

/* Padding */
.p-0 { -fx-padding: 0; }
.p-xs { -fx-padding: 4px; }
.p-sm { -fx-padding: 8px; }
.p-md { -fx-padding: 12px; }
.p-lg { -fx-padding: 16px; }
.p-xl { -fx-padding: 20px; }
.p-2xl { -fx-padding: 24px; }
```

### Text Color

```css
.text-primary { -fx-text-fill: -fx-color-text-primary; }
.text-secondary { -fx-text-fill: -fx-color-text-secondary; }
.text-brand { -fx-text-fill: -fx-color-brand-primary; }
.text-white { -fx-text-fill: -fx-color-text-inverse; }
```

### Text Alignment

```css
.text-center { -fx-text-alignment: center; }
.text-left { -fx-text-alignment: left; }
.text-right { -fx-text-alignment: right; }
```

---

## Esempi di Utilizzo

### Product Card Standard

```java
VBox card = new VBox();
card.getStyleClass().add("product-card");

// Titolo
Label title = new Label("Libro di Analisi");
title.getStyleClass().addAll("text-h4", "fw-bold");

// Prezzo
Label price = new Label("25.00");
price.getStyleClass().addAll("text-h3", "fw-bold", "text-brand");

// Descrizione
Label desc = new Label("Libro usato in ottime condizioni...");
desc.getStyleClass().add("text-body");
```

### Button Standard

```java
Button btn = new Button("Scopri di più");
btn.getStyleClass().add("details-button");
// Già configurato nel CSS con:
// - padding: 12px 20px
// - border-radius: 8px
// - font-weight: 500
```

### Form Field

```java
TextField input = new TextField();
input.setPromptText("Cerca prodotti...");
input.getStyleClass().add("search-field");
// Già configurato nel CSS con:
// - padding: 12px 20px
// - border-radius: 30px 0 0 30px
// - font-size: 16px
```

---

## Miglioramenti Applicati

### 1. Eliminazione Ridondanze
- Rimossi 3 definizioni duplicate di `.product-card`
- Unificate le definizioni della scrollbar
- Consolidati gli stili per button

### 2. Sistema Token Centralizzato
- Tutti i colori definiti come variabili in `.root`
- Spaziature definiti come token riutilizzabili
- Border radius standardizzati

### 3. Gerarchia Tipografica Chiara
- 11 livelli di dimensione testo definiti
- 5 livelli di font weight
- Classi per ogni combinazione

### 4. Organizzazione Modulare
- 17 sezioni logiche nel CSS
- Commenti chiari per ogni sezione
- Utility class separate

---

## Note JavaFX

### Variabili CSS in JavaFX
JavaFX supporta variabili CSS definite in `.root` ma con limitazioni:
- Le variabili funzionano per riferimenti nella stessa proprietà
- Esempio: `-fx-padding: -fx-space-md` NON funziona direttamente
- Invece usa il valore: `-fx-padding: 12px`

### Workaround
Per ora, i token sono definiti come riferimento. In JavaFX:
1. Usa i valori numerici diretti nel CSS
2. Oppure definisci una classe per ogni combinazione comune

### Classi Tipografiche
Le classi come `.text-h1`, `.text-body` etc. possono essere applicate direttamente:
```java
label.getStyleClass().add("text-h1");
```
