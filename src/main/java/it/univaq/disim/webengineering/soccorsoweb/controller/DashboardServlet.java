package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/DashboardServlet"})
public class DashboardServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345"; 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        Map<String, Object> dataModel = new HashMap<>();
        List<Map<String, Object>> richieste = new ArrayList<>();
        List<Map<String, Object>> missioni = new ArrayList<>();

        try {
            // Carica il driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

                // 1. Estrazione Richieste
                String sqlRichieste = "SELECT id_richiesta, nome_segnalante, email_segnalante, posizione, descrizione FROM richiesta_soccorso WHERE stato = 'ATTIVA'";
                try (PreparedStatement stmt = conn.prepareStatement(sqlRichieste);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> r = new HashMap<>();
                        r.put("id_richiesta", rs.getInt("id_richiesta"));
                        r.put("nome_segnalante", rs.getString("nome_segnalante"));
                        r.put("email_segnalante", rs.getString("email_segnalante"));
                        r.put("posizione", rs.getString("posizione"));
                        r.put("descrizione", rs.getString("descrizione"));
                        richieste.add(r);
                    }
                }
                dataModel.put("richieste", richieste);

                // 2. Estrazione Missioni
                String sqlMissioni = "SELECT m.id_missione, m.id_richiesta, m.obiettivo, m.stato, r.posizione " +
                                     "FROM missione m JOIN richiesta_soccorso r ON m.id_richiesta = r.id_richiesta " +
                                     "WHERE m.stato = 'IN_CORSO'";
                try (PreparedStatement stmt = conn.prepareStatement(sqlMissioni);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id_missione", rs.getInt("id_missione"));
                        m.put("id_richiesta", rs.getInt("id_richiesta"));
                        m.put("obiettivo", rs.getString("obiettivo"));
                        m.put("posizione", rs.getString("posizione"));
                        m.put("stato", rs.getString("stato"));
                        missioni.add(m);
                    }
                }
                dataModel.put("missioni", missioni);

                // IL PEZZO CRUCIALE CHE MANCAVA: passiamo la request a FreeMarker per i link
                dataModel.put("request", request);

                // Rendering con FreeMarker
                TemplateManager.process("dashboard.ftl", dataModel, response, getServletContext());
            }

       } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("ECCO IL VERO ERRORE DA MANDARE A GEMINI:\n\n" + sw.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}