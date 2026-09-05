package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CaptchaServlet", urlPatterns = {"/CaptchaServlet"})
public class CaptchaServlet extends HttpServlet {

    // Usiamo SecureRandom come da manuale per evitare che i bot indovinino la sequenza
    private SecureRandom generatoreCasuale;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // Inizializziamo il motore random una volta sola all'avvio della servlet
            generatoreCasuale = new SecureRandom();
        } catch (Exception e) {
            System.err.println("Panico: Impossibile inizializzare il generatore random per il Captcha!");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Generiamo due numeri a caso da 1 a 10 per fare una somma semplicissima
            int primoNumero = generatoreCasuale.nextInt(10) + 1;
            int secondoNumero = generatoreCasuale.nextInt(10) + 1;
            int risultatoAtteso = primoNumero + secondoNumero;

            // Salviamo il risultato in sessione così quando il form viene spedito (nella CreaRichiestaServlet)
            // possiamo controllare se l'utente ha risposto giusto
            HttpSession sessioneAttuale = request.getSession(true);
            sessioneAttuale.setAttribute("captchaRisultato", risultatoAtteso);

            // Diciamo al browser che gli stiamo per mandare un payload JSON puro
            response.setContentType("application/json;charset=UTF-8");

            // Assembliamo la stringa JSON grezza a mano (vecchia scuola)
            String stringaJson = "{ \"domanda\": \"Quanto fa " + primoNumero + " + " + secondoNumero + "?\" }";

            // Spariamo il JSON nello stream verso il client javascript (try-with-resources per chiudere il writer)
            try (PrintWriter stampante = response.getWriter()) {
                stampante.write(stringaJson);
            }

        } catch (Exception e) {
            // Catch d'ordinanza per non far crashare male la chiamata fetch(AJAX) del client
            System.err.println("Disastro durante la generazione o l'invio del Captcha in JSON...");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
