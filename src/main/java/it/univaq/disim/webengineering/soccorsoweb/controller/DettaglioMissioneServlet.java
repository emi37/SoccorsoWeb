package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.AggiornamentoMissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DettaglioMissioneServlet", urlPatterns = {"/DettaglioMissioneServlet"})
public class DettaglioMissioneServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

            MissioneDAO missioneDao = new MissioneDAO();
            AggiornamentoMissioneDAO aggiornamentoDao = new AggiornamentoMissioneDAO();

            Map<String, Object> dettagliMissione = missioneDao.estraiDettagliCompleti(idDellaMissione);
            List<Map<String, String>> cronistoria = aggiornamentoDao.estraiCronistoria(idDellaMissione);

            if (dettagliMissione != null && !dettagliMissione.isEmpty()) {

                // MOTORE FREEMARKER: Usiamo la HashMap e il TemplateManager invece del Dispatcher
                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("request", request); // Essenziale per il contextPath del CSS
                dataModel.put("missione", dettagliMissione);
                dataModel.put("timeline", cronistoria);

                TemplateManager.process("dettaglio_missione.ftl", dataModel, response, getServletContext());

            } else {
                response.sendRedirect(request.getContextPath() + dashboardRitorno);
            }

        } catch (Exception e) {
            System.err.println("Panico nel controller durante la lettura dei dettagli missione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + dashboardRitorno);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        try {
            int idDellaMissione = Integer.parseInt(idMissioneStringa);
            String messaggioDescrittivo = request.getParameter("testo_descrittivo");
            int idUtenteLoggato = Integer.parseInt(sessioneAttuale.getAttribute("id_utente").toString());

            MissioneDAO missioneDao = new MissioneDAO();
            AggiornamentoMissioneDAO aggiornamentoDao = new AggiornamentoMissioneDAO();

            String statoAttuale = missioneDao.estraiStatoMissione(idDellaMissione);

            if ("IN_CORSO".equals(statoAttuale)) {
                aggiornamentoDao.salvaNuovoAggiornamento(idDellaMissione, idUtenteLoggato, messaggioDescrittivo);
            }

        } catch (Exception e) {
            System.err.println("Disastro totale durante l'inserimento di un log missione...");
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissioneStringa);
    }
}