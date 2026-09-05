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
import java.util.HashMap;
import java.util.Map;

public class RichiestaSoccorsoDAO {

    // Questo metodo va a rimpiazzare il mappazzone SQL dentro IgnoraRichiestaServlet
    public boolean aggiornaStatoRichiestaInIgnorata(int idRichiestaDaScartare) {
        boolean operazioneRiuscita = false;

        // Assembliamo la query in modo ordinato
        String queryAggiornamento = "UPDATE richiesta_soccorso "
                + "SET stato = 'IGNORATA' "
                + "WHERE id_richiesta = ?";

        // Connessione presa dalla nostra classe centralizzata
        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementUpdate = connessioneDb.prepareStatement(queryAggiornamento)) {

            // Bindo il parametro per evitare SQL injection
            statementUpdate.setInt(1, idRichiestaDaScartare);

            // Eseguo e conto quante righe sono state colpite
            int righeAggiornate = statementUpdate.executeUpdate();
            if (righeAggiornate > 0) {
                operazioneRiuscita = true;
            }

        } catch (Exception e) {
            // Classico catch da università: stampo l'errore e non blocco tutto
            System.err.println("Panico: Errore durante l'aggiornamento dello stato (IGNORATA)...");
            e.printStackTrace();
        }

        return operazioneRiuscita;
    }

    // Metodo per cancellare i token usati (potrebbe servirti dopo la convalida)
    public boolean svuotaTokenConvalida(String tokenUsato) {
        boolean tokenSvuotato = false;

        String queryPulizia = "UPDATE richiesta_soccorso "
                + "SET stato = 'ATTIVA', "
                + "    token_convalida = NULL, "
                + "    timestamp_convalida = CURRENT_TIMESTAMP "
                + "WHERE token_convalida = ? "
                + "  AND stato = 'IN_ATTESA' "
                + "  AND timestamp_creazione >= NOW() - INTERVAL 10 MINUTE";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementPulizia = connessioneDb.prepareStatement(queryPulizia)) {

            statementPulizia.setString(1, tokenUsato);

            int affectedRows = statementPulizia.executeUpdate();
            if (affectedRows > 0) {
                tokenSvuotato = true;
            }

        } catch (Exception e) {
            System.err.println("Errore db durante la pulizia del token convalida...");
            e.printStackTrace();
        }

        return tokenSvuotato;
    }
    // Estrae i dettagli di una specifica richiesta per mostrarla all'admin
    public Map<String, String> estraiDettagliRichiesta(int idRichiesta) {
        Map<String, String> dettagli = new HashMap<>();
        
        String query = "SELECT nome_segnalante, posizione, descrizione, timestamp_segnalazione " +
                       "FROM richiesta_soccorso " +
                       "WHERE id_richiesta = ?";
                       
        try (Connection connessioneDb = DBManager.getConnection();
             PreparedStatement statement = connessioneDb.prepareStatement(query)) {
            
            statement.setInt(1, idRichiesta);
            
            try (ResultSet risultati = statement.executeQuery()) {
                if (risultati.next()) {
                    dettagli.put("id_richiesta", String.valueOf(idRichiesta));
                    dettagli.put("nome", risultati.getString("nome_segnalante"));
                    dettagli.put("posizione", risultati.getString("posizione"));
                    dettagli.put("descrizione", risultati.getString("descrizione"));
                    dettagli.put("data_ora", risultati.getTimestamp("timestamp_segnalazione").toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'estrazione dei dettagli della richiesta...");
            e.printStackTrace();
        }
        return dettagli;
    }
}
