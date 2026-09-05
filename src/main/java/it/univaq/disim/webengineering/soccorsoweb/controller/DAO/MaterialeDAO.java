/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

/**
 *
 * @author edoar
 */
import it.univaq.disim.webengineering.soccorsoweb.model.Materiale;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialeDAO {

    // Salva un nuovo materiale (sostituisce la logica di AggiungiMaterialeServlet)
    public boolean salvaNuovoMateriale(Materiale nuovoMateriale) {
        boolean salvataggioRiuscito = false;

        // Spezzo la query a mano per tenerla pulita
        String queryInserimento = "INSERT INTO materiale "
                + "(nome, descrizione, attivo) "
                + "VALUES (?, ?, 1)";

        // Try-with-resources per chiudere tutto da solo, come da slide
        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementInserimento = connessioneDb.prepareStatement(queryInserimento)) {

            // Bindo i parametri estraendoli dall'entità
            statementInserimento.setString(1, nuovoMateriale.getNome());
            statementInserimento.setString(2, nuovoMateriale.getDescrizione());

            // Lancio l'update sul db
            int righeAffette = statementInserimento.executeUpdate();
            if (righeAffette > 0) {
                salvataggioRiuscito = true;
            }

        } catch (Exception e) {
            // Gestione errore ruspante a console
            System.err.println("Errore catastrofico durante l'inserimento del nuovo materiale...");
            e.printStackTrace();
        }

        return salvataggioRiuscito;
    }

    // Soft-delete: nasconde il materiale se non è a bordo di un mezzo impegnato
    public boolean nascondiMaterialeSeLibero(int idDaCancellare) {
        boolean eliminazioneOk = false;

        String queryCancellazione = "UPDATE materiale SET attivo = 0 "
                + "WHERE id_materiale = ? AND id_materiale NOT IN ("
                + "    SELECT id_materiale FROM assegnazione_materiale_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE mis.stato = 'IN_CORSO'"
                + ")";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementCanc = connessioneDb.prepareStatement(queryCancellazione)) {

            statementCanc.setInt(1, idDaCancellare);
            int righeModificate = statementCanc.executeUpdate();

            if (righeModificate > 0) {
                eliminazioneOk = true;
            }

        } catch (Exception e) {
            System.err.println("Disastro durante l'eliminazione logica del materiale...");
            e.printStackTrace();
        }

        return eliminazioneOk;
    }

    // Estrae tutti i materiali calcolando al volo se sono IMPEGNATI (sostituisce GestioneMaterialiServlet)
    public List<Map<String, String>> estraiTuttiIMaterialiConStato() {
        List<Map<String, String>> listaMateriali = new ArrayList<>();

        String queryLista = "SELECT m.id_materiale, m.nome, m.descrizione, "
                + "CASE WHEN EXISTS ("
                + "    SELECT 1 FROM assegnazione_materiale_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE amm.id_materiale = m.id_materiale AND mis.stato = 'IN_CORSO'"
                + ") THEN 'IMPEGNATO' ELSE 'LIBERO' END AS stato_attuale "
                + "FROM materiale m WHERE m.attivo = 1";

        try (Connection connessioneDb = DBManager.getConnection(); PreparedStatement statementLista = connessioneDb.prepareStatement(queryLista); ResultSet risultatiLista = statementLista.executeQuery()) {

            // Frulliamo i risultati
            while (risultatiLista.next()) {
                Map<String, String> mappaMateriale = new HashMap<>();
                mappaMateriale.put("id", String.valueOf(risultatiLista.getInt("id_materiale")));
                mappaMateriale.put("nome", risultatiLista.getString("nome"));
                mappaMateriale.put("descrizione", risultatiLista.getString("descrizione"));
                mappaMateriale.put("stato", risultatiLista.getString("stato_attuale"));

                listaMateriali.add(mappaMateriale);
            }

        } catch (Exception e) {
            System.err.println("Errore brutto durante il fetch dei materiali...");
            e.printStackTrace();
        }

        return listaMateriali;
    }
    // Trova i materiali attivi e non usati in missioni in corso
    public List<Map<String, String>> estraiMaterialiDisponibili() {
        List<Map<String, String>> listaMateriali = new ArrayList<>();
        
        String query = "SELECT id_materiale, nome, descrizione " +
                       "FROM materiale " +
                       "WHERE attivo = 1 " +
                       "AND id_materiale NOT IN ( " +
                       "    SELECT ama.id_materiale " +
                       "    FROM assegnazione_materiale_missione ama " +
                       "    JOIN missione m ON ama.id_missione = m.id_missione " +
                       "    WHERE m.stato = 'IN_CORSO' " +
                       ")";
                       
        try (Connection connessioneDb = DBManager.getConnection();
             PreparedStatement statement = connessioneDb.prepareStatement(query);
             ResultSet risultati = statement.executeQuery()) {
            
            while (risultati.next()) {
                Map<String, String> materiale = new HashMap<>();
                materiale.put("id_materiale", risultati.getString("id_materiale"));
                materiale.put("nome", risultati.getString("nome"));
                materiale.put("descrizione", risultati.getString("descrizione"));
                listaMateriali.add(materiale);
            }
        } catch (Exception e) {
            System.err.println("Errore brutto durante la ricerca dei materiali disponibili...");
            e.printStackTrace();
        }
        return listaMateriali;
    }
}
