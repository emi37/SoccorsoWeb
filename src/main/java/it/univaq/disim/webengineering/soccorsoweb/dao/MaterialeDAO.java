package it.univaq.disim.webengineering.soccorsoweb.dao;

import it.univaq.disim.webengineering.soccorsoweb.model.Materiale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Contratto di accesso ai dati per Materiale. L'implementazione per MySQL e' in dao.impl.MaterialeDAO_MySQL. */
public interface MaterialeDAO {

    boolean salvaNuovoMateriale(Materiale nuovoMateriale);

    boolean nascondiMaterialeSeLibero(int idDaCancellare);

    List<Map<String, String>> estraiTuttiIMaterialiConStato();

    List<Map<String, String>> estraiMaterialiDisponibili();

    // aggiorna nome e descrizione di un elemento ancora attivo
    boolean modificaMateriale(int id, String nome, String descrizione);

}
