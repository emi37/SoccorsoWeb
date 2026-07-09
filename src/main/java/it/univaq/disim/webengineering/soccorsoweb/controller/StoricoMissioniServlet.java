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
            out.println("<head>");
            out.println("  <meta charset='UTF-8'>");
            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("  <title>Storico Interventi Conclusi</title>");
            out.println("</head>");
            out.println("<body style='font-family: Arial, sans-serif; padding: 15px; background-color: #f4f6f9; margin: 0;'>");
            
            // Contenitore principale fluido
            out.println("<div style='max-width: 1000px; width: 100%; margin: 20px auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); box-sizing: border-box;'>");
            
            // Intestazione reattiva con Flexbox wrap
            out.println("<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px;'>");
            out.println("  <h2 style='margin: 0; color: #333;'>Storico delle missioni concluse</h2>");
            out.println("  <a href='" + request.getContextPath() + "/DashboardServlet' style='background-color: #007bff; color: white; padding: 8px 14px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 14px; display: inline-block;'>Torna al pannello di controllo</a>");
            out.println("</div>");

            // Div di scorrimento protettivo per la tabella storica
            out.println("<div style='width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; border: 1px solid #dee2e6; border-radius: 4px;'>");
            out.println("  <table style='width: 100%; border-collapse: collapse; text-align: left; min-width: 800px;'>");
            out.println("    <tr style='background-color: #e9ecef; border-bottom: 2px solid #dee2e6;'>");
            out.println("      <th style='padding: 12px; width: 110px;'>ID missione</th>");
            out.println("      <th style='padding: 12px; width: 110px;'>ID richiesta</th>");
            out.println("      <th style='padding: 12px; width: 250px;'>Dettagli operazione</th>");
            out.println("      <th style='padding: 12px; width: 140px;'>Valutazione (0-5)</th>");
            out.println("      <th style='padding: 12px;'>Relazione finale</th>");
            out.println("    </tr>");

            // 2. Query sul DB per estrarre lo storico
            try (Connection conn = DBManager.getConnection()) {
                String sql = "SELECT id_missione, id_richiesta, obiettivo, livello_successo, commenti FROM missione WHERE stato = 'CHIUSA' ORDER BY id_missione DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    boolean ciSonoMissioni = false;
                    while (rs.next()) {
                        ciSonoMissioni = true;
                        out.println("<tr style='border-bottom: 1px solid #dee2e6;'>");
                        out.println("  <td style='padding: 12px;'><b>#" + rs.getInt("id_missione") + "</b></td>");
                        out.println("  <td style='padding: 12px;'>#" + rs.getInt("id_richiesta") + "</td>");
                        out.println("  <td style='padding: 12px; max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" + rs.getString("obiettivo") + "</td>");
                        
                        // Formattazione estetica del livello di successo
                        int voto = rs.getInt("livello_successo");
                        out.println("  <td style='padding: 12px; font-weight: bold; color: #28a745;'>" + voto + " / 5</td>");
                        
                        String commento = rs.getString("commenti");
                        String testoCommento = (commento == null || commento.trim().isEmpty()) ? "Nessuna relazione depositata." : commento;
                        out.println("  <td style='padding: 12px; color: #495057; font-style: italic; max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" + testoCommento + "</td>");
                        out.println("</tr>");
                    }
                    
                    if (!ciSonoMissioni) {
                        out.println("    <tr><td colspan='5' style='text-align: center; color: gray; padding: 20px;'>Nessun intervento ancora archiviato nello storico.</td></tr>");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                out.println("    <tr><td colspan='5' style='text-align: center; color: red; padding: 20px;'>Errore durante il caricamento dello storico.</td></tr>");
            }

            out.println("  </table>");
            out.println("</div>");
            
            out.println("</div></body></html>");
        }
    }
}