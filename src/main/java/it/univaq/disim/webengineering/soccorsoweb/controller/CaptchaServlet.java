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

    private SecureRandom random;

    @Override
    public void init() throws ServletException {
        // inizializzo SecureRandom qui per preparare il contesto una sola volta all'avvio
        try {
            random = new SecureRandom();
        } catch (Exception e) {
            System.err.println("Errore inizializzazione generatore random");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // genero i due numeri per l'antispam
        int numero1 = random.nextInt(10) + 1;
        int numero2 = random.nextInt(10) + 1;
        int risultatoCorretto = numero1 + numero2;

        // salvo il risultato in sessione per validarlo successivamente al submit del form
        HttpSession session = request.getSession(true);
        session.setAttribute("captchaRisultato", risultatoCorretto);

        // preparo il payload JSON da restituire al client
        response.setContentType("application/json;charset=UTF-8");
        String jsonResponse = "{ \"domanda\": \"Quanto fa " + numero1 + " + " + numero2 + "?\" }";

        // scrivo la risposta nello stream, usando try-with-resources per chiudere in automatico
        try (PrintWriter out = response.getWriter()) {
            out.write(jsonResponse);
        } catch (Exception e) {
            System.err.println("Errore scrittura JSON nella response");
            e.printStackTrace();
        }
    }
}