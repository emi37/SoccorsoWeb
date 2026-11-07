package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.NotificaService;
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

@WebServlet(name = "ConcludiMissioneServlet", urlPatterns = {"/ConcludiMissioneServlet"})
public class ConcludiMissioneServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ConcludiMissioneServlet.class.getName());

    private static final int MAX_LIVELLO_SUCCESSO = 5;
    private static final int MAX_LUNGHEZZA_COMMENTI = 1000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Verifica dei permessi di amministratore
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idMissione = Integer.parseInt(request.getParameter("id_missione"));
            String idRichiesta = request.getParameter("id_richiesta");
            
            MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();
            Map<String, Object> missioneCorrente = missioneDao.estraiDettagliCompleti(idMissione);
            
            missioneCorrente.put("id_richiesta", idRichiesta);
            
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("missione", missioneCorrente); 
            dataModel.put("request", request);            
            
            TemplateManager.process("concludi_missione.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del form di conclusione missione: " + e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo")) || session.getAttribute("id_utente") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idMissione = Integer.parseInt(request.getParameter("id_missione"));
            int livelloSuccesso = Integer.parseInt(request.getParameter("voto_successo"));
            String commenti = request.getParameter("commenti");
            int idAdminLoggato = Integer.parseInt(session.getAttribute("id_utente").toString());

            // il livello di successo va da 0 (fallimento) a 5 (successo pieno): il form lo garantisce,
            // ma una richiesta costruita a mano potrebbe mandare qualsiasi numero
            if (livelloSuccesso < 0 || livelloSuccesso > MAX_LIVELLO_SUCCESSO
                    || (commenti != null && commenti.length() > MAX_LUNGHEZZA_COMMENTI)) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=chiusura_non_valida");
                return;
            }

            // NB: la richiesta da chiudere NON arriva piu' dal form: la ricava il DAO dalla missione
            MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();
            boolean terminataOk = missioneDao.chiudiMissioneInTransazione(idMissione, livelloSuccesso, commenti, idAdminLoggato);

            if (terminataOk) {
                String rapporto = (commenti == null || commenti.trim().isEmpty()) ? "(nessun commento)" : commenti.trim();
                NotificaService.notificaOperatori(missioneDao.estraiContattiOperatoriMissione(idMissione),
                        "Missione #" + idMissione + " conclusa",
                        "La missione #" + idMissione + " e' stata conclusa.\n"
                        + "Livello di successo: " + livelloSuccesso + " / " + MAX_LIVELLO_SUCCESSO + "\n"
                        + "Rapporto finale: " + rapporto + "\n\n"
                        + "Tu e le risorse assegnate (mezzi e materiali) siete di nuovo liberi.\n"
                        + "Dettagli: " + NotificaService.baseUrl(request) + "/DettaglioMissioneServlet?id_missione=" + idMissione);
                response.sendRedirect(request.getContextPath() + "/DashboardServlet?msg=missione_conclusa");
            } else {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=salvataggio_fallito");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'elaborazione della chiusura missione: " + e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=eccezione_chiusura");
        }
    }
}
