# SWAPUNAIN AUDIT SUMMARY - FASE 2

## Executive Summary
This audit covers security vulnerabilities, functional gaps, JavaFX stability issues, and design consistency for the SwapUnina desktop application. Based on analysis of 86 Java source files, the codebase shows mixed progress: 4 critical security issues from Phase 1 have been addressed (AES-GCM, master password from env, codici in chiaro, SecureRandom), but significant P0-P3 issues remain.

---

## � PRIORITY P0: CRITICAL SECURITY

### P0-1: Password in Chiaro in UserDAO [CRITICO] - NOT FULLY RESOLVED
- **File**: `src/main/java/application/DB/UserDAO.java`
- **Issues**:
  - `login()` at line 94: Compares password in cleartext (`stmt.setString(2, password)`)
  - `createUser()` at line 128: Stores password in cleartext (`stmt.setString(4, user.getPassword())`)
  - `changePassword()` at line 236: Updates password in cleartext (`stmt.setString(1, newPassword)`)
- **Risk**: Massive account theft, impersonation, unauthorized access to all features
- **Status**: ✅ **IN PROGRESS** - Hashing framework (BCrypt/SCRYPT) not yet implemented
- **Reference**: AUDIT_SICUREZZA.md §1-2, PIANO_INTERVENTI.md §Problema 1
- **Fix Required**: 
  - Hash passwords with BCrypt before storage
  - Verify hash during login (BCrypt.checkpw), not compare cleartext
  - Never return passwords in logs/reports

### P0-2: CORS Aperto [MEDIUM] - NOT RESOLVED
- **File**: `src/main/java/application/api/SwapUninaAPI.java:94`
- **Issue**: `response.header("Access-Control-Allow-Origin", "*")` allows all origins
- **Risk**: API accessible from malicious scripts, CSRF potential
- **Status**: ✅ **IN PROGRESS** - Currently configured as `*` needs restriction to trusted origins
- **Reference**: AUDIT_SICUREZZA.md §5, PIANO_INTERVENTI.md §Problema 2
- **Fix Required**: Restrict to `http://localhost:8081` (or specific trusted origins)

### P0-3: Token senza Scadenza [MEDIUM] - NOT RESOLVED
- **File**: `src/main/java/application/api/SwapUninaAPI.java:201`
- **Issue**: `String token = UUID.randomUUID().toString(); activeTokens.put(token, user.getId());` - no expiry, no revocation mechanism
- **Risk**: Tokens never expire, logout only removes from in-memory map, impersonation risk
- **Status**: ✅ **IN PROGRESS** - JWT implementation with expiration not yet deployed
- **Reference**: AUDIT_SICUREZZA.md §9, PIANO_INTERVENTI.md §Problema 3
- **Fix Required**: Implement JWT with expiration time + refresh token mechanism

### P0-4: Endpoint Senza Autenticazione [MEDIUM]
- **File**: `src/main/java/application/api/SwapUninaAPI.java`
- **Issues**:
  - `getAnnunci`, `getAnnunciUtente` callable without token
  - `getCarrello` requires token but validation is weak (just checks map)
- **Risk**: Unauthorized data exposure
- **Status**: ✅ **PARTIALLY ADDRESSED** - Some endpoints have basic token check, but `getAnnunciUtente` returns empty list
- **Reference**: AUDIT_SICUREZZA.md §6

### P0-5: Impersonazione Utenti [HIGH]
- **File**: `src/main/java/application/api/SwapUninaAPI.java:59, 201-220`
- **Issue**: Simple `Map<String, Integer> activeTokens` without user property validation - any token can access any user's data
- **Risk**: Full impersonation if token is compromised
- **Status**: ✅ **IN PROGRESS** - JWT should include user ID and claims for proper validation

---

## � PRIORITY P1: FUNCTIONAL GAPS

### P1-1: aggiornaAnnuncio e cancellaAnnuncio Non Implementati [HIGH]
- **File**: `src/main/java/application/api/SwapUninaAPI.java:334-351`
- **Issues**:
  - `aggiornaAnnuncio()`: Placeholder returning "Aggiornamento non ancora implementato"
  - `cancellaAnnuncio()`: Placeholder returning "Cancellazione non ancora implementata"
- **Status**: ✅ **NOT IMPLEMENTED**
- **Reference**: PIANO_INTERVENTI.md §Problema 1
- **Fix Required**:
  - `aggiornaAnnuncio`: Validate permissions, call `annuncioDAO.aggiornaStatoAnnuncio()`
  - `cancellaAnnuncio`: Validate permissions, call `annuncioDAO.eliminaAnnuncio()`

### P1-2: getAnnunciUtente Incompleto [HIGH]
- **File**: `src/main/java/application/api/SwapUninaAPI.java:353-357`
- **Issue**: Returns `Collections.emptyList()` without querying the database
- **Status**: ✅ **NOT IMPLEMENTED**
- **Reference**: PIANO_INTERVENTI.md §Problema 2
- **Fix Required**: Implement `annuncioDAO.getAnnunciPerVenditore(userId)` query

### P1-3: Search TODO in Main.java [MEDIUM]
- **File**: `src/main/java/application/Main.java:330-338`
- **Issue**: `configuraGestoreRicerca()` has TODO comment - search query logged but no API call
- **Current behavior**: Prints "🔍 Ricerca effettuata: " + query to console only
- **Status**: ✅ **NOT IMPLEMENTED**
- **Reference**: PIANO_INTERVENTI.md §Problema 3
- **Fix Required**: Connect search bar with API endpoint `GET /api/annunci?search=...`

---

## � PRIORITY P2: STABILITY AND ARCHITECTURE

### P2-1: Thread JavaFX e Operazioni Lente [MEDIUM]
- **File**: Tutte le schermate JavaFX
- **Issue**: Database operations may block the Application Thread
- **Status**: ✅ **NEEDS VERIFICATION**
- **Reference**: PIANO_INTERVENTI.md §Problema 1
- **Fix Required**: Verify all DB operations run in background thread, not JavaFX Application Thread

### P2-2: Risorse Non Chiuse [MEDIUM]
- **File**: DAO files (AnnuncioDAO, MessaggioDAO, OffertaDAO, etc.)
- **Issue**: Some connections/resultsets not closed with try-with-resources
- **Status**: ✅ **MOSTLY ADDRESSED** - Majority of DAOs use try-with-resources, but printStackTrace in catch blocks may expose resources
- **Reference**: PIANO_INTERVENTI.md §Problema 2
- **Fix Required**: Add complete try-with-resources, verify ConnessioneDB handling

### P2-3: Test Mancanti [MEDIUM]
- **File**: Various DAOs, no dedicated test directory structure
- **Status**: ✅ **NOT IMPLEMENTED**
- **Reference**: PIANO_INTERVENTI.md §Problema 3
- **Reference files**: `test_backup/MessaggioDAOTest.java`, `test_backup/OffertaDAOTest.java`
- **Fix Required**: Create unit tests for UserDAO, CarrelloDAO, AnnuncioDAO main methods

---

## � PRIORITY P3: GRAFICA E USABILITÀ

### P3-1: Coerenza Palette Colori [MEDIUM]
- **Defined palette**: `#1769AA` (blu primario), `#111827` (nero grafite), `#EAF4FF` (azzurro chiaro), `#FFFFFF` (bianco), `#64748B` (grigio secondario)
- **File**: TopBar.java, controllers, FXML, CSS
- **Current state** (from CSS analysis):
  - `#FFFFFF` (white) heavily used - background for buttons, cards, chat
  - `#111827` (grafite) used for text fill in button.css
  - `#EAF4FF` (azzurro chiaro) NOT found in CSS
  - `#1769AA` (blu primario) found only in typography.css: `-fx-text-fill: #1769AA` on line 19
  - `#64748B` (grigio secondario) found in button.css: `-fx-prompt-text-fill: #64748B`
- **Status**: ✅ **NOT RESOLVED** - Palette inconsistently applied
- **Reference**: PIANO_INTERVENTI.md §Problema 1, DESIGN_SYSTEM.md §Sistema Colori
- **Fix Required**: Uniformare tutti i componenti alla palette definita

### P3-2: Stati Vuoti e Caricamento [LOW]
- **File**: Schermate elenco, carrello, chat
- **Issue**: Placeholder implementations returning empty lists/data without user-friendly messages
- **Status**: ✅ **PARTIALLY ADDRESSED**
- **Fix Required**: Show "Nessun dato disponibile" friendly messages

---

## 📊 TRACKED vs NEW ISSUES

### Already Tracked (from previous audits)
| ID | Priority | Problem | File | Status |
|---|---|---|---|---|
| P0-1 | P0 | Password chiaro UserDAO | UserDAO.java | In attesa |
| P0-2 | P0 | CORS aperto | SwapUninaAPI.java | In attesa |
| P0-3 | P0 | Token senza scadenza | SwapUninaAPI.java | In attesa |
| P1-1 | P1 | aggiornaAnnuncio | API.java | In attesa |
| P1-2 | P1 | getAnnunciUtente | API.java | In attesa |
| P2-1 | P2 | Thread JavaFX | Main.java | In attesa |
| P3-1 | P3 | Palette colori | Tutti CSS | In attesa |

### New Issues Identified
- **P0-4**: Endpoint senza autenticazione parziale - `getAnnunciUtente` returns empty list
- **P0-5**: Impersonazione utenti - token map senza validazione proprietà utente
- **P1-3**: Search TODO in Main.java - no API integration
- **P2-2**: Alcuni DAO hanno printStackTrace che espone dettagli implementazione
- **P3-2**: Stati vuoti non amichevoli in schermate

---

## 📋 COMPLETE ACTION CHECKLIST

### P0 - Immediate (Security Critical)
- [ ] Implement BCrypt hashing in UserDAO: login, createUser, changePassword
- [ ] Restrict CORS to trusted origins only (remove `*`)
- [ ] Implement JWT with expiration + refresh token
- [ ] Add token validation per-endpoint (verify token matches authenticated user)

### P1 - Short Term (Functionality)
- [ ] Implement `aggiornaAnnuncio` handler calling `annuncioDAO.aggiornaStatoAnnuncio()`
- [ ] Implement `cancellaAnnuncio` handler calling `annuncioDAO.eliminaAnnuncio()`
- [ ] Implement `getAnnunciUtente` querying `annuncioDAO.getAnnunciPerVenditore(userId)`
- [ ] Connect Main.java search handler with `GET /api/annunci?search=...` API endpoint

### P2 - Medium Term (Stability)
- [ ] Verify all JavaFX DB operations run in background thread
- [ ] Add complete try-with-resources in all DAOs
- [ ] Replace printStackTrace with secure logging (LOGGER)
- [ ] Fix System.exit() in GUI → use Platform.exit()

### P3 - Medium Term (Design)
- [ ] Uniformare colori palette: #1769AA, #111827, #EAF4FF, #FFFFFF, #64748B
- [ ] Replace hardcoded hex colors in CSS with palette tokens
- [ ] Ensure all components respect design system colors

---

## 🔧 FILES REFERENCE SUMMARY

| Category | Files Affected |
|---|---|
| **UserDAO** | `src/main/java/application/DB/UserDAO.java:93-136` (login, createUser, changePassword) |
| **SwapUninaAPI** | `src/main/java/application/api/SwapUninaAPI.java:59-203` (tokens), `:334-351` (aggiorna/cancella), `:353-357` (getAnnunciUtente) |
| **Main.java** | `src/main/java/application/Main.java:330-338` (search TODO) |
| **CSS/Palette** | `src/main/resources/style/*.css`, `TopBar.java`, controllers |
| **AnnuncioDAO** | `src/main/java/application/DB/AnnuncioDAO.java:322-354` (aggiornaStato, elimina) |
| **ConfigManager** | `src/main/java/application/config/ConfigManager.java:30-80` (master password) |
| **Test files** | `test_backup/MessaggioDAOTest.java`, `test_backup/OffertaDAOTest.java` |

---

## ✅ WHAT'S WORKING
- PreparedStatement used everywhere (no SQL injection in queries)
- Try-with-resources in most DAOs
- AES-256-GCM encryption implemented (Report_Sicurezza_Migliorata.md)
- Master password from environment variable (not hardcoded)
- SecureRandom for code generation
- BCrypt mentioned as intended correction (not yet implemented for UserDAO)
- Connection pool (HikariCP) configured
- Java.time API used (no old java.util.Date)

---

## ⚠️ CRITICAL FINDINGS
1. **Passwords still in cleartext** - UserDAO login/create/change all use cleartext
2. **CORS wildcard** - API accepts requests from any origin
3. **Tokens never expire** - No JWT, no expiration, no revocation beyond logout
4. **Key functional gaps** - aggiornaAnnuncio, cancellaAnnuncio, getAnnunciUtente not implemented
5. **Color palette inconsistent** - Only partial application of defined colors
6. **Search not connected** - Main.java search handler has no API integration