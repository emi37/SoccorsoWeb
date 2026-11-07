package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.UtenteDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "DettaglioMissioneServlet", urlPatterns = {"/DettaglioMissioneServlet"})
public class DettaglioMissioneServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DettaglioMissioneServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || sessioneAttuale.getAttribute("ruolo") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String ruoloLoggato = (String) sessioneAttuale.getAttribute("ruolo");
        if (!"ADMIN".equals(ruoloLoggato) && !"OPERATORE".equals(ruoloLoggato)) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String dashboardRitorno = "ADMIN".equals(ruoloLoggato) ? "/DashboardServlet" : "/DashboardOperatoreServlet";

        try {
            int idDellaMissione = Integer.parseInt(request.getParameter("id_missione"));

            MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();
            UtenteDAO utenteDao = SoccorsoDataLayer.getInstance().getUtenteDAO();
            MezzoDAO mezzoDao = SoccorsoDataLayer.getInstance().getMezzoDAO();
            MaterialeDAO materialeDao = SoccorsoDataLayer.getInstance().getMaterialeDAO();

            Map<String, Object> dettagliMissione = missioneDao.estraiDettagliCompleti(idDellaMissione);

            if (dettagliMissione != null && !dettagliMissione.isEmpty()) {
                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("request", request);
                dataModel.put("missione", dettagliMissione);
                dataModel.put("operatori_disponibili", utenteDao.estraiOperatoriDisponibili());
                dataModel.put("mezzi_disponibili", mezzoDao.estraiMezziDisponibili());
                dataModel.put("materiali_disponibili", materialeDao.estraiMaterialiDisponibili());
                dataModel.put("ruolo_utente", ruoloLoggato);

                TemplateManager.process("dettaglio_missione.ftl", dataModel, response, getServletContext());
            } else {
                response.sendRedirect(request.getContextPath() + dashboardRitorno);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
            response.sendRedirect(request.getContextPath() + dashboardRitorno);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || sessioneAttuale.getAttribute("ruolo") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String ruoloLoggato = (String) sessioneAttuale.getAttribute("ruolo");
        if (!"ADMIN".equals(ruoloLoggato) && !"OPERATORE".equals(ruoloLoggato)) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissioneStringa = request.getParameter("id_missione");
        String azione = request.getParameter("azione");

        try {
            int idDellaMissione = Integer.parseInt(idMissioneStringa);
            MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();
            String statoAttuale = missioneDao.estraiStatoMissione(idDellaMissione);

            if ("IN_CORSO".equals(statoAttuale) && "modifica_missione".equals(azione)) {
                String nuovoObiettivo = request.getParameter("obiettivo");
                String nuovaPosizione = request.getParameter("posizione");
                String[] nuoviOperatori = request.getParameterValues("id_operatore");
                String[] nuoviMezzi = request.getParameterValues("id_mezzo");
                String[] nuoviMateriali = request.getParameterValues("id_materiale");

                missioneDao.modificaMissioneInCorso(
                        idDellaMissione, nuovoObiettivo, nuovaPosizione,
                        nuoviOperatori, nuoviMezzi, nuoviMateriali
                );
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
        }

        response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissioneStringa);
    }
}