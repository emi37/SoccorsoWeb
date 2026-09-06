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
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check permessi Admin
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // 1. Prendiamo ENTRAMBI gli ID passati dal bottone blu della dashboard
            String idMissione = request.getParameter("id_missione");
            String idRichiesta = request.getParameter("id_richiesta");
            
            // 2. Creiamo l'oggetto "missione" esatto che il tuo file .ftl sta cercando
            Map<String, String> missioneCorrente = new HashMap<>();
            missioneCorrente.put("id_missione", idMissione);
            missioneCorrente.put("id_richiesta", idRichiesta);
            
            // 3. Impacchettiamo tutto per FreeMarker
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("missione", missioneCorrente); 
            dataModel.put("request", request);           
            
            // 4. Lanciamo la grafica
            TemplateManager.process("concludi_missione.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            System.err.println("Errore nel caricamento del form di conclusione missione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // prelevo i dati dal post inviato dal form HTML
            int idMissione = Integer.parseInt(request.getParameter("id_missione"));
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));
            int livelloSuccesso = Integer.parseInt(request.getParameter("voto_successo"));
            String commenti = request.getParameter("commenti");
            
            // FIX CRITICO: Estrazione sicura dell'ID (nessun crash!)
            Object objId = sessioneAttuale.getAttribute("id_utente");
            int idAdminLoggato = 1; // Fallback di sicurezza
            if (objId != null) {
                idAdminLoggato = Integer.parseInt(objId.toString());
            }

            // Deleghiamo tutta la mostruosità della transazione (update, update, insert) al DAO
            MissioneDAO missioneDao = new MissioneDAO();
            boolean terminataOk = missioneDao.chiudiMissioneInTransazione(idMissione, idRichiesta, livelloSuccesso, commenti, idAdminLoggato);

            // Se va a buon fine, ti sparo sulla Dashboard così vedi la tabella aggiornata!
            if (terminataOk) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            } else {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=salvataggio_fallito");
            }

        } catch (Exception e) {
            System.err.println("Errore ruspante durante l'elaborazione della chiusura missione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=eccezione_chiusura");
        }
    }
}