package it.univaq.disim.webengineering.soccorsoweb.controller.DAO;

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

    // inserisce un nuovo mezzo
    public boolean salvaNuovoMezzo(Mezzo nuovoMezzo) {
        String query = "INSERT INTO mezzo (nome, descrizione, attivo) VALUES (?, ?, 1)";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setString(1, nuovoMezzo.getNome());
            statement.setString(2, nuovoMezzo.getDescrizione());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("errore in inserimento del nuovo mezzo: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // nasconde un mezzo solo se non è impegnato in missione
    public boolean rimuoviMezzoSeLibero(int idDaCancellare) {
                // quando clicco " elimina " in GestioneMezziServlet , non elimino effettivamente il mezzo dal db, 
                // ma non viene semplicemente piu mosrtato nei materiali disponibili nella tabella all'admin
        String query = "UPDATE mezzo SET attivo = 0 " //settando a 0 , non posso + vederlo nei mezzi disponibili
                + "WHERE id_mezzo = ? AND id_mezzo NOT IN ("
                + "    SELECT id_mezzo FROM assegnazione_mezzi_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE mis.stato = 'IN_CORSO'" // se il mezzo è utilizzato in una missione in corso allora non posso eliminarlo
                + ")";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setInt(1, idDaCancellare);
            
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("errore durante l'eliminazione del mezzo: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // estrae tutti i mezzi per la dashboard e mostra se sono IMPEGNATI o LIBERI
    public List<Map<String, String>> estraiTuttiIMezziConStato() {
        List<Map<String, String>> listaMezzi = new ArrayList<>();

        String query = "SELECT m.id_mezzo, m.nome, m.descrizione, "
                + "CASE WHEN EXISTS ("
                + "    SELECT 1 FROM assegnazione_mezzi_missione amm "
                + "    JOIN missione mis ON amm.id_missione = mis.id_missione "
                + "    WHERE amm.id_mezzo = m.id_mezzo AND mis.stato = 'IN_CORSO'" //semplicemente dico che se quell'id mezzo è associato ad una missione, allora dico IMPEGNATO, altrimenti se non lo è, dico che è LIBERO
                + ") THEN 'IMPEGNATO' ELSE 'LIBERO' END AS stato_attuale "
                + "FROM mezzo m WHERE m.attivo = 1"; 

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query); 
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, String> mappaMezzo = new HashMap<>();
                mappaMezzo.put("id_mezzo", String.valueOf(resultSet.getInt("id_mezzo")));
                mappaMezzo.put("nome", resultSet.getString("nome"));
                mappaMezzo.put("descrizione", resultSet.getString("descrizione"));
                mappaMezzo.put("attivo", resultSet.getString("stato_attuale"));

                listaMezzi.add(mappaMezzo);
            }

        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }

        return listaMezzi;
    }

    // trova i mezzi non assegnati a missioni(quindi attivi)
    public List<Map<String, String>> estraiMezziDisponibili() {
        List<Map<String, String>> listaMezzi = new ArrayList<>();
        
        String query = "SELECT id_mezzo, nome, descrizione "
                + "FROM mezzo "
                + "WHERE attivo = 1 "
                + "AND id_mezzo NOT IN ( "
                + "    SELECT amm.id_mezzo "
                + "    FROM assegnazione_mezzi_missione amm "
                + "    JOIN missione m ON amm.id_missione = m.id_missione "
                + "    WHERE m.stato = 'IN_CORSO' "
                + ")";
                       
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Map<String, String> mezzo = new HashMap<>();
                mezzo.put("id_mezzo", resultSet.getString("id_mezzo"));
                mezzo.put("nome", resultSet.getString("nome"));
                mezzo.put("descrizione", resultSet.getString("descrizione"));
                listaMezzi.add(mezzo);
            }
            
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dei mezzi liberi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaMezzi;
    }
    
}