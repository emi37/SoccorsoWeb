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

@WebServlet("/IgnoraRichiesta")
public class IgnoraRichiestaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Controllo sicurezza sessione amministratore
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        // 2. Recupero l'id della richiesta da ignorare
        String idParam = request.getParameter("id");
        
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int idRichiesta = Integer.parseInt(idParam);
                
                // 3. Query per marcare la richiesta come 'IGNORATA'
                String query = "UPDATE richiesta_soccorso SET stato = 'IGNORATA' WHERE id_richiesta = ?";
                
                try (Connection conn = DBManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {
                    
                    ps.setInt(1, idRichiesta);
                    ps.executeUpdate();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 4. Ritorniamo subito alla Dashboard amministratore aggiornata
        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    }
}