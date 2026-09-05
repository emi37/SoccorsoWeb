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
    // Metodo per estrarre velocemente lo stato di una missione (ci serve per i check prima degli aggiornamenti)
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
            System.err.println("Panico durante l'estrazione dello stato della missione...");
            e.printStackTrace();
        }
        
        return statoAttuale;
    }

    // Mega-metodo che sostituisce le 4 query della vecchia DettaglioMissioneServlet
    public Map<String, Object> estraiDettagliCompleti(int idMissione) {
        Map<String, Object> mappaDettagli = new HashMap<>();

        // Apro una SOLA connessione e mi faccio tutti i giri che mi servono
        try (Connection connessioneDb = DBManager.getConnection()) {

            // 1. Info base della missione
            String queryBase = "SELECT obiettivo, posizione, stato, timestamp_inizio " +
                               "FROM missione WHERE id_missione = ?";
                               
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
                        // Se non trova la missione, esce subito restituendo una mappa vuota
                        return mappaDettagli;
                    }
                }
            }

            // 2. Cerchiamo il Caposquadra con una JOIN
            String nomeCaposquadra = "Non ancora assegnato";
            String queryCapo = "SELECT u.nome, u.cognome " +
                               "FROM utente u " +
                               "JOIN assegnazione_operatori_missione aom ON u.id_utente = aom.id_utente " +
                               "WHERE aom.id_missione = ? AND aom.is_caposquadra = 1";
                               
            try (PreparedStatement stmtCapo = connessioneDb.prepareStatement(queryCapo)) {
                stmtCapo.setInt(1, idMissione);
                try (ResultSet rsCapo = stmtCapo.executeQuery()) {
                    if (rsCapo.next()) {
                        nomeCaposquadra = rsCapo.getString("nome") + " " + rsCapo.getString("cognome");
                    }
                }
            }
            mappaDettagli.put("caposquadra", nomeCaposquadra);

            // 3. Estraiamo i Mezzi associati
            List<String> listaMezzi = new ArrayList<>();
            String queryMezzi = "SELECT m.nome, m.descrizione " +
                                "FROM mezzo m " +
                                "JOIN assegnazione_mezzi_missione amm ON m.id_mezzo = amm.id_mezzo " +
                                "WHERE amm.id_missione = ?";
                                
            try (PreparedStatement stmtMezzi = connessioneDb.prepareStatement(queryMezzi)) {
                stmtMezzi.setInt(1, idMissione);
                try (ResultSet rsMezzi = stmtMezzi.executeQuery()) {
                    while (rsMezzi.next()) {
                        // Concateno la stringa così FreeMarker deve solo stamparla in un loop <li>
                        listaMezzi.add(rsMezzi.getString("nome") + " (" + rsMezzi.getString("descrizione") + ")");
                    }
                }
            }
            mappaDettagli.put("mezzi", listaMezzi);

            // 4. Estraiamo i Materiali associati
            List<String> listaMateriali = new ArrayList<>();
            String queryMateriali = "SELECT mat.nome, mat.descrizione " +
                                    "FROM materiale mat " +
                                    "JOIN assegnazione_materiale_missione ama ON mat.id_materiale = ama.id_materiale " +
                                    "WHERE ama.id_missione = ?";
                                    
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
            System.err.println("Errore brutto durante il recupero dei dettagli completi della missione dal DB...");
            e.printStackTrace();
        }

        return mappaDettagli;
    }
}
