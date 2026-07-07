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

@WebServlet(name = "DashboardServlet", urlPatterns = {"/DashboardServlet"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Pannello di controllo admin</title>");
            out.println("</head>");
            out.println("<body style='font-family: Arial; padding: 20px; background-color: #f4f6f9;'>");
            out.println("<div style='max-width: 1000px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05);'>");
            
            out.println("<div style='display: flex; justify-content: space-between; align-items: center;'>");
            out.println("<h2>Pannello di controllo admin</h2>");
            out.println("<a href='LogoutServlet' style='color: red; font-weight: bold; text-decoration: none;'>Esci</a>");
            out.println("</div>");
            out.println("<p>Benvenuto! </p>");
            
          //   sezione pulsanti per la gestione delle risorse logistiche
            out.println("<div style='margin-bottom: 25px; padding: 15px; background-color: #f8f9fa; border-radius: 6px; border: 1px solid #dee2e6;'>");
            out.println("<span style='font-weight: bold; color: #495057; margin-right: 15px;'>Gestione logistica:</span>");
            out.println("<a href='GestioneMezzi' style='background-color: #6c757d; color: white; padding: 8px 14px; text-decoration: none; border-radius: 4px; font-weight: bold; margin-right: 10px; font-size: 14px;'>Gestisci mezzi</a>");
            out.println("<a href='GestioneMateriali' style='background-color: #6c757d; color: white; padding: 8px 14px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 14px;'>Gestisci materiali</a>");
            out.println("</div>");

            out.println("<hr><br>");

            try (Connection conn = DBManager.getConnection()) {
                
                // mostra le segnalazioni avvenute con risorse ancora da assegnare
                out.println("<h3>Nuove segnalazioni da gestire: </h3>");
                out.println("<table border='1' cellpadding='10' cellspacing='0' style='width:100%; border-collapse: collapse; text-align: left; margin-bottom: 40px;'>");
                out.println("<tr style='background-color: #e9ecef;'><th>ID</th><th>Segnalante</th><th>Posizione</th><th>Descrizione Emergenza</th><th>Azione</th></tr>");
                
                String sqlAttive = "SELECT id_richiesta, nome_segnalante, posizione, descrizione FROM richiesta_soccorso WHERE stato = 'ATTIVA' ORDER BY id_richiesta DESC";
                try (PreparedStatement stmt1 = conn.prepareStatement(sqlAttive);
                     ResultSet rs1 = stmt1.executeQuery()) {
                    
                    boolean ciSonoAttive = false;
                    while (rs1.next()) {
                        ciSonoAttive = true;
                        int id = rs1.getInt("id_richiesta");
                        out.println("<tr>");
                        out.println("<td>" + id + "</td>");
                        out.println("<td>" + rs1.getString("nome_segnalante") + "</td>");
                        out.println("<td>" + rs1.getString("posizione") + "</td>");
                        out.println("<td>" + rs1.getString("descrizione") + "</td>");
                        out.println("<td><a href='GestioneRichiestaServletDallAdmin?id=" + id + "' style='background-color: #007bff; color: white; padding: 6px 12px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Assegna Risorse</a></td>");
                        out.println("</tr>");
                    }
                    if (!ciSonoAttive) {
                        out.println("<tr><td colspan='5' style='text-align: center; color: green;'>Nessuna nuova richiesta in attesa di gestione.</td></tr>");
                    }
                }
                out.println("</table>");

                // mostra le missioni in corso
                out.println("<h3>Interventi attivi </h3>");   // e missioni sul Campo
                out.println("<table border='1' cellpadding='10' cellspacing='0' style='width:100%; border-collapse: collapse; text-align: left;'>");
                out.println("<tr style='background-color: #f8d7da;'><th>ID Missione</th><th>ID Richiesta</th><th>Dettagli Operazione</th><th>Stato Corrente</th><th>Azione</th></tr>");
                
                String sqlInCorso = "SELECT id_missione, id_richiesta, obiettivo FROM missione WHERE stato = 'IN_CORSO' ORDER BY id_missione DESC";
                try (PreparedStatement stmt2 = conn.prepareStatement(sqlInCorso);
                     ResultSet rs2 = stmt2.executeQuery()) {
                    
                    boolean ciSonoInCorso = false;
                    while (rs2.next()) {
                        ciSonoInCorso = true;
                        int idM = rs2.getInt("id_missione");
                        out.println("<tr>");
                        
                        out.println("<td>" + idM + "</td>");
                        out.println("<td>" + rs2.getInt("id_richiesta") + "</td>");
                        out.println("<td>" + rs2.getString("obiettivo") + "</td>");
                        out.println("<td><span style='color: red; font-weight: bold;'>IN CORSO</span></td>");
                        out.println("<td style='text-align: center; width: 160px;'>");
                        out.println("<a href='DettaglioMissioneServlet?id_missione=" + idM + "' style='display: block; text-align: center; background-color: #17a2b8; color: white; padding: 6px 0; text-decoration: none; border-radius: 4px; font-weight: bold; margin-bottom: 6px; width: 140px; margin-left: auto; margin-right: auto;'>Diario di Bordo</a>");
                        out.println("<a href='ConcludiMissioneServlet?id_missione=" + idM + "' style='display: block; text-align: center; background-color: #28a745; color: white; padding: 6px 0; text-decoration: none; border-radius: 4px; font-weight: bold; width: 140px; margin-left: auto; margin-right: auto;'>Termina Intervento</a>");
                        out.println("</td>");
                        out.println("</tr>");
                    }
                    if (!ciSonoInCorso) {
                        out.println("<tr><td colspan='5' style='text-align: center; color: gray;'>Nessuna missione attualmente operativa sul campo.</td></tr>");
                    }
                }
                out.println("</table>");
                
            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p style='color: red;'>Errore durante la lettura dei dati operativi.</p>");
            }
            
            out.println("</div></body></html>");
        }
    }
}