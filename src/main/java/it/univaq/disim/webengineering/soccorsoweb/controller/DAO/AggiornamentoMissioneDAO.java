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

public class AggiornamentoMissioneDAO {

    // Salva un nuovo messaggio nella cronistoria della missione
    public boolean salvaNuovoAggiornamento(int idMissione, int idAdminLoggato, String messaggio) {
        boolean salvatoOk = false;

        // Spezzo la query a mano col +
        String queryInserimento = "INSERT INTO aggiornamento_missione "
                + "(id_missione, id_admin, testo_descrittivo) "
                + "VALUES (?, ?, ?)";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementInserimento = connessioneDb.prepareStatement(queryInserimento)) {

            // Bindo i parametri
            statementInserimento.setInt(1, idMissione);
            statementInserimento.setInt(2, idAdminLoggato);
            statementInserimento.setString(3, messaggio);

            int righeModificate = statementInserimento.executeUpdate();
            if (righeModificate > 0) {
                salvatoOk = true;
            }

        } catch (Exception e) {
            System.err.println("Impossibile salvare l'aggiornamento missione nel DB...");
            e.printStackTrace();
        }

        return salvatoOk;
    }

    // Estrae tutti i messaggi per stamparli nella timeline della pagina di dettaglio
    public List<Map<String, String>> estraiCronistoria(int idMissione) {
        List<Map<String, String>> cronistoria = new ArrayList<>();

        String queryTimeline = "SELECT am.testo_descrittivo, am.timestamp_inserimento, u.nome, u.cognome "
                + "FROM aggiornamento_missione am "
                + "JOIN utente u ON am.id_admin = u.id_utente "
                + "WHERE am.id_missione = ? "
                + "ORDER BY am.timestamp_inserimento DESC";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementTimeline = connessioneDb.prepareStatement(queryTimeline)) {

            statementTimeline.setInt(1, idMissione);

            try (ResultSet risultati = statementTimeline.executeQuery()) {
                // Faccio frullare il resultset
                while (risultati.next()) {
                    Map<String, String> singoloUpdate = new HashMap<>();
                    singoloUpdate.put("messaggio", risultati.getString("testo_descrittivo"));
                    singoloUpdate.put("data_ora", risultati.getTimestamp("timestamp_inserimento").toString());
                    singoloUpdate.put("operatore", risultati.getString("nome") + " " + risultati.getString("cognome"));

                    cronistoria.add(singoloUpdate);
                }
            }

        } catch (Exception e) {
            System.err.println("Errore brutto durante la lettura della timeline della missione...");
            e.printStackTrace();
        }

        return cronistoria;
    }
}
