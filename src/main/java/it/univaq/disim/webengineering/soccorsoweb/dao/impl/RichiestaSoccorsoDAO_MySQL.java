package it.univaq.disim.webengineering.soccorsoweb.dao.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.RichiestaSoccorsoDAO;

import it.univaq.disim.webengineering.soccorsoweb.model.RichiestaSoccorso;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichiestaSoccorsoDAO_MySQL implements RichiestaSoccorsoDAO {

    private static final Logger LOGGER = Logger.getLogger(RichiestaSoccorsoDAO_MySQL.class.getName());

    // inserisce una nuova richiesta in stato IN_ATTESA dal form di rchiesta
    @Override
    public boolean salvaNuovaRichiesta(RichiestaSoccorso richiesta) {
        String query = "INSERT INTO richiesta_soccorso "
                + "(nome_segnalante, email_segnalante, posizione, descrizione, ip_origine, token_convalida, stato, timestamp_creazione) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'IN_ATTESA', NOW())";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setString(1, richiesta.getNomeSegnalante());
            statement.setString(2, richiesta.getEmailSegnalante());
            statement.setString(3, richiesta.getPosizione());
            statement.setString(4, richiesta.getDescrizione());
            statement.setString(5, richiesta.getIpOrigine());
            statement.setString(6, richiesta.getTokenConvalida());

            return statement.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante il salvataggio della richiesta : " + e.getMessage(), e);
        }
        return false;
    }

    // anti-spam: true se la stessa email (o lo stesso IP) ha già inviato una richiesta negli ultimi N minuti
    @Override
    public boolean esisteRichiestaRecente(String email, String ipOrigine, int minuti) {
        String query = "SELECT COUNT(*) FROM richiesta_soccorso "
                + "WHERE (email_segnalante = ? OR ip_origine = ?) "
                + "  AND timestamp_creazione >= NOW() - INTERVAL ? MINUTE";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, ipOrigine);
            statement.setInt(3, minuti);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante il controllo anti-spam: " + e.getMessage(), e);
        }
        // se il controllo fallisce non blocchiamo una possibile vera emergenza
        return false;
    }

    // convalida il token via email e attiva la richiesta (se non convalidi entro 10 min la richiesta rimane in stato di attesa sul DB)
    @Override
    public boolean svuotaTokenConvalida(String tokenUsato) {
        String query = "UPDATE richiesta_soccorso "
                + "SET stato = 'ATTIVA', "
                + "    token_convalida = NULL, "
                + "    timestamp_convalida = CURRENT_TIMESTAMP "
                + "WHERE token_convalida = ? "
                + "  AND stato = 'IN_ATTESA' "
                + "  AND timestamp_creazione >= NOW() - INTERVAL 10 MINUTE";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setString(1, tokenUsato);
            
            return statement.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore db durante la convalida del token: " + e.getMessage(), e);
        }
        return false;
    }

    // estrae le richieste con lo stato "ATTIVA" (viene richiamato dalla dashboard dell'admin)
    @Override
    public List< Map<String, String> > estraiRichiesteAttive() {
        List< Map<String, String> > listaRichieste = new ArrayList<>();
        
        String query = "SELECT id_richiesta, nome_segnalante, email_segnalante, posizione, descrizione "
                + "FROM richiesta_soccorso WHERE stato = 'ATTIVA' ORDER BY timestamp_convalida ASC";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query); 
             ResultSet resultSet = statement.executeQuery()) {
             
            while (resultSet.next()) {
                Map< String, String > mappaRichiesta = new HashMap<>();
                mappaRichiesta.put("id", String.valueOf(resultSet.getInt("id_richiesta")));
                mappaRichiesta.put("segnalante", resultSet.getString("nome_segnalante"));
                mappaRichiesta.put("email", resultSet.getString("email_segnalante"));
                mappaRichiesta.put("posizione", resultSet.getString("posizione"));
                mappaRichiesta.put("descrizione", resultSet.getString("descrizione"));
                
                listaRichieste.add(mappaRichiesta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione delle richieste attive: " + e.getMessage(), e);
        }
        return listaRichieste;
    }

    // scarta la richiesta impostandogli lo stato sul DB in "IGNORATA "
    @Override
    public boolean aggiornaStatoRichiestaInIgnorata(int idRichiestaDaScartare) {
        String query = "UPDATE richiesta_soccorso SET stato = 'IGNORATA' WHERE id_richiesta = ?";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setInt(1, idRichiestaDaScartare);
            
            return statement.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'aggiornamento dello stato ignorata: " + e.getMessage(), e);
        }
        return false;
    }

    // estrae i dettagli di una richiesta(la richieta estratta è quella con id_richiesta = ?)
    @Override
    public Map< String, String > estraiDettagliRichiesta(int idRichiesta) {
        Map< String, String > dettagli = new HashMap<>();
        
        String query = "SELECT nome_segnalante, posizione, descrizione, timestamp_creazione " +
                       "FROM richiesta_soccorso WHERE id_richiesta = ?";
                       
        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setInt(1, idRichiesta);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    dettagli.put("id_richiesta", String.valueOf(idRichiesta));
                    dettagli.put("nome", resultSet.getString("nome_segnalante"));
                    dettagli.put("posizione", resultSet.getString("posizione"));
                    dettagli.put("descrizione", resultSet.getString("descrizione"));
                    dettagli.put("data_ora", resultSet.getTimestamp("timestamp_creazione").toString());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione dei dettagli della richiesta: " + e.getMessage(), e);
        }
        return dettagli;
    }
}