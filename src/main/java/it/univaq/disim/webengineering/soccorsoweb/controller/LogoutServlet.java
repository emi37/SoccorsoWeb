package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "Logout", urlPatterns = {"/Logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Prendo la sessione SOLO se esiste (false)
        HttpSession sessioneAttuale = request.getSession(false);

        if (sessioneAttuale != null) {
            // Straccio la sessione e cancello tutti i dati
            sessioneAttuale.invalidate();
        }

        // CORREZIONE: Ti rimando alla pagina di login che sappiamo esistere, evitando freeze
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}