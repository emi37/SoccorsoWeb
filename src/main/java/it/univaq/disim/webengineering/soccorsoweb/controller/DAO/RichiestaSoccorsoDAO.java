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

    // 1. Inserisce una nuova richiesta IN_ATTESA (Serve per il form pubblico)
    public boolean salvaNuovaRichiesta(RichiestaSoccorso richiesta) {
        boolean salvataggioOk = false;
        String query = "INSERT INTO richiesta_soccorso "
                + "(nome_segnalante, email_segnalante, posizione, descrizione, ip_origine, token_convalida, stato, timestamp_creazione) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'ATTIVA', NOW())";

        try (Connection conn = DBManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, richiesta.getNomeSegnalante());
            stmt.setString(2, richiesta.getEmailSegnalante());
            stmt.setString(3, richiesta.getPosizione());
            stmt.setString(4, richiesta.getDescrizione());
            stmt.setString(5, richiesta.getIpOrigine());
            stmt.setString(6, richiesta.getTokenConvalida());

            int righeInserite = stmt.executeUpdate();
            if (righeInserite > 0) {
                salvataggioOk = true;
            }
        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio della richiesta pubblica...");
            e.printStackTrace();
        }
        return salvataggioOk;
    }

    // 2. Convalida il token e attiva la richiesta (Il tuo metodo con scadenza 10 min)
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

    // 3. Estrae le richieste 'ATTIVE' per popolare la dashboard Admin
    public List<Map<String, String>> estraiRichiesteAttive() {
        List<Map<String, String>> listaRichieste = new ArrayList<>();
        String query = "SELECT id_richiesta, nome_segnalante, email_segnalante, posizione, descrizione "
                + "FROM richiesta_soccorso WHERE stato = 'ATTIVA' ORDER BY timestamp_convalida ASC";

        try (Connection conn = DBManager.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, String> mappaRichiesta = new HashMap<>();
                mappaRichiesta.put("id", String.valueOf(rs.getInt("id_richiesta")));
                mappaRichiesta.put("segnalante", rs.getString("nome_segnalante"));
                mappaRichiesta.put("email", rs.getString("email_segnalante"));
                mappaRichiesta.put("posizione", rs.getString("posizione"));
                mappaRichiesta.put("descrizione", rs.getString("descrizione"));
                listaRichieste.add(mappaRichiesta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaRichieste;
    }

    // 4. Scarta la richiesta (Il tuo metodo)
    public boolean aggiornaStatoRichiestaInIgnorata(int idRichiestaDaScartare) {
        boolean operazioneRiuscita = false;
        String queryAggiornamento = "UPDATE richiesta_soccorso SET stato = 'IGNORATA' WHERE id_richiesta = ?";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementUpdate = connessioneDb.prepareStatement(queryAggiornamento)) {
            statementUpdate.setInt(1, idRichiestaDaScartare);
            int righeAggiornate = statementUpdate.executeUpdate();
            if (righeAggiornate > 0) {
                operazioneRiuscita = true;
            }
        } catch (Exception e) {
            System.err.println("Panico: Errore durante l'aggiornamento dello stato (IGNORATA)...");
            e.printStackTrace();
        }
        return operazioneRiuscita;
    }

    // 5. Estrae i dettagli specifici (Corretto nome colonna in timestamp_creazione)
    public Map<String, String> estraiDettagliRichiesta(int idRichiesta) {
        Map<String, String> dettagli = new HashMap<>();
        String query = "SELECT nome_segnalante, posizione, descrizione, timestamp_creazione " +
                       "FROM richiesta_soccorso WHERE id_richiesta = ?";
                       
        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statement = connessioneDb.prepareStatement(query)) {
            statement.setInt(1, idRichiesta);
            try (ResultSet risultati = statement.executeQuery()) {
                if (risultati.next()) {
                    dettagli.put("id_richiesta", String.valueOf(idRichiesta));
                    dettagli.put("nome", risultati.getString("nome_segnalante"));
                    dettagli.put("posizione", risultati.getString("posizione"));
                    dettagli.put("descrizione", risultati.getString("descrizione"));
                    dettagli.put("data_ora", risultati.getTimestamp("timestamp_creazione").toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'estrazione dei dettagli della richiesta...");
            e.printStackTrace();
        }
        return dettagli;
    }
}