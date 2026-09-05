package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.GestioneEmail;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DettaglioMissioneServlet", urlPatterns = {"/DettaglioMissioneServlet"})
public class DettaglioMissioneServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico driver mysql al boot
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC mancante");
            e.printStackTrace();
        }
    }

    // helper per leggere l'HTML statico da usare come base
    private String leggiHtml(String nomeFile) throws IOException {
        InputStream is = getServletContext().getResourceAsStream("/" + nomeFile);
        if (is == null) return "";
        try (Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi admin
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissione = request.getParameter("id_missione");
        response.setContentType("text/html;charset=UTF-8");

        // apro connessione nativa
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); 
             PrintWriter out = response.getWriter()) {

            // 1. info base missione
            String sqlMissione = "SELECT obiettivo, posizione, stato, timestamp_inizio " +
                                 "FROM missione WHERE id_missione = ?";
            
            try (PreparedStatement stmtM = conn.prepareStatement(sqlMissione)) {
                stmtM.setInt(1, Integer.parseInt(idMissione));
                try (ResultSet rsM = stmtM.executeQuery()) {
                    
                    if (rsM.next()) {
                        String statoAttuale = rsM.getString("stato");
                        String obiettivo = rsM.getString("obiettivo");
                        String posizione = rsM.getString("posizione");
                        String inizio = rsM.getTimestamp("timestamp_inizio").toString();
                        
                        // pesco il template giusto in base allo stato
                        String template = "IN_CORSO".equals(statoAttuale) 
                                ? leggiHtml("dettaglio_missione_attiva.html") 
                                : leggiHtml("dettaglio_missione_chiusa.html");

                        // caposquadra
                        String nomeCaposquadra = "Non ancora assegnato";
                        String sqlCapo = "SELECT u.nome, u.cognome " +
                                         "FROM utente u " +
                                         "JOIN assegnazione_operatori_missione aom ON u.id_utente = aom.id_utente " +
                                         "WHERE aom.id_missione = ? AND aom.is_caposquadra = 1";
                        
                        try (PreparedStatement stmtC = conn.prepareStatement(sqlCapo)) {
                            stmtC.setInt(1, Integer.parseInt(idMissione));
                            try (ResultSet rsC = stmtC.executeQuery()) {
                                if (rsC.next()) {
                                    nomeCaposquadra = rsC.getString("nome") + " " + rsC.getString("cognome");
                                }
                            }
                        }

                        // mezzi
                        StringBuilder mezziHtml = new StringBuilder();
                        String sqlMezzi = "SELECT m.nome, m.descrizione " +
                                          "FROM mezzo m " +
                                          "JOIN assegnazione_mezzi_missione amm ON m.id_mezzo = amm.id_mezzo " +
                                          "WHERE amm.id_missione = ?";
                                          
                        try (PreparedStatement stmtMz = conn.prepareStatement(sqlMezzi)) {
                            stmtMz.setInt(1, Integer.parseInt(idMissione));
                            try (ResultSet rsMz = stmtMz.executeQuery()) {
                                while (rsMz.next()) {
                                    mezziHtml.append("<li>").append(rsMz.getString("nome")).append(" (").append(rsMz.getString("descrizione")).append(")</li>");
                                }
                            }
                        }
                        String bloccoMezzi = mezziHtml.length() > 0 ? "<ul>" + mezziHtml.toString() + "</ul>" : "<p class='text-muted'>Nessun automezzo associato.</p>";

                        // materiali
                        StringBuilder materialiHtml = new StringBuilder();
                        String sqlMat = "SELECT mat.nome, mat.descrizione " +
                                        "FROM materiale mat " +
                                        "JOIN assegnazione_materiale_missione ama ON mat.id_materiale = ama.id_materiale " +
                                        "WHERE ama.id_missione = ?";
                                        
                        try (PreparedStatement stmtMat = conn.prepareStatement(sqlMat)) {
                            stmtMat.setInt(1, Integer.parseInt(idMissione));
                            try (ResultSet rsMat = stmtMat.executeQuery()) {
                                while (rsMat.next()) {
                                    materialiHtml.append("<li>").append(rsMat.getString("nome")).append(" - ").append(rsMat.getString("descrizione")).append("</li>");
                                }
                            }
                        }
                        String bloccoMateriali = materialiHtml.length() > 0 ? "<ul>" + materialiHtml.toString() + "</ul>" : "<p class='text-muted'>Nessun materiale registrato.</p>";

                        // timeline
                        StringBuilder timelineHtml = new StringBuilder();
                        String sqlTime = "SELECT am.testo_descrittivo, am.timestamp_inserimento, u.nome, u.cognome " +
                                         "FROM aggiornamento_missione am " +
                                         "JOIN utente u ON am.id_admin = u.id_utente " +
                                         "WHERE am.id_missione = ? ORDER BY am.timestamp_inserimento DESC";
                                         
                        try (PreparedStatement stmtT = conn.prepareStatement(sqlTime)) {
                            stmtT.setInt(1, Integer.parseInt(idMissione));
                            try (ResultSet rsT = stmtT.executeQuery()) {
                                while (rsT.next()) {
                                    timelineHtml.append("<div class='timeline-item'>")
                                                .append("<p class='timeline-date'><b>Data/Ora:</b> ").append(rsT.getTimestamp("timestamp_inserimento"))
                                                .append(" | <b>Operatore:</b> ").append(rsT.getString("nome")).append(" ").append(rsT.getString("cognome")).append("</p>")
                                                .append("<p class='timeline-content'>").append(rsT.getString("testo_descrittivo")).append("</p>")
                                                .append("</div>");
                                }
                            }
                        }
                        String bloccoTimeline = timelineHtml.length() > 0 ? timelineHtml.toString() : "<p class='text-muted'>Nessun aggiornamento ancora registrato.</p>";

                        // replace dei token nell'HTML letto da file
                        template = template.replace("[ID_MISSIONE]", idMissione);
                        template = template.replace("[OBIETTIVO]", obiettivo);
                        template = template.replace("[POSIZIONE]", posizione);
                        template = template.replace("[INIZIO]", inizio);
                        template = template.replace("[CAPOSQUADRA]", nomeCaposquadra);
                        template = template.replace("[LISTA_MEZZI]", bloccoMezzi);
                        template = template.replace("[LISTA_MATERIALI]", bloccoMateriali);
                        template = template.replace("[TIMELINE]", bloccoTimeline);

                        // scrivo l'output nel browser
                        out.print(template);

                    } else {
                        // missione non trovata
                        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore fetch dettagli missione");
            e.printStackTrace();
        }
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

        String idMissione = request.getParameter("id_missione");
        String testoDescrittivo = request.getParameter("testo_descrittivo");
        int idAdminLoggato = (int) session.getAttribute("id_utente");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            // controllo se posso aggiungere agg.ti (solo se IN_CORSO)
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
                String sqlInsert = "INSERT INTO aggiornamento_missione " +
                                   "(id_missione, id_admin, testo_descrittivo) " +
                                   "VALUES (?, ?, ?)";
                                   
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                    stmt.setInt(1, Integer.parseInt(idMissione));
                    stmt.setInt(2, idAdminLoggato);
                    stmt.setString(3, testoDescrittivo);

                    int righe = stmt.executeUpdate();
                    if (righe > 0) {
                        // invio notifica fake (es. stampe in console)
                        GestioneEmail.notificaOperatoriAssegnati(
                                conn,
                                Integer.parseInt(idMissione),
                                "Nuovo aggiornamento missione #" + idMissione,
                                "È stato inserito un nuovo aggiornamento per la missione #" + idMissione + ":\n\n" + testoDescrittivo
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore inserimento update missione");
            e.printStackTrace();
        }

        // pattern PRG
        response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissione);
    }
}