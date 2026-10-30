package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.AggiornamentoMissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.UtenteDAO;
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
            UtenteDAO utenteDao = new UtenteDAO();
            MezzoDAO mezzoDao = new MezzoDAO();
            MaterialeDAO materialeDao = new MaterialeDAO();

            Map<String, Object> dettagliMissione = missioneDao.estraiDettagliCompleti(idDellaMissione);

            if (dettagliMissione != null && !dettagliMissione.isEmpty()) {

                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("request", request);
                dataModel.put("missione", dettagliMissione);
                
                // Passiamo le risorse disponibili per il form di modifica
                dataModel.put("operatori_disponibili", utenteDao.estraiOperatoriDisponibili());
                dataModel.put("mezzi_disponibili", mezzoDao.estraiMezziDisponibili());
                dataModel.put("materiali_disponibili", materialeDao.estraiMaterialiDisponibili());
                
                dataModel.put("ruolo_utente", ruoloLoggato);

                TemplateManager.process("dettaglio_missione.ftl", dataModel, response, getServletContext());

            } else {
                response.sendRedirect(request.getContextPath() + dashboardRitorno);
            }

        } catch (Exception e) {
            System.err.println("Errore durante la lettura dei dettagli missione...");
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
        String azione = request.getParameter("azione");

        try {
            int idDellaMissione = Integer.parseInt(idMissioneStringa);
            MissioneDAO missioneDao = new MissioneDAO();
            String statoAttuale = missioneDao.estraiStatoMissione(idDellaMissione);

            // Modifica permessa solo agli ADMIN e solo se la missione è IN_CORSO
            if ("IN_CORSO".equals(statoAttuale) && "modifica_missione".equals(azione) && "ADMIN".equals(ruoloLoggato)) {
                
                String nuovoObiettivo = request.getParameter("obiettivo");
                String nuovaPosizione = request.getParameter("posizione");

                String[] nuoviOperatori = request.getParameterValues("id_operatore");
                String[] nuoviMezzi = request.getParameterValues("id_mezzo");
                String[] nuoviMateriali = request.getParameterValues("id_materiale");

                boolean esitoModifica = missioneDao.modificaMissioneInCorso(
                        idDellaMissione, nuovoObiettivo, nuovaPosizione,
                        nuoviOperatori, nuoviMezzi, nuoviMateriali
                );
                
                if(esitoModifica) {
                     System.out.println("[SISTEMA] Missione #" + idDellaMissione + " aggiornata con successo.");
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'elaborazione del salvataggio missione...");
            e.printStackTrace();
        }

        // Pattern Post-Redirect-Get
        response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissioneStringa);
    }
}