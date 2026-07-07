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
        
        // Controllo di sicurezza: Solo l'ADMIN può visualizzare e aggiornare il diario di bordo
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // Recupero dei parametri di input
        String idMissione = request.getParameter("id_missione");
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Diario di Bordo - Missione #" + idMissione + "</title></head>");
            out.println("<body style='font-family: Arial; padding: 20px; background-color: #f4f6f9;'>");
            out.println("<div style='max-width: 800px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05);'>");
            
            out.println("<h2>DIARIO DI BORDO - Missione #" + idMissione + "</h2>");
            out.println("<a href='DashboardServlet' style='text-decoration: none; color: #007bff;'>&larr; Torna alla Dashboard</a>");
            out.println("<hr><br>");

            try (Connection conn = DBManager.getConnection()) {
                
                // Recupero informazioni principali della Missione
                String sqlMissione = "SELECT obiettivo, posizione, stato, timestamp_inizio FROM missione WHERE id_missione = ?";
                try (PreparedStatement stmtM = conn.prepareStatement(sqlMissione)) {
                    stmtM.setInt(1, Integer.parseInt(idMissione));
                    try (ResultSet rsM = stmtM.executeQuery()) {
                        if (rsM.next()) {
                            out.println("<h3>Dettagli Contesto</h3>");
                            out.println("<p><b>Obiettivo:</b> " + rsM.getString("obiettivo") + "</p>");
                            out.println("<p><b>Posizione:</b> " + rsM.getString("posizione") + "</p>");
                            out.println("<p><b>Stato:</b> " + rsM.getString("stato") + "</p>");
                            out.println("<p><b>Inizio Intervento:</b> " + rsM.getTimestamp("timestamp_inizio") + "</p>");
                        } else {
                            out.println("<p style='color:red;'>Errore: Missione non trovata.</p>");
                            out.println("</div></body></html>");
                            return;
                        }
                    }
                }
                
                out.println("<br><hr>");

                // Form di inserimento nuovo aggiornamento (Solo se la missione è ancora in corso)
                out.println("<h3>Aggiungi Comunicazione / Aggiornamento campo</h3>");
                out.println("<form action='DettaglioMissioneServlet' method='POST'>");
                out.println("<input type='hidden' name='id_missione' value='" + idMissione + "'>");
                out.println("<label for='testo'><b>Messaggio dalla centrale o dalla squadra:</b></label><br>");
                out.println("<textarea id='testo' name='testo_descrittivo' rows='4' style='width: 100%; margin-top: 8px;' placeholder='Es: Squadra arrivata sul posto. Inizio evacuazione...' required></textarea><br><br>");
                out.println("<button type='submit' style='background-color: #17a2b8; color: white; padding: 10px 20px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer;'>Invia Aggiornamento</button>");
                out.println("</form>");
                
                out.println("<br><hr>");

                // Storico degli aggiornamenti
                out.println("<h3>Cronistoria Eventi (Timeline)</h3>");
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
                            out.println("<div style='background-color: #f8f9fa; padding: 15px; margin-bottom: 15px; border-left: 5px solid #17a2b8; border-radius: 4px;'>");
                            out.println("<p style='margin: 0; font-size: 14px; color: gray;'><b>Data/Ora:</b> " + rsA.getTimestamp("timestamp_inserimento") + " | <b>Operatore Centrale:</b> " + rsA.getString("nome") + " " + rsA.getString("cognome") + "</p>");
                            out.println("<p style='margin-top: 8px; font-size: 16px;'>" + rsA.getString("testo_descrittivo") + "</p>");
                            out.println("</div>");
                        }
                        
                        if (!ciSonoEventi) {
                            out.println("<p style='color: gray; italic;'>Nessun aggiornamento ancora registrato per questa missione.</p>");
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p style='color: red;'>Errore nel caricamento del diario di bordo.</p>");
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

        // Recupero parametri dal Form e dalla Sessione dell'Admin loggato
        String idMissione = request.getParameter("id_missione");
        String testoDescrittivo = request.getParameter("testo_descrittivo");
        int idAdminLoggato = (int) session.getAttribute("id_utente");

        boolean inserito = false;

        // Scrittura nel DB
        try (Connection conn = DBManager.getConnection()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirect alla GET per rinfrescare la pagina e mostrare l'evento in timeline
        if (inserito) {
            response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissione);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<script>");
                out.println("alert('Errore durante il salvataggio della nota.');");
                out.println("window.history.back();");
                out.println("</script>");
            }
        }
    }
}