package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AggiungiMezzo")
public class AggiungiMezzoServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico il driver una sola volta durante l'inizializzazione della servlet,
        // evitando di appesantire inutilmente le successive chiamate POST
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Impossibile caricare il driver MySQL nel classpath");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // recupero la sessione esistente senza crearne una nuova a vuoto
        HttpSession session = request.getSession(false);
        
        // controllo permessi: se non c'è sessione o l'utente non è admin, 
        // blocco l'operazione e lo rimando alla pagina di login
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        // estraggo i parametri inviati dal client tramite il form
        String nome = request.getParameter("nome");
        String descrizione = request.getParameter("descrizione");

        // procedo con la logica di inserimento solo se il nome del mezzo è valorizzato
        if (nome != null && !nome.trim().isEmpty()) {
            
            // spezzo la query su più righe per renderla facilmente leggibile e modificabile
            String query = "INSERT INTO mezzo " +
                           "(nome, descrizione, attivo) " +
                           "VALUES (?, ?, 1)";
            
            // apro la connessione nativa e preparo lo statement.
            // uso il try-with-resources per assicurarmi che vengano chiusi in automatico a fine blocco
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(query)) {
                
                // imposto i parametri della query usando lo statement per prevenire le SQL injection
                ps.setString(1, nome);
                ps.setString(2, descrizione);
                
                // eseguo la query di scrittura a database
                ps.executeUpdate();
                
            } catch (Exception e) {
                // log manuale in console in caso di fallimento della query o della connessione
                System.err.println("Errore durante l'inserimento del nuovo mezzo a db");
                e.printStackTrace();
            }
        }
        
        // al termine del blocco (con o senza errori), riporto l'utente alla vista generale
        response.sendRedirect(request.getContextPath() + "/GestioneMezzi");
    }
}