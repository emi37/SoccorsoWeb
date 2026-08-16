package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        request.setAttribute("nomeOperatore", nomeOperatore);

        String patentiCorrenti = "";
        String abilitaCorrenti = "";
        List<Map<String, Object>> listaMissioni = new ArrayList<>();

        try (Connection conn = DBManager.getConnection()) {

            // Recupero patenti dell'operatore
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
            request.setAttribute("pat", patentiCorrenti);

            // Recupero abilità dell'operatore
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
            request.setAttribute("ab", abilitaCorrenti);

            // Recupero missioni dell'operatore
            String sql = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo "
                    + "FROM missione m "
                    + "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione "
                    + "WHERE aom.id_utente = ? "
                    + "ORDER BY m.stato DESC, m.id_missione DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idOperatore);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> missione = new HashMap<>();
                        missione.put("id_missione", rs.getInt("id_missione"));
                        missione.put("obiettivo", rs.getString("obiettivo"));
                        missione.put("posizione", rs.getString("posizione"));
                        missione.put("stato", rs.getString("stato"));

                        int livelloSuccesso = rs.getInt("livello_successo");
                        String visualizzaVoto = rs.wasNull() ? "-" : livelloSuccesso + " / 5";
                        missione.put("visualizzaVoto", visualizzaVoto);

                        listaMissioni.add(missione);
                    }
                }
            }
            request.setAttribute("missioni", listaMissioni);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errore", e.getMessage());
        }

        // Inoltro della richiesta alla pagina HTML
        request.getRequestDispatcher("/operatore/dashboard.jsp").forward(request, response);
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
                // Sincronizzazione patenti
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

                // Sincronizzazione abilità
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

        // Reindirizzamento al GET della servlet
        response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
    }
}
