package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ConcludiMissioneServlet", urlPatterns = {"/ConcludiMissioneServlet"})
public class ConcludiMissioneServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico il driver una volta sola all'avvio
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL mancante nel classpath");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");

        Map<String, Object> dataModel = new HashMap<>();
        Map<String, Object> missioneData = new HashMap<>();

        // query di select su più righe
        String sql = "SELECT id_missione, id_richiesta, obiettivo " +
                     "FROM missione " +
                     "WHERE id_missione = ? AND stato = 'IN_CORSO'";

        // connessione nativa senza manager
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idMissione);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    missioneData.put("id_missione", rs.getInt("id_missione"));
                    missioneData.put("id_richiesta", rs.getInt("id_richiesta"));
                    missioneData.put("obiettivo", rs.getString("obiettivo"));
                }
            }
        } catch (Exception e) {
            System.err.println("Errore fetch dettagli missione");
            e.printStackTrace();
        }

        if (!missioneData.isEmpty()) {
            dataModel.put("missione", missioneData);
        }

        // TODO: Invocazione del motore di template (es. Freemarker)
        // TemplateManager.processTemplate("concludiMissione.html", dataModel, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // prelevo i dati dal post
        String idMissione = request.getParameter("id_missione");
        String idRichiesta = request.getParameter("id_richiesta");
        String livelloSuccesso = request.getParameter("voto_successo");
        String commenti = request.getParameter("commenti");
        int idAdminLoggato = (int) session.getAttribute("id_utente");
        
        boolean terminata = false;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            // disabilito autocommit per la transazione
            conn.setAutoCommit(false);

            try {
                // 1. update missione
                String sqlMissione = "UPDATE missione " +
                                     "SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP " +
                                     "WHERE id_missione = ?";
                
                try (PreparedStatement stmtM = conn.prepareStatement(sqlMissione)) {
                    stmtM.setInt(1, Integer.parseInt(livelloSuccesso));
                    stmtM.setString(2, commenti);
                    stmtM.setInt(3, Integer.parseInt(idMissione));
                    stmtM.executeUpdate();
                }

                // 2. update richiesta
                String sqlRichiesta = "UPDATE richiesta_soccorso " +
                                      "SET stato = 'CHIUSA' " +
                                      "WHERE id_richiesta = ?";
                
                try (PreparedStatement stmtR = conn.prepareStatement(sqlRichiesta)) {
                    stmtR.setString(1, idRichiesta);
                    stmtR.executeUpdate();
                }

                // 3. insert in timeline
                String sqlTimeline = "INSERT INTO aggiornamento_missione " +
                                     "(id_missione, id_admin, testo_descrittivo) " +
                                     "VALUES (?, ?, ?)";
                
                try (PreparedStatement stmtT = conn.prepareStatement(sqlTimeline)) {
                    stmtT.setInt(1, Integer.parseInt(idMissione));
                    stmtT.setInt(2, idAdminLoggato);
                    stmtT.setString(3, "CHIUSURA MISSIONE. Rapporto finale: " + commenti);
                    stmtT.executeUpdate();
                }

                // tutto ok, committiamo la transazione
                conn.commit();
                terminata = true;

            } catch (Exception ex) {
                System.err.println("Errore in transazione, eseguo rollback");
                conn.rollback();
                ex.printStackTrace();
            } finally {
                // ripristino l'autocommit a true prima di rilasciare la connessione
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.err.println("Errore connessione db");
            e.printStackTrace();
        }

        // pattern PRG (Post-Redirect-Get) per l'esito
        session = request.getSession(true);
        if (terminata) {
            session.setAttribute("messaggioEsito", "Intervento concluso con successo. Report archiviato.");
            response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissione);
        } else {
            session.setAttribute("messaggioEsito", "Errore salvataggio report.");
            response.sendRedirect(request.getContextPath() + "/ConcludiMissioneServlet?id_missione=" + idMissione);
        }
    }
}