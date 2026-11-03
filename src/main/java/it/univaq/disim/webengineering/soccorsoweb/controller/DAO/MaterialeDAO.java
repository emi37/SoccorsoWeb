package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

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

    public boolean salvaNuovoMateriale(Materiale nuovoMateriale) {
        String query = "INSERT INTO materiale (nome, descrizione, attivo) VALUES (?, ?, 1)";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setString(1, nuovoMateriale.getNome());
            statement.setString(2, nuovoMateriale.getDescrizione());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("errore durante l'inserimento del nuovo materiale: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean nascondiMaterialeSeLibero(int idDaCancellare) {
        // eliminazione del materiale se non è attualmente in uso
        String query = "UPDATE materiale SET attivo = 0 "
                + "WHERE id_materiale = ? AND id_materiale NOT IN ("
                + "    SELECT id_materiale FROM assegnazione_materiale_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE mis.stato = 'IN_CORSO'"
                + ")";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idDaCancellare);
            
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("errore durante l'eliminazione del materiale: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public List<Map<String, String>> estraiTuttiIMaterialiConStato() {
        List<Map<String, String>> listaMateriali = new ArrayList<>();

        // query per trovare se lo stato del materiale è "impegnato" o "libero"
        String query = "SELECT m.id_materiale, m.nome, m.descrizione, "
                + "CASE WHEN EXISTS ("
                + "    SELECT 1 FROM assegnazione_materiale_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE amm.id_materiale = m.id_materiale AND mis.stato = 'IN_CORSO'"
                + ") THEN 'IMPEGNATO' ELSE 'LIBERO' END AS stato_attuale "
                + "FROM materiale m WHERE m.attivo = 1";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query); 
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, String> mappaMateriale = new HashMap<>();
                
                // dati per l'interfaccia di freemarker
                mappaMateriale.put("id_materiale", String.valueOf(resultSet.getInt("id_materiale")));
                mappaMateriale.put("nome", resultSet.getString("nome"));
                mappaMateriale.put("descrizione", resultSet.getString("descrizione"));
                mappaMateriale.put("attivo", resultSet.getString("stato_attuale"));

                listaMateriali.add(mappaMateriale);
            }

        } catch (Exception e) {
            System.err.println("errore durante l'estrazione della lista dei materiali: " + e.getMessage());
            e.printStackTrace();
        }

        return listaMateriali;
    }

    public List<Map<String, String>> estraiMaterialiDisponibili() {
        List<Map<String, String>> listaMateriali = new ArrayList<>();
        
        //trova i materiali che non sono associati a missioni in corso(quindi liberi) 
        String query = "SELECT id_materiale, nome, descrizione "
                + "FROM materiale "
                + "WHERE attivo = 1 "
                + "AND id_materiale NOT IN ( "
                + "    SELECT ama.id_materiale "
                + "    FROM assegnazione_materiale_missione ama "
                + "    JOIN missione m ON ama.id_missione = m.id_missione "
                + "    WHERE m.stato = 'IN_CORSO' "
                + ")";
                       
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Map<String, String> materiale = new HashMap<>();
                materiale.put("id_materiale", resultSet.getString("id_materiale"));
                materiale.put("nome", resultSet.getString("nome"));
                materiale.put("descrizione", resultSet.getString("descrizione"));
                listaMateriali.add(materiale);
            }
            
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dei materiali disponibili: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaMateriali;
    }
    
    public boolean eliminaMateriale(int idMateriale) {
    String query = "DELETE FROM materiale WHERE id_materiale = ?";
    try (Connection connessione = DBManager.getConnection();
         PreparedStatement statement = connessione.prepareStatement(query)) {
        
        statement.setInt(1, idMateriale);
        return statement.executeUpdate() > 0;
        
    } catch (Exception e) {
        System.err.println("Errore durante l'eliminazione del materiale: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
  }
    
}