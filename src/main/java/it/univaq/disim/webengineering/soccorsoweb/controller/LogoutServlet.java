package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/LogoutServlet"})
public class LogoutServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Prendo la sessione SOLO se esiste (false), non ha senso crearla per poi distruggerla
        HttpSession sessioneAttuale = request.getSession(false);

        if (sessioneAttuale != null) {
            // Straccio la sessione e cancello tutti i dati
            sessioneAttuale.invalidate();
        }

        // Ti butto fuori, tornando alla home page pubblica
        response.sendRedirect(request.getContextPath() + "/index.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se per caso il logout viene chiamato tramite un form in POST, lo giriamo alla GET
        doGet(request, response);
    }
}
