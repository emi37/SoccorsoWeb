package it.univaq.disim.webengineering.soccorsoweb.controller;

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
import java.util.Map;

@WebServlet(name = "ConcludiMissioneServlet", urlPatterns = {"/ConcludiMissioneServlet"})
public class ConcludiMissioneServlet extends HttpServlet {

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
            
            MissioneDAO missioneDao = new MissioneDAO();
            Map<String, Object> missioneCorrente = missioneDao.estraiDettagliCompleti(idMissione);
            
            missioneCorrente.put("id_richiesta", idRichiesta);
            
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("missione", missioneCorrente); 
            dataModel.put("request", request);            
            
            TemplateManager.process("concludi_missione.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            System.err.println("Errore nel caricamento del form di conclusione missione: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idMissione = Integer.parseInt(request.getParameter("id_missione"));
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));
            int livelloSuccesso = Integer.parseInt(request.getParameter("voto_successo"));
            String commenti = request.getParameter("commenti");
            
            //estrae l'ide dell'utente della sessione
            int idAdminLoggato = 1; 
            Object objId = session.getAttribute("id_utente");
            if (objId != null) {
                idAdminLoggato = Integer.parseInt(objId.toString());
            }

            MissioneDAO missioneDao = new MissioneDAO();
            boolean terminataOk = missioneDao.chiudiMissioneInTransazione(idMissione, idRichiesta, livelloSuccesso, commenti, idAdminLoggato);
            
            if (terminataOk) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            } else {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=salvataggio_fallito");
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'elaborazione della chiusura missione: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=eccezione_chiusura");
        }
    }
}