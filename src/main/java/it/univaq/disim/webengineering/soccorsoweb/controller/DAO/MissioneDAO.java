package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissioneDAO {

    public List< Map<String, String> > estraiMissioniInCorso() {
        List< Map<String, String> > listaMissioni = new ArrayList<>();
        
        String query = "SELECT m.id_missione, m.id_richiesta, m.obiettivo, m.posizione, m.stato, "
                + "u.nome AS nome_capo, u.cognome AS cognome_capo "
                + "FROM missione m "
                + "LEFT JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione AND aom.is_caposquadra = 1 "
                + "LEFT JOIN utenti u ON aom.id_utente = u.id_utente "
                + "WHERE m.stato = 'IN_CORSO' ORDER BY m.id_missione DESC";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map< String, String > mappaMissione = new HashMap<>();
                mappaMissione.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                mappaMissione.put("id_richiesta", String.valueOf(resultSet.getInt("id_richiesta")));
                mappaMissione.put("obiettivo", resultSet.getString("obiettivo"));
                mappaMissione.put("posizione", resultSet.getString("posizione"));
                mappaMissione.put("stato", resultSet.getString("stato"));
                
                String nomeCapo = resultSet.getString("nome_capo");
                String cognomeCapo = resultSet.getString("cognome_capo");
                
                if (nomeCapo != null && cognomeCapo != null) {
                    mappaMissione.put("caposquadra", nomeCapo + " " + cognomeCapo);
                } else {
                    mappaMissione.put("caposquadra", "Non assegnato");
                }
                
                listaMissioni.add(mappaMissione);
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione delle missioni in corso...");
            e.printStackTrace();
        }
        return listaMissioni;
    }

    public boolean creaMissioneInTransazione(int idRichiesta, String obiettivo, String posizione, int idCaposquadra, String[] idOperatori, String[] idMezzi, String[] idMateriali, int idAdminLoggato) {
        boolean successo = false;
        Connection connessione = null;

        String queryMissione = "INSERT INTO missione (id_richiesta, obiettivo, posizione, stato, timestamp_inizio) VALUES (?, ?, ?, 'IN_CORSO', CURRENT_TIMESTAMP)";
        String queryRichiesta = "UPDATE richiesta_soccorso SET stato = 'IN_CORSO' WHERE id_richiesta = ?";
        String queryOperatore = "INSERT INTO assegnazione_operatori_missione (id_missione, id_utente, is_caposquadra) VALUES (?, ?, ?)";
        String queryMezzo = "INSERT INTO assegnazione_mezzi_missione (id_missione, id_mezzo) VALUES (?, ?)";
        String queryMateriale = "INSERT INTO assegnazione_materiale_missione (id_missione, id_materiale) VALUES (?, ?)";
        String queryLog = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";

        try {
            connessione = DBManager.getConnection();
            connessione.setAutoCommit(false);

            int idMissioneGenerato = -1;

            try (PreparedStatement statementMissione = connessione.prepareStatement(queryMissione, Statement.RETURN_GENERATED_KEYS)) {
                statementMissione.setInt(1, idRichiesta);
                statementMissione.setString(2, obiettivo);
                statementMissione.setString(3, posizione);
                statementMissione.executeUpdate();

                try (ResultSet resultSetChiavi = statementMissione.getGeneratedKeys()) {
                    if (resultSetChiavi.next()) {
                        idMissioneGenerato = resultSetChiavi.getInt(1);
                    }
                }
            }

            if (idMissioneGenerato == -1) {
                connessione.rollback();
                return false;
            }

            try (PreparedStatement statementRichiesta = connessione.prepareStatement(queryRichiesta)) {
                statementRichiesta.setInt(1, idRichiesta);
                statementRichiesta.executeUpdate();
            }

            try (PreparedStatement statementCaposquadra = connessione.prepareStatement(queryOperatore)) {
                statementCaposquadra.setInt(1, idMissioneGenerato);
                statementCaposquadra.setInt(2, idCaposquadra);
                statementCaposquadra.setInt(3, 1);
                statementCaposquadra.executeUpdate();
            }

            if (idOperatori != null && idOperatori.length > 0) {
                try (PreparedStatement statementOperatori = connessione.prepareStatement(queryOperatore)) {
                    for (String operatore : idOperatori) {
                        int idOp = Integer.parseInt(operatore.trim());
                        if (idOp != idCaposquadra) {
                            statementOperatori.setInt(1, idMissioneGenerato);
                            statementOperatori.setInt(2, idOp);
                            statementOperatori.setInt(3, 0); 
                            statementOperatori.executeUpdate();
                        }
                    }
                }
            }

            if (idMezzi != null && idMezzi.length > 0) {
                try (PreparedStatement statementMezzi = connessione.prepareStatement(queryMezzo)) {
                    for (String mezzo : idMezzi) {
                        statementMezzi.setInt(1, idMissioneGenerato);
                        statementMezzi.setInt(2, Integer.parseInt(mezzo.trim()));
                        statementMezzi.executeUpdate();
                    }
                }
            }

            if (idMateriali != null && idMateriali.length > 0) {
                try (PreparedStatement statementMateriali = connessione.prepareStatement(queryMateriale)) {
                    for (String materiale : idMateriali) {
                        statementMateriali.setInt(1, idMissioneGenerato);
                        statementMateriali.setInt(2, Integer.parseInt(materiale.trim()));
                        statementMateriali.executeUpdate();
                    }
                }
            }

            try (PreparedStatement statementLog = connessione.prepareStatement(queryLog)) {
                statementLog.setInt(1, idMissioneGenerato);
                statementLog.setInt(2, idAdminLoggato);
                statementLog.setString(3, "Missione avviata con successo per l'emergenza #" + idRichiesta);
                statementLog.executeUpdate();
            }

            connessione.commit();
            successo = true;

        } catch (Exception e) {
            System.err.println("errore durante la creazione della missione");
            e.printStackTrace();
            if (connessione != null) {
                try {
                    connessione.rollback();
                } catch (Exception exRollback) {
                    exRollback.printStackTrace();
                }
            }
        } finally {
            if (connessione != null) {
                try {
                    connessione.setAutoCommit(true);
                    connessione.close();
                } catch (Exception exClose) {
                    exClose.printStackTrace();
                }
            }
        }
        return successo;
    }

    public List< Map<String, String> > estraiStoricoMissioni() {
        List< Map<String, String> > storicoTrovato = new ArrayList<>();
        
        String query = "SELECT m.id_missione, m.id_richiesta, m.obiettivo, m.livello_successo, m.commenti, "
                + "u.nome AS nome_capo, u.cognome AS cognome_capo "
                + "FROM missione m "
                + "LEFT JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione AND aom.is_caposquadra = 1 "
                + "LEFT JOIN utenti u ON aom.id_utente = u.id_utente "
                + "WHERE m.stato = 'CHIUSA' ORDER BY m.id_missione DESC";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map< String, String > mappaMissione = new HashMap<>();
                mappaMissione.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                mappaMissione.put("id_richiesta", String.valueOf(resultSet.getInt("id_richiesta")));
                mappaMissione.put("obiettivo", resultSet.getString("obiettivo"));
                mappaMissione.put("livello_successo", String.valueOf(resultSet.getInt("livello_successo")));
                mappaMissione.put("commenti", resultSet.getString("commenti"));
                
                String nomeCapo = resultSet.getString("nome_capo");
                String cognomeCapo = resultSet.getString("cognome_capo");
                
                if (nomeCapo != null && cognomeCapo != null) {
                    mappaMissione.put("caposquadra", nomeCapo + " " + cognomeCapo);
                } else {
                    mappaMissione.put("caposquadra", "Nessuno");
                }
                
                storicoTrovato.add(mappaMissione);
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione dello storico missioni...");
            e.printStackTrace();
        }
        return storicoTrovato;
    }

    public List< Map<String, String> > estraiMissioniPerOperatore(int idOperatoreCercato) {
        List< Map<String, String> > missioniPersonali = new ArrayList<>();
        
        String query = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo "
                + "FROM missione m "
                + "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione "
                + "WHERE aom.id_utente = ? "
                + "ORDER BY m.stato DESC, m.id_missione DESC";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idOperatoreCercato);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map< String, String > singolaMissione = new HashMap<>();
                    singolaMissione.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                    singolaMissione.put("obiettivo", resultSet.getString("obiettivo"));
                    singolaMissione.put("posizione", resultSet.getString("posizione"));
                    singolaMissione.put("stato", resultSet.getString("stato"));

                    int livelloSuccesso = resultSet.getInt("livello_successo");
                    String votoFormattato = resultSet.wasNull() ? "-" : livelloSuccesso + " / 5";
                    singolaMissione.put("visualizzaVoto", votoFormattato);

                    missioniPersonali.add(singolaMissione);
                }
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione delle missioni per l'operatore...");
            e.printStackTrace();
        }
        return missioniPersonali;
    }

    public boolean chiudiMissioneInTransazione(int idMissione, int idRichiesta, int livelloSuccesso, String commenti, int idAdminLoggato) {
        boolean successo = false;
        Connection connessione = null;

        String queryMissione = "UPDATE missione SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP WHERE id_missione = ?";
        String queryRichiesta = "UPDATE richiesta_soccorso SET stato = 'CHIUSA' WHERE id_richiesta = ?";
        String queryTimeline = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";

        try {
            connessione = DBManager.getConnection();
            connessione.setAutoCommit(false);

            try (PreparedStatement statementMissione = connessione.prepareStatement(queryMissione)) {
                statementMissione.setInt(1, livelloSuccesso);
                statementMissione.setString(2, commenti);
                statementMissione.setInt(3, idMissione);
                statementMissione.executeUpdate();
            }

            try (PreparedStatement statementRichiesta = connessione.prepareStatement(queryRichiesta)) {
                statementRichiesta.setInt(1, idRichiesta);
                statementRichiesta.executeUpdate();
            }

            try (PreparedStatement statementTimeline = connessione.prepareStatement(queryTimeline)) {
                statementTimeline.setInt(1, idMissione);
                statementTimeline.setInt(2, idAdminLoggato);
                statementTimeline.setString(3, "CHIUSURA MISSIONE. Rapporto finale: " + commenti);
                statementTimeline.executeUpdate();
            }

            connessione.commit();
            successo = true;

        } catch (Exception e) {
            System.err.println("errore durante la chiusura della missione...");
            e.printStackTrace();
            if (connessione != null) {
                try {
                    connessione.rollback();
                } catch (Exception exRollback) {
                    exRollback.printStackTrace();
                }
            }
        } finally {
            if (connessione != null) {
                try {
                    connessione.setAutoCommit(true);
                    connessione.close();
                } catch (Exception exClose) {
                    exClose.printStackTrace();
                }
            }
        }
        return successo;
    }

    public String estraiStatoMissione(int idMissione) {
        String statoAttuale = "";
        String query = "SELECT stato FROM missione WHERE id_missione = ?";
        
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idMissione);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    statoAttuale = resultSet.getString("stato");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statoAttuale;
    }

    public Map< String, Object > estraiDettagliCompleti(int idMissione) {
        Map< String, Object > mappaDettagli = new HashMap<>();
        String statoDellaMissione = ""; 

        try (Connection connessione = DBManager.getConnection()) {
            
            String queryBase = "SELECT obiettivo, posizione, stato, timestamp_inizio FROM missione WHERE id_missione = ?";
            try (PreparedStatement statementBase = connessione.prepareStatement(queryBase)) {
                statementBase.setInt(1, idMissione);
                try (ResultSet resultSetBase = statementBase.executeQuery()) {
                    if (resultSetBase.next()) {
                        statoDellaMissione = resultSetBase.getString("stato"); 
                        
                        mappaDettagli.put("id_missione", idMissione);
                        mappaDettagli.put("obiettivo", resultSetBase.getString("obiettivo"));
                        mappaDettagli.put("posizione", resultSetBase.getString("posizione"));
                        mappaDettagli.put("stato", statoDellaMissione); 
                        mappaDettagli.put("inizio", resultSetBase.getTimestamp("timestamp_inizio").toString());
                    } else {
                        return mappaDettagli;
                    }
                }
            }

            String nomeCaposquadra = "CHIUSA".equals(statoDellaMissione) ? "Dato non registrato agli atti" : "Non ancora assegnato";
            String queryCapo = "SELECT u.nome, u.cognome FROM utenti u "
                    + "JOIN assegnazione_operatori_missione aom ON u.id_utente = aom.id_utente "
                    + "WHERE aom.id_missione = ? AND aom.is_caposquadra = 1";
                    
            try (PreparedStatement statementCapo = connessione.prepareStatement(queryCapo)) {
                statementCapo.setInt(1, idMissione);
                try (ResultSet resultSetCapo = statementCapo.executeQuery()) {
                    if (resultSetCapo.next()) {
                        nomeCaposquadra = resultSetCapo.getString("nome") + " " + resultSetCapo.getString("cognome");
                    }
                }
            }
            mappaDettagli.put("caposquadra", nomeCaposquadra);
            
            List<String> listaMezzi = new ArrayList<>();
            String queryMezzi = "SELECT m.nome, m.descrizione FROM mezzo m "
                    + "JOIN assegnazione_mezzi_missione amm ON m.id_mezzo = amm.id_mezzo "
                    + "WHERE amm.id_missione = ?";
                    
            try (PreparedStatement statementMezzi = connessione.prepareStatement(queryMezzi)) {
                statementMezzi.setInt(1, idMissione);
                try (ResultSet resultSetMezzi = statementMezzi.executeQuery()) {
                    while (resultSetMezzi.next()) {
                        listaMezzi.add(resultSetMezzi.getString("nome") + " (" + resultSetMezzi.getString("descrizione") + ")");
                    }
                }
            }
            mappaDettagli.put("mezzi", listaMezzi);

            List<String> listaMateriali = new ArrayList<>();
            String queryMateriali = "SELECT mat.nome, mat.descrizione FROM materiale mat "
                    + "JOIN assegnazione_materiale_missione ama ON mat.id_materiale = ama.id_materiale "
                    + "WHERE ama.id_missione = ?";
                    
            try (PreparedStatement statementMateriali = connessione.prepareStatement(queryMateriali)) {
                statementMateriali.setInt(1, idMissione);
                try (ResultSet resultSetMateriali = statementMateriali.executeQuery()) {
                    while (resultSetMateriali.next()) {
                        listaMateriali.add(resultSetMateriali.getString("nome") + " - " + resultSetMateriali.getString("descrizione"));
                    }
                }
            }
            mappaDettagli.put("materiali", listaMateriali);

        } catch (Exception e) {
            System.err.println("errore durante l'estrazione dei dettagli completi della missione...");
            e.printStackTrace();
        }

        return mappaDettagli;
    }

    public List< Map<String, String> > estraiMissioniAttivePerOperatore(long idOperatore) {
        List< Map<String, String> > listaMissioni = new ArrayList<>();
        
        String query = "SELECT m.id_missione, m.id_richiesta, m.obiettivo, m.posizione, m.stato " +
                       "FROM missione m " +
                       "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione " +
                       "WHERE aom.id_utente = ? AND m.stato = 'IN_CORSO'";
                       
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setLong(1, idOperatore);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map< String, String > mappaMissione = new HashMap<>();
                    mappaMissione.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                    mappaMissione.put("id_richiesta", String.valueOf(resultSet.getInt("id_richiesta")));
                    mappaMissione.put("obiettivo", resultSet.getString("obiettivo"));
                    mappaMissione.put("posizione", resultSet.getString("posizione"));
                    mappaMissione.put("stato", resultSet.getString("stato"));
                    
                    listaMissioni.add(mappaMissione);
                }
            }
        } catch (Exception e) {
            System.err.println("errore nell'estrazione delle missioni attive per l'operatore...");
            e.printStackTrace();
        }
        
        return listaMissioni;
    }

    public boolean modificaMissioneInCorso(int idMissione, String nuovoObiettivo, String nuovaPosizione,
                                           String[] nuoviOperatori, String[] nuoviMezzi, String[] nuoviMateriali) {
        
        boolean successo = false;
        Connection connessione = null;
        
        String queryMissione = "UPDATE missione SET obiettivo = ?, posizione = ? WHERE id_missione = ?";
        String queryOperatore = "INSERT IGNORE INTO assegnazione_operatori_missione (id_missione, id_utente, is_caposquadra) VALUES (?, ?, 0)";
        String queryMezzo = "INSERT IGNORE INTO assegnazione_mezzi_missione (id_missione, id_mezzo) VALUES (?, ?)";
        String queryMateriale = "INSERT IGNORE INTO assegnazione_materiale_missione (id_missione, id_materiale) VALUES (?, ?)";

        try {
            connessione = DBManager.getConnection();
            connessione.setAutoCommit(false); 

            try (PreparedStatement statementMissione = connessione.prepareStatement(queryMissione)) {
                statementMissione.setString(1, nuovoObiettivo);
                statementMissione.setString(2, nuovaPosizione);
                statementMissione.setInt(3, idMissione);
                statementMissione.executeUpdate();
            }

            if (nuoviOperatori != null) {
                try (PreparedStatement statementOperatori = connessione.prepareStatement(queryOperatore)) {
                    for (String operatore : nuoviOperatori) {
                        statementOperatori.setInt(1, idMissione);
                        statementOperatori.setInt(2, Integer.parseInt(operatore.trim()));
                        statementOperatori.executeUpdate();
                    }
                }
            }

            if (nuoviMezzi != null) {
                try (PreparedStatement statementMezzi = connessione.prepareStatement(queryMezzo)) {
                    for (String mezzo : nuoviMezzi) {
                        statementMezzi.setInt(1, idMissione);
                        statementMezzi.setInt(2, Integer.parseInt(mezzo.trim()));
                        statementMezzi.executeUpdate();
                    }
                }
            }

            if (nuoviMateriali != null) {
                try (PreparedStatement statementMateriali = connessione.prepareStatement(queryMateriale)) {
                    for (String materiale : nuoviMateriali) {
                        statementMateriali.setInt(1, idMissione);
                        statementMateriali.setInt(2, Integer.parseInt(materiale.trim()));
                        statementMateriali.executeUpdate();
                    }
                }
            }

            connessione.commit();
            successo = true;

        } catch (Exception e) {
            System.err.println("errore durante la modifica della missione.");
            e.printStackTrace();
            if (connessione != null) {
                try {
                    connessione.rollback();
                } catch (Exception exRollback) {
                    exRollback.printStackTrace();
                }
            }
        } finally {
            if (connessione != null) {
                try {
                    connessione.setAutoCommit(true);
                    connessione.close();
                } catch (Exception exClose) {
                    exClose.printStackTrace();
                }
            }
        }
        
        return successo;
    }
}