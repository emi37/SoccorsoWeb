/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

/**
 *
 * @author edoar
 */
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissioneDAO {

    // Estrae la lista delle missioni concluse (sostituisce la logica in StoricoMissioniServlet)
    public List<Map<String, String>> estraiStoricoMissioni() {
        List<Map<String, String>> storicoTrovato = new ArrayList<>();

        // Assemblo la query col + per tenerla bella leggibile
        String queryStorico = "SELECT id_missione, id_richiesta, obiettivo, livello_successo, commenti "
                + "FROM missione "
                + "WHERE stato = 'CHIUSA' "
                + "ORDER BY id_missione DESC";

        // Try-with-resources per chiudere tutto da solo e non lasciare buchi in memoria
        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementStorico = connessioneDb.prepareStatement(queryStorico); ResultSet risultati = statementStorico.executeQuery()) {

            // Frullo i risultati per riempire la lista
            while (risultati.next()) {
                Map<String, String> mappaMissione = new HashMap<>();
                mappaMissione.put("id_missione", String.valueOf(risultati.getInt("id_missione")));
                mappaMissione.put("id_richiesta", String.valueOf(risultati.getInt("id_richiesta")));
                mappaMissione.put("obiettivo", risultati.getString("obiettivo"));
                mappaMissione.put("livello_successo", String.valueOf(risultati.getInt("livello_successo")));
                mappaMissione.put("commenti", risultati.getString("commenti"));

                storicoTrovato.add(mappaMissione);
            }

        } catch (Exception e) {
            // Gestione errore ruspante a console
            System.err.println("Errore catastrofico durante l'estrazione dello storico missioni...");
            e.printStackTrace();
        }

        return storicoTrovato;
    }

    // Estrae le missioni per la dashboard del singolo operatore
    public List<Map<String, String>> estraiMissioniPerOperatore(int idOperatoreCercato) {
        List<Map<String, String>> missioniPersonali = new ArrayList<>();

        String queryMissioniOp = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo "
                + "FROM missione m "
                + "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione "
                + "WHERE aom.id_utente = ? "
                + "ORDER BY m.stato DESC, m.id_missione DESC";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementMissioni = connessioneDb.prepareStatement(queryMissioniOp)) {

            statementMissioni.setInt(1, idOperatoreCercato);

            try (ResultSet risultati = statementMissioni.executeQuery()) {
                while (risultati.next()) {
                    Map<String, String> singolaMissione = new HashMap<>();
                    singolaMissione.put("id_missione", String.valueOf(risultati.getInt("id_missione")));
                    singolaMissione.put("obiettivo", risultati.getString("obiettivo"));
                    singolaMissione.put("posizione", risultati.getString("posizione"));
                    singolaMissione.put("stato", risultati.getString("stato"));

                    // Gestisco il NULL in caso la missione sia ancora aperta e non abbia voto
                    int livelloSuccesso = risultati.getInt("livello_successo");
                    String votoFormattato = risultati.wasNull() ? "-" : livelloSuccesso + " / 5";
                    singolaMissione.put("visualizzaVoto", votoFormattato);

                    missioniPersonali.add(singolaMissione);
                }
            }

        } catch (Exception e) {
            System.err.println("Disastro totale: Impossibile estrarre le missioni personali dell'operatore...");
            e.printStackTrace();
        }

        return missioniPersonali;
    }

    // Questo metodo fa il lavoro sporco di ConcludiMissioneServlet gestendo la transazione
    public boolean chiudiMissioneInTransazione(int idMissione, int idRichiesta, int livelloSuccesso, String commenti, int idAdminLoggato) {
        boolean missioneChiusa = false;
        Connection connessioneDb = null;

        try {
            // Qui non uso il try-with-resources per la Connection perché devo chiamare rollback() e commit() a mano
            connessioneDb = DBManager.getConnection();

            // Stacco l'autocommit per bloccare tutto in una transazione unica
            connessioneDb.setAutoCommit(false);

            // 1. Aggiorno la missione
            String queryMissione = "UPDATE missione "
                    + "SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP "
                    + "WHERE id_missione = ?";

            try (PreparedStatement statementMissione = connessioneDb.prepareStatement(queryMissione)) {
                statementMissione.setInt(1, livelloSuccesso);
                statementMissione.setString(2, commenti);
                statementMissione.setInt(3, idMissione);
                statementMissione.executeUpdate();
            }

            // 2. Chiudo anche la richiesta soccorso collegata
            String queryRichiesta = "UPDATE richiesta_soccorso "
                    + "SET stato = 'CHIUSA' "
                    + "WHERE id_richiesta = ?";

            try (PreparedStatement statementRichiesta = connessioneDb.prepareStatement(queryRichiesta)) {
                statementRichiesta.setInt(1, idRichiesta);
                statementRichiesta.executeUpdate();
            }

            // 3. Butto l'aggiornamento finale nella timeline
            String queryTimeline = "INSERT INTO aggiornamento_missione "
                    + "(id_missione, id_admin, testo_descrittivo) "
                    + "VALUES (?, ?, ?)";

            try (PreparedStatement statementTimeline = connessioneDb.prepareStatement(queryTimeline)) {
                statementTimeline.setInt(1, idMissione);
                statementTimeline.setInt(2, idAdminLoggato);
                statementTimeline.setString(3, "CHIUSURA MISSIONE. Rapporto finale: " + commenti);
                statementTimeline.executeUpdate();
            }

            // Se non ci sono stati intoppi, committo tutto sul database in un colpo solo!
            connessioneDb.commit();
            missioneChiusa = true;

        } catch (Exception e) {
            // Panico! Facciamo rollback per non lasciare il database con dati a metà
            System.err.println("Errore in transazione durante la chiusura, eseguo rollback immediato...");
            e.printStackTrace();
            if (connessioneDb != null) {
                try {
                    connessioneDb.rollback();
                } catch (Exception exRollback) {
                    exRollback.printStackTrace();
                }
            }
        } finally {
            // Rimettiamo a posto l'autocommit prima di rilasciare la connessione e la chiudiamo a mano
            if (connessioneDb != null) {
                try {
                    connessioneDb.setAutoCommit(true);
                    connessioneDb.close();
                } catch (Exception exClose) {
                    exClose.printStackTrace();
                }
            }
        }

        return missioneChiusa;
    }
}
