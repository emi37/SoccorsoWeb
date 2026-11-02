package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

import it.univaq.disim.webengineering.soccorsoweb.model.RichiestaSoccorso;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichiestaSoccorsoDAO {

    // inserisce una nuova richiesta in stato IN_ATTESA dal form di rchiesta
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
            System.err.println("errore durante il salvataggio della richiesta : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // convalida il token via email e attiva la richiesta (se non convalidi entro 10 min la richiesta rimane in stato di attesa sul DB)
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
            System.err.println("errore db durante la convalida del token: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // estrae le richieste con lo stato "ATTIVA" (viene richiamato dalla dashboard dell'admin)
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
            System.err.println("errore durante l'estrazione delle richieste attive: " + e.getMessage());
            e.printStackTrace();
        }
        return listaRichieste;
    }

    // scarta la richiesta impostandogli lo stato sul DB in "IGNORATA "
    public boolean aggiornaStatoRichiestaInIgnorata(int idRichiestaDaScartare) {
        String query = "UPDATE richiesta_soccorso SET stato = 'IGNORATA' WHERE id_richiesta = ?";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setInt(1, idRichiestaDaScartare);
            
            return statement.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.err.println("errore durante l'aggiornamento dello stato ignorata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // estrae i dettagli di una richiesta(la richieta estratta è quella con id_richiesta = ?)
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
            System.err.println("errore durante l'estrazione dei dettagli della richiesta: " + e.getMessage());
            e.printStackTrace();
        }
        return dettagli;
    }
}