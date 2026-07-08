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
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='it'>");
            out.println("<head><meta charset='UTF-8'><title>Storico Interventi Conclusi</title></head>");
            out.println("<body style='font-family: Arial; padding: 20px; background-color: #f4f6f9;'>");
            out.println("<div style='max-width: 1000px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05);'>");
            
            out.println("<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;'>");
            out.println("<h2>Storico delle missioni concluse</h2>");
            
            // CORREZIONE LINK: aggiunto request.getContextPath() per reindirizzare correttamente alla dashboard
            out.println("<a href='" + request.getContextPath() + "/DashboardServlet' style='background-color: #007bff; color: white; padding: 8px 14px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 14px;'>Torna al pannello di controllo</a>");
            out.println("</div>");

            out.println("<table border='1' cellpadding='10' cellspacing='0' style='width:100%; border-collapse: collapse; text-align: left;'>");
            out.println("<tr style='background-color: #e9ecef;'><th>ID missione</th><th>ID richiesta</th><th>Dettagli operazione</th><th>Valutazione (0-5)</th><th>Relazione finale</th></tr>");

            // 2. Query sul DB per estrarre lo storico
            try (Connection conn = DBManager.getConnection()) {
                String sql = "SELECT id_missione, id_richiesta, obiettivo, livello_successo, commenti FROM missione WHERE stato = 'CHIUSA' ORDER BY id_missione DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    boolean ciSonoMissioni = false;
                    while (rs.next()) {
                        ciSonoMissioni = true;
                        out.println("<tr>");
                        out.println("<td><b>#" + rs.getInt("id_missione") + "</b></td>");
                        out.println("<td>" + rs.getInt("id_richiesta") + "</td>");
                        out.println("<td>" + rs.getString("objective" != null ? "obiettivo" : "obiettivo") + "</td>");
                        
                        // Formattazione del voto esteticamente caruccia 
                        int voto = rs.getInt("livello_successo");
                        out.println("<td style='font-weight: bold; color: #28a745;'>" + voto + " / 5</td>");
                        
                        out.println("<td style='color: #495057; font-style: italic;'>" + rs.getString("commenti") + "</td>");
                        out.println("</tr>");
                    }
                    
                    if (!ciSonoMissioni) {
                        out.println("<tr><td colspan='5' style='text-align: center; color: gray;'>Nessun intervento ancora archiviato nello storico.</td></tr>");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p style='color: red;'>Errore durante il caricamento dello storico.</p>");
            }

            out.println("</table>");
            out.println("</div></body></html>");
        }
    }
}