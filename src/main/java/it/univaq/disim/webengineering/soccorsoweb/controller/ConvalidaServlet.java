package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.RichiestaSoccorsoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ConvalidaServlet", urlPatterns = {"/ConvalidaServlet"})
public class ConvalidaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // prendo il token dall'URL
        String tokenRicevuto = request.getParameter("token");
        boolean convalidaOk = false;

        try {
            // verifico che il token esista e non sia una stringa vuota
            if (tokenRicevuto != null && !tokenRicevuto.trim().isEmpty()) {
                RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
                
                // faccio la convalida del token tramite il DAO
                convalidaOk = richiestaDao.svuotaTokenConvalida(tokenRicevuto);
            }
        } catch (Exception e) {
            // In caso di problemi col database o altro, stampo l'errore per il debug
            System.err.println("Errore durante la convalida del token: " + e.getMessage());
            e.printStackTrace();
        }

        //reindirizzo l'utente alla pagina corretta in base all'esito
        if (convalidaOk) {
            response.sendRedirect(request.getContextPath() + "/convalida.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/CreazioneUtente_errore.html");
        }
    }
}