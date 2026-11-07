package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Missioni a cui ha partecipato un operatore, o in cui sono stati usati un mezzo o un materiale. */
@WebServlet(name = "StoricoRisorsaServlet", urlPatterns = {"/StoricoRisorsa"})
public class StoricoRisorsaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StoricoRisorsaServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessione = request.getSession(false);
        if (sessione == null || !"ADMIN".equals(sessione.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String tipo = request.getParameter("tipo");
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            var dao = SoccorsoDataLayer.getInstance().getMissioneDAO();
            List<Map<String, String>> storico;
            String titolo;
            String ritorno;
            if ("operatore".equals(tipo)) {
                storico = dao.estraiStoricoOperatore(id);
                titolo = "Storico missioni dell'operatore #" + id;
                ritorno = "GestioneOperatori";
            } else if ("mezzo".equals(tipo)) {
                storico = dao.estraiStoricoMezzo(id);
                titolo = "Storico missioni del mezzo #" + id;
                ritorno = "GestioneMezzi";
            } else if ("materiale".equals(tipo)) {
                storico = dao.estraiStoricoMateriale(id);
                titolo = "Storico missioni del materiale #" + id;
                ritorno = "GestioneMateriali";
            } else {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
                return;
            }

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("titolo", titolo);
            dataModel.put("ritorno", ritorno);
            dataModel.put("storico", storico);
            TemplateManager.process("storico_risorsa.ftl", dataModel, response, getServletContext());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nello storico risorsa", e);
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }
}
