package it.univaq.disim.webengineering.soccorsoweb.dao;

import it.univaq.disim.webengineering.soccorsoweb.model.Utenti;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Contratto di accesso ai dati per Utente. L'implementazione per MySQL e' in dao.impl.UtenteDAO_MySQL. */
public interface UtenteDAO {

    // recupera i dati dell'utente per effettuare il login
    Utenti estraiUtentePerEmail(String emailCercata);

    // inserisce un nuovo utente e restituisce il suo id generato
    long salvaNuovoUtente(Utenti nuovoUtente);

    void collegaAbilita(long idUtente, int idAbilita);

    void collegaPatente(long idUtente, int idPatente);

    // estrae tutti gli operatori e mostra se sono liberi o impegnati
    List<Map<String, String>> estraiTuttiGliOperatoriConStato();

    // estrae la lista degli operatori attualmente non assegnati a nessuna missione in corso
    List< Map<String, String> > estraiOperatoriDisponibili();

    // 1 - i due metodi seguenti servono a popolare i menu a tendina dei form
    List< Map<String, String> > estraiTutteLeAbilita();

    // 2 -
    List< Map<String, String> > estraiTutteLePatenti();

    List<String> estraiPatentiOperatore(int idOperatore);

    List<Map<String, String>> estraiPatentiMancanti(int idOperatore);

    boolean aggiungiPatenteOperatore(int idOperatore, int idPatente);

    List<String> estraiAbilitaOperatore(int idOperatore);

    List<Map<String, String>> estraiAbilitaMancanti(int idOperatore);

    boolean aggiungiAbilitaOperatore(int idOperatore, int idAbilita);

}
