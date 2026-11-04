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
            connessione = DBManager.getConnection(); //in ogni caso sotto di qualsiasi try, catch e finally, la connessione è sempre diversa da null, solo se non sei connesso al wifi di casa allora la connessione sarà null, altriment la connessionen al db da qui in poi sarà sempre diversa da null
            connessione.setAutoCommit(false);//cioe per capire meglio, connessione diversa da null vuol dire che la connessione da netbeans al DB esiste e sono effettivamente collegati e possono scambiarsi dati e informazioni 

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
            System.err.println("errore durante l'estrazione dello storico missioni...");
            e.printStackTrace();
        }
        return storicoTrovato;
    }

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
            System.err.println("errore durante l'estrazione delle missioni per l'operatore...");
            e.printStackTrace();
        }
        return missioniPersonali;
    }
        //qua ancziche' passare i parametri modoficati tramite i java bean, li passo direttamente dal metodo questo perche' vado a modificare 3 tabelle diverse sul DB missione , richiesta_soccorso , aggiornamento_missione
        //quindi anziche fare new nella servlet di tre oggetti diversi per passare 5 parametri, li passo direttamente qui nel metodo per comodità e tenere il codice+pulito
    public boolean chiudiMissioneInTransazione(int idMissione, int idRichiesta, int livelloSuccesso, String commenti, int idAdminLoggato) {
        boolean successo = false; //diventa true solo se tutte e 3 le query vanno a buon fine
        Connection connessione = null;
        //faccio 3 query perche cliccando il bottone " concludi " , succedono 2 update e 1 insert nel DB                           //time stamp salva l orario e la data di conclusione della missione 
        String queryMissione = "UPDATE missione SET stato = 'CHIUSA', livello_successo = ?, commenti = ?, timestamp_fine = CURRENT_TIMESTAMP WHERE id_missione = ?";
        String queryRichiesta = "UPDATE richiesta_soccorso SET stato = 'CHIUSA' WHERE id_richiesta = ?";
        String queryTimeline = "INSERT INTO aggiornamento_missione (id_missione, id_admin, testo_descrittivo) VALUES (?, ?, ?)";

        try {
            connessione = DBManager.getConnection();
            connessione.setAutoCommit(false); // non salva le modifiche fatte dalla prima query sul DB appena viene eseguita, ma lo fa solo quando tutte e 3 le query sono state eseguite
            //quindi fa il commit in locale sul DB solo quando tutte e 3 le queery vanno a buon fine
            try (PreparedStatement statementMissione = connessione.prepareStatement(queryMissione)) {
                statementMissione.setInt(1, livelloSuccesso);
                statementMissione.setString(2, commenti);
                statementMissione.setInt(3, idMissione);
                statementMissione.executeUpdate(); //qui esegue la prima query, non la va a committare subito sul db, ma committa solo alla fine
                                                   //  quando tutte e 3 le query sono state eseguite
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
                    connessione.rollback(); // rollback parte quando anche 1 sola query fallisce e dice al DB, elimina tutte le modoficeh che stavi per fare date lealtre query gia eseguiti, ripulisce le modofiche che stavano per essere fatte falle query quando faccio connessione.commit();
                } catch (Exception exRollback) { 
                    exRollback.printStackTrace();
                }
            }
        } finally { //ci entri sempre, sta fuori da try e catch
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

    public Map<String, Object> estraiDettagliCompleti(int idMissione) {
        Map<String, Object> mappaDettagli = new HashMap<>();
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
    
    
    public List<String> estraiPatentiOperatore(int idOperatore) {
        List<String> miePatenti = new ArrayList<>();
        String query = "SELECT p.codice FROM patente p " +
                       "JOIN utente_patente up ON p.id_patente = up.id_patente " +
                       "WHERE up.id_utente = ?";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idOperatore);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    miePatenti.add(resultSet.getString("codice"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return miePatenti;
    }

    public List<Map<String, String>> estraiPatentiMancanti(int idOperatore) {
        List<Map<String, String>> listaPatenti = new ArrayList<>();
        
        String query = "SELECT id_patente, codice FROM patente " +
                       "WHERE id_patente NOT IN (SELECT id_patente FROM utente_patente WHERE id_utente = ?) " +
                       "ORDER BY codice ASC";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idOperatore);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> mappaPatente = new HashMap<>();
                    mappaPatente.put("id_patente", String.valueOf(resultSet.getInt("id_patente")));
                    mappaPatente.put("codice", resultSet.getString("codice"));
                    listaPatenti.add(mappaPatente);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaPatenti;
    }

    public boolean aggiungiPatenteOperatore(int idOperatore, int idPatente) {
        String query = "INSERT IGNORE INTO utente_patente (id_utente, id_patente) VALUES (?, ?)";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idOperatore);
            statement.setInt(2, idPatente);
            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    
    
    public List<String> estraiAbilitaOperatore(int idOperatore) {
        List<String> mieAbilita = new ArrayList<>();
        String query = "SELECT a.nome FROM abilita a " +
                       "JOIN utente_abilita ua ON a.id_abilita = ua.id_abilita " +
                       "WHERE ua.id_utente = ?";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idOperatore);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    mieAbilita.add(resultSet.getString("nome"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mieAbilita;
    }

    public List<Map<String, String>> estraiAbilitaMancanti(int idOperatore) {
        List<Map<String, String>> listaAbilita = new ArrayList<>();
        
        String query = "SELECT id_abilita, nome FROM abilita " +
                       "WHERE id_abilita NOT IN (SELECT id_abilita FROM utente_abilita WHERE id_utente = ?) " +
                       "ORDER BY nome ASC";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idOperatore);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> mappaAbilita = new HashMap<>();
                    mappaAbilita.put("id_abilita", String.valueOf(resultSet.getInt("id_abilita")));
                    mappaAbilita.put("nome", resultSet.getString("nome"));
                    listaAbilita.add(mappaAbilita);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaAbilita;
    }

    public boolean aggiungiAbilitaOperatore(int idOperatore, int idAbilita) {
        String query = "INSERT IGNORE INTO utente_abilita (id_utente, id_abilita) VALUES (?, ?)";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idOperatore);
            statement.setInt(2, idAbilita);
            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
}