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
    public void init() throws ServletException {
        
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        
        String tokenRicevuto = request.getParameter("token");
        boolean convalidaOk = false;

        try {
           
            if (tokenRicevuto != null && !tokenRicevuto.trim().isEmpty()) {

                //  evochiamo il DAO e gli lasciamo sbrigare la pratica.
                RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
                convalidaOk = richiestaDao.svuotaTokenConvalida(tokenRicevuto);
            }
        } catch (Exception e) {
            // Stampata per verificare
            System.err.println("Panico nel controller durante il check della convalida...");
            e.printStackTrace();
        }

        
        // Se la convalida è andata a buon fine, il DAO ha già impostato la richiesta su 'ATTIVA'
        if (convalidaOk) {
            response.sendRedirect(request.getContextPath() + "/convalida.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/CreazioneUtente_errore.html");
        }
    }
}
