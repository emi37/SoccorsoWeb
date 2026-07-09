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
            out.println("<html lang='it'>");
            out.println("<head>");
            out.println("  <meta charset='UTF-8'>");
            // Tag viewport vitale per abilitare il responsive sui telefoni
            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("  <title>Pannello di controllo admin</title>");
            out.println("</head>");
            out.println("<body style='font-family: Arial, sans-serif; padding: 15px; background-color: #f4f6f9; margin: 0;'>");
            
            // Contenitore principale flessibile e proporzionato
            out.println("<div style='max-width: 1000px; width: 100%; margin: 20px auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); box-sizing: border-box;'>");
            
            out.println("<div style='display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;'>");
            out.println("  <h2 style='margin: 0; color: #333;'>Pannello di controllo admin</h2>");
            out.println("  <a href='LogoutServlet' style='color: red; font-weight: bold; text-decoration: none; font-size: 16px;'>Esci</a>");
            out.println("</div>");
            out.println("<p style='color: #666;'>Benvenuto nel sistema di gestione della centrale operativa.</p>");
            
            // SEZIONE PULSANTI RESPONSIVE: risolto il bug delle sovrapposizioni dello screenshot
            out.println("<div style='margin-bottom: 25px; padding: 15px; background-color: #f8f9fa; border-radius: 6px; border: 1px solid #dee2e6; display: flex; flex-direction: row; flex-wrap: wrap; gap: 10px; align-items: center;'>");
            out.println("  <span style='font-weight: bold; color: #495057;'>Gestione logistica:</span>");
            out.println("  <a href='GestioneMezzi' style='background-color: #6c757d; color: white; padding: 8px 14px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 14px; display: inline-block;'>Gestisci mezzi</a>");
            out.println("  <a href='GestioneMateriali' style='background-color: #6c757d; color: white; padding: 8px 14px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 14px; display: inline-block;'>Gestisci materiali</a>");
            out.println("  <a href='StoricoMissioni' style='background-color: #17a2b8; color: white; padding: 8px 14px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 14px; display: inline-block;'>Vedi lo storico delle missioni</a>");
            out.println("</div>");

            out.println("<hr style='border: 0; border-top: 1px solid #dee2e6; margin: 20px 0;'>");

            try (Connection conn = DBManager.getConnection()) {
                
                // 1. Tabella Nuove Segnalazioni con isolamento dello scorrimento orizzontale
                out.println("<h3 style='color: #333;'>Nuove segnalazioni da gestire:</h3>");
                out.println("<div style='width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; border: 1px solid #dee2e6; border-radius: 4px; margin-bottom: 40px;'>");
                out.println("  <table style='width: 100%; border-collapse: collapse; text-align: left; min-width: 750px;'>");
                out.println("    <tr style='background-color: #e9ecef; border-bottom: 2px solid #dee2e6;'>");
                out.println("      <th style='padding: 12px; width: 60px;'>ID</th>");
                out.println("      <th style='padding: 12px; width: 140px;'>Segnalante</th>");
                out.println("      <th style='padding: 12px; width: 150px;'>Posizione</th>");
                out.println("      <th style='padding: 12px;'>Descrizione Emergenza</th>");
                out.println("      <th style='padding: 12px; width: 220px; text-align: center;'>Azione</th>");
                out.println("    </tr>");
                
                String sqlAttiveSafe = "SELECT id_richiesta, nome_segnalante, posizione, descrizione FROM richiesta_soccorso WHERE stato = 'ATTIVA' ORDER BY id_richiesta DESC";
                
                try (PreparedStatement stmt1 = conn.prepareStatement(sqlAttiveSafe);
                     ResultSet rs1 = stmt1.executeQuery()) {
                    
                    boolean ciSonoAttive = false;
                    while (rs1.next()) {
                        ciSonoAttive = true;
                        int id = rs1.getInt("id_richiesta");
                        out.println("<tr style='border-bottom: 1px solid #dee2e6;'>");
                        out.println("  <td style='padding: 12px;'><b>" + id + "</b></td>");
                        out.println("  <td style='padding: 12px;'>" + rs1.getString("nome_segnalante") + "</td>");
                        out.println("  <td style='padding: 12px;'>" + rs1.getString("posizione") + "</td>");
                        out.println("  <td style='padding: 12px; max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" + rs1.getString("descrizione") + "</td>");
                        
                        out.println("  <td style='padding: 12px; text-align: center;'>");
                        out.println("    <a href='GestioneRichiestaServletDallAdmin?id=" + id + "' style='background-color: #007bff; color: white; padding: 6px 12px; text-decoration: none; border-radius: 4px; font-weight: bold; margin-right: 5px; display: inline-block; font-size: 13px;'>Assegna Risorse</a>");
                        out.println("    <a href='IgnoraRichiesta?id=" + id + "' style='background-color: #dc3545; color: white; padding: 6px 12px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block; font-size: 13px;' onclick=\"return confirm('Sei sicuro di voler ignorare e archiviare questa richiesta?');\">Ignora</a>");
                        out.println("  </td>");
                        out.println("</tr>");
                    }
                    if (!ciSonoAttive) {
                        out.println("    <tr><td colspan='5' style='text-align: center; color: green; padding: 20px; font-weight: bold;'>Nessuna nuova richiesta in attesa di gestione.</td></tr>");
                    }
                }
                out.println("  </table>");
                out.println("</div>");


                // 2. Tabella Interventi Attivi con isolamento dello scorrimento orizzontale
                out.println("<h3 style='color: #333;'>Interventi attivi:</h3>");
                out.println("<div style='width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; border: 1px solid #dee2e6; border-radius: 4px;'>");
                out.println("  <table style='width: 100%; border-collapse: collapse; text-align: left; min-width: 750px;'>");
                out.println("    <tr style='background-color: #f8d7da; border-bottom: 2px solid #f5c6cb;'>");
                out.println("      <th style='padding: 12px; width: 110px;'>ID Missione</th>");
                out.println("      <th style='padding: 12px; width: 110px;'>ID Richiesta</th>");
                out.println("      <th style='padding: 12px;'>Dettagli Operazione</th>");
                out.println("      <th style='padding: 12px; width: 130px;'>Stato Corrente</th>");
                out.println("      <th style='padding: 12px; width: 180px; text-align: center;'>Azione</th>");
                out.println("    </tr>");
                
                String sqlInCorso = "SELECT id_missione, id_richiesta, obiettivo FROM missione WHERE stato = 'IN_CORSO' ORDER BY id_missione DESC";
                try (PreparedStatement stmt2 = conn.prepareStatement(sqlInCorso);
                     ResultSet rs2 = stmt2.executeQuery()) {
                    
                    boolean ciSonoInCorso = false;
                    while (rs2.next()) {
                        ciSonoInCorso = true;
                        int idM = rs2.getInt("id_missione");
                        out.println("<tr style='border-bottom: 1px solid #dee2e6;'>");
                        out.println("  <td style='padding: 12px;'><b>#" + idM + "</b></td>");
                        out.println("  <td style='padding: 12px;'>#" + rs2.getInt("id_richiesta") + "</td>");
                        out.println("  <td style='padding: 12px; max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" + rs2.getString("obiettivo") + "</td>");
                        out.println("  <td style='padding: 12px;'><span style='color: #721c24; background-color: #f8d7da; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 12px;'>IN CORSO</span></td>");
                        
                        out.println("  <td style='padding: 12px; text-align: center;'>");
                        out.println("    <a href='DettaglioMissioneServlet?id_missione=" + idM + "' style='display: inline-block; background-color: #17a2b8; color: white; padding: 6px 10px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 13px; margin-bottom: 2px; margin-right: 4px;'>Dettaglio</a>");
                        out.println("    <a href='ConcludiMissioneServlet?id_missione=" + idM + "' style='display: inline-block; background-color: #28a745; color: white; padding: 6px 10px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 13px;'>Termina</a>");
                        out.println("  </td>");
                        out.println("</tr>");
                    }
                    if (!ciSonoInCorso) {
                        out.println("    <tr><td colspan='5' style='text-align: center; color: gray; padding: 20px;'>Nessuna missione attualmente operativa sul campo.</td></tr>");
                    }
                }
                out.println("  </table>");
                out.println("</div>");
                
            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p style='color: red;'>Errore durante la lettura dei dati operativi.</p>");
            }
            
            // 3. Form per la registrazione di Amministratori e Operatori (Reso fluido con Flex-Wrap)
            out.println("<br><hr style='border: 0; border-top: 1px solid #dee2e6; margin: 25px 0;'><br>");
            out.println("<h3 style='color: #333;'>Registrazione nuovo personale (Amministratori / Operatori)</h3>");
            out.println("<form action='" + request.getContextPath() + "/CreaAdminEOperatoreServlet' method='POST' style='background: #f8f9fa; padding: 20px; border-radius: 6px; border: 1px solid #dee2e6; box-sizing: border-box;'>");

            out.println("  <div style='display: flex; gap: 15px; margin-bottom: 15px; flex-wrap: wrap;'>");
            out.println("    <div style='flex: 1; min-width: 240px;'>");
            out.println("      <label for='nome_u'><b>Nome:</b></label><br>");
            out.println("      <input type='text' id='nome_u' name='nome' required style='width: 100%; padding: 10px; margin-top: 6px; border-radius: 4px; border: 1px solid #ced4da; box-sizing: border-box; font-size: 14px;'>");
            out.println("    </div>");
            out.println("    <div style='flex: 1; min-width: 240px;'>");
            out.println("      <label for='cognome_u'><b>Cognome:</b></label><br>");
            out.println("      <input type='text' id='cognome_u' name='cognome' required style='width: 100%; padding: 10px; margin-top: 6px; border-radius: 4px; border: 1px solid #ced4da; box-sizing: border-box; font-size: 14px;'>");
            out.println("    </div>");
            out.println("  </div>");

            out.println("  <div style='display: flex; gap: 15px; margin-bottom: 15px; flex-wrap: wrap;'>");
            out.println("    <div style='flex: 1; min-width: 240px;'>");
            out.println("      <label for='email_u'><b>E-mail:</b></label><br>");
            out.println("      <input type='email' id='email_u' name='email' required style='width: 100%; padding: 10px; margin-top: 6px; border-radius: 4px; border: 1px solid #ced4da; box-sizing: border-box; font-size: 14px;'>");
            out.println("    </div>");
            out.println("    <div style='flex: 1; min-width: 240px;'>");
            out.println("      <label for='pass_u'><b>Password:</b></label><br>");
            out.println("      <input type='password' id='pass_u' name='password' required style='width: 100%; padding: 10px; margin-top: 6px; border-radius: 4px; border: 1px solid #ced4da; box-sizing: border-box; font-size: 14px;'>");
            out.println("    </div>");
            out.println("  </div>");

            out.println("<div style='margin-bottom: 20px;'>");
            out.println("<label for='ruolo_u'><b>Ruolo assegnato:</b></label><br>");
            out.println("<select id='ruolo_u' name='ruolo' style='width: 100%; padding: 10px; margin-top: 6px; border-radius: 4px; border: 1px solid #ced4da; box-sizing: border-box; background: white; font-size: 14px;'>");
            out.println("<option value='OPERATORE'>Operatore sul campo</option>");
            out.println(" <option value='ADMIN'>Amministratore di sistema</option>");
            out.println(" </select>");
            out.println("</div>");

            out.println("  <button type='submit' style='background-color: #007bff; color: white; padding: 12px 24px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 15px; width: 100%; max-width: 280px; box-sizing: border-box;'>Registra account personale</button>");
            out.println("</form>");
            
            out.println("</div></body></html>");
        }
    }
}