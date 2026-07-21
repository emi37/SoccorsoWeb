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
// Mappa la servlet all'URL /AggiungiMezzo
@WebServlet("/AggiungiMezzo")
public class AggiungiMezzoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // controllo credenziali amministratore attivo
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        String nome = request.getParameter("nome");
        String descrizione = request.getParameter("descrizione");

        if (nome != null && !nome.trim().isEmpty()) {
            String query = "INSERT INTO mezzo (nome, descrizione, attivo) VALUES (?, ?, 1)";
            
            // uso del dbmanager centralizzato del progetto per evitare errori di driver
            try (Connection conn = DBManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                
                ps.setString(1, nome);
                ps.setString(2, descrizione);
                ps.executeUpdate();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // reindirizzamento sicuro alla servlet di gestione
        response.sendRedirect(request.getContextPath() + "/GestioneMezzi");
    }
}