package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AbilitaDAO {

    public boolean salvaAbilitaEPatentiOperatore(int idOperatore, String[] patenti, String[] abilita) {
        boolean salvataggioOk = false;
        Connection connessioneDb = null;

        try {
            connessioneDb = DBManager.getConnection();
            
            // disabilito l'autocommit per gestire la transazione manualmente
            connessioneDb.setAutoCommit(false);

            // inserimento patenti
            if (patenti != null && patenti.length > 0) {
                String queryInserisciPatente = "INSERT IGNORE INTO patente (codice) VALUES (?)";
                String queryAssociaPatente = "INSERT IGNORE INTO utente_patente (id_utente, id_patente) "
                        + "VALUES (?, (SELECT id_patente FROM patente WHERE codice = ?))";

                try (PreparedStatement stmtInserisci = connessioneDb.prepareStatement(queryInserisciPatente);
                     PreparedStatement stmtAssocia = connessioneDb.prepareStatement(queryAssociaPatente)) {

                    for (String p : patenti) {
                        String pPulita = p.trim().toUpperCase();
                        if (!pPulita.isEmpty()) {
                            stmtInserisci.setString(1, pPulita);
                            stmtInserisci.executeUpdate();

                            stmtAssocia.setInt(1, idOperatore);
                            stmtAssocia.setString(2, pPulita);
                            stmtAssocia.executeUpdate();
                        }
                    }
                }
            }

            // inserimento abilità
            if (abilita != null && abilita.length > 0) {
                String queryInserisciAbilita = "INSERT IGNORE INTO abilita (nome) VALUES (?)";
                String queryAssociaAbilita = "INSERT IGNORE INTO utente_abilita (id_utente, id_abilita) "
                        + "VALUES (?, (SELECT id_abilita FROM abilita WHERE nome = ?))";

                try (PreparedStatement stmtInserisci = connessioneDb.prepareStatement(queryInserisciAbilita);
                     PreparedStatement stmtAssocia = connessioneDb.prepareStatement(queryAssociaAbilita)) {

                    for (String a : abilita) {
                        String aPulita = a.trim().toLowerCase();
                        if (!aPulita.isEmpty()) {
                            stmtInserisci.setString(1, aPulita);
                            stmtInserisci.executeUpdate();

                            stmtAssocia.setInt(1, idOperatore);
                            stmtAssocia.setString(2, aPulita);
                            stmtAssocia.executeUpdate();
                        }
                    }
                }
            }

            // confermo le operazioni sul db
            connessioneDb.commit();
            salvataggioOk = true;

        } catch (Exception e) {
            System.err.println("errore durante il salvataggio: eseguo il rollback");
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
                } catch (Exception exAutoCommit) {
                    exAutoCommit.printStackTrace();
                } finally {
                    // la connessione DEVE tornare al pool in ogni caso
                    try { connessioneDb.close(); } catch (Exception exClose) { exClose.printStackTrace(); }
                }
            }
        }

        return salvataggioOk;
    }

    public String estraiPatentiFormatoStringa(int idOperatore) {
        String patentiCorrenti = "";
        
        // raggruppa le patenti in un'unica stringa separata da virgola
        String query = "SELECT GROUP_CONCAT(p.codice SEPARATOR ', ') AS lista_patenti "
                + "FROM utente_patente up "
                + "JOIN patente p ON up.id_patente = p.id_patente "
                + "WHERE up.id_utente = ?";

        try (Connection conn = DBManager.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idOperatore);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getString("lista_patenti") != null) {
                    patentiCorrenti = rs.getString("lista_patenti");
                }
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione delle patenti");
            e.printStackTrace();
        }

        return patentiCorrenti;
    }

    public String estraiAbilitaFormatoStringa(int idOperatore) {
        String abilitaCorrenti = "";

        // raggruppa le abilità in un'unica stringa separata da virgola
        String query = "SELECT GROUP_CONCAT(a.nome SEPARATOR ', ') AS lista_abilita "
                + "FROM utente_abilita ua "
                + "JOIN abilita a ON ua.id_abilita = a.id_abilita "
                + "WHERE ua.id_utente = ?";

        try (Connection conn = DBManager.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idOperatore);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getString("lista_abilita") != null) {
                    abilitaCorrenti = rs.getString("lista_abilita");
                }
            }
        } catch (Exception e) {
            System.err.println("errore durante l'estrazione delle abilità");
            e.printStackTrace();
        }

        return abilitaCorrenti;
    }
}