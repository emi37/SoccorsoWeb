package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
        
        // Controllo della sessione e del ruolo
        HttpSession session = request.getSession(false);
        if (session == null || !"OPERATORE".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) session.getAttribute("id_utente");
        String nomeOperatore = (String) session.getAttribute("nome");

        response.setContentType("text/html;charset=UTF-8");
        
        String patentiCorrenti = "";
        String abilitaCorrenti = "";

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='it'>");
            out.println("<head><meta charset='UTF-8'><title>Area riservata per gli operatori</title></head>");
            out.println("<body style='font-family: Arial; padding: 20px; background-color: #e9ecef;'>");
            out.println("<div style='max-width: 900px; margin: auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.1);'>");
            
            out.println("<div style='display: flex; justify-content: space-between; align-items: center;'>");
            out.println("<h2>Pannello operatore: " + nomeOperatore + "</h2>");
            out.println("<a href='" + request.getContextPath() + "/LogoutServlet' style='color: red; font-weight: bold; text-decoration: none;'>Esci</a>");
            out.println("</div>");
            out.println("<p>Qui puoi monitorare lo stato delle missioni alle quali sei stato assegnato e aggiornare il tuo profilo professionale.</p>");
            out.println("<hr><br>");

            try (Connection conn = DBManager.getConnection()) {
                
                // 1. Recupero patenti dell'operatore aggregate tramite relazione pivot
                String sqlPat = "SELECT GROUP_CONCAT(p.codice SEPARATOR ', ') AS lista_patenti "
                              + "FROM utente_patente up "
                              + "JOIN patente p ON up.id_patente = p.id_patente "
                              + "WHERE up.id_utente = ?";
                try (PreparedStatement stmtPat = conn.prepareStatement(sqlPat)) {
                    stmtPat.setInt(1, idOperatore);
                    try (ResultSet rsPat = stmtPat.executeQuery()) {
                        if (rsPat.next() && rsPat.getString("lista_patenti") != null) {
                            patentiCorrenti = rsPat.getString("lista_patenti");
                        }
                    }
                }

                // 2. Recupero abilità dell'operatore aggregate tramite relazione pivot
                String sqlAb = "SELECT GROUP_CONCAT(a.nome SEPARATOR ', ') AS lista_abilita "
                             + "FROM utente_abilita ua "
                             + "JOIN abilita a ON ua.id_abilita = a.id_abilita "
                             + "WHERE ua.id_utente = ?";
                try (PreparedStatement stmtAb = conn.prepareStatement(sqlAb)) {
                    stmtAb.setInt(1, idOperatore);
                    try (ResultSet rsAb = stmtAb.executeQuery()) {
                        if (rsAb.next() && rsAb.getString("lista_abilita") != null) {
                            abilitaCorrenti = rsAb.getString("lista_abilita");
                        }
                    }
                }

                // Tabella elenco missioni allineata a m.livello_successo e assegnazione_operatori_missione
                out.println("<h3>Elenco delle tue missioni:</h3>");
                out.println("<table border='1' cellpadding='10' cellspacing='0' style='width:100%; border-collapse: collapse; text-align: left;'>");
                out.println("<tr style='background-color: #f8f9fa;'><th>Id missione</th><th>Obiettivo</th><th>Posizione</th><th>Stato</th><th>Esito (0-5)</th></tr>");
                
                // Query corretta: estratto m.livello_successo invece di m.voto
                String sql = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo "
                           + "FROM missione m "
                           + "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione "
                           + "WHERE aom.id_utente = ? "
                           + "ORDER BY m.stato DESC, m.id_missione DESC";
                
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
                            
                            // Lettura corretta del campo livello_successo dal recordset
                            int livelloSuccesso = rs.getInt("livello_successo");
                            String visualizzaVoto = rs.wasNull() ? "-" : livelloSuccesso + " / 5";
                            
                            out.println("<tr>");
                            out.println("<td><b>#" + rs.getInt("id_missione") + "</b></td>");
                            out.println("<td>" + rs.getString("obiettivo") + "</td>");
                            out.println("<td>" + rs.getString("posizione") + "</td>");
                            out.println("<td><span style='" + stileStato + "'>" + stato.toLowerCase() + "</span></td>");
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
                out.println("<p style='color: red;'>Errore durante il recupero dei dati delle missioni: <b>" + e.getMessage() + "</b></p>");
            }
            
            // Form di modifica profilo
            out.println("<br><hr><br>");
            out.println("<h3>Aggiorna profilo e competenze</h3>");
            out.println("<form action='" + request.getContextPath() + "/DashboardOperatoreServlet' method='POST' style='background: #f8f9fa; padding: 20px; border-radius: 6px; border: 1px solid #dee2e6;'>");
            
            out.println("<div style='margin-bottom: 15px;'>");
            out.println("<label for='patenti'><b>Patenti di guida possedute (es. b, c, d):</b></label><br>");
            out.println("<input type='text' id='patenti' name='patenti' value='" + patentiCorrenti + "' placeholder='Es. b, c' style='width: 100%; padding: 8px; margin-top: 5px; border-radius: 4px; border: 1px solid #ccc;'>");
            out.println("</div>");
            
            out.println("<div style='margin-bottom: 15px;'>");
            out.println("<label for='abilita'><b>Abilità extra e specializzazioni:</b></label><br>");
            out.println("<input type='text' id='abilita' name='abilita' value='" + abilitaCorrenti + "' placeholder='Es. primo soccorso, soccorso fluviale' style='width: 100%; padding: 8px; margin-top: 5px; border-radius: 4px; border: 1px solid #ccc;'>");
            out.println("</div>");
            
            out.println("<button type='submit' style='background-color: #28a745; color: white; padding: 10px 20px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer;'>Salva modifiche scheda</button>");
            out.println("</form>");
            
            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || !"OPERATORE".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) session.getAttribute("id_utente");
        String patentiRaw = request.getParameter("patenti");
        String abilitaRaw = request.getParameter("abilita");

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Sincronizzazione Patenti
                String delPat = "DELETE FROM utente_patente WHERE id_utente = ?";
                try (PreparedStatement stDelPat = conn.prepareStatement(delPat)) {
                    stDelPat.setInt(1, idOperatore);
                    stDelPat.executeUpdate();
                }
                
                if (patentiRaw != null && !patentiRaw.trim().isEmpty()) {
                    String[] tokens = patentiRaw.split(",");
                    for (String t : tokens) {
                        String tokenPuto = t.trim().toUpperCase();
                        if (!tokenPuto.isEmpty()) {
                            // Inserisce o ignora il codice della patente
                            String insPat = "INSERT IGNORE INTO patente (codice) VALUES (?)";
                            try (PreparedStatement stInsPat = conn.prepareStatement(insPat)) {
                                stInsPat.setString(1, tokenPuto);
                                stInsPat.executeUpdate();
                            }
                            // Associa la patente all'utente
                            String insUserPat = "INSERT INTO utente_patente (id_utente, id_patente) "
                                              + "VALUES (?, (SELECT id_patente FROM patente WHERE codice = ?))";
                            try (PreparedStatement stInsUserPat = conn.prepareStatement(insUserPat)) {
                                stInsUserPat.setInt(1, idOperatore);
                                stInsUserPat.setString(2, tokenPuto);
                                stInsUserPat.executeUpdate();
                            }
                        }
                    }
                }

                // 2. Sincronizzazione Abilità
                String delAb = "DELETE FROM utente_abilita WHERE id_utente = ?";
                try (PreparedStatement stDelAb = conn.prepareStatement(delAb)) {
                    stDelAb.setInt(1, idOperatore);
                    stDelAb.executeUpdate();
                }
                
                if (abilitaRaw != null && !abilitaRaw.trim().isEmpty()) {
                    String[] tokens = abilitaRaw.split(",");
                    for (String t : tokens) {
                        String tokenPuto = t.trim().toLowerCase();
                        if (!tokenPuto.isEmpty()) {
                            // Inserisce o ignora il nome dell'abilità
                            String insAb = "INSERT IGNORE INTO abilita (nome) VALUES (?)";
                            try (PreparedStatement stInsAb = conn.prepareStatement(insAb)) {
                                stInsAb.setString(1, tokenPuto);
                                stInsAb.executeUpdate();
                            }
                            // Associa l'abilità all'utente
                            String insUserAb = "INSERT INTO utente_abilita (id_utente, id_abilita) "
                                             + "VALUES (?, (SELECT id_abilita FROM abilita WHERE nome = ?))";
                            try (PreparedStatement stInsUserAb = conn.prepareStatement(insUserAb)) {
                                stInsUserAb.setInt(1, idOperatore);
                                stInsUserAb.setString(2, tokenPuto);
                                stInsUserAb.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            out.println("<script>");
            out.println("alert('Profilo aggiornato con successo!');");
            out.println("window.location.href='" + request.getContextPath() + "/DashboardOperatoreServlet';");
            out.println("</script>");
            out.println("</body></html>");
        }
    }
}