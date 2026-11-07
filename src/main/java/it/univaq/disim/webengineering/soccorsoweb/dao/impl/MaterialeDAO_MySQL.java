package it.univaq.disim.webengineering.soccorsoweb.dao.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.MaterialeDAO;

import it.univaq.disim.webengineering.soccorsoweb.model.Materiale;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialeDAO_MySQL implements MaterialeDAO {

    private static final Logger LOGGER = Logger.getLogger(MaterialeDAO_MySQL.class.getName());

    @Override
    public boolean salvaNuovoMateriale(Materiale nuovoMateriale) {
        String query = "INSERT INTO materiale (nome, descrizione, attivo) VALUES (?, ?, 1)";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setString(1, nuovoMateriale.getNome());
            statement.setString(2, nuovoMateriale.getDescrizione());

            return statement.executeUpdate() > 0; // se nel db viene inserito 1 nuovo materiale(quindi ...> 0) torna true, altrimenti false

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'inserimento del nuovo materiale: " + e.getMessage(), e);
        }

        return false;
    }

    @Override
    public boolean nascondiMaterialeSeLibero(int idDaCancellare) {
        // quando clicco " elimina " in GestioneMaterialiServlet , non elimino effettivamente il materiale dal db,
        // ma non viene semplicemente piu mostrato nei materiali disponibili nella tabella all'admin
        String query = "UPDATE materiale SET attivo = 0 "  //settando a 0 , non posso + vederlo nei materiali disponibili
                + " WHERE id_materiale = ? AND id_materiale NOT IN ("
                + "    SELECT id_materiale FROM assegnazione_materiale_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE mis.stato = 'IN_CORSO'" // se il materiale è utilizzato in una missione in corso allora non posso eliminarlo
                + ")";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idDaCancellare);
            
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'eliminazione del materiale: " + e.getMessage(), e);
        }

        return false;
    }

    @Override
    public List<Map<String, String>> estraiTuttiIMaterialiConStato() {
        List<Map<String, String>> listaMateriali = new ArrayList<>();

        // query per trovare se lo stato del materiale è "impegnato" o "libero"
        String query = "SELECT m.id_materiale, m.nome, m.descrizione, "
                + "CASE WHEN EXISTS ("
                + "    SELECT 1 FROM assegnazione_materiale_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE amm.id_materiale = m.id_materiale AND mis.stato = 'IN_CORSO'"  //semplicemente dico che se quell'id materiale è associato ad una missione, allora dico IMPEGNATO, altrimenti se non lo è, dico che è LIBERO
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
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione della lista dei materiali: " + e.getMessage(), e);
        }

        return listaMateriali;
    }

    @Override
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
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca dei materiali disponibili: " + e.getMessage(), e);
        }
        
        return listaMateriali;
    }

    @Override
    public boolean modificaMateriale(int id, String nome, String descrizione) {
        String query = "UPDATE materiale SET nome = ?, descrizione = ? WHERE id_materiale = ? AND attivo = 1";
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {
            statement.setString(1, nome);
            statement.setString(2, descrizione);
            statement.setInt(3, id);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante la modifica (materiale)", e);
        }
        return false;
    }

}
