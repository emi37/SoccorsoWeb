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
import java.sql.Statement;

public class AssegnazioneOperatoreMissioneDAO {

    // Questo mostro gestisce l'intera POST della GestioneRichiestaServletDallAdmin
    public boolean avviaMissioneConRisorse(int idRichiesta, String[] operatoriScelti, String caposquadraScelto, String[] mezziScelti, String[] materialiScelti) {
        boolean avvioRiuscito = false;
        Connection connessioneDb = null;

        try {
            connessioneDb = DBManager.getConnection();

            // Stacchiamo l'autocommit: se salta una singola assegnazione, annulliamo tutto
            connessioneDb.setAutoCommit(false);

            // 1. Pesco i dati originali della richiesta per travasarli nella missione
            String posizioneReale = "ND";
            String descrizioneReale = "Emergenza";

            String queryGetRichiesta = "SELECT posizione, descrizione "
                    + "FROM richiesta_soccorso "
                    + "WHERE id_richiesta = ?";

            try (PreparedStatement stmtGet = connessioneDb.prepareStatement(queryGetRichiesta)) {
                stmtGet.setInt(1, idRichiesta);
                try (ResultSet risultati = stmtGet.executeQuery()) {
                    if (risultati.next()) {
                        posizioneReale = risultati.getString("posizione");
                        descrizioneReale = risultati.getString("descrizione");
                    }
                }
            }

            // 2. Aggiorno la richiesta mettendola in corso
            String queryUpdateRichiesta = "UPDATE richiesta_soccorso "
                    + "SET stato = 'IN_CORSO' "
                    + "WHERE id_richiesta = ?";
            try (PreparedStatement stmtUpdateReq = connessioneDb.prepareStatement(queryUpdateRichiesta)) {
                stmtUpdateReq.setInt(1, idRichiesta);
                stmtUpdateReq.executeUpdate();
            }

            // 3. Creo la missione vera e propria e mi faccio restituire l'ID generato
            int idMissioneGenerata = 0;
            String queryInsertMissione = "INSERT INTO missione "
                    + "(id_richiesta, obiettivo, posizione, stato) "
                    + "VALUES (?, ?, ?, 'IN_CORSO')";

            // Passo RETURN_GENERATED_KEYS per farmi ridare l'ID appena creato dal DB
            try (PreparedStatement stmtInsertMis = connessioneDb.prepareStatement(queryInsertMissione, Statement.RETURN_GENERATED_KEYS)) {
                stmtInsertMis.setInt(1, idRichiesta);
                stmtInsertMis.setString(2, descrizioneReale);
                stmtInsertMis.setString(3, posizioneReale);
                stmtInsertMis.executeUpdate();

                try (ResultSet chiaviGenerate = stmtInsertMis.getGeneratedKeys()) {
                    if (chiaviGenerate.next()) {
                        idMissioneGenerata = chiaviGenerate.getInt(1);
                    }
                }
            }

            // 4. Se la missione è nata, ci schiaffo dentro gli operatori (Assegnazione Operatori)
            if (operatoriScelti != null && idMissioneGenerata > 0) {
                String queryAssociaOp = "INSERT INTO assegnazione_operatori_missioni "
                        + "(id_missione, id_utente, is_caposquadra) "
                        + "VALUES (?, ?, ?)";

                try (PreparedStatement stmtOp = connessioneDb.prepareStatement(queryAssociaOp)) {
                    for (String idOpStringa : operatoriScelti) {
                        int isCapo = (idOpStringa.equals(caposquadraScelto)) ? 1 : 0;
                        stmtOp.setInt(1, idMissioneGenerata);
                        stmtOp.setInt(2, Integer.parseInt(idOpStringa));
                        stmtOp.setInt(3, isCapo);
                        stmtOp.executeUpdate();
                    }
                }
            }

            // 5. Assegno i mezzi di soccorso (Assegnazione Mezzi)
            if (mezziScelti != null && idMissioneGenerata > 0) {
                String queryAssociaMezzi = "INSERT INTO assegnazione_mezzi_missione "
                        + "(id_missione, id_mezzo) "
                        + "VALUES (?, ?)";

                try (PreparedStatement stmtMezzo = connessioneDb.prepareStatement(queryAssociaMezzi)) {
                    for (String idMezzoStr : mezziScelti) {
                        stmtMezzo.setInt(1, idMissioneGenerata);
                        stmtMezzo.setInt(2, Integer.parseInt(idMezzoStr));
                        stmtMezzo.executeUpdate();
                    }
                }
            }

            // 6. Assegno i materiali (Assegnazione Materiali)
            if (materialiScelti != null && idMissioneGenerata > 0) {
                String queryAssociaMat = "INSERT INTO assegnazione_materiale_missione "
                        + "(id_missione, id_materiale) "
                        + "VALUES (?, ?)";

                try (PreparedStatement stmtMat = connessioneDb.prepareStatement(queryAssociaMat)) {
                    for (String idMatStr : materialiScelti) {
                        stmtMat.setInt(1, idMissioneGenerata);
                        stmtMat.setInt(2, Integer.parseInt(idMatStr));
                        stmtMat.executeUpdate();
                    }
                }
            }

            // Se arrivo vivo fin qui, salvo tutto definitivamente sul db
            connessioneDb.commit();
            avvioRiuscito = true;

        } catch (Exception e) {
            System.err.println("Errore catastrofico durante l'avvio della missione. Faccio rollback immediato!");
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
                    connessioneDb.close();
                } catch (Exception exClose) {
                    exClose.printStackTrace();
                }
            }
        }

        return avvioRiuscito;
    }
}
