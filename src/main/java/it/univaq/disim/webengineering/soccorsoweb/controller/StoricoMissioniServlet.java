package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
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
            // rispondo con 401 per far triggerare il catch lato javascript
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // output json
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            
            // query spezzata per leggerezza
            String sql = "SELECT id_missione, id_richiesta, obiettivo, livello_successo, commenti " +
                         "FROM missione " +
                         "WHERE stato = 'CHIUSA' " +
                         "ORDER BY id_missione DESC";

            // connessione nativa
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                // buildo il json a mano
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

            } catch (Exception e) {
                System.err.println("Errore db fetch storico missioni");
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Errore durante il caricamento dello storico.\"}");
            }
        }
    }

    // escape per non rompere il parse json lato client
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