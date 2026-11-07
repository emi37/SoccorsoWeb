package it.univaq.disim.webengineering.soccorsoweb.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Contratto di accesso ai dati per Missione. L'implementazione per MySQL e' in dao.impl.MissioneDAO_MySQL. */
public interface MissioneDAO {

    List< Map<String, String> > estraiMissioniInCorso();

    int creaMissioneInTransazione(int idRichiesta, String obiettivo, String posizione, int idCaposquadra, String[] idOperatori, String[] idMezzi, String[] idMateriali, int idAdminLoggato);

    List<Map<String, String>> estraiStoricoMissioni();

    List<Map<String, String>> estraiMissioniPerOperatore(int idOperatoreCercato);

    //qua ancziche' passare i parametri modoficati tramite i java bean, li passo direttamente dal metodo questo perche' vado a modificare 3 tabelle diverse sul DB missione , richiesta_soccorso , aggiornamento_missione
    //quindi anziche fare new nella servlet di tre oggetti diversi per passare 5 parametri, li passo direttamente qui nel metodo per comodità e tenere il codice+pulito
    boolean chiudiMissioneInTransazione(int idMissione, int livelloSuccesso, String commenti, int idAdminLoggato);

    String estraiStatoMissione(int idMissione);

    Map<String, Object> estraiDettagliCompleti(int idMissione);

    List<Map<String, String>> estraiMissioniAttivePerOperatore(long idOperatore);

    boolean modificaMissioneInCorso(int idMissione, String nuovoObiettivo, String nuovaPosizione, String[] nuoviOperatori, String[] nuoviMezzi, String[] nuoviMateriali);

    // true se l'operatore fa parte della squadra della missione
    boolean operatoreCoinvoltoInMissione(int idMissione, int idUtente);

    // nome, cognome, email di tutti gli operatori della missione (caposquadra per primo): serve per le notifiche
    List<Map<String, String>> estraiContattiOperatoriMissione(int idMissione);

    // storico delle missioni in cui un operatore / un mezzo / un materiale e' stato coinvolto
    List<Map<String, String>> estraiStoricoOperatore(int idOperatore);

    List<Map<String, String>> estraiStoricoMezzo(int idMezzo);

    List<Map<String, String>> estraiStoricoMateriale(int idMateriale);

}
