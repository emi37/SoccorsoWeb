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

@WebServlet("/IgnoraRichiesta")
public class IgnoraRichiestaServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico il driver una sola volta al boot
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL mancante");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        // pesco l'id da scartare
        String idParam = request.getParameter("id");
        
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int idRichiesta = Integer.parseInt(idParam);
                
                // query spezzata per leggibilità
                String query = "UPDATE richiesta_soccorso " +
                               "SET stato = 'IGNORATA' " +
                               "WHERE id_richiesta = ?";
                
                // connessione nativa
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = conn.prepareStatement(query)) {
                    
                    ps.setInt(1, idRichiesta);
                    ps.executeUpdate();
                }
                
            } catch (Exception e) {
                System.err.println("Errore durante l'aggiornamento dello stato della richiesta (IGNORATA)");
                e.printStackTrace();
            }
        }

        // torno alla dashboard pulendo l'url (pattern PRG)
        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    }
}