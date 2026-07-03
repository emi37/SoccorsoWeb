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

@WebServlet(name = "DashboardOperatoreServlet", urlPatterns = {"/DashboardOperatoreServlet"})
public class DashboardOperatoreServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // controllo della sessioner e se l'utente sia un operatore o non
        HttpSession session = request.getSession(false);
        if (session == null || !"OPERATORE".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) session.getAttribute("id_utente");
        String nomeOperatore = (String) session.getAttribute("nome");

        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Area riservata per gli operatori</title>");
            out.println("</head>");
            out.println("<body style='font-family: Arial; padding: 20px; background-color: #e9ecef;'>");
            out.println("<div style='max-width: 900px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.1);'>");
            
            out.println("<div style='display: flex; justify-content: space-between; align-items: center;'>");
            out.println("<h2>Pannello operatore: " + nomeOperatore + "</h2>");

            out.println("<a href='" + request.getContextPath() + "/LogoutServlet' style='color: red; font-weight: bold; text-decoration: none;'>Esci</a>");
            out.println("</div>");
            out.println("<p>Qui puoi monitorare lo stato delle missioni alle quali sei stato assegnato.</p>");
            out.println("<hr><br>");

            try (Connection conn = DBManager.getConnection()) {
                
                out.println("<h3>Elenco delle tue missioni: </h3>");
                out.println("<table border='1' cellpadding='10' cellspacing='0' style='width:100%; border-collapse: collapse; text-align: left;'>");
                out.println("<tr style='background-color: #f8f9fa;'><th>ID missione</th><th>Obiettivo</th><th>Posizione</th><th>Stato</th><th>Esito (0-5)</th></tr>");
                
                // query con la join per prendere solo le missioni dell'operatore loggato
                String sql = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo "
                           + "FROM missione m "
                           + "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione "
                           + "WHERE aom.id_utente = ? "
                           + "ORDER BY m.stato DESC, m.id_missione DESC"; // Mostra prima quelle IN_CORSO
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, idOperatore);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        boolean haMissioni = false;
                        
                        while (rs.next()) {
                            haMissioni = true;
                            String stato = rs.getString("stato");
                            String stileStato = "";
                            
                            
                            
                            if ("IN_CORSO".equals(stato)) {
                                stileStato = "background-color: #f8d7da; color: #721c24; font-weight: bold; padding: 4px 8px; border-radius: 4px;";
                            } else {
                                stileStato = "background-color: #d4edda; color: #155724; padding: 4px 8px; border-radius: 4px;";
                            }
                            
                            int livelloSuccesso = rs.getInt("livello_successo");
                            String visualizzaVoto = rs.wasNull() ? "-" : String.valueOf(livelloSuccesso);
                            
                            out.println("<tr>");
                            out.println("<td>" + rs.getInt("id_missione") + "</td>");
                            out.println("<td>" + rs.getString("obiettivo") + "</td>");
                            out.println("<td>" + rs.getString("posizione") + "</td>");
                            out.println("<td><span style='" + stileStato + "'>" + stato + "</span></td>");
                            out.println("<td>" + visualizzaVoto + "</td>");
                            out.println("</tr>");
                        }
                        
                        if (!haMissioni) {
                            out.println("<tr><td colspan='5' style='text-align: center; color: gray;'>Non sei ancora stato assegnato a nessuna missione.</td></tr>");
                        }
                    }
                }
                out.println("</table>");
                
            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p style='color: red;'>Errore durante il recupero dei dati delle missioni.</p>");
            }
            
            out.println("<br><hr><br>");
            out.println("<a href='#' style='background-color: #6c757d; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Modifica anagrafica e patenti possedute:</a>");
            
            out.println("</div></body></html>");
        }
    }
}