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

@WebServlet(name = "ConcludiMissioneServlet", urlPatterns = {"/ConcludiMissioneServlet"})
public class ConcludiMissioneServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("  <meta charset='UTF-8'>");
            // Aggiunto il viewport fondamentale per dispositivi mobili
            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("  <title>Chiusura Intervento</title>");
            out.println("</head>");
            out.println("<body style='font-family: Arial, sans-serif; padding: 15px; background-color: #f8f9fa; margin: 0;'>");
            
            // Modificato il container per essere fluido (width: 100% e box-sizing)
            out.println("<div style='max-width: 600px; width: 100%; margin: 40px auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); box-sizing: border-box;'>");
            
            try (Connection conn = DBManager.getConnection()) {
                String sql = "SELECT id_missione, id_richiesta, obiettivo FROM missione WHERE id_missione = ? AND stato = 'IN_CORSO'";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, idMissione);
                    try (ResultSet rs = stmt.executeQuery()) {
                        
                        if (rs.next()) {
                            out.println("<h2 style='margin-top: 0; color: #333;'>Chiusura report missione #" + rs.getInt("id_missione") + "</h2>");
                            out.println("<p><b>ID Richiesta collegata:</b> " + rs.getInt("id_richiesta") + "</p>");
                            out.println("<p><b>Dettaglio operazione:</b> " + rs.getString("obiettivo") + "</p>");
                            out.println("<hr style='border: 0; border-top: 1px solid #dee2e6; margin: 20px 0;'>");
                            
                            out.println("<form action='" + request.getContextPath() + "/ConcludiMissioneServlet' method='POST'>");
                            out.println("<input type='hidden' name='id_missione' value='" + idMissione + "'>");
                            out.println("<input type='hidden' name='id_richiesta' value='" + rs.getInt("id_richiesta") + "'>");
                            
                            out.println("<div style='margin-bottom: 18px;'>");
                            out.println("  <label for='voto' style='font-weight: bold; color: #495057;'>Valutazione esito intervento (0-5):</label><br>");
                            out.println("  <input type='number' id='voto' name='voto_successo' min='0' max='5' value='5' required style='width: 100%; max-width: 120px; padding: 10px; margin-top: 6px; border: 1px solid #ced4da; border-radius: 4px; box-sizing: border-box;'>");
                            out.println("</div>");
                            
                            out.println("<div style='margin-bottom: 20px;'>");
                            out.println("  <label for='commenti' style='font-weight: bold; color: #495057;'>Relazione finale della squadra:</label><br>");
                            out.println("  <textarea id='commenti' name='commenti' rows='5' style='width: 100%; padding: 10px; margin-top: 6px; border: 1px solid #ced4da; border-radius: 4px; box-sizing: border-box; font-family: Arial, sans-serif; resize: vertical;' required placeholder='Scrivi qui come si è concluso il soccorso sul posto...'></textarea>");
                            out.println("</div>");
                            
                            out.println("<button type='submit' style='background-color: #28a745; color: white; padding: 12px 24px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; width: 100%; font-size: 16px; transition: background 0.2s;'>REGISTRA CHIUSURA E LIBERA RISORSE</button>");
                            out.println("</form>");
                        } else {
                            out.println("<p style='color: red; font-weight: bold;'>Missione non trovata o già conclusa.</p>");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            out.println("<br><br><a href='" + request.getContextPath() + "/DashboardServlet' style='color: #007bff; text-decoration: none; font-weight: bold;'>Annulla e torna alla Dashboard</a>");
            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");
        String idRichiesta = request.getParameter("id_richiesta");
        String livelloSuccesso = request.getParameter("voto_successo");
        String commenti = request.getParameter("commenti");
        
        boolean terminata = false;

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String sqlMissione = "UPDATE missione SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP WHERE id_missione = ?";
                try (PreparedStatement stmtM = conn.prepareStatement(sqlMissione)) {
                    stmtM.setInt(1, Integer.parseInt(livelloSuccesso));
                    stmtM.setString(2, commenti);
                    stmtM.setInt(3, Integer.parseInt(idMissione));
                    stmtM.executeUpdate();
                }
                
                String sqlRichiesta = "UPDATE richiesta_soccorso SET stato = 'CHIUSA' WHERE id_richiesta = ?";
                try (PreparedStatement stmtR = conn.prepareStatement(sqlRichiesta)) {
                    stmtR.setString(1, idRichiesta);
                    stmtR.executeUpdate();
                }
                
                conn.commit();
                terminata = true;
                
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            if (terminata) {
                out.println("<script>");
                out.println("alert('Intervento concluso. I mezzi e gli operatori sono di nuovo disponibili!');");
                out.println("window.location.href='" + request.getContextPath() + "/DashboardServlet';");
                out.println("</script>");
            } else {
                out.println("<h2>Errore durante l'archiviazione del report.</h2>");
            }
            out.println("</body></html>");
        }
    }
}