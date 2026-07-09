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

@WebServlet(name = "DettaglioMissioneServlet", urlPatterns = {"/DettaglioMissioneServlet"})
public class DettaglioMissioneServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Controllo di sicurezza: Solo l'ADMIN può accedere
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='it'>");
            out.println("<head>");
            out.println("  <meta charset='UTF-8'>");
            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("  <title>Dettagli - Missione #" + idMissione + "</title>");
            out.println("</head>");
out.println("  <body style='font-family: Arial, sans-serif; padding: 15px; background-color: #f4f6f9; margin: 0;'>");
            
            // Contenitore fluido responsive
            out.println("<div style='max-width: 800px; width: 100%; margin: 20px auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); box-sizing: border-box;'>");
            
            out.println("<h2 style='margin-top: 0; color: #333;'>Dettagli - Missione #" + idMissione + "</h2>");
            out.println("<a href='DashboardServlet' style='text-decoration: none; color: #007bff; font-weight: bold;'>&larr; Torna alla Dashboard</a>");
            out.println("<hr style='border: 0; border-top: 1px solid #dee2e6; margin: 20px 0;'>");

            String statoMissione = "";

            try (Connection conn = DBManager.getConnection()) {
                
                // Recupero informazioni principali della Missione
                String sqlMissione = "SELECT obiettivo, posizione, stato, timestamp_inizio FROM missione WHERE id_missione = ?";
                try (PreparedStatement stmtM = conn.prepareStatement(sqlMissione)) {
                    stmtM.setInt(1, Integer.parseInt(idMissione));
                    try (ResultSet rsM = stmtM.executeQuery()) {
                        if (rsM.next()) {
                            statoMissione = rsM.getString("stato");
                            
                            String stileStato = "IN_CORSO".equals(statoMissione) 
                                ? "background-color: #f8d7da; color: #721c24; padding: 4px 8px; border-radius: 4px; font-weight: bold;"
                                : "background-color: #e2e3e5; color: #383d41; padding: 4px 8px; border-radius: 4px; font-weight: bold;";

                            //etrazione del caposquadra assegnato a questa specifica missione
                            String nomeCaposquadra = "Non ancora assegnato o non rilevato";
                            String sqlCapo = "SELECT u.nome, u.cognome FROM utente u "
                                           + "JOIN assegnazione_operatori_missione aom ON u.id_utente = aom.id_utente "
                                           + "WHERE aom.id_missione = ? AND aom.is_caposquadra = 1";
                            try (PreparedStatement stmtC = conn.prepareStatement(sqlCapo)) {
                                stmtC.setInt(1, Integer.parseInt(idMissione));
                                try (ResultSet rsC = stmtC.executeQuery()) {
                                    if (rsC.next()) {
                                        nomeCaposquadra = rsC.getString("nome") + " " + rsC.getString("cognome");
                                    }
                                }
                            }

                            out.println("<h3 style='color: #333;'>Dettagli Contesto</h3>");
                            out.println("<p><b>Obiettivo:</b> " + rsM.getString("obiettivo") + "</p>");
                            out.println("<p><b>Posizione:</b> " + rsM.getString("posizione") + "</p>");
                            out.println("<p><b>Stato:</b> <span style='" + stileStato + "'>" + statoMissione + "</span></p>");
                            out.println("<p><b>Inizio Intervento:</b> " + rsM.getTimestamp("timestamp_inizio") + "</p>");
                            // Stampa a video del Caposquadra con un badge evidenziato giallo
                            out.println("<p><b>Caposquadra:</b> <mark style='background-color: #fff3cd; color: #856404; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 14px;'>" + nomeCaposquadra + "</mark></p>");
                            
                        } else {
                            out.println("<p style='color:red; font-weight:bold;'>Errore: Missione non trovata.</p>");
                            out.println("</div></body></html>");
                            return;
                        }
                    }
                }
                
                out.println("<hr style='border: 0; border-top: 1px solid #dee2e6; margin: 25px 0;'>");

                // PUNTO 4: Mostra il form di inserimento SOLO se la missione è attiva ('IN_CORSO')
                if ("IN_CORSO".equals(statoMissione)) {
                    out.println("<h3 style='color: #333;'>Aggiungi Comunicazione / Aggiornamento campo</h3>");
                    out.println("<form action='DettaglioMissioneServlet' method='POST' style='box-sizing: border-box;'>");
                    out.println("  <input type='hidden' name='id_missione' value='" + idMissione + "'>");
                    out.println("  <label for='testo' style='font-weight: bold; color: #495057;'>Messaggio dalla centrale o dalla squadra:</label><br>");
                    out.println("  <textarea id='testo' name='testo_descrittivo' rows='4' style='width: 100%; margin-top: 8px; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; box-sizing: border-box; font-family: Arial, sans-serif; resize: vertical;' placeholder='Es: Squadra arrivata sul posto. Inizio evacuazione...' required></textarea><br><br>");
                    out.println("  <button type='submit' style='background-color: #17a2b8; color: white; padding: 12px 24px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 15px; width: 100%; max-width: 220px;'>Invia Aggiornamento</button>");
                    out.println("</form>");
                } else {
                    out.println("<div style='background-color: #e2e3e5; color: #383d41; padding: 15px; border-radius: 6px; border: 1px solid #d6d8db; font-weight: bold; text-align: center;'>");
                    out.println("🔒 Missione conclusa ed archiviata. Non è possibile aggiungere ulteriori aggiornamenti.");
                    out.println("</div>");
                }
                
                out.println("<hr style='border: 0; border-top: 1px solid #dee2e6; margin: 25px 0;'>");

                // Storico degli aggiornamenti (Timeline)
                out.println("<h3 style='color: #333; margin-bottom: 15px;'>Cronistoria Eventi (Timeline)</h3>");
                String sqlAggiornamenti = "SELECT am.testo_descrittivo, am.timestamp_inserimento, u.nome, u.cognome "
                        + "FROM aggiornamento_missione am "
                        + "JOIN utente u ON am.id_admin = u.id_utente "
                        + "WHERE am.id_missione = ? "
                        + "ORDER BY am.timestamp_inserimento DESC";
                
                try (PreparedStatement stmtA = conn.prepareStatement(sqlAggiornamenti)) {
                    stmtA.setInt(1, Integer.parseInt(idMissione));
                    try (ResultSet rsA = stmtA.executeQuery()) {
                        boolean ciSonoEventi = false;
                        
                        while (rsA.next()) {
                            ciSonoEventi = true;
                            out.println("<div style='background-color: #f8f9fa; padding: 15px; margin-bottom: 15px; border-left: 5px solid #17a2b8; border-radius: 4px; box-sizing: border-box;'>");
                            out.println("  <p style='margin: 0; font-size: 13px; color: gray;'><b>Data/Ora:</b> " + rsA.getTimestamp("timestamp_inserimento") + " | <b>Operatore:</b> " + rsA.getString("nome") + " " + rsA.getString("cognome") + "</p>");
                            out.println("  <p style='margin-top: 8px; font-size: 15px; color: #212529; line-height: 1.4; white-space: pre-wrap;'>" + rsA.getString("testo_descrittivo") + "</p>");
                            out.println("</div>");
                        }
                        
                        if (!ciSonoEventi) {
                            out.println("<p style='color: gray; font-style: italic;'>Nessun aggiornamento ancora registrato per questa missione.</p>");
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p style='color: red;'>Errore nel caricamento del diario della missione.</p>");
            }

            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Controllo Sessione
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");
        String testoDescrittivo = request.getParameter("testo_descrittivo");
        int idAdminLoggato = (int) session.getAttribute("id_utente");

        boolean inserito = false;
        String messaggioErrore = "Errore durante il salvataggio della nota.";

        try (Connection conn = DBManager.getConnection()) {
            
            // Protezione Backend: Verifichiamo lo stato prima di inserire la riga
            String sqlCheck = "SELECT stato FROM missione WHERE id_missione = ?";
            String statoAttuale = "";
            try (PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {
                stmtCheck.setInt(1, Integer.parseInt(idMissione));
                try (ResultSet rsCheck = stmtCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        statoAttuale = rsCheck.getString("stato");
                    }
                }
            }

            if ("IN_CORSO".equals(statoAttuale)) {
                String sqlInsert = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                    stmt.setInt(1, Integer.parseInt(idMissione));
                    stmt.setInt(2, idAdminLoggato);
                    stmt.setString(3, testoDescrittivo);
                    
                    int righe = stmt.executeUpdate();
                    if (righe > 0) {
                        inserito = true;
                    }
                }
            } else {
                messaggioErrore = "Impossibile inserire note: la missione risulta già completata o chiusa.";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Risposta e redirect
        if (inserito) {
            response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissione);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<script>");
                out.println("alert('" + messaggioErrore + "');");
                out.println("window.history.back();");
                out.println("<" + "/script>"); // Separazione protettiva tag script
            }
        }
    }
}