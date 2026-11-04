package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

import it.univaq.disim.webengineering.soccorsoweb.model.Utenti;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtenteDAO {

    // recupera i dati dell'utente per effettuare il login
    public Utenti estraiUtentePerEmail(String emailCercata) {
        Utenti utenteTrovato = null;
        
        String query = "SELECT id_utente, nome, cognome, email, password, ruolo " + "FROM utenti " + "WHERE email = ? AND attivo = 1";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setString(1, emailCercata);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    utenteTrovato = new Utenti();
                    utenteTrovato.setIdUtente(resultSet.getLong("id_utente"));
                    utenteTrovato.setNome(resultSet.getString("nome"));
                    utenteTrovato.setCognome(resultSet.getString("cognome"));
                    utenteTrovato.setPassword(resultSet.getString("password"));
                    utenteTrovato.setRuolo(resultSet.getString("ruolo"));
                    utenteTrovato.setEmail(emailCercata);
                }
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione dell'utente tramite la mail: " + e.getMessage());
            e.printStackTrace();
        }
        
        return utenteTrovato;
    }

    // inserisce un nuovo utente e restituisce il suo id generato
    public long salvaNuovoUtente(Utenti nuovoUtente) {
        long idGenerato = -1;
        
        String query = "INSERT INTO utenti (nome, cognome, email, password, ruolo, attivo) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, nuovoUtente.getNome());
            statement.setString(2, nuovoUtente.getCognome());
            statement.setString(3, nuovoUtente.getEmail());
            statement.setString(4, nuovoUtente.getPassword());
            statement.setString(5, nuovoUtente.getRuolo());

            if (statement.executeUpdate() > 0) {
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        idGenerato = resultSet.getLong(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("errore durante il salvataggio del nuovo utente: " + e.getMessage());
            e.printStackTrace();
        }
        
        return idGenerato;
    }

    public void collegaAbilita(long idUtente, int idAbilita) {
        String query = "INSERT INTO utente_abilita (id_utente, id_abilita) VALUES (?, ?)";
        
        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setLong(1, idUtente);
            statement.setInt(2, idAbilita);
            statement.executeUpdate();
            
        } catch (Exception e) {
            System.err.println("errore durante il collegamento dell'abilità all'utente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void collegaPatente(long idUtente, int idPatente) {
        String query = "INSERT INTO utente_patente (id_utente, id_patente) VALUES (?, ?)";
        
        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setLong(1, idUtente);
            statement.setInt(2, idPatente);
            statement.executeUpdate();
            
        } catch (Exception e) {
            System.err.println("errore durante il collegamento della patente all'utente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // estrae tutti gli operatori e mostra se sono liberi o impegnati
    public List<Map<String, String>> estraiTuttiGliOperatoriConStato() {
        List<Map<String, String>> listaOperatori = new ArrayList<>();
        
        String query = "SELECT u.id_utente, u.nome, u.cognome, u.email, u.ruolo, "
                + " CASE "
                + " WHEN u.ruolo = 'ADMIN' THEN 'AMMINISTRATORE' "
                + " WHEN EXISTS ("
                + " SELECT 1 FROM assegnazione_operatori_missione aom "
                + " JOIN missione mis ON aom.id_missione = mis.id_missione "
                + " WHERE aom.id_utente = u.id_utente AND mis.stato = 'IN_CORSO'"
                + " ) THEN 'IMPEGNATO' ELSE 'LIBERO' "
                + " END AS stato_attuale "
                + " FROM utenti u "
                + " WHERE u.attivo = 1 "
                + " ORDER BY u.id_utente DESC";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query); 
             ResultSet resultSet = statement.executeQuery()) {
             
            while (resultSet.next()) {
                Map< String, String > operatore = new HashMap<>();
                operatore.put("id_utente", String.valueOf(resultSet.getInt("id_utente")));
                operatore.put("nome", resultSet.getString("nome"));
                operatore.put("cognome", resultSet.getString("cognome"));
                operatore.put("email", resultSet.getString("email"));
                operatore.put("ruolo", resultSet.getString("ruolo")); 
                operatore.put("stato_attuale", resultSet.getString("stato_attuale"));
                
                listaOperatori.add(operatore);
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione degli operatori con stato: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaOperatori;
    }

    // estrae la lista degli operatori attualmente non assegnati a nessuna missione in corso
    public List< Map<String, String> > estraiOperatoriDisponibili() {
        List< Map<String, String> > listaOperatori = new ArrayList<>();
        
        String query = "SELECT id_utente, nome, cognome " +
                       "FROM utenti " +
                       "WHERE ruolo = 'OPERATORE' AND attivo = 1 " +
                       "AND id_utente NOT IN ( " +
                       "    SELECT aom.id_utente " +
                       "    FROM assegnazione_operatori_missione aom " +
                       "    JOIN missione m ON aom.id_missione = m.id_missione " +
                       "    WHERE m.stato = 'IN_CORSO' " +
                       ") " +
                       "ORDER BY cognome, nome";
                       
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Map< String, String > operatore = new HashMap<>();
                operatore.put("id_utente", resultSet.getString("id_utente"));
                operatore.put("nome_completo", resultSet.getString("cognome") + " " + resultSet.getString("nome"));
                listaOperatori.add(operatore);
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione degli operatori disponibili: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaOperatori;
    }

    // 1 - i due metodi seguenti servono a popolare i menu a tendina dei form
    public List< Map<String, String> > estraiTutteLeAbilita() {
        List< Map<String, String> > lista = new ArrayList<>();
        String query = "SELECT id_abilita, nome FROM abilita";
        
        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query); 
             ResultSet resultSet = statement.executeQuery()) {
             
            while (resultSet.next()) {
                Map< String, String > mappaAbilita = new HashMap<>();
                mappaAbilita.put("id_abilita", String.valueOf(resultSet.getInt("id_abilita")));
                mappaAbilita.put("nome", resultSet.getString("nome"));
                lista.add(mappaAbilita);
            }
        } catch (Exception e) { 
            System.err.println("errore durante l'estrazione del catalogo abilità: " + e.getMessage());
            e.printStackTrace(); 
        }
        
        return lista;
    }
// 2 - 
    public List< Map<String, String> > estraiTutteLePatenti() {
        List< Map<String, String> > lista = new ArrayList<>();
        String query = "SELECT id_patente, codice FROM patente";
        
        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query); 
             ResultSet resultSet = statement.executeQuery()) {
             
            while (resultSet.next()) {
                Map< String, String > mappaPatente = new HashMap<>();
                mappaPatente.put("id_patente", String.valueOf(resultSet.getInt("id_patente")));
                mappaPatente.put("codice", resultSet.getString("codice"));
                lista.add(mappaPatente);
            }
        } catch (Exception e) { 
            System.err.println("errore durante l'estrazione del catalogo patenti: " + e.getMessage());
            e.printStackTrace(); 
        }
        
        return lista;
    }
}