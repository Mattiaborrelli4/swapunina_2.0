package application.api;

import application.Classe.*;
import application.DB.*;
import application.config.ConfigManager;
import application.messagistica.MessageEncryptionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * REST API per SwapUnina
 * Fornisce endpoint per l'app mobile e integrazioni esterne
 *
 * <p><b>Architettura:</b>
 * <ul>
 *   <li>Framework: Spark Java</li>
 *   <li>JSON: Gson per serializzazione</li>
 *   <li>Autenticazione: Token-based (da implementare con JWT)</li>
 *   <li>CORS: Abilitato per client browser</li>
 * </ul>
 * </p>
 *
 * <p><b>Endpoint principali:</b>
 * <ul>
 *   <li>POST /api/auth/login - Autenticazione</li>
 *   <li>GET /api/annunci - Lista annunci</li>
 *   <li>POST /api/annunci - Crea annuncio</li>
 *   <li>GET /api/annunci/:id - Dettaglio annuncio</li>
 *   <li>GET /api/messaggi/:userId - Messaggi utente</li>
 *   <li>POST /api/messaggi - Invia messaggio</li>
 *   <li>GET /api/carrello - Carrello utente</li>
 *   <li>POST /api/carrello - Aggiungi al carrello</li>
 *   <li>GET /api/offerte - Offerte utente</li>
 *   <li>POST /api/offerte - Crea offerta</li>
 * </ul>
 * </p>
 */
public class SwapUninaAPI {

    private final Gson gson;
    private final int port;
    private final AnnuncioDAO annuncioDAO;
    private final MessaggioDAO messaggioDAO;
    private final OffertaDAO offertaDAO;
    private final CarrelloDAO carrelloDAO;
    private final UtentiDAO utentiDAO;

    // Token di sessione semplificato (produzione: usare JWT)
    private final Map<String, Integer> activeTokens;

    public SwapUninaAPI(int port) {
        this.port = port;
        this.gson = new Gson();
        this.annuncioDAO = new AnnuncioDAO();
        this.messaggioDAO = new MessaggioDAO();
        this.offertaDAO = new OffertaDAO();
        this.carrelloDAO = new CarrelloDAO();
        this.utentiDAO = new UtentiDAO();
        this.activeTokens = new HashMap<>();
    }

    /**
     * Avvia il server REST API
     */
    public void start() {
        // Configurazione Spark
        port(port);

        // CORS headers
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Content-Type", "application/json; charset=utf-8");
        });

        // ========== AUTH ENDPOINTS ==========

        post("/api/auth/login", this::handleLogin);
        post("/api/auth/register", this::handleRegister);
        post("/api/auth/logout", this::handleLogout);

        // ========== ANNUNCI ENDPOINTS ==========

        get("/api/annunci", this::getAnnunci);
        get("/api/annunci/:id", this::getAnnuncioById);
        post("/api/annunci", this::creaAnnuncio);
        put("/api/annunci/:id", this::aggiornaAnnuncio);
        delete("/api/annunci/:id", this::cancellaAnnuncio);
        get("/api/annunci/utente/:userId", this::getAnnunciUtente);

        // ========== MESSAGGI ENDPOINTS ==========

        get("/api/messaggi/:userId", this::getMessaggi);
        get("/api/messaggi/conversazione/:userId1/:userId2", this::getConversazione);
        post("/api/messaggi", this::inviaMessaggio);
        get("/api/messaggi/interlocutori/:userId", this::getInterlocutori);

        // ========== CARRELLO ENDPOINTS ==========

        get("/api/carrello", this::getCarrello);
        post("/api/carrello", this::aggiungiAlCarrello);
        delete("/api/carrello/:id", this::rimuoviDalCarrello);
        put("/api/carrello/:id", this::aggiornaQuantita);

        // ========== OFFERTE ENDPOINTS ==========

        get("/api/offerte/ricevute", this::getOfferteRicevute);
        get("/api/offerte/fatte", this::getOfferteFatte);
        post("/api/offerte", this::creaOfferta);
        put("/api/offerte/:id/accetta", this::accettaOfferta);
        put("/api/offerte/:id/rifiuta", this::rifiutaOfferta);
        post("/api/offerte/:id/controfferta", this::creaControfferta);

        // ========== UTENTI ENDPOINTS ==========

        get("/api/utenti/:id", this::getUtente);
        get("/api/utenti/:id/annunci", this::getAnnunciVenditore);
        get("/api/utenti/:id/recensioni", this::getRecensioniUtente);

        // ========== HEALTH CHECK ==========

        get("/api/health", (req, res) -> {
            JsonObject health = new JsonObject();
            health.addProperty("status", "UP");
            health.addProperty("timestamp", LocalDateTime.now().toString());
            health.addProperty("version", "2.0");

            // Verifica connessione database
            boolean dbOk = ConnessioneDB.verificaConnessioneRapida();
            health.addProperty("database", dbOk ? "UP" : "DOWN");

            return gson.toJson(health);
        });

        // ========== ERROR HANDLING ==========

        exception(Exception.class, (exception, request, response) -> {
            response.status(500);

            JsonObject error = new JsonObject();
            error.addProperty("error", exception.getMessage());
            error.addProperty("timestamp", LocalDateTime.now().toString());

            response.body(gson.toJson(error));
        });

        System.out.println("✅ REST API avviata su http://localhost:" + port);
        System.out.println("📚 Documentazione endpoint disponibili");
    }

    /**
     * Ferma il server REST API
     */
    public void stopAPI() {
        System.out.println("🛑 Arresto REST API...");
        spark.Spark.stop();
        System.out.println("✅ REST API arrestata");
    }

    // ========== HANDLER AUTENTICAZIONE ==========

    private Object handleLogin(Request req, Response res) {
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
        String email = body.get("email").getAsString();
        String password = body.get("password").getAsString();

        // Verifica credenziali
        boolean credenzialiValide = utentiDAO.verificaCredenziali(email, password);

        if (credenzialiValide) {
            // Ottieni l'utente
            java.util.Optional<utente> userOpt = utentiDAO.getUtenteByEmail(email);

            if (userOpt.isPresent()) {
                utente user = userOpt.get();

                // Genera token sessione
                String token = UUID.randomUUID().toString();
                activeTokens.put(token, user.getId());

                JsonObject response = new JsonObject();
                response.addProperty("token", token);
                response.addProperty("userId", user.getId());
                response.addProperty("nome", user.getNome());
                response.addProperty("cognome", user.getCognome());
                response.addProperty("email", user.getEmail());

                res.status(200);
                return gson.toJson(response);
            }
        }

        res.status(401);
        JsonObject error = new JsonObject();
        error.addProperty("error", "Credenziali non valide");
        return gson.toJson(error);
    }

    private Object handleRegister(Request req, Response res) {
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();

        utente user = new utente();
        user.setMatricola(body.get("matricola").getAsString());
        user.setNome(body.get("nome").getAsString());
        user.setCognome(body.get("cognome").getAsString());
        user.setEmail(body.get("email").getAsString());
        user.setPassword(body.get("password").getAsString());

        boolean registered = utentiDAO.registraUtente(user);

        if (registered) {
            res.status(201);
            JsonObject response = new JsonObject();
            response.addProperty("message", "Utente registrato con successo");
            return gson.toJson(response);
        } else {
            res.status(400);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Registrazione fallita");
            return gson.toJson(error);
        }
    }

    private Object handleLogout(Request req, Response res) {
        String token = req.headers("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            activeTokens.remove(token);
        }

        JsonObject response = new JsonObject();
        response.addProperty("message", "Logout effettuato");
        return gson.toJson(response);
    }

    // ========== HANDLER ANNUNCI ==========

    private Object getAnnunci(Request req, Response res) {
        String categoria = req.queryParams("categoria");
        String search = req.queryParams("search");
        int limit = req.queryParams().contains("limit") ? Integer.parseInt(req.queryParams("limit")) : 50;
        int offset = req.queryParams().contains("offset") ? Integer.parseInt(req.queryParams("offset")) : 0;

        List<Annuncio> annunci = annuncioDAO.getAnnunciAttivi();

        // Filtri (implementazione semplificata)
        if (categoria != null && !categoria.isEmpty()) {
            annunci = annunci.stream()
                .filter(a -> a.getOggetto().getCategoria().name().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            annunci = annunci.stream()
                .filter(a -> a.getTitolo().toLowerCase().contains(searchLower) ||
                           (a.getDescrizione() != null && a.getDescrizione().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
        }

        // Pagination
        int fromIndex = Math.min(offset, annunci.size());
        int toIndex = Math.min(offset + limit, annunci.size());
        List<Annuncio> paginated = annunci.subList(fromIndex, toIndex);

        JsonObject response = new JsonObject();
        response.add("annunci", gson.toJsonTree(paginated));
        response.addProperty("total", annunci.size());
        response.addProperty("limit", limit);
        response.addProperty("offset", offset);

        return gson.toJson(response);
    }

    private Object getAnnuncioById(Request req, Response res) {
        int id = Integer.parseInt(req.params(":id"));
        Annuncio annuncio = annuncioDAO.getAnnuncioById(id);

        if (annuncio != null) {
            return gson.toJson(annuncio);
        } else {
            res.status(404);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Annuncio non trovato");
            return gson.toJson(error);
        }
    }

    private Object creaAnnuncio(Request req, Response res) {
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();

        Annuncio annuncio = gson.fromJson(body, Annuncio.class);
        int venditoreId = body.get("venditoreId").getAsInt();

        int annuncioId = annuncioDAO.inserisciAnnuncioComplessivo(annuncio, venditoreId);

        if (annuncioId > 0) {
            res.status(201);
            JsonObject response = new JsonObject();
            response.addProperty("id", annuncioId);
            response.addProperty("message", "Annuncio creato con successo");
            return gson.toJson(response);
        } else {
            res.status(400);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Creazione annuncio fallita");
            return gson.toJson(error);
        }
    }

    private Object aggiornaAnnuncio(Request req, Response res) {
        int id = Integer.parseInt(req.params(":id"));
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();

        // Implementare aggiornamento
        JsonObject response = new JsonObject();
        response.addProperty("message", "Aggiornamento non ancora implementato");
        return gson.toJson(response);
    }

    private Object cancellaAnnuncio(Request req, Response res) {
        int id = Integer.parseInt(req.params(":id"));

        // Implementare cancellazione
        JsonObject response = new JsonObject();
        response.addProperty("message", "Cancellazione non ancora implementata");
        return gson.toJson(response);
    }

    private Object getAnnunciUtente(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":userId"));
        // Implementare
        return gson.toJson(Collections.emptyList());
    }

    // ========== HANDLER MESSAGGI ==========

    private Object getMessaggi(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":userId"));
        List<Messaggio> messaggi = messaggioDAO.getConversazione(userId, 0); // Implementare correttamente
        return gson.toJson(messaggi);
    }

    private Object getConversazione(Request req, Response res) {
        int userId1 = Integer.parseInt(req.params(":userId1"));
        int userId2 = Integer.parseInt(req.params(":userId2"));

        List<Messaggio> conversazione = messaggioDAO.getConversazione(userId1, userId2);
        return gson.toJson(conversazione);
    }

    private Object inviaMessaggio(Request req, Response res) {
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();

        int mittenteId = body.get("mittenteId").getAsInt();
        int destinatarioId = body.get("destinatarioId").getAsInt();
        String testo = body.get("testo").getAsString();
        Integer annuncioId = body.has("annuncioId") ? body.get("annuncioId").getAsInt() : null;

        Messaggio messaggio = new Messaggio(mittenteId, destinatarioId, testo, annuncioId);

        boolean inviato = messaggioDAO.inviaMessaggio(messaggio);

        if (inviato) {
            res.status(201);
            JsonObject response = new JsonObject();
            response.addProperty("id", messaggio.getId());
            response.addProperty("timestamp", messaggio.getDataInvio().toString());
            return gson.toJson(response);
        } else {
            res.status(500);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Invio messaggio fallito");
            return gson.toJson(error);
        }
    }

    private Object getInterlocutori(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":userId"));
        List<utente> interlocutori = messaggioDAO.getInterlocutoriUtenti(userId);
        return gson.toJson(interlocutori);
    }

    // ========== HANDLER CARRELLO ==========

    private Object getCarrello(Request req, Response res) {
        // Richiede autenticazione
        String token = req.headers("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            res.status(401);
            return gson.toJson(Map.of("error", "Unauthorized"));
        }

        token = token.substring(7);
        Integer userId = activeTokens.get(token);

        if (userId == null) {
            res.status(401);
            return gson.toJson(Map.of("error", "Invalid token"));
        }

        List<CarrelloItem> carrello = carrelloDAO.getCarrelloPerUtente(userId);
        return gson.toJson(carrello);
    }

    private Object aggiungiAlCarrello(Request req, Response res) {
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
        int userId = body.get("userId").getAsInt();
        int annuncioId = body.get("annuncioId").getAsInt();

        boolean aggiunto = carrelloDAO.aggiungiAlCarrello(userId, annuncioId);

        if (aggiunto) {
            res.status(201);
            return gson.toJson(Map.of("message", "Prodotto aggiunto al carrello"));
        } else {
            res.status(500);
            return gson.toJson(Map.of("error", "Aggiunta al carrello fallita"));
        }
    }

    private Object rimuoviDalCarrello(Request req, Response res) {
        int carrelloId = Integer.parseInt(req.params(":id"));
        boolean rimosso = carrelloDAO.rimuoviItem(carrelloId);

        if (rimosso) {
            return gson.toJson(Map.of("message", "Prodotto rimosso dal carrello"));
        } else {
            res.status(500);
            return gson.toJson(Map.of("error", "Rimozione fallita"));
        }
    }

    private Object aggiornaQuantita(Request req, Response res) {
        int carrelloId = Integer.parseInt(req.params(":id"));
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
        int nuovaQuantita = body.get("quantita").getAsInt();

        boolean aggiornato = carrelloDAO.aggiornaQuantita(carrelloId, nuovaQuantita);

        if (aggiornato) {
            return gson.toJson(Map.of("message", "Quantità aggiornata"));
        } else {
            res.status(500);
            return gson.toJson(Map.of("error", "Aggiornamento fallito"));
        }
    }

    // ========== HANDLER OFFERTE ==========

    private Object getOfferteRicevute(Request req, Response res) {
        String token = req.headers("Authorization");
        token = token.substring(7);
        Integer userId = activeTokens.get(token);

        if (userId != null) {
            List<OffertaNegoziazione> offerte = offertaDAO.getOfferteRicevute(userId);
            return gson.toJson(offerte);
        } else {
            res.status(401);
            return gson.toJson(Map.of("error", "Unauthorized"));
        }
    }

    private Object getOfferteFatte(Request req, Response res) {
        String token = req.headers("Authorization");
        token = token.substring(7);
        Integer userId = activeTokens.get(token);

        if (userId != null) {
            List<OffertaNegoziazione> offerte = offertaDAO.getOfferteFatte(userId);
            return gson.toJson(offerte);
        } else {
            res.status(401);
            return gson.toJson(Map.of("error", "Unauthorized"));
        }
    }

    private Object creaOfferta(Request req, Response res) {
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();

        OffertaNegoziazione offerta = new OffertaNegoziazione();
        offerta.setAnnuncioId(body.get("annuncioId").getAsInt());
        offerta.setOfferenteId(body.get("offerenteId").getAsInt());
        offerta.setVenditoreId(body.get("venditoreId").getAsInt());
        offerta.setImporto(body.get("importo").getAsDouble());
        offerta.setMessaggio(body.get("messaggio").getAsString());

        int offertaId = offertaDAO.creaOfferta(offerta);

        if (offertaId > 0) {
            res.status(201);
            JsonObject response = new JsonObject();
            response.addProperty("id", offertaId);
            return gson.toJson(response);
        } else {
            res.status(500);
            return gson.toJson(Map.of("error", "Creazione offerta fallita"));
        }
    }

    private Object accettaOfferta(Request req, Response res) {
        int offertaId = Integer.parseInt(req.params(":id"));
        boolean accettata = offertaDAO.accettaOfferta(offertaId);

        if (accettata) {
            return gson.toJson(Map.of("message", "Offerta accettata"));
        } else {
            res.status(500);
            return gson.toJson(Map.of("error", "Accettazione fallita"));
        }
    }

    private Object rifiutaOfferta(Request req, Response res) {
        int offertaId = Integer.parseInt(req.params(":id"));
        boolean rifiutata = offertaDAO.rifiutaOfferta(offertaId);

        if (rifiutata) {
            return gson.toJson(Map.of("message", "Offerta rifiutata"));
        } else {
            res.status(500);
            return gson.toJson(Map.of("error", "Rifiuto fallito"));
        }
    }

    private Object creaControfferta(Request req, Response res) {
        int offertaPadreId = Integer.parseInt(req.params(":id"));
        JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();

        double nuovoImporto = body.get("importo").getAsDouble();
        String messaggio = body.get("messaggio").getAsString();

        int controffertaId = offertaDAO.creaControfferta(offertaPadreId, nuovoImporto, messaggio);

        if (controffertaId > 0) {
            res.status(201);
            JsonObject response = new JsonObject();
            response.addProperty("id", controffertaId);
            return gson.toJson(response);
        } else {
            res.status(500);
            return gson.toJson(Map.of("error", "Creazione contro-offerta fallita"));
        }
    }

    // ========== HANDLER UTENTI ==========

    private Object getUtente(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":id"));

        // Costruisci un oggetto utente minimo dalle informazioni disponibili
        String email = utentiDAO.getEmailById(userId);
        if (email != null) {
            java.util.Optional<utente> userOpt = utentiDAO.getUtenteByEmail(email);
            if (userOpt.isPresent()) {
                return gson.toJson(userOpt.get());
            }
        }

        res.status(404);
        return gson.toJson(Map.of("error", "Utente non trovato"));
    }

    private Object getAnnunciVenditore(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":id"));
        // Implementare
        return gson.toJson(Collections.emptyList());
    }

    private Object getRecensioniUtente(Request req, Response res) {
        int userId = Integer.parseInt(req.params(":id"));
        // Implementare
        return gson.toJson(Collections.emptyList());
    }

    // ========== ENTRY POINT ==========

    public static void main(String[] args) {
        SwapUninaAPI api = new SwapUninaAPI(8080);

        try {
            api.start();

            // Mantieni il server in esecuzione
            System.out.println("Premi Ctrl+C per arrestare il server");
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Errore avvio API: " + e.getMessage());
            e.printStackTrace();
        } finally {
            api.stopAPI();
        }
    }
}
