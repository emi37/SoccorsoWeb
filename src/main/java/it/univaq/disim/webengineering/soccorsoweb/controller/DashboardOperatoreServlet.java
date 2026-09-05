package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
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

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico il driver una volta sola all'avvio
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"OPERATORE".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) session.getAttribute("id_utente");
        String nomeOperatore = (String) session.getAttribute("nome");

        // output in JSON puro
        response.setContentType("application/json;charset=UTF-8");
        
        String patentiCorrenti = "";
        String abilitaCorrenti = "";

        // connessione nativa
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PrintWriter out = response.getWriter()) {
            
            // 1. recupero patenti
            String sqlPat = "SELECT GROUP_CONCAT(p.codice SEPARATOR ', ') AS lista_patenti " +
                            "FROM utente_patente up " +
                            "JOIN patente p ON up.id_patente = p.id_patente " +
                            "WHERE up.id_utente = ?";
            
            try (PreparedStatement stmtPat = conn.prepareStatement(sqlPat)) {
                stmtPat.setInt(1, idOperatore);
                try (ResultSet rsPat = stmtPat.executeQuery()) {
                    if (rsPat.next() && rsPat.getString("lista_patenti") != null) {
                        patentiCorrenti = rsPat.getString("lista_patenti");
                    }
                }
            }

            // 2. recupero specializzazioni/abilità
            String sqlAb = "SELECT GROUP_CONCAT(a.nome SEPARATOR ', ') AS lista_abilita " +
                           "FROM utente_abilita ua " +
                           "JOIN abilita a ON ua.id_abilita = a.id_abilita " +
                           "WHERE ua.id_utente = ?";
                           
            try (PreparedStatement stmtAb = conn.prepareStatement(sqlAb)) {
                stmtAb.setInt(1, idOperatore);
                try (ResultSet rsAb = stmtAb.executeQuery()) {
                    if (rsAb.next() && rsAb.getString("lista_abilita") != null) {
                        abilitaCorrenti = rsAb.getString("lista_abilita");
                    }
                }
            }

            // buildo il JSON a mano
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"nomeOperatore\": \"").append(escapeJson(nomeOperatore)).append("\",");
            json.append("\"pat\": \"").append(escapeJson(patentiCorrenti)).append("\",");
            json.append("\"ab\": \"").append(escapeJson(abilitaCorrenti)).append("\",");
            json.append("\"missioni\": [");

            // 3. recupero missioni associate
            String sql = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo " +
                         "FROM missione m " +
                         "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione " +
                         "WHERE aom.id_utente = ? " +
                         "ORDER BY m.stato DESC, m.id_missione DESC";
            
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
            
            // sparo il json al client
            out.print(json.toString());

        } catch (Exception e) {
            System.err.println("Errore db durante fetch dati operatore");
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno del server");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"OPERATORE".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) session.getAttribute("id_utente");
        
        // input dal form (patenti e abilità separate da virgola)
        String patentiRaw = request.getParameter("patenti");
        String abilitaRaw = request.getParameter("abilita");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // stacco l'autocommit per la transazione
            conn.setAutoCommit(false);
            
            try {
                // sync patenti
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
                            
                            String insUserPat = "INSERT IGNORE INTO utente_patente (id_utente, id_patente) " +
                                                "VALUES (?, (SELECT id_patente FROM patente WHERE codice = ?))";
                            try (PreparedStatement stInsUserPat = conn.prepareStatement(insUserPat)) {
                                stInsUserPat.setInt(1, idOperatore);
                                stInsUserPat.setString(2, tokenPuto);
                                stInsUserPat.executeUpdate();
                            }
                        }
                    }
                }

                // sync specializzazioni
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
                            
                            String insUserAb = "INSERT IGNORE INTO utente_abilita (id_utente, id_abilita) " +
                                               "VALUES (?, (SELECT id_abilita FROM abilita WHERE nome = ?))";
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
                System.err.println("Errore in transazione, faccio rollback");
                conn.rollback();
                throw ex; // rilancio per far scattare il catch esterno
            } finally {
                // rimetto a posto l'autocommit
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("Errore aggiornamento competenze a db");
            e.printStackTrace();
        }

        // pattern PRG (Post-Redirect-Get) per pulire lo stato
        response.sendRedirect(request.getContextPath() + "/operatore/dashboard.html");
    }

    // escape ruspante ma funzionale per il json
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", " ")
                  .replace("\r", " ");
    }
}