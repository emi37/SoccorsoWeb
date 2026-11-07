package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
// @WebServlet è uno standard di Java che serve per creare applicazioni web(fa parte di è di Jakarta EE)
@WebServlet(name = "CaptchaServlet", urlPatterns = {"/CaptchaServlet"})// quando l utente riempie il captcha e preme invio, la richieta viene intercettata da questa classe qui
public class CaptchaServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CaptchaServlet.class.getName());
//l'estensione dice che quesat classe è ijn grado di ricevere richieste http e indirizzare dati sul browser

    // inizializzazione standard del generatore random all'interno della classe
    private final SecureRandom generatoreCasuale = new SecureRandom();

    @Override //il doGet: Gestisce le richieste HTTP di tipo GET, quindi richieste di lettura dati, come le SELECT(non salva nulla sul DB)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            //generazione di due numeri casuali da 1 a 10
            int primoNumero = generatoreCasuale.nextInt(10) + 1;
            int secondoNumero = generatoreCasuale.nextInt(10) + 1;
            int risultatoAtteso = primoNumero + secondoNumero;

            // salvataggio del risultato atteso nella sessione dell'utente
            HttpSession sessioneAttuale = request.getSession(true);
            sessioneAttuale.setAttribute("captchaRisultato", risultatoAtteso);

            response.setContentType("application/json;charset=UTF-8");

            // costruzione e invio della risposta in formato json
            String stringaJson = "{ \"domanda\": \"Quanto fa " + primoNumero + " + " + secondoNumero + "?\" }";

            try (PrintWriter stampante = response.getWriter()) {
                stampante.write(stringaJson);
            }

        } catch (Exception e) {
            
            LOGGER.log(Level.SEVERE, "errore durante la generazione del captcha : " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}