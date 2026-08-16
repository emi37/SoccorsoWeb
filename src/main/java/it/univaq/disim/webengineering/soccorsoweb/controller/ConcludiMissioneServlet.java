package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.sql.Connection;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");

        // Struttura dati per il template Freemarker
        Map<String, Object> dataModel = new HashMap<>();
        Map<String, Object> missioneData = new HashMap<>();

        try (Connection conn = DBManager.getConnection()) {
            String sql = "SELECT id_missione, id_richiesta, obiettivo FROM missione WHERE id_missione = ? AND stato = 'IN_CORSO'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idMissione);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        missioneData.put("id_missione", rs.getInt("id_missione"));
                        missioneData.put("id_richiesta", rs.getInt("id_richiesta"));
                        missioneData.put("obiettivo", rs.getString("obiettivo"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!missioneData.isEmpty()) {
            dataModel.put("missione", missioneData);
        }

        // Invocazione del motore di template (es. Freemarker) per processare il file HTML
        // Esempio fittizio basato sull'architettura del corso:
        // TemplateManager.processTemplate("concludiMissione.html", dataModel, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");
        String idRichiesta = request.getParameter("id_richiesta");
        String livelloSuccesso = request.getParameter("voto_successo");
        String commenti = request.getParameter("commenti");

        int idAdminLoggato = (int) session.getAttribute("id_utente");
        boolean terminata = false;

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Aggiorno lo stato della missione
                String sqlMissione = "UPDATE missione SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP WHERE id_missione = ?";
                try (PreparedStatement stmtM = conn.prepareStatement(sqlMissione)) {
                    stmtM.setInt(1, Integer.parseInt(livelloSuccesso));
                    stmtM.setString(2, commenti);
                    stmtM.setInt(3, Integer.parseInt(idMissione));
                    stmtM.executeUpdate();
                }

                // 2. Aggiorno lo stato della richiesta
                String sqlRichiesta = "UPDATE richiesta_soccorso SET stato = 'CHIUSA' WHERE id_richiesta = ?";
                try (PreparedStatement stmtR = conn.prepareStatement(sqlRichiesta)) {
                    stmtR.setString(1, idRichiesta);
                    stmtR.executeUpdate();
                }

                // 3. Inseriamo il report finale nella timeline
                String sqlTimeline = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";
                try (PreparedStatement stmtT = conn.prepareStatement(sqlTimeline)) {
                    stmtT.setInt(1, Integer.parseInt(idMissione));
                    stmtT.setInt(2, idAdminLoggato);
                    stmtT.setString(3, "CHIUSURA MISSIONE. Rapporto finale: " + commenti);
                    stmtT.executeUpdate();
                }

                conn.commit();
                terminata = true;

            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Applicazione del pattern Post-Redirect-Get
        // I messaggi di notifica vengono salvati in sessione prima di eseguire la redirect
        session = request.getSession(true);
        if (terminata) {
            session.setAttribute("messaggioEsito", "Intervento concluso con successo. Report registrato nel diario!");
            response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissione);
        } else {
            session.setAttribute("messaggioEsito", "Errore durante l'archiviazione del report.");
            response.sendRedirect(request.getContextPath() + "/ConcludiMissioneServlet?id_missione=" + idMissione);
        }
    }
}
