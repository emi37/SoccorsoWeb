package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.sql.Connection;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // controllo sull'autorizzazione per l'amministratore nella form
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }
// Leggo i parametri inviati dal form HTML
        String nome = request.getParameter("nome");
        String descrizione = request.getParameter("descrizione");
// Controllo che il nome non sia nullo o vuoto
        if (nome != null && !nome.trim().isEmpty()) {
            String query = "INSERT INTO materiale (nome, descrizione, attivo) VALUES (?, ?, 1)";
            
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DBManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {
                    
                    ps.setString(1, nome);
                    ps.setString(2, descrizione);
                    // Eseguo l'inserimento nel database
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
         // Dopo l'operazione torno alla gestione materiali
        response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
    }
}