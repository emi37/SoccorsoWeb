package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.security.SecureRandom;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CaptchaServlet", urlPatterns = {"/CaptchaServlet"})
public class CaptchaServlet extends HttpServlet {

    private final SecureRandom random = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int numero1 = random.nextInt(10) + 1;
        int numero2 = random.nextInt(10) + 1;
        int risultato = numero1 + numero2;

        HttpSession session = request.getSession(true);
        session.setAttribute("captchaRisultato", risultato);

        response.setContentType("application/json;charset=UTF-8");

        String json = "{ \"domanda\": \"Quanto fa " + numero1 + " + " + numero2 + "?\" }";

        response.getWriter().write(json);
    }
}