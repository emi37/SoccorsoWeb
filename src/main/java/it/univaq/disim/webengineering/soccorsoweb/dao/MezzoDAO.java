package it.univaq.disim.webengineering.soccorsoweb.dao;

import it.univaq.disim.webengineering.soccorsoweb.model.Mezzo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Contratto di accesso ai dati per Mezzo. L'implementazione per MySQL e' in dao.impl.MezzoDAO_MySQL. */
public interface MezzoDAO {

    // inserisce un nuovo mezzo
    boolean salvaNuovoMezzo(Mezzo nuovoMezzo);

    // nasconde un mezzo solo se non è impegnato in missione
    boolean rimuoviMezzoSeLibero(int idDaCancellare);

    // estrae tutti i mezzi per la dashboard e mostra se sono IMPEGNATI o LIBERI
    List<Map<String, String>> estraiTuttiIMezziConStato();

    // trova i mezzi non assegnati a missioni(quindi attivi)
    List<Map<String, String>> estraiMezziDisponibili();

    // aggiorna nome e descrizione di un elemento ancora attivo
    boolean modificaMezzo(int id, String nome, String descrizione);

}
