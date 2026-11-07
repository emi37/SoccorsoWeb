package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.RichiestaSoccorsoDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "IgnoraRichiestaServlet", urlPatterns = {"/IgnoraRichiestaServlet"})
public class IgnoraRichiestaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(IgnoraRichiestaServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //se non sei ADMIN loggato, torni al login 
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // prendo l'id della richiesta da ignorare dalla query string
        String idDaIgnorareStr = request.getParameter("id_richiesta");

        try {
            //controllo per evitare delle stringhe vuote
            if (idDaIgnorareStr != null && !idDaIgnorareStr.trim().isEmpty()) {

                int idRichiesta = Integer.parseInt(idDaIgnorareStr);

                //il dao fa la query di upadate
                RichiestaSoccorsoDAO richiestaDao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
                richiestaDao.aggiornaStatoRichiestaInIgnorata(idRichiesta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante lo scarto della richiesta...", e);
        }

        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

     
        doGet(request, response);
    }
}
