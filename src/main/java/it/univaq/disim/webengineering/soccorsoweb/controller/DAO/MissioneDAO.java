package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissioneDAO {

    // 1. Estrae le missioni attive (IN_CORSO) per la tabella principale della Dashboard Admin
    public List<Map<String, String>> estraiMissioniInCorso() {
        List<Map<String, String>> lista = new ArrayList<>();
        String query = "SELECT id_missione, id_richiesta, obiettivo, posizione, stato "
                + "FROM missione WHERE stato = 'IN_CORSO' ORDER BY id_missione DESC";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("id_missione", String.valueOf(rs.getInt("id_missione")));
                m.put("id_richiesta", String.valueOf(rs.getInt("id_richiesta")));
                m.put("obiettivo", rs.getString("obiettivo"));
                m.put("posizione", rs.getString("posizione"));
                m.put("stato", rs.getString("stato"));
                lista.add(m);
            }
        } catch (Exception e) {
            System.err.println("Errore estrazione missioni in corso...");
            e.printStackTrace();
        }
        return lista;
    }

    // 2. Transazione per creare una missione e vincolare tutte le risorse collegate
    public boolean creaMissioneInTransazione(int idRichiesta, String obiettivo, String posizione,
                                             int idCaposquadra, String[] idOperatori,
                                             String[] idMezzi, String[] idMateriali,
                                             int idAdminLoggato) {
        boolean successo = false;
        Connection conn = null;

        String queryMissione = "INSERT INTO missione (id_richiesta, obiettivo, posizione, stato, timestamp_inizio) "
                + "VALUES (?, ?, ?, 'IN_CORSO', CURRENT_TIMESTAMP)";
        String queryRichiesta = "UPDATE richiesta_soccorso SET stato = 'IN_CORSO' WHERE id_richiesta = ?";
        String queryOp = "INSERT INTO assegnazione_operatori_missione (id_missione, id_utente, is_caposquadra) VALUES (?, ?, ?)";
        String queryMezzo = "INSERT INTO assegnazione_mezzi_missione (id_missione, id_mezzo) VALUES (?, ?)";
        String queryMat = "INSERT INTO assegnazione_materiale_missione (id_missione, id_materiale) VALUES (?, ?)";
        String queryLog = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";

        try {
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);

            int idMissioneGenerato = -1;

            // Step A: Creazione record missione con recupero ID generato
            try (PreparedStatement stmtMis = conn.prepareStatement(queryMissione, Statement.RETURN_GENERATED_KEYS)) {
                stmtMis.setInt(1, idRichiesta);
                stmtMis.setString(2, obiettivo);
                stmtMis.setString(3, posizione);
                stmtMis.executeUpdate();

                try (ResultSet rsKey = stmtMis.getGeneratedKeys()) {
                    if (rsKey.next()) {
                        idMissioneGenerato = rsKey.getInt(1);
                    }
                }
            }

            if (idMissioneGenerato == -1) {
                conn.rollback();
                return false;
            }

            // Step B: Aggiorna stato della richiesta soccorso a IN_CORSO
            try (PreparedStatement stmtRich = conn.prepareStatement(queryRichiesta)) {
                stmtRich.setInt(1, idRichiesta);
                stmtRich.executeUpdate();
            }

            // Step C: Assegnazione caposquadra
            try (PreparedStatement stmtCapo = conn.prepareStatement(queryOp)) {
                stmtCapo.setInt(1, idMissioneGenerato);
                stmtCapo.setInt(2, idCaposquadra);
                stmtCapo.setInt(3, 1);
                stmtCapo.executeUpdate();
            }

            // Step D: Assegnazione altri operatori (is_caposquadra = 0)
            if (idOperatori != null && idOperatori.length > 0) {
                try (PreparedStatement stmtOps = conn.prepareStatement(queryOp)) {
                    for (String opStr : idOperatori) {
                        int idOp = Integer.parseInt(opStr.trim());
                        if (idOp != idCaposquadra) {
                            stmtOps.setInt(1, idMissioneGenerato);
                            stmtOps.setInt(2, idOp);
                            stmtOps.setInt(3, 0);
                            stmtOps.executeUpdate();
                        }
                    }
                }
            }

            // Step E: Assegnazione mezzi
            if (idMezzi != null && idMezzi.length > 0) {
                try (PreparedStatement stmtMez = conn.prepareStatement(queryMezzo)) {
                    for (String mStr : idMezzi) {
                        stmtMez.setInt(1, idMissioneGenerato);
                        stmtMez.setInt(2, Integer.parseInt(mStr.trim()));
                        stmtMez.executeUpdate();
                    }
                }
            }

            // Step F: Assegnazione materiali
            if (idMateriali != null && idMateriali.length > 0) {
                try (PreparedStatement stmtMat = conn.prepareStatement(queryMat)) {
                    for (String matStr : idMateriali) {
                        stmtMat.setInt(1, idMissioneGenerato);
                        stmtMat.setInt(2, Integer.parseInt(matStr.trim()));
                        stmtMat.executeUpdate();
                    }
                }
            }

            // Step G: Primo evento di audit log
            try (PreparedStatement stmtLog = conn.prepareStatement(queryLog)) {
                stmtLog.setInt(1, idMissioneGenerato);
                stmtLog.setInt(2, idAdminLoggato);
                stmtLog.setString(3, "Missione avviata con successo per l'emergenza #" + idRichiesta);
                stmtLog.executeUpdate();
            }

            conn.commit();
            successo = true;

        } catch (Exception e) {
            System.err.println("Errore in transazione durante la creazione missione. Rollback in corso...");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception exRollback) {
                    exRollback.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception exClose) {
                    exClose.printStackTrace();
                }
            }
        }
        return successo;
    }

    // 3. Estrae lo storico delle missioni chiuse
    public List<Map<String, String>> estraiStoricoMissioni() {
        List<Map<String, String>> storicoTrovato = new ArrayList<>();
        String queryStorico = "SELECT id_missione, id_richiesta, obiettivo, livello_successo, commenti "
                + "FROM missione WHERE stato = 'CHIUSA' ORDER BY id_missione DESC";

        try (Connection connessioneDb = DBManager.getConnection();
             PreparedStatement statementStorico = connessioneDb.prepareStatement(queryStorico);
             ResultSet risultati = statementStorico.executeQuery()) {

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
            System.err.println("Errore estrazione storico missioni...");
            e.printStackTrace();
        }
        return storicoTrovato;
    }

    // 4. Estrae le missioni per un singolo operatore
    public List<Map<String, String>> estraiMissioniPerOperatore(int idOperatoreCercato) {
        List<Map<String, String>> missioniPersonali = new ArrayList<>();
        String queryMissioniOp = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo "
                + "FROM missione m "
                + "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione "
                + "WHERE aom.id_utente = ? "
                + "ORDER BY m.stato DESC, m.id_missione DESC";

        try (Connection connessioneDb = DBManager.getConnection();
             PreparedStatement statementMissioni = connessioneDb.prepareStatement(queryMissioniOp)) {

            statementMissioni.setInt(1, idOperatoreCercato);

            try (ResultSet risultati = statementMissioni.executeQuery()) {
                while (risultati.next()) {
                    Map<String, String> singolaMissione = new HashMap<>();
                    singolaMissione.put("id_missione", String.valueOf(risultati.getInt("id_missione")));
                    singolaMissione.put("obiettivo", risultati.getString("obiettivo"));
                    singolaMissione.put("posizione", risultati.getString("posizione"));
                    singolaMissione.put("stato", risultati.getString("stato"));

                    int livelloSuccesso = risultati.getInt("livello_successo");
                    String votoFormattato = risultati.wasNull() ? "-" : livelloSuccesso + " / 5";
                    singolaMissione.put("visualizzaVoto", votoFormattato);

                    missioniPersonali.add(singolaMissione);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore estrazione missioni operatore...");
            e.printStackTrace();
        }
        return missioniPersonali;
    }

    // 5. Chiusura missione con transazione
    public boolean chiudiMissioneInTransazione(int idMissione, int idRichiesta, int livelloSuccesso, String commenti, int idAdminLoggato) {
        boolean missioneChiusa = false;
        Connection connessioneDb = null;

        try {
            connessioneDb = DBManager.getConnection();
            connessioneDb.setAutoCommit(false);

            String queryMissione = "UPDATE missione SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP WHERE id_missione = ?";
            try (PreparedStatement statementMissione = connessioneDb.prepareStatement(queryMissione)) {
                statementMissione.setInt(1, livelloSuccesso);
                statementMissione.setString(2, commenti);
                statementMissione.setInt(3, idMissione);
                statementMissione.executeUpdate();
            }

            String queryRichiesta = "UPDATE richiesta_soccorso SET stato = 'CHIUSA' WHERE id_richiesta = ?";
            try (PreparedStatement statementRichiesta = connessioneDb.prepareStatement(queryRichiesta)) {
                statementRichiesta.setInt(1, idRichiesta);
                statementRichiesta.executeUpdate();
            }

            String queryTimeline = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";
            try (PreparedStatement statementTimeline = connessioneDb.prepareStatement(queryTimeline)) {
                statementTimeline.setInt(1, idMissione);
                statementTimeline.setInt(2, idAdminLoggato);
                statementTimeline.setString(3, "CHIUSURA MISSIONE. Rapporto finale: " + commenti);
                statementTimeline.executeUpdate();
            }

            connessioneDb.commit();
            missioneChiusa = true;

        } catch (Exception e) {
            System.err.println("Errore chiusura missione. Rollback...");
            e.printStackTrace();
            if (connessioneDb != null) {
                try {
                    connessioneDb.rollback();
                } catch (Exception exRollback) {
                    exRollback.printStackTrace();
                }
            }
        } finally {
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

    // 6. Stato rapido
    public String estraiStatoMissione(int idMissione) {
        String statoAttuale = "";
        String queryStato = "SELECT stato FROM missione WHERE id_missione = ?";
        try (Connection connessioneDb = DBManager.getConnection();
             PreparedStatement statementStato = connessioneDb.prepareStatement(queryStato)) {

            statementStato.setInt(1, idMissione);
            try (ResultSet risultati = statementStato.executeQuery()) {
                if (risultati.next()) {
                    statoAttuale = risultati.getString("stato");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statoAttuale;
    }

    // 7. Dettagli completi missione per scheda operativa
    public Map<String, Object> estraiDettagliCompleti(int idMissione) {
        Map<String, Object> mappaDettagli = new HashMap<>();

        try (Connection connessioneDb = DBManager.getConnection()) {
            String queryBase = "SELECT obiettivo, posizione, stato, timestamp_inizio FROM missione WHERE id_missione = ?";
            try (PreparedStatement stmtBase = connessioneDb.prepareStatement(queryBase)) {
                stmtBase.setInt(1, idMissione);
                try (ResultSet rsBase = stmtBase.executeQuery()) {
                    if (rsBase.next()) {
                        mappaDettagli.put("id_missione", idMissione);
                        mappaDettagli.put("obiettivo", rsBase.getString("obiettivo"));
                        mappaDettagli.put("posizione", rsBase.getString("posizione"));
                        mappaDettagli.put("stato", rsBase.getString("stato"));
                        mappaDettagli.put("inizio", rsBase.getTimestamp("timestamp_inizio").toString());
                    } else {
                        return mappaDettagli;
                    }
                }
            }

            String nomeCaposquadra = "Non ancora assegnato";
            String queryCapo = "SELECT u.nome, u.cognome FROM utenti u "
                    + "JOIN assegnazione_operatori_missione aom ON u.id_utente = aom.id_utente "
                    + "WHERE aom.id_missione = ? AND aom.is_caposquadra = 1";
            try (PreparedStatement stmtCapo = connessioneDb.prepareStatement(queryCapo)) {
                stmtCapo.setInt(1, idMissione);
                try (ResultSet rsCapo = stmtCapo.executeQuery()) {
                    if (rsCapo.next()) {
                        nomeCaposquadra = rsCapo.getString("nome") + " " + rsCapo.getString("cognome");
                    }
                }
            }
            mappaDettagli.put("caposquadra", nomeCaposquadra);

            List<String> listaMezzi = new ArrayList<>();
            String queryMezzi = "SELECT m.nome, m.descrizione FROM mezzo m "
                    + "JOIN assegnazione_mezzi_missione amm ON m.id_mezzo = amm.id_mezzo "
                    + "WHERE amm.id_missione = ?";
            try (PreparedStatement stmtMezzi = connessioneDb.prepareStatement(queryMezzi)) {
                stmtMezzi.setInt(1, idMissione);
                try (ResultSet rsMezzi = stmtMezzi.executeQuery()) {
                    while (rsMezzi.next()) {
                        listaMezzi.add(rsMezzi.getString("nome") + " (" + rsMezzi.getString("descrizione") + ")");
                    }
                }
            }
            mappaDettagli.put("mezzi", listaMezzi);

            List<String> listaMateriali = new ArrayList<>();
            String queryMateriali = "SELECT mat.nome, mat.descrizione FROM materiale mat "
                    + "JOIN assegnazione_materiale_missione ama ON mat.id_materiale = ama.id_materiale "
                    + "WHERE ama.id_missione = ?";
            try (PreparedStatement stmtMat = connessioneDb.prepareStatement(queryMateriali)) {
                stmtMat.setInt(1, idMissione);
                try (ResultSet rsMat = stmtMat.executeQuery()) {
                    while (rsMat.next()) {
                        listaMateriali.add(rsMat.getString("nome") + " - " + rsMat.getString("descrizione"));
                    }
                }
            }
            mappaDettagli.put("materiali", listaMateriali);

        } catch (Exception e) {
            System.err.println("Errore estrazione dettagli completi missione...");
            e.printStackTrace();
        }

        return mappaDettagli;
    }
    // Estrae le missioni attualmente IN_CORSO a cui è stato assegnato uno specifico operatore
    public List<Map<String, String>> estraiMissioniAttivePerOperatore(long idOperatore) {
        List<Map<String, String>> listaMissioni = new ArrayList<>();
        
        // La query fa una JOIN tra le missioni e la tabella delle assegnazioni
        String query = "SELECT m.id_missione, m.id_richiesta, m.obiettivo, m.posizione, m.stato " +
                       "FROM missione m " +
                       "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione " +
                       "WHERE aom.id_utente = ? AND m.stato = 'IN_CORSO'";
                       
        try (Connection connessioneDb = DBManager.getConnection();
             PreparedStatement statement = connessioneDb.prepareStatement(query)) {
             
            // Inseriamo l'ID dell'operatore al posto del punto interrogativo
            statement.setLong(1, idOperatore);
            
            try (ResultSet risultati = statement.executeQuery()) {
                while (risultati.next()) {
                    Map<String, String> missione = new HashMap<>();
                    missione.put("id_missione", String.valueOf(risultati.getInt("id_missione")));
                    missione.put("id_richiesta", String.valueOf(risultati.getInt("id_richiesta")));
                    missione.put("obiettivo", risultati.getString("obiettivo"));
                    missione.put("posizione", risultati.getString("posizione"));
                    missione.put("stato", risultati.getString("stato"));
                    
                    listaMissioni.add(missione);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nell'estrazione delle missioni per l'operatore ID: " + idOperatore);
            e.printStackTrace();
        }
        
        return listaMissioni;
    }
}