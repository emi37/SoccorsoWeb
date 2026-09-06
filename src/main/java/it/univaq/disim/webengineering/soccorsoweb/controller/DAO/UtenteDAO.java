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

    // 1. Estrazione per il Login
    public Utenti estraiUtentePerEmail(String emailCercata) {
        Utenti utenteTrovato = null;
        String queryRicerca = "SELECT id_utente, nome, cognome, email, password, ruolo "
                + "FROM utenti "
                + "WHERE email = ? AND attivo = 1";

        try (Connection connessioneDb = DBManager.getConnection(); 
             PreparedStatement statementRicerca = connessioneDb.prepareStatement(queryRicerca)) {

            statementRicerca.setString(1, emailCercata);

            try (ResultSet risultati = statementRicerca.executeQuery()) {
                if (risultati.next()) {
                    utenteTrovato = new Utenti();
                    utenteTrovato.setIdUtente(risultati.getLong("id_utente"));
                    utenteTrovato.setNome(risultati.getString("nome"));
                    utenteTrovato.setCognome(risultati.getString("cognome"));
                    utenteTrovato.setPassword(risultati.getString("password"));
                    utenteTrovato.setRuolo(risultati.getString("ruolo"));
                    utenteTrovato.setEmail(emailCercata);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore catastrofico durante l'estrazione dell'utente per email...");
            e.printStackTrace();
        }
        return utenteTrovato;
    }

    // 2. Salvataggio Utente (Restituisce l'ID generato dal DB)
    public long salvaNuovoUtente(Utenti nuovoUtente) {
        long idGenerato = -1;
        String queryInserimento = "INSERT INTO utenti (nome, cognome, email, password, ruolo, attivo) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection connessioneDb = DBManager.getConnection(); 
             PreparedStatement statementInserimento = connessioneDb.prepareStatement(queryInserimento, Statement.RETURN_GENERATED_KEYS)) {

            statementInserimento.setString(1, nuovoUtente.getNome());
            statementInserimento.setString(2, nuovoUtente.getCognome());
            statementInserimento.setString(3, nuovoUtente.getEmail());
            statementInserimento.setString(4, nuovoUtente.getPassword());
            statementInserimento.setString(5, nuovoUtente.getRuolo());

            int righeModificate = statementInserimento.executeUpdate();
            
            if (righeModificate > 0) {
                try (ResultSet rs = statementInserimento.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerato = rs.getLong(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Impossibile salvare il nuovo utente nel database...");
            e.printStackTrace();
        }
        return idGenerato;
    }

    // 3. Collegamento Abilità all'utente
    public void collegaAbilita(long idUtente, int idAbilita) {
        String query = "INSERT INTO utente_abilita (id_utente, id_abilita) VALUES (?, ?)";
        try (Connection conn = DBManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, idUtente);
            stmt.setInt(2, idAbilita);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Errore durante il collegamento dell'abilità...");
            e.printStackTrace();
        }
    }

    // 4. Collegamento Patente all'utente
    public void collegaPatente(long idUtente, int idPatente) {
        String query = "INSERT INTO utente_patente (id_utente, id_patente) VALUES (?, ?)";
        try (Connection conn = DBManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, idUtente);
            stmt.setInt(2, idPatente);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Errore durante il collegamento della patente...");
            e.printStackTrace();
        }
    }

    // 5. Estrae TUTTI gli operatori per la gestione Admin (con calcolo stato LIBERO/IMPEGNATO)
    public List<Map<String, String>> estraiTuttiGliOperatoriConStato() {
        List<Map<String, String>> listaOperatori = new ArrayList<>();
        
        // Query sistemata: prende TUTTI gli utenti attivi. Se Admin scrive "AMMINISTRATORE", se Operatore calcola LIBERO/IMPEGNATO
        String query = "SELECT u.id_utente, u.nome, u.cognome, u.email, "
                + "CASE "
                + "  WHEN u.ruolo = 'ADMIN' THEN 'AMMINISTRATORE' "
                + "  WHEN EXISTS ("
                + "    SELECT 1 FROM assegnazione_operatori_missione aom "
                + "    JOIN missione mis ON aom.id_missione = mis.id_missione "
                + "    WHERE aom.id_utente = u.id_utente AND mis.stato = 'IN_CORSO'"
                + "  ) THEN 'IMPEGNATO' ELSE 'LIBERO' "
                + "END AS stato_attuale "
                + "FROM utente u "
                + "WHERE u.attivo = 1 "
                + "ORDER BY u.id_utente DESC";

        try (Connection conn = DBManager.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                Map<String, String> operatore = new HashMap<>();
                operatore.put("id_utente", String.valueOf(rs.getInt("id_utente")));
                operatore.put("nome", rs.getString("nome"));
                operatore.put("cognome", rs.getString("cognome"));
                operatore.put("email", rs.getString("email"));
                operatore.put("stato_attuale", rs.getString("stato_attuale"));
                listaOperatori.add(operatore);
            }
        } catch (Exception e) {
            System.err.println("Errore estrazione operatori con stato...");
            e.printStackTrace();
        }
        return listaOperatori;
    }

    // 6. Estrae gli operatori liberi per assegnarli a una missione
    public List<Map<String, String>> estraiOperatoriDisponibili() {
        List<Map<String, String>> listaOperatori = new ArrayList<>();
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
            e.printStackTrace();
        }
        return listaOperatori;
    }

    // 7. Cataloghi per formattare i form HTML (Abilità e Patenti)
    public List<Map<String, String>> estraiTutteLeAbilita() {
        List<Map<String, String>> lista = new ArrayList<>();
        try (Connection conn = DBManager.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT id_abilita, nome FROM abilita"); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("id_abilita", String.valueOf(rs.getInt("id_abilita")));
                m.put("nome", rs.getString("nome"));
                lista.add(m);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public List<Map<String, String>> estraiTutteLePatenti() {
        List<Map<String, String>> lista = new ArrayList<>();
        try (Connection conn = DBManager.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT id_patente, codice FROM patente"); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("id_patente", String.valueOf(rs.getInt("id_patente")));
                m.put("codice", rs.getString("codice"));
                lista.add(m);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}