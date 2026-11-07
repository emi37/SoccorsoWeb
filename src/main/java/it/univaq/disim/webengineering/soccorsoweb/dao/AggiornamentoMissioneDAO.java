package it.univaq.disim.webengineering.soccorsoweb.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Contratto di accesso ai dati per AggiornamentoMissione. L'implementazione per MySQL e' in dao.impl.AggiornamentoMissioneDAO_MySQL. */
public interface AggiornamentoMissioneDAO {

    boolean salvaNuovoAggiornamento(int idMissione, int idAdminLoggato, String messaggio);

    List<Map<String, String>> estraiCronistoria(int idMissione);

}
