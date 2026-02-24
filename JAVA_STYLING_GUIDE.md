# SwapUniNa - Guida Rapida allo Stile in Java

## Come Applicare lo Stile in JavaFX

### 1. Importare i CSS nel codice

```java
// Nel tuo controller o application
scene.getStylesheets().addAll(
    getClass().getResource("/style/principali.css").toExternalForm(),
    getClass().getResource("/style/typography.css").toExternalForm()
);
```

### 2. Applicare Classi di Tipografia

```java
// Titolo principale
Label titleLabel = new Label("SwapUniNa");
titleLabel.getStyleClass().addAll("text-h1", "fw-bold", "text-primary");

// Sottotitolo
Label subtitleLabel = new Label("Piattaforma di scambio universitario");
subtitleLabel.getStyleClass().addAll("text-body-lg", "text-secondary");

// Prezzo prodotto
Label priceLabel = new Label("25,00 EUR");
priceLabel.getStyleClass().addAll("text-h3", "fw-bold", "text-brand");

// Metadata
Label metaLabel = new Label("Pubblicato 2 ore fa");
metaLabel.getStyleClass().addAll("text-caption", "text-tertiary");
```

### 3. Applicare Spaziature

```java
VBox container = new VBox();
container.getStyleClass().addAll("p-xl", "bg-white", "rounded-xl");

// Elemento con margin personalizzato
Label label = new Label("Titolo sezione");
label.getStyleClass().addAll("text-h3", "mb-md");
```

### 4. Creare Button con Varianti

```java
Button primaryButton = new Button("Accedi");
primaryButton.getStyleClass().addAll("btn-lg", "bg-brand", "text-white");

Button secondaryButton = new Button("Annulla");
secondaryButton.getStyleClass().addAll("btn-lg", "bg-secondary", "text-primary");

Button smallButton = new Button("Dettagli");
smallButton.getStyleClass().addAll("btn-sm", "rounded-md");
```

### 5. Badge

```java
Label badge = new Label("Nuovo");
badge.getStyleClass().addAll("badge", "badge-success");

Label categoryBadge = new Label("Libri");
categoryBadge.getStyleClass().addAll("badge", "badge-primary");
```

### 6. Card Variants

```java
VBox card = new VBox();
card.getStyleClass().addAll("p-lg", "bg-white", "rounded-xl", "card-elevated");
```

### 7. Input Fields

```java
TextField searchField = new TextField();
searchField.setPromptText("Cerca prodotti...");
searchField.getStyleClass().addAll("input-lg", "rounded-md");
```

---

## Riferimento Classi Utilità

### Tipografia

| Classe | Effetto |
|--------|---------|
| `text-display-xl` | 36px, bold |
| `text-display-lg` | 32px, bold |
| `text-h1` | 28px, bold |
| `text-h2` | 24px, bold |
| `text-h3` | 20px, bold |
| `text-h4` | 18px, bold |
| `text-body-lg` | 16px |
| `text-body` | 14px |
| `text-body-sm` | 13px |
| `text-caption` | 12px |
| `text-overline` | 11px, uppercase |

### Font Weight

| Classe | Effetto |
|--------|---------|
| `fw-light` | 300 |
| `fw-regular` | 400 |
| `fw-medium` | 500 |
| `fw-semibold` | 600 |
| `fw-bold` | 700 |

### Colori Testo

| Classe | Effetto |
|--------|---------|
| `text-primary` | #1e293b |
| `text-secondary` | #64748b |
| `text-tertiary` | #94a3b8 |
| `text-white` | #ffffff |
| `text-brand` | #3b82f6 |
| `text-success` | #166534 |
| `text-error` | #dc2626 |
| `text-warning` | #92400e |
| `text-info` | #1e40af |

### Colori Sfondo

| Classe | Effetto |
|--------|---------|
| `bg-primary` | #f8fafc |
| `bg-secondary` | #f1f5f9 |
| `bg-white` | #ffffff |
| `bg-brand` | #3b82f6 |
| `bg-success` | #dcfce7 |
| `bg-error` | #fee2e2 |
| `bg-warning` | #fef3c7 |
| `bg-info` | #dbeafe |

### Padding

| Classe | Effetto |
|--------|---------|
| `p-0` | 0 |
| `p-xs` | 4px |
| `p-sm` | 8px |
| `p-md` | 12px |
| `p-lg` | 16px |
| `p-xl` | 20px |
| `p-2xl` | 24px |
| `p-3xl` | 32px |
| `p-4xl` | 40px |

### Padding direzionale

| Classe | Effetto |
|--------|---------|
| `pt-sm`, `pb-sm`, `pl-sm`, `pr-sm` | 8px |
| `pt-md`, `pb-md`, `pl-md`, `pr-md` | 12px |
| `pt-lg`, `pb-lg`, `pl-lg`, `pr-lg` | 16px |
| `pt-xl`, `pb-xl`, `pl-xl`, `pr-xl` | 20px |

### Margin (stesso pattern di padding)

| Classe | Effetto |
|--------|---------|
| `m-0` | 0 |
| `m-xs` | 4px |
| `m-sm` | 8px |
| `m-md` | 12px |
| `m-lg` | 16px |
| `m-xl` | 20px |
| `m-2xl` | 24px |
| `mt-`, `mb-`, `ml-`, `mr-` | Directional |

### Border Radius

| Classe | Effetto |
|--------|---------|
| `rounded-none` | 0 |
| `rounded-sm` | 4px |
| `rounded-md` | 8px |
| `rounded-lg` | 12px |
| `rounded-xl` | 16px |
| `rounded-2xl` | 20px |
| `rounded-full` | 9999px |

### Button Size

| Classe | Padding | Font-size |
|--------|---------|-----------|
| `btn-xs` | 4px 8px | 11px |
| `btn-sm` | 6px 12px | 12px |
| `btn-md` | 8px 16px | 13px |
| `btn-lg` | 12px 20px | 14px |
| `btn-xl` | 14px 28px | 16px |

---

## Esempi Completi

### Product Card

```java
VBox productCard = new VBox();
productCard.getStyleClass().addAll("bg-white", "rounded-xl", "card-elevated");

// Immagine
ImageView imageView = new ImageView(productImage);
imageView.setFitHeight(200);
imageView.setFitWidth(320);

// Content
VBox content = new VBox();
content.getStyleClass().addAll("p-lg");
content.setSpacing(8);

// Titolo
Label title = new Label(product.getName());
title.getStyleClass().addAll("text-h4", "fw-bold", "text-primary");
title.setWrapText(true);

// Prezzo
Label price = new Label(String.format("%.2f EUR", product.getPrice()));
price.getStyleClass().addAll("text-h3", "fw-bold", "text-brand");

// Descrizione
Label description = new Label(product.getDescription());
description.getStyleClass().addAll("text-body-sm", "text-secondary");
description.setWrapText(true);

// Metadata
Label metadata = new Label("Pubblicato " + timeAgo);
metadata.getStyleClass().addAll("text-caption", "text-tertiary");

content.getChildren().addAll(title, price, description, metadata);
productCard.getChildren().addAll(imageView, content);
```

### Form Field

```java
VBox formGroup = new VBox();
formGroup.getStyleClass().addAll("mb-md");
formGroup.setSpacing(4);

Label label = new Label("Email");
label.getStyleClass().addAll("text-body-sm", "fw-medium", "text-primary");

TextField emailField = new TextField();
emailField.setPromptText("tuo.email@unina.it");
emailField.getStyleClass().addAll("input-md", "rounded-md");

Label errorLabel = new Label("Email non valida");
errorLabel.getStyleClass().addAll("text-caption", "text-error");
errorLabel.setVisible(false);

formGroup.getChildren().addAll(label, emailField, errorLabel);
```

### Dialog Box

```java
VBox dialogBox = new VBox();
dialogBox.getStyleClass().addAll("p-2xl", "bg-white", "rounded-xl", "card-elevated");
dialogBox.setSpacing(16);
dialogBox.setMaxWidth(400);

Label dialogTitle = new Label("Conferma eliminazione");
dialogTitle.getStyleClass().addAll("text-h3", "fw-bold", "text-primary");

Label dialogMessage = new Label("Sei sicuro di voler eliminare questo annuncio?");
dialogMessage.getStyleClass().addAll("text-body", "text-secondary");

HBox buttonBox = new HBox();
buttonBox.setSpacing(8);
buttonBox.setAlignment(Pos.CENTER_RIGHT);

Button cancelBtn = new Button("Annulla");
cancelBtn.getStyleClass().addAll("btn-md", "bg-secondary", "text-primary", "rounded-md");

Button confirmBtn = new Button("Elimina");
confirmBtn.getStyleClass().addAll("btn-md", "bg-error", "text-white", "rounded-md");

buttonBox.getChildren().addAll(cancelBtn, confirmBtn);
dialogBox.getChildren().addAll(dialogTitle, dialogMessage, buttonBox);
```

---

## Best Practices

1. **Usa le classi utilità invece di style inline**
   ```java
   // Buono
   label.getStyleClass().addAll("text-h3", "fw-bold");
   // Evita
   label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
   ```

2. **Combina più classi per effetti complessi**
   ```java
   button.getStyleClass().addAll("btn-lg", "bg-brand", "text-white", "rounded-md");
   ```

3. **Usa spaziature coerenti**
   ```java
   // Card spacing
   vBox.setSpacing(12); // -fx-space-md equivalent
   // Section spacing
   vBox.setSpacing(24); // -fx-space-2xl equivalent
   ```

4. **Rispetta la gerarchia visiva**
   ```java
   // Titolo sezione
   sectionTitle.getStyleClass().addAll("text-h2", "fw-bold", "mb-md");
   // Sottotitolo
   subtitle.getStyleClass().addAll("text-body-lg", "text-secondary", "mb-sm");
   ```

5. **Usa i colori semantici**
   ```java
   // Per stato successo
   successLabel.getStyleClass().add("text-success");
   // Per stato errore
   errorLabel.getStyleClass().add("text-error");
   // Per prezzo/brand
   priceLabel.getStyleClass().add("text-brand");
   ```
