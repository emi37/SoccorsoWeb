package it.univaq.disim.webengineering.soccorsoweb.dao.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.AggiornamentoMissioneDAO;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggiornamentoMissioneDAO_MySQL implements AggiornamentoMissioneDAO {

    private static final Logger LOGGER = Logger.getLogger(AggiornamentoMissioneDAO_MySQL.class.getName());

    @Override
    public boolean salvaNuovoAggiornamento(int idMissione, int idAdminLoggato, String messaggio) {
        String queryInserimento = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(queryInserimento)) {

            statement.setInt(1, idMissione);
            statement.setInt(2, idAdminLoggato);
            statement.setString(3, messaggio);

            // ritorna true se l'inserimento ha modificato almeno una riga
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante il salvataggio dell'aggiornamento: " + e.getMessage(), e);
        }

        return false;
    }

    @Override
    public List<Map<String, String>> estraiCronistoria(int idMissione) {
        List<Map<String, String>> cronistoria = new ArrayList<>();

        // estrazione dei messaggi ordinati per data per la timeline della missione
        String query = "SELECT am.testo_descrittivo, am.timestamp_inserimento, u.nome, u.cognome "
                + "FROM aggiornamento_missione am "
                + "JOIN utenti u ON am.id_admin = u.id_utente "
                + "WHERE am.id_missione = ? "
                + "ORDER BY am.timestamp_inserimento ASC";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idMissione);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map< String, String > singoloUpdate = new HashMap<>();
                    singoloUpdate.put("messaggio", resultSet.getString("testo_descrittivo"));
                    singoloUpdate.put("data_ora", resultSet.getTimestamp("timestamp_inserimento").toLocalDateTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    singoloUpdate.put("operatore", resultSet.getString("nome") + " " + resultSet.getString("cognome"));

                    cronistoria.add(singoloUpdate);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante la lettura della cronistoria: " + e.getMessage(), e);
        }

        return cronistoria;
    }
}