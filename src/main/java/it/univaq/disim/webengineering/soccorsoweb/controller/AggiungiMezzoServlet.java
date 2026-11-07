package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.Mezzo;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AggiungiMezzo")
public class AggiungiMezzoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AggiungiMezzoServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // recupero la sessione senza crearne di nuove a vuoto
        HttpSession sessioneAttuale = request.getSession(false);

        // check permessi dell admin
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // estraggo i parametri inviati dal client 
            String nomeInserito = request.getParameter("nome");
            String descrizioneInserita = request.getParameter("descrizione");

            if (nomeInserito != null && !nomeInserito.trim().isEmpty()) {

                // metto i dati al mezzo 
                Mezzo nuovoMezzo = new Mezzo();
                nuovoMezzo.setNome(nomeInserito);
                nuovoMezzo.setDescrizione(descrizioneInserita);

                
                MezzoDAO mezzoDao = SoccorsoDataLayer.getInstance().getMezzoDAO();
                mezzoDao.salvaNuovoMezzo(nuovoMezzo);
            }
        } catch (Exception e) {
            
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiunta del mezzo", e);
        }

        response.sendRedirect(request.getContextPath() + "/GestioneMezzi");
    }
}