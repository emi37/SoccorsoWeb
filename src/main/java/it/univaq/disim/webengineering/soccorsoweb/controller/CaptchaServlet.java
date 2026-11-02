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

    // inizializzazione standard del generatore random all'interno della classe
    private final SecureRandom generatoreCasuale = new SecureRandom();

    @Override
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
            
            System.err.println("errore durante la generazione del captcha : " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}