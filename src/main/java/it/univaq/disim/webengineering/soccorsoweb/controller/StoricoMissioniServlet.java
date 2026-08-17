package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "StoricoMissioniServlet", urlPatterns = {"/StoricoMissioni"})
public class StoricoMissioniServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Controllo sicurezza sessione
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            // Segnala al Fetch in JavaScript di reindirizzare la pagina tramite status HTTP 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Dichiariamo che stiamo restituendo un payload in formato JSON
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // 2. Query sul DB per estrarre lo storico
            try (Connection conn = DBManager.getConnection()) {
                String sql = "SELECT id_missione, id_richiesta, obiettivo, livello_successo, commenti FROM missione WHERE stato = 'CHIUSA' ORDER BY id_missione DESC";

                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    // Costruiamo manualmente l'array JSON per evitare librerie di terze parti
                    StringBuilder json = new StringBuilder();
                    json.append("[");
                    boolean first = true;

                    while (rs.next()) {
                        if (!first) {
                            json.append(",");
                        }
                        first = false;

                        json.append("{")
                            .append("\"id_missione\":").append(rs.getInt("id_missione")).append(",")
                            .append("\"id_richiesta\":").append(rs.getInt("id_richiesta")).append(",")
                            .append("\"obiettivo\":\"").append(escapeJson(rs.getString("obiettivo"))).append("\",")
                            .append("\"livello_successo\":").append(rs.getInt("livello_successo")).append(",")
                            .append("\"commenti\":\"").append(escapeJson(rs.getString("commenti"))).append("\"")
                            .append("}");
                    }
                    
                    json.append("]");
                    out.print(json.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Errore durante il caricamento dello storico.\"}");
            }
        }
    }

    // Metodo di utility per gestire correttamente i caratteri speciali che romperebbero la sintassi JSON
    private String escapeJson(String data) {
        if (data == null) {
            return "";
        }
        return data.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}