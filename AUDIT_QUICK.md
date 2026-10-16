╔════════════════════════════════════════════════════════╗
║        SWAPUNAIN FASE 2 - AUDIT SUMMARY REPORT        ║
╚════════════════════════════════════════════════════════╝

P0 - SICUREZZA CRITICA:
  ❌ Password in chiaro UserDAO (login/create/changePassword)
  ❌ CORS aperto (*) - needs trusted origins restriction
  ❌ Token senza scadenza - needs JWT + expiration
  ⚠️ Endpoint senza auth parziale

P1 - FUNZIONALITÀ MANCANTI:
  ❌ aggiornaAnnuncio - placeholder only
  ❌ cancellaAnnuncio - placeholder only
  ❌ getAnnunciUtente - returns empty list, not implemented
  ❌ Search TODO in Main.java - no API integration

P2 - STABILITÀ:
  ⚠️ Thread JavaFX - verifica operazioni DB in background
  ⚠️ Risorse non chiuse - mostly addressed in DAOs
  ❌ Test mancanti - no unit tests

P3 - DESIGN:
  ❌ Palette colori non uniforme - #1769AA, #111827, #EAF4FF, #FFFFFF, #64748B
  ⚠️ #1769AA only in typography.css:19
  ⚠️ #64748B only in button.css:27
  ❌ #EAF4FF not found in any CSS

BUILD: mvnw compile ✅ | mvnw test ⚠️ (Java 24 / Maven 3.11 mismatch)