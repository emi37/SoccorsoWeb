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

@WebServlet("/AggiungiMateriale")
public class AggiungiMaterialeServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // tomcat(il server)chiama questo metodo solo una volta all'avvio della servlet.
      
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Errore fatale: Driver MySQL non trovato nel classpath!");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // prendo la sessione
        HttpSession sessioneAttuale = request.getSession(false);
        
        
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

       
        String nomeMateriale = request.getParameter("nome");
        String descrizioneMateriale = request.getParameter("descrizione");

       
        if (nomeMateriale != null && !nomeMateriale.trim().isEmpty()) {
            
            String queryInserimento = "INSERT INTO materiale " +
                                      "(nome, descrizione, attivo) " +
                                      "VALUES (?, ?, 1)";
            
            // apro la connessione nativa (niente DBManager esterno) e il prepared statement
            // il try-with-resources li chiuderà in automatico a fine blocco
            try (Connection connessione = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement statementInserimento = connessione.prepareStatement(queryInserimento)) {
                
                // bindo i parametri al posto dei punti interrogativi
                statementInserimento.setString(1, nomeMateriale);
                statementInserimento.setString(2, descrizioneMateriale);
                
                // insert al database
                statementInserimento.executeUpdate();
                
            } catch (Exception e) {
                // gestione errore pragmatica: stampo il guaio e sputo lo stacktrace
                System.err.println("Errore durante l'inserimento del nuovo materiale a db...");
                e.printStackTrace();
            }
        }
        
        // torno alla vista della gestione dei materiali
        response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
    }
}