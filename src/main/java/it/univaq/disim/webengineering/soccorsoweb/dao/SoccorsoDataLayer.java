package it.univaq.disim.webengineering.soccorsoweb.dao;

import it.univaq.disim.webengineering.soccorsoweb.dao.impl.AggiornamentoMissioneDAO_MySQL;
import it.univaq.disim.webengineering.soccorsoweb.dao.impl.MaterialeDAO_MySQL;
import it.univaq.disim.webengineering.soccorsoweb.dao.impl.MezzoDAO_MySQL;
import it.univaq.disim.webengineering.soccorsoweb.dao.impl.MissioneDAO_MySQL;
import it.univaq.disim.webengineering.soccorsoweb.dao.impl.RichiestaSoccorsoDAO_MySQL;
import it.univaq.disim.webengineering.soccorsoweb.dao.impl.UtenteDAO_MySQL;

/**
 * Punto unico da cui le servlet ottengono i DAO (come il DataLayer degli esempi del corso).
 *
 * Le servlet dipendono solo dalle INTERFACCE dei DAO: quale implementazione (MySQL o altra)
 * viene usata si decide qui, in un solo posto. I DAO non hanno stato (ogni metodo preleva
 * la sua connessione dal pool e la restituisce), quindi un'unica istanza condivisa da tutte
 * le richieste è sicura.
 */
public final class SoccorsoDataLayer {

    private static final SoccorsoDataLayer ISTANZA = new SoccorsoDataLayer();

    private final AggiornamentoMissioneDAO aggiornamentoMissioneDAO = new AggiornamentoMissioneDAO_MySQL();
    private final MaterialeDAO materialeDAO = new MaterialeDAO_MySQL();
    private final MezzoDAO mezzoDAO = new MezzoDAO_MySQL();
    private final MissioneDAO missioneDAO = new MissioneDAO_MySQL();
    private final RichiestaSoccorsoDAO richiestaSoccorsoDAO = new RichiestaSoccorsoDAO_MySQL();
    private final UtenteDAO utenteDAO = new UtenteDAO_MySQL();

    private SoccorsoDataLayer() {
    }

    public static SoccorsoDataLayer getInstance() {
        return ISTANZA;
    }

    public AggiornamentoMissioneDAO getAggiornamentoMissioneDAO() {
        return aggiornamentoMissioneDAO;
    }

    public MaterialeDAO getMaterialeDAO() {
        return materialeDAO;
    }

    public MezzoDAO getMezzoDAO() {
        return mezzoDAO;
    }

    public MissioneDAO getMissioneDAO() {
        return missioneDAO;
    }

    public RichiestaSoccorsoDAO getRichiestaSoccorsoDAO() {
        return richiestaSoccorsoDAO;
    }

    public UtenteDAO getUtenteDAO() {
        return utenteDAO;
    }
}
