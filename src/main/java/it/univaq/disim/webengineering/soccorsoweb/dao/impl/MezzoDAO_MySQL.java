package it.univaq.disim.webengineering.soccorsoweb.dao.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.MezzoDAO;

import it.univaq.disim.webengineering.soccorsoweb.model.Mezzo;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MezzoDAO_MySQL implements MezzoDAO {

    private static final Logger LOGGER = Logger.getLogger(MezzoDAO_MySQL.class.getName());

    // inserisce un nuovo mezzo
    @Override
    public boolean salvaNuovoMezzo(Mezzo nuovoMezzo) {
        String query = "INSERT INTO mezzo (nome, descrizione, attivo) VALUES (?, ?, 1)";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setString(1, nuovoMezzo.getNome());
            statement.setString(2, nuovoMezzo.getDescrizione());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore in inserimento del nuovo mezzo: " + e.getMessage(), e);
        }

        return false;
    }

    // nasconde un mezzo solo se non è impegnato in missione
    @Override
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
            LOGGER.log(Level.SEVERE, "errore durante l'eliminazione del mezzo: " + e.getMessage(), e);
        }

        return false;
    }

    // estrae tutti i mezzi per la dashboard e mostra se sono IMPEGNATI o LIBERI
    @Override
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
            LOGGER.log(Level.SEVERE, "Errore: " + e.getMessage(), e);
        }

        return listaMezzi;
    }

    // trova i mezzi non assegnati a missioni(quindi attivi)
    @Override
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
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca dei mezzi liberi: " + e.getMessage(), e);
        }
        
        return listaMezzi;
    }
    
}