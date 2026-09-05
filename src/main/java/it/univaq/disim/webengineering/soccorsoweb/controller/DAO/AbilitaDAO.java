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

public class AbilitaDAO {

    // Sostituisce il blocco POST della DashboardOperatoreServlet
    public boolean salvaAbilitaEPatentiOperatore(int idOperatore, String[] patenti, String[] abilita) {
        boolean salvataggioOk = false;
        Connection connessioneDb = null;

        try {
            // uso il metodo originale del tuo DBManager
            connessioneDb = DBManager.getConnection();

            // stacco l'autocommit per gestire la transazione manualmente
            connessioneDb.setAutoCommit(false);

            // 1. Inserimento patenti
            if (patenti != null && patenti.length > 0) {
                for (String singolaPatente : patenti) {
                    String patentePulita = singolaPatente.trim().toUpperCase();

                    if (!patentePulita.isEmpty()) {
                        String queryInserisciPatente = "INSERT IGNORE INTO patente (codice) VALUES (?)";
                        try (PreparedStatement statementPatente = connessioneDb.prepareStatement(queryInserisciPatente)) {
                            statementPatente.setString(1, patentePulita);
                            statementPatente.executeUpdate();
                        }

                        String queryAssociaPatente = "INSERT IGNORE INTO utente_patente (id_utente, id_patente) "
                                + "VALUES (?, (SELECT id_patente FROM patente WHERE codice = ?))";
                        try (PreparedStatement statementAssocia = connessioneDb.prepareStatement(queryAssociaPatente)) {
                            statementAssocia.setInt(1, idOperatore);
                            statementAssocia.setString(2, patentePulita);
                            statementAssocia.executeUpdate();
                        }
                    }
                }
            }

            // 2. Inserimento abilità
            if (abilita != null && abilita.length > 0) {
                for (String singolaAbilita : abilita) {
                    String abilitaPulita = singolaAbilita.trim().toLowerCase();

                    if (!abilitaPulita.isEmpty()) {
                        String queryInserisciAbilita = "INSERT IGNORE INTO abilita (nome) VALUES (?)";
                        try (PreparedStatement statementAbilita = connessioneDb.prepareStatement(queryInserisciAbilita)) {
                            statementAbilita.setString(1, abilitaPulita);
                            statementAbilita.executeUpdate();
                        }

                        String queryAssociaAbilita = "INSERT IGNORE INTO utente_abilita (id_utente, id_abilita) "
                                + "VALUES (?, (SELECT id_abilita FROM abilita WHERE nome = ?))";
                        try (PreparedStatement statementAssocia = connessioneDb.prepareStatement(queryAssociaAbilita)) {
                            statementAssocia.setInt(1, idOperatore);
                            statementAssocia.setString(2, abilitaPulita);
                            statementAssocia.executeUpdate();
                        }
                    }
                }
            }

            // tutto liscio, confermo le scritture sul db
            connessioneDb.commit();
            salvataggioOk = true;

        } catch (Exception e) {
            // becco l'errore e faccio rollback per salvare l'integrità del db
            System.err.println("Errore durante il salvataggio di patenti e abilità. Faccio rollback!");
            e.printStackTrace();
            if (connessioneDb != null) {
                try {
                    connessioneDb.rollback();
                } catch (Exception exRollback) {
                    exRollback.printStackTrace();
                }
            }
        } finally {
            // ripristino sempre l'autocommit a true prima di rilasciare la connessione
            if (connessioneDb != null) {
                try {
                    connessioneDb.setAutoCommit(true);
                    connessioneDb.close();
                } catch (Exception exClose) {
                    exClose.printStackTrace();
                }
            }
        }

        return salvataggioOk;
    }
}
