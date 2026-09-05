/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

/**
 *
 * @author edoar
 */
import it.univaq.disim.webengineering.soccorsoweb.model.Utenti;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtenteDAO {

    // Metodo per cercare un utente tramite la sua email (ci serve per il login)
    public Utenti estraiUtentePerEmail(String emailCercata) {
        Utenti utenteTrovato = null;

        // Spezziamo la query col + per leggerla senza impazzire
        String queryRicerca = "SELECT id_utente, nome, cognome, password, ruolo "
                + "FROM utente "
                + "WHERE email = ? AND attivo = TRUE";

        // Apro la connessione col nostro DBManager centralizzato e preparo lo statement
        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementRicerca = connessioneDb.prepareStatement(queryRicerca)) {

            // Bindo l'email al posto del punto interrogativo
            statementRicerca.setString(1, emailCercata);

            try (ResultSet risultati = statementRicerca.executeQuery()) {
                // Se la query trova qualcosa, assembliamo l'oggetto Utente
                if (risultati.next()) {
                    utenteTrovato = new Utenti();
                    utenteTrovato.setIdUtente(risultati.getLong("id_utente"));
                    utenteTrovato.setNome(risultati.getString("nome"));
                    utenteTrovato.setCognome(risultati.getString("cognome"));
                    utenteTrovato.setPassword(risultati.getString("password")); // Questo è l'hash BCrypt
                    utenteTrovato.setRuolo(risultati.getString("ruolo"));
                    utenteTrovato.setEmail(emailCercata);
                }
            }

        } catch (Exception e) {
            // Stampata ignorante a console per beccare subito il problema
            System.err.println("Errore catastrofico durante l'estrazione dell'utente per email...");
            e.printStackTrace();
        }

        return utenteTrovato;
    }

    // Metodo per salvare un nuovo amministratore o operatore
    public boolean salvaNuovoUtente(Utenti nuovoUtente) {
        boolean inserimentoRiuscito = false;

        String queryInserimento = "INSERT INTO utente "
                + "(nome, cognome, email, password, ruolo, attivo) "
                + "VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementInserimento = connessioneDb.prepareStatement(queryInserimento)) {

            // Mi tiro giù i dati dall'oggetto e li bindo
            statementInserimento.setString(1, nuovoUtente.getNome());
            statementInserimento.setString(2, nuovoUtente.getCognome());
            statementInserimento.setString(3, nuovoUtente.getEmail());
            statementInserimento.setString(4, nuovoUtente.getPassword()); // Hash già calcolato dal controller
            statementInserimento.setString(5, nuovoUtente.getRuolo());

            int righeModificate = statementInserimento.executeUpdate();
            if (righeModificate > 0) {
                inserimentoRiuscito = true;
            }

        } catch (Exception e) {
            System.err.println("Impossibile salvare il nuovo utente nel database...");
            e.printStackTrace();
        }

        return inserimentoRiuscito;
    }
    // Trova tutti gli operatori che non sono attualmente impiegati in missioni attive
    public List<Map<String, String>> estraiOperatoriDisponibili() {
        List<Map<String, String>> listaOperatori = new ArrayList<>();
        
        // Magia SQL: Uso la subquery per escludere chi sta in una missione IN_CORSO
        String query = "SELECT id_utente, nome, cognome " +
                       "FROM utente " +
                       "WHERE ruolo = 'OPERATORE' " +
                       "AND id_utente NOT IN ( " +
                       "    SELECT aom.id_utente " +
                       "    FROM assegnazione_operatori_missioni aom " +
                       "    JOIN missione m ON aom.id_missione = m.id_missione " +
                       "    WHERE m.stato = 'IN_CORSO' " +
                       ") " +
                       "ORDER BY cognome, nome";
                       
        try (Connection connessioneDb = DBManager.getConnection();
             PreparedStatement statement = connessioneDb.prepareStatement(query);
             ResultSet risultati = statement.executeQuery()) {
            
            while (risultati.next()) {
                Map<String, String> operatore = new HashMap<>();
                operatore.put("id_utente", risultati.getString("id_utente"));
                operatore.put("nome_completo", risultati.getString("cognome") + " " + risultati.getString("nome"));
                listaOperatori.add(operatore);
            }
        } catch (Exception e) {
            System.err.println("Panico durante la ricerca degli operatori liberi...");
            e.printStackTrace();
        }
        return listaOperatori;
    }
}
