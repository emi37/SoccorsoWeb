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
        // Chiamata standard al padre.
        // Niente più caricamento del driver MySQL qui, ci pensa il nostro fidato DBManager!
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Tiriamo giù il token dalla query string dell'URL (es. ?token=123-abc)
        String tokenRicevuto = request.getParameter("token");
        boolean convalidaOk = false;

        try {
            // Procediamo solo se il token non è nullo e non è uno spazio vuoto
            if (tokenRicevuto != null && !tokenRicevuto.trim().isEmpty()) {

                // Magia pura: evochiamo il DAO e gli lasciamo sbrigare la pratica.
                // Il metodo svuotaTokenConvalida controlla automaticamente se sono passati meno di 10 minuti
                RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
                convalidaOk = richiestaDao.svuotaTokenConvalida(tokenRicevuto);
            }
        } catch (Exception e) {
            // Stampata ignorante a console in caso di esplosioni
            System.err.println("Panico nel controller durante il check della convalida...");
            e.printStackTrace();
        }

        // Smistamento finale verso le pagine statiche HTML (in attesa di FreeMarker)
        // Se la convalida è andata a buon fine, il DAO ha già impostato la richiesta su 'ATTIVA'
// Smistamento finale verso le pagine statiche HTML
        if (convalidaOk) {
            // CORREZIONE: Il file su NetBeans si chiama convalida.html
            response.sendRedirect(request.getContextPath() + "/convalida.html");
        } else {
            // Se il token è scaduto, sbagliato o già usato
            response.sendRedirect(request.getContextPath() + "/CreazioneUtente_errore.html");
        }
    }
}
