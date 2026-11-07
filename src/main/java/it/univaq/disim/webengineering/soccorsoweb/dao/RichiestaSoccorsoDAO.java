package it.univaq.disim.webengineering.soccorsoweb.dao;

import it.univaq.disim.webengineering.soccorsoweb.model.RichiestaSoccorso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Contratto di accesso ai dati per RichiestaSoccorso. L'implementazione per MySQL e' in dao.impl.RichiestaSoccorsoDAO_MySQL. */
public interface RichiestaSoccorsoDAO {

    // inserisce una nuova richiesta in stato IN_ATTESA dal form di rchiesta
    boolean salvaNuovaRichiesta(RichiestaSoccorso richiesta);

    // anti-spam: true se la stessa email (o lo stesso IP) ha già inviato una richiesta negli ultimi N minuti
    boolean esisteRichiestaRecente(String email, String ipOrigine, int minuti);

    // convalida il token via email e attiva la richiesta (se non convalidi entro 10 min la richiesta rimane in stato di attesa sul DB)
    boolean svuotaTokenConvalida(String tokenUsato);

    // estrae le richieste con lo stato "ATTIVA" (viene richiamato dalla dashboard dell'admin)
    List< Map<String, String> > estraiRichiesteAttive();

    // scarta la richiesta impostandogli lo stato sul DB in "IGNORATA "
    boolean aggiornaStatoRichiestaInIgnorata(int idRichiestaDaScartare);

    // estrae i dettagli di una richiesta(la richieta estratta è quella con id_richiesta = ?)
    Map< String, String > estraiDettagliRichiesta(int idRichiesta);

}
