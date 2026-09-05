/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

/**
 *
 * @author edoar
 */
import it.univaq.disim.webengineering.soccorsoweb.model.Mezzo;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MezzoDAO {

    // Inserisce un nuovo mezzo in flotta
    public boolean salvaNuovoMezzo(Mezzo nuovoMezzo) {
        boolean salvataggioOk = false;

        String queryAggiunta = "INSERT INTO mezzo "
                + "(nome, descrizione, attivo) "
                + "VALUES (?, ?, 1)";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementAggiunta = connessioneDb.prepareStatement(queryAggiunta)) {

            statementAggiunta.setString(1, nuovoMezzo.getNome());
            statementAggiunta.setString(2, nuovoMezzo.getDescrizione());

            int righeInserite = statementAggiunta.executeUpdate();
            if (righeInserite > 0) {
                salvataggioOk = true;
            }

        } catch (Exception e) {
            System.err.println("Errore in inserimento del nuovo mezzo a db...");
            e.printStackTrace();
        }

        return salvataggioOk;
    }

    // Nasconde un mezzo (soft-delete) solo se non è impegnato in missione
    public boolean rimuoviMezzoSeLibero(int idDaCancellare) {
        boolean eliminazioneOk = false;

        String queryCancellazione = "UPDATE mezzo SET attivo = 0 "
                + "WHERE id_mezzo = ? AND id_mezzo NOT IN ("
                + "    SELECT id_mezzo FROM assegnazione_mezzi_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE mis.stato = 'IN_CORSO'"
                + ")";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementCancellazione = connessioneDb.prepareStatement(queryCancellazione)) {

            statementCancellazione.setInt(1, idDaCancellare);
            int righeNascoste = statementCancellazione.executeUpdate();

            if (righeNascoste > 0) {
                eliminazioneOk = true;
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione logica del mezzo...");
            e.printStackTrace();
        }

        return eliminazioneOk;
    }

    // Estrae tutti i mezzi per la dashboard, calcolando al volo se sono IMPEGNATI o LIBERI
    public List<Map<String, String>> estraiTuttiIMezziConStato() {
        List<Map<String, String>> listaMezzi = new ArrayList<>();

        String queryLista = "SELECT m.id_mezzo, m.nome, m.descrizione, "
                + "CASE WHEN EXISTS ("
                + "    SELECT 1 FROM assegnazione_mezzi_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE amm.id_mezzo = m.id_mezzo AND mis.stato = 'IN_CORSO'"
                + ") THEN 'IMPEGNATO' ELSE 'LIBERO' END AS stato_attuale "
                + "FROM mezzo m WHERE m.attivo = 1";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementLista = connessioneDb.prepareStatement(queryLista); ResultSet risultatiLista = statementLista.executeQuery()) {

            while (risultatiLista.next()) {
                // Impacchetto i dati in una mappa per passarla comodamente alla JSP/FreeMarker
                Map<String, String> mappaMezzo = new HashMap<>();
                mappaMezzo.put("id", String.valueOf(risultatiLista.getInt("id_mezzo")));
                mappaMezzo.put("nome", risultatiLista.getString("nome"));
                mappaMezzo.put("descrizione", risultatiLista.getString("descrizione"));
                mappaMezzo.put("stato", risultatiLista.getString("stato_attuale"));

                listaMezzi.add(mappaMezzo);
            }

        } catch (Exception e) {
            System.err.println("Si è rotto qualcosa mentre fetchavo i mezzi dal DB...");
            e.printStackTrace();
        }

        return listaMezzi;
    }
}
