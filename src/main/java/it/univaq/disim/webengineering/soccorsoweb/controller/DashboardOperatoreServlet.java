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
        
        // Controllo della sessione e del ruolo (fondamenti di sicurezza e sessioni)
        HttpSession session = request.getSession(false);
        if (session == null || !"OPERATORE".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) session.getAttribute("id_utente");
        String nomeOperatore = (String) session.getAttribute("nome");

        // Impostazione del tipo di contenuto della risposta come JSON puro per il client JS
        response.setContentType("application/json;charset=UTF-8");
        
        String patentiCorrenti = "";
        String abilitaCorrenti = "";

        try (Connection conn = DBManager.getConnection();
             PrintWriter out = response.getWriter()) {
            
            // 1. Recupero patenti dell'operatore tramite JDBC
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

            // 2. Recupero abilità dell'operatore tramite JDBC
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

            // Costruzione manuale della struttura JSON di risposta
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"nomeOperatore\": \"").append(escapeJson(nomeOperatore)).append("\",");
            json.append("\"pat\": \"").append(escapeJson(patentiCorrenti)).append("\",");
            json.append("\"ab\": \"").append(escapeJson(abilitaCorrenti)).append("\",");
            json.append("\"missioni\": [");

            // 3. Recupero missioni dell'operatore con ordinamento
            String sql = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo "
                       + "FROM missione m "
                       + "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione "
                       + "WHERE aom.id_utente = ? "
                       + "ORDER BY m.stato DESC, m.id_missione DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idOperatore);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) json.append(",");
                        first = false;

                        int livelloSuccesso = rs.getInt("livello_successo");
                        String visualizzaVoto = rs.wasNull() ? "-" : livelloSuccesso + " / 5";
                        String stato = rs.getString("stato");

                        json.append("{");
                        json.append("\"id_missione\": ").append(rs.getInt("id_missione")).append(",");
                        json.append("\"obiettivo\": \"").append(escapeJson(rs.getString("obiettivo"))).append("\",");
                        json.append("\"posizione\": \"").append(escapeJson(rs.getString("posizione"))).append("\",");
                        json.append("\"stato\": \"").append(escapeJson(stato)).append("\",");
                        json.append("\"visualizzaVoto\": \"").append(escapeJson(visualizzaVoto)).append("\"");
                        json.append("}");
                    }
                }
            }
            json.append("]}");
            
            out.print(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno del server");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Controllo di sicurezza della sessione anche per le richieste POST
        HttpSession session = request.getSession(false);
        if (session == null || !"OPERATORE".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) session.getAttribute("id_utente");
        String patentiRaw = request.getParameter("patenti"); // Lettura dei parametri POST
        String abilitaRaw = request.getParameter("abilita");

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Sincronizzazione Patenti (Accumulativa)
                if (patentiRaw != null && !patentiRaw.trim().isEmpty()) {
                    String[] tokens = patentiRaw.split(",");
                    for (String t : tokens) {
                        String tokenPuto = t.trim().toUpperCase();
                        if (!tokenPuto.isEmpty()) {
                            String insPat = "INSERT IGNORE INTO patente (codice) VALUES (?)";
                            try (PreparedStatement stInsPat = conn.prepareStatement(insPat)) {
                                stInsPat.setString(1, tokenPuto);
                                stInsPat.executeUpdate();
                            }
                            
                            String insUserPat = "INSERT IGNORE INTO utente_patente (id_utente, id_patente) "
                                              + "VALUES (?, (SELECT id_patente FROM patente WHERE codice = ?))";
                            try (PreparedStatement stInsUserPat = conn.prepareStatement(insUserPat)) {
                                stInsUserPat.setInt(1, idOperatore);
                                stInsUserPat.setString(2, tokenPuto);
                                stInsUserPat.executeUpdate();
                            }
                        }
                    }
                }

                // Sincronizzazione Abilità (Accumulativa)
                if (abilitaRaw != null && !abilitaRaw.trim().isEmpty()) {
                    String[] tokens = abilitaRaw.split(",");
                    for (String t : tokens) {
                        String tokenPuto = t.trim().toLowerCase();
                        if (!tokenPuto.isEmpty()) {
                            String insAb = "INSERT IGNORE INTO abilita (nome) VALUES (?)";
                            try (PreparedStatement stInsAb = conn.prepareStatement(insAb)) {
                                stInsAb.setString(1, tokenPuto);
                                stInsAb.executeUpdate();
                            }
                            
                            String insUserAb = "INSERT IGNORE INTO utente_abilita (id_utente, id_abilita) "
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
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pattern Post-Redirect-Get (P-R-G): reindirizzamento pulito dopo la POST
        response.sendRedirect(request.getContextPath() + "/operatore/dashboard.html");
    }

    // Metodo di utilità per l'escape di caratteri speciali nelle stringhe JSON
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", " ")
                  .replace("\r", " ");
    }
}