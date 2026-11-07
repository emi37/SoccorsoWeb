package it.univaq.disim.webengineering.soccorsoweb.dao.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.MissioneDAO;

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

public class MissioneDAO_MySQL implements MissioneDAO {

    private static final Logger LOGGER = Logger.getLogger(MissioneDAO_MySQL.class.getName());

    private static final java.time.format.DateTimeFormatter FORMATO_DATA = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
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
                // variabile per il file .ftl(l'etichetta front'end)           ,  variabile del DB
                mappaMissione.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                //in pratica la CHIAVE (a sinistra) è il parametro nel file FTL
                //invece il VALORE (a destra) è il dato vero e proprio che resultSet sta estraendo in questo istante dalla tabella del db
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
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione delle missioni in corso...", e);
        }
        return listaMissioni;
    }

    @Override
    public int creaMissioneInTransazione(int idRichiesta, String obiettivo, String posizione, int idCaposquadra, String[] idOperatori, String[] idMezzi, String[] idMateriali, int idAdminLoggato) {
        int idMissioneCreata = 0; // 0 = missione non creata
        Connection connessione = null;

        String queryMissione = "INSERT INTO missione (id_richiesta, obiettivo, posizione, stato, timestamp_inizio) VALUES (?, ?, ?, 'IN_CORSO', CURRENT_TIMESTAMP)";
        String queryRichiesta = "UPDATE richiesta_soccorso SET stato = 'IN_CORSO' WHERE id_richiesta = ? AND stato = 'ATTIVA'";
        String queryOperatore = "INSERT INTO assegnazione_operatori_missione (id_missione, id_utente, is_caposquadra) VALUES (?, ?, ?)";
        String queryMezzo = "INSERT INTO assegnazione_mezzi_missione (id_missione, id_mezzo) VALUES (?, ?)";
        String queryMateriale = "INSERT INTO assegnazione_materiale_missione (id_missione, id_materiale) VALUES (?, ?)";
        String queryLog = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";

        try {
            connessione = DBManager.getConnection(); //in ogni caso sotto di qualsiasi try, catch e finally, la connessione è sempre diversa da null, solo se non sei connesso al wifi di casa allora la connessione sarà null, altriment la connessionen al db da qui in poi sarà sempre diversa da null
            connessione.setAutoCommit(false);//cioe per capire meglio, connessione diversa da null vuol dire che la connessione da netbeans al DB esiste e sono effettivamente collegati e possono scambiarsi dati e informazioni 

            // la richiesta passa da ATTIVA a IN_CORSO in modo atomico: se qualcuno l'ha gia' presa in carico
            // (o ignorata) l'UPDATE non tocca nessuna riga e la missione NON viene creata
            try (PreparedStatement statementRichiesta = connessione.prepareStatement(queryRichiesta)) {
                statementRichiesta.setInt(1, idRichiesta);
                if (statementRichiesta.executeUpdate() != 1) {
                    connessione.rollback();
                    return 0;
                }
            }

            int idMissioneGenerato = -1; // perche' non so ancora che valore il db darà a questo id di questa missione, quindi gliene do io uno fittizzio, poi verra modificato

            try (PreparedStatement statementMissione = connessione.prepareStatement(queryMissione, Statement.RETURN_GENERATED_KEYS)) {//dico al db che mi serve l'id della missione che verra creata
                statementMissione.setInt(1, idRichiesta);
                statementMissione.setString(2, obiettivo);
                statementMissione.setString(3, posizione);
                statementMissione.executeUpdate(); // qui ho eseguito la qwuery quindi il DB salva l'id della missione e lo vado a prendere facendo statementMissione.getGeneratedKeys qui sotto
                                            
                try (ResultSet resultSetChiavi = statementMissione.getGeneratedKeys()) {
                    if (resultSetChiavi.next()) {
                        idMissioneGenerato = resultSetChiavi.getInt(1); // qui viene modificato il meno 1 con il vero valore dell id missione appena generata/fatta partire sul campo 
                    }                                   //legge (1) la prima riga del resultSetChiavi che ha salvato in quella nica riga, il valore vero dell'id della missione appena creata, che avevo fatto ricordare al DB prima  
                }
            }

            if (idMissioneGenerato == -1) { // se non dovesse cambiare da meno 1 al valore vero per ualche motivo, allora devo annullare tute le modifche dfatte al DB fin ora
                connessione.rollback();     // facendo rollback
                return 0;
            }

            // il caposquadra deve essere un operatore attivo e libero
            if (!operatoreLibero(connessione, idCaposquadra, idMissioneGenerato)) {
                connessione.rollback();
                return 0;
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
                            if (!operatoreLibero(connessione, idOp, idMissioneGenerato)) {
                                connessione.rollback();
                                return 0;
                            }
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
                        int idMezzo = Integer.parseInt(mezzo.trim());
                        if (!mezzoLibero(connessione, idMezzo, idMissioneGenerato)) {
                            connessione.rollback();
                            return 0;
                        }
                        statementMezzi.setInt(1, idMissioneGenerato);
                        statementMezzi.setInt(2, idMezzo);
                        statementMezzi.executeUpdate();
                    }
                }
            }

            if (idMateriali != null && idMateriali.length > 0) {
                try (PreparedStatement statementMateriali = connessione.prepareStatement(queryMateriale)) {
                    for (String materiale : idMateriali) {
                        int idMateriale = Integer.parseInt(materiale.trim());
                        if (!materialeLibero(connessione, idMateriale, idMissioneGenerato)) {
                            connessione.rollback();
                            return 0;
                        }
                        statementMateriali.setInt(1, idMissioneGenerato);
                        statementMateriali.setInt(2, idMateriale);
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
            idMissioneCreata = idMissioneGenerato;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante la creazione della missione", e);
            if (connessione != null) {
                try {
                    connessione.rollback();
                } catch (Exception exRollback) {
                    LOGGER.log(Level.SEVERE, "Errore imprevisto", exRollback);
                }
            }
        } finally {
            if (connessione != null) { 
                try {
                    connessione.setAutoCommit(true);
                } catch (Exception exAutoCommit) {
                    LOGGER.log(Level.SEVERE, "Errore imprevisto", exAutoCommit);
                } finally {
                    // la connessione DEVE tornare al pool in ogni caso
                    try { connessione.close(); } catch (Exception exClose) { LOGGER.log(Level.SEVERE, "Errore imprevisto", exClose); }
                }
            }
        }
        return idMissioneCreata;
    }

    @Override
    public List<Map<String, String>> estraiStoricoMissioni() {
        List<Map<String, String>> storicoTrovato = new ArrayList<>();
        
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
                Map<String, String> mappaMissione = new HashMap<>();
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
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione dello storico missioni...", e);
        }
        return storicoTrovato;
    }

    @Override
    public List<Map<String, String>> estraiMissioniPerOperatore(int idOperatoreCercato) {
        List<Map<String, String>> missioniPersonali = new ArrayList<>();
        
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
                    Map<String, String> singolaMissione = new HashMap<>();
                    singolaMissione.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                    singolaMissione.put("obiettivo", resultSet.getString("obiettivo"));
                    singolaMissione.put("posizione", resultSet.getString("posizione"));
                    singolaMissione.put("stato", resultSet.getString("stato"));

                    int livelloSuccesso = resultSet.getInt("livello_successo");
                    String votoFormattato = resultSet.wasNull() ? "-" : livelloSuccesso + " / 5";
                    singolaMissione.put("visualizzaVoto", votoFormattato);
            //se una miss. è ancora in corso, sul db ha livello_successo impostato a null, ma un intero come livello_successo non puo avere null ovviamente
            //quindi uso coem controllo di sicurezza wasNull, se la missione è ancora in corso, sostiotuisco NULL con un trattino, altriemnt se non lo è, ci metto la sua votazione, in pratica
                    missioniPersonali.add(singolaMissione);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione delle missioni per l'operatore...", e);
        }
        return missioniPersonali;
    }
        //qua ancziche' passare i parametri modoficati tramite i java bean, li passo direttamente dal metodo questo perche' vado a modificare 3 tabelle diverse sul DB missione , richiesta_soccorso , aggiornamento_missione
        //quindi anziche fare new nella servlet di tre oggetti diversi per passare 5 parametri, li passo direttamente qui nel metodo per comodità e tenere il codice+pulito
    @Override
    public boolean chiudiMissioneInTransazione(int idMissione, int livelloSuccesso, String commenti, int idAdminLoggato) {
        boolean successo = false;
        Connection connessione = null;

        // il rapporto finale e' facoltativo: se manca non deve comparire "null" nel testo
        String rapporto = (commenti == null) ? "" : commenti.trim();

        // la missione si chiude solo se e' ancora IN_CORSO: niente doppie chiusure (doppio click, pagina riaperta)
        String queryMissione = "UPDATE missione SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP "
                + "WHERE id_missione = ? AND stato = 'IN_CORSO'";
        // la richiesta da chiudere si ricava dalla missione stessa e NON da un campo del form,
        // altrimenti chi manomette il form potrebbe chiudere la richiesta di un'altra missione
        String queryRichiesta = "UPDATE richiesta_soccorso r JOIN missione m ON m.id_richiesta = r.id_richiesta "
                + "SET r.stato = 'CHIUSA' WHERE m.id_missione = ?";
        String queryTimeline = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";

        try {
            connessione = DBManager.getConnection();
            // le 3 operazioni (2 UPDATE e 1 INSERT) vengono salvate insieme solo se riescono tutte
            connessione.setAutoCommit(false);

            try (PreparedStatement statementMissione = connessione.prepareStatement(queryMissione)) {
                statementMissione.setInt(1, livelloSuccesso);
                statementMissione.setString(2, rapporto);
                statementMissione.setInt(3, idMissione);
                if (statementMissione.executeUpdate() != 1) {
                    // missione inesistente o gia' chiusa
                    connessione.rollback();
                    return false;
                }
            }

            try (PreparedStatement statementRichiesta = connessione.prepareStatement(queryRichiesta)) {
                statementRichiesta.setInt(1, idMissione);
                statementRichiesta.executeUpdate();
            }

            try (PreparedStatement statementTimeline = connessione.prepareStatement(queryTimeline)) {
                statementTimeline.setInt(1, idMissione);
                statementTimeline.setInt(2, idAdminLoggato);
                statementTimeline.setString(3, rapporto.isEmpty()
                        ? "CHIUSURA MISSIONE."
                        : "CHIUSURA MISSIONE. Rapporto finale: " + rapporto);
                statementTimeline.executeUpdate();
            }

            connessione.commit();
            successo = true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante la chiusura della missione", e);
            if (connessione != null) {
                try {
                    connessione.rollback();
                } catch (Exception exRollback) {
                    LOGGER.log(Level.SEVERE, "Errore imprevisto", exRollback);
                }
            }
        } finally {
            if (connessione != null) {
                try {
                    connessione.setAutoCommit(true);
                } catch (Exception exAutoCommit) {
                    LOGGER.log(Level.SEVERE, "Errore imprevisto", exAutoCommit);
                } finally {
                    // la connessione DEVE tornare al pool in ogni caso
                    try { connessione.close(); } catch (Exception exClose) { LOGGER.log(Level.SEVERE, "Errore imprevisto", exClose); }
                }
            }
        }
        return successo;
    }

    @Override
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
            LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
        }
        return statoAttuale;
    }

    @Override
    public Map<String, Object> estraiDettagliCompleti(int idMissione) {
        Map<String, Object> mappaDettagli = new HashMap<>();
        String statoDellaMissione = ""; 

        try (Connection connessione = DBManager.getConnection()) {
            
            String queryBase = "SELECT obiettivo, posizione, stato, timestamp_inizio, timestamp_fine, livello_successo, commenti FROM missione WHERE id_missione = ?";
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
                        // dati di chiusura (restano vuoti finche' la missione e' in corso)
                        mappaDettagli.put("fine", formattaData(resultSetBase.getTimestamp("timestamp_fine")));
                        int livelloSuccesso = resultSetBase.getInt("livello_successo");
                        if (!resultSetBase.wasNull()) {
                            mappaDettagli.put("livello_successo", livelloSuccesso);
                        }
                        mappaDettagli.put("commenti", resultSetBase.getString("commenti"));
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

            // squadra completa: caposquadra per primo, poi gli altri operatori
            List<Map<String, Object>> squadra = new ArrayList<>();
            String querySquadra = "SELECT u.nome, u.cognome, aom.is_caposquadra FROM utenti u "
                    + "JOIN assegnazione_operatori_missione aom ON u.id_utente = aom.id_utente "
                    + "WHERE aom.id_missione = ? ORDER BY aom.is_caposquadra DESC, u.cognome, u.nome";
            try (PreparedStatement statementSquadra = connessione.prepareStatement(querySquadra)) {
                statementSquadra.setInt(1, idMissione);
                try (ResultSet resultSetSquadra = statementSquadra.executeQuery()) {
                    while (resultSetSquadra.next()) {
                        Map<String, Object> membro = new HashMap<>();
                        membro.put("nome", resultSetSquadra.getString("nome") + " " + resultSetSquadra.getString("cognome"));
                        membro.put("caposquadra", resultSetSquadra.getInt("is_caposquadra") == 1);
                        squadra.add(membro);
                    }
                }
            }
            mappaDettagli.put("squadra", squadra);
            
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
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione dei dettagli completi della missione...", e);
        }

        return mappaDettagli;
    }

    @Override
    public List<Map<String, String>> estraiMissioniAttivePerOperatore(long idOperatore) {
        List<Map<String, String>> listaMissioni = new ArrayList<>();
        
        String query = "SELECT m.id_missione, m.id_richiesta, m.obiettivo, m.posizione, m.stato " +
                       "FROM missione m " +
                       "JOIN assegnazione_operatori_missione aom ON m.id_missione = aom.id_missione " +
                       "WHERE aom.id_utente = ? AND m.stato = 'IN_CORSO'";
                       
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setLong(1, idOperatore);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> mappaMissione = new HashMap<>();
                    mappaMissione.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                    mappaMissione.put("id_richiesta", String.valueOf(resultSet.getInt("id_richiesta")));
                    mappaMissione.put("obiettivo", resultSet.getString("obiettivo"));
                    mappaMissione.put("posizione", resultSet.getString("posizione"));
                    mappaMissione.put("stato", resultSet.getString("stato"));
                    
                    listaMissioni.add(mappaMissione);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore nell'estrazione delle missioni attive per l'operatore...", e);
        }
        
        return listaMissioni;
    }

    @Override
    public boolean modificaMissioneInCorso(int idMissione, String nuovoObiettivo, String nuovaPosizione,
                                           String[] nuoviOperatori, String[] nuoviMezzi, String[] nuoviMateriali) {
        
        boolean successo = false;
        Connection connessione = null;
        
        String queryMissione = "UPDATE missione SET obiettivo = ?, posizione = ? WHERE id_missione = ? AND stato = 'IN_CORSO'";
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
                // se la missione nel frattempo e' stata chiusa non si modifica piu' nulla
                if (statementMissione.executeUpdate() != 1) {
                    connessione.rollback();
                    return false;
                }
            }

            if (nuoviOperatori != null) {
                try (PreparedStatement statementOperatori = connessione.prepareStatement(queryOperatore)) {
                    for (String operatore : nuoviOperatori) {
                        int idOp = Integer.parseInt(operatore.trim());
                        if (!operatoreLibero(connessione, idOp, idMissione)) {
                            connessione.rollback();
                            return false;
                        }
                        statementOperatori.setInt(1, idMissione);
                        statementOperatori.setInt(2, idOp);
                        statementOperatori.executeUpdate();
                    }
                }
            }

            if (nuoviMezzi != null) {
                try (PreparedStatement statementMezzi = connessione.prepareStatement(queryMezzo)) {
                    for (String mezzo : nuoviMezzi) {
                        int idMezzo = Integer.parseInt(mezzo.trim());
                        if (!mezzoLibero(connessione, idMezzo, idMissione)) {
                            connessione.rollback();
                            return false;
                        }
                        statementMezzi.setInt(1, idMissione);
                        statementMezzi.setInt(2, idMezzo);
                        statementMezzi.executeUpdate();
                    }
                }
            }

            if (nuoviMateriali != null) {
                try (PreparedStatement statementMateriali = connessione.prepareStatement(queryMateriale)) {
                    for (String materiale : nuoviMateriali) {
                        int idMateriale = Integer.parseInt(materiale.trim());
                        if (!materialeLibero(connessione, idMateriale, idMissione)) {
                            connessione.rollback();
                            return false;
                        }
                        statementMateriali.setInt(1, idMissione);
                        statementMateriali.setInt(2, idMateriale);
                        statementMateriali.executeUpdate();
                    }
                }
            }

            connessione.commit();
            successo = true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante la modifica della missione.", e);
            if (connessione != null) {
                try {
                    connessione.rollback();
                } catch (Exception exRollback) {
                    LOGGER.log(Level.SEVERE, "Errore imprevisto", exRollback);
                }
            }
        } finally {
            if (connessione != null) {
                try {
                    connessione.setAutoCommit(true);
                } catch (Exception exAutoCommit) {
                    LOGGER.log(Level.SEVERE, "Errore imprevisto", exAutoCommit);
                } finally {
                    // la connessione DEVE tornare al pool in ogni caso
                    try { connessione.close(); } catch (Exception exClose) { LOGGER.log(Level.SEVERE, "Errore imprevisto", exClose); }
                }
            }
        }
        
        return successo;
    }

    // ---- controlli di disponibilita' lato server -----------------------------------------
    // Usano gli STESSI criteri delle liste "disponibili" mostrate all'admin (utente OPERATORE
    // attivo, mezzo/materiale attivo, non impegnato in una missione IN_CORSO), ma vengono
    // eseguiti dentro la transazione: non ci si puo' fidare solo di cio' che il form mostra,
    // perche' una richiesta puo' essere costruita a mano o un'altra missione puo' aver
    // preso la risorsa nel frattempo.
    // idMissioneDaEscludere: la missione che stiamo creando/modificando, le cui assegnazioni
    // non devono contare come "impegno".

    private boolean operatoreLibero(Connection connessione, int idUtente, int idMissioneDaEscludere) throws SQLException {
        String query = "SELECT COUNT(*) FROM utenti u WHERE u.id_utente = ? AND u.ruolo = 'OPERATORE' AND u.attivo = 1 "
                + "AND NOT EXISTS (SELECT 1 FROM assegnazione_operatori_missione aom "
                + "JOIN missione m ON aom.id_missione = m.id_missione "
                + "WHERE aom.id_utente = u.id_utente AND m.stato = 'IN_CORSO' AND m.id_missione <> ?)";
        return esisteAlmenoUno(connessione, query, idUtente, idMissioneDaEscludere);
    }

    private boolean mezzoLibero(Connection connessione, int idMezzo, int idMissioneDaEscludere) throws SQLException {
        String query = "SELECT COUNT(*) FROM mezzo x WHERE x.id_mezzo = ? AND x.attivo = 1 "
                + "AND NOT EXISTS (SELECT 1 FROM assegnazione_mezzi_missione amm "
                + "JOIN missione m ON amm.id_missione = m.id_missione "
                + "WHERE amm.id_mezzo = x.id_mezzo AND m.stato = 'IN_CORSO' AND m.id_missione <> ?)";
        return esisteAlmenoUno(connessione, query, idMezzo, idMissioneDaEscludere);
    }

    private boolean materialeLibero(Connection connessione, int idMateriale, int idMissioneDaEscludere) throws SQLException {
        String query = "SELECT COUNT(*) FROM materiale x WHERE x.id_materiale = ? AND x.attivo = 1 "
                + "AND NOT EXISTS (SELECT 1 FROM assegnazione_materiale_missione ama "
                + "JOIN missione m ON ama.id_missione = m.id_missione "
                + "WHERE ama.id_materiale = x.id_materiale AND m.stato = 'IN_CORSO' AND m.id_missione <> ?)";
        return esisteAlmenoUno(connessione, query, idMateriale, idMissioneDaEscludere);
    }

    private boolean esisteAlmenoUno(Connection connessione, String query, int primo, int secondo) throws SQLException {
        try (PreparedStatement statement = connessione.prepareStatement(query)) {
            statement.setInt(1, primo);
            statement.setInt(2, secondo);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean operatoreCoinvoltoInMissione(int idMissione, int idUtente) {
        String query = "SELECT COUNT(*) FROM assegnazione_operatori_missione WHERE id_missione = ? AND id_utente = ?";
        try (Connection connessione = DBManager.getConnection()) {
            return esisteAlmenoUno(connessione, query, idMissione, idUtente);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore nel controllo della partecipazione alla missione", e);
        }
        return false;
    }

    @Override
    public List<Map<String, String>> estraiContattiOperatoriMissione(int idMissione) {
        List<Map<String, String>> contatti = new ArrayList<>();
        String query = "SELECT u.nome, u.cognome, u.email, aom.is_caposquadra FROM utenti u "
                + "JOIN assegnazione_operatori_missione aom ON u.id_utente = aom.id_utente "
                + "WHERE aom.id_missione = ? ORDER BY aom.is_caposquadra DESC, u.cognome, u.nome";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {
            statement.setInt(1, idMissione);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> contatto = new HashMap<>();
                    contatto.put("nome", resultSet.getString("nome"));
                    contatto.put("cognome", resultSet.getString("cognome"));
                    contatto.put("email", resultSet.getString("email"));
                    contatto.put("caposquadra", String.valueOf(resultSet.getInt("is_caposquadra") == 1));
                    contatti.add(contatto);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore nell'estrazione dei contatti degli operatori della missione", e);
        }
        return contatti;
    }

    @Override
    public List<Map<String, String>> estraiStoricoOperatore(int idOperatore) {
        return estraiStoricoRisorsa("assegnazione_operatori_missione", "id_utente", idOperatore);
    }

    @Override
    public List<Map<String, String>> estraiStoricoMezzo(int idMezzo) {
        return estraiStoricoRisorsa("assegnazione_mezzi_missione", "id_mezzo", idMezzo);
    }

    @Override
    public List<Map<String, String>> estraiStoricoMateriale(int idMateriale) {
        return estraiStoricoRisorsa("assegnazione_materiale_missione", "id_materiale", idMateriale);
    }

    // storico delle missioni in cui una risorsa (operatore, mezzo o materiale) e' stata coinvolta.
    // tabella e colonna sono costanti scelte dal codice qui sopra, mai valori arrivati dall'utente
    private List<Map<String, String>> estraiStoricoRisorsa(String tabellaAssegnazione, String colonnaRisorsa, int idRisorsa) {
        List<Map<String, String>> storico = new ArrayList<>();
        String query = "SELECT m.id_missione, m.obiettivo, m.posizione, m.stato, m.livello_successo, m.timestamp_inizio, m.timestamp_fine "
                + "FROM missione m JOIN " + tabellaAssegnazione + " a ON m.id_missione = a.id_missione "
                + "WHERE a." + colonnaRisorsa + " = ? "
                + "ORDER BY m.timestamp_inizio DESC, m.id_missione DESC";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {
            statement.setInt(1, idRisorsa);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> riga = new HashMap<>();
                    riga.put("id_missione", String.valueOf(resultSet.getInt("id_missione")));
                    riga.put("obiettivo", resultSet.getString("obiettivo"));
                    riga.put("posizione", resultSet.getString("posizione"));
                    riga.put("stato", resultSet.getString("stato"));
                    int livello = resultSet.getInt("livello_successo");
                    riga.put("esito", resultSet.wasNull() ? "-" : livello + " / 5");
                    riga.put("inizio", formattaData(resultSet.getTimestamp("timestamp_inizio")));
                    riga.put("fine", formattaData(resultSet.getTimestamp("timestamp_fine")));
                    storico.add(riga);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore nell'estrazione dello storico della risorsa", e);
        }
        return storico;
    }

    private String formattaData(java.sql.Timestamp data) {
        return data == null ? null : data.toLocalDateTime().format(FORMATO_DATA);
    }

}
