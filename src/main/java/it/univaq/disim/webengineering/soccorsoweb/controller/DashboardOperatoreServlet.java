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
import java.util.List;
import java.util.Map;

@WebServlet(name = "DashboardOperatoreServlet", urlPatterns = {"/DashboardOperatoreServlet"})
public class DashboardOperatoreServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Controllo Sicurezza: Solo gli OPERATORI possono entrare qui
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"OPERATORE".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // FIX: Estrazione sicura dell'ID per evitare crash (ClassCastException)
            Object objId = sessioneAttuale.getAttribute("id_utente");
            long idOperatore = -1;
            if (objId != null) {
                idOperatore = Long.parseLong(objId.toString());
            }

            // 2. Chiamo i DAO per recuperare le missioni
            MissioneDAO missioneDao = new MissioneDAO();
            List<Map<String, String>> mieMissioni = missioneDao.estraiMissioniAttivePerOperatore(idOperatore);
            
            // 3. Prepara i dati per FreeMarker usando una Mappa (dataModel)
            Map<String, Object> dataModel = new HashMap<>();
            
            // FIX: Passiamo la request per far caricare il CSS senza che FreeMarker esploda
            dataModel.put("request", request);
            
            // FIX: Rinominiamo la variabile nel nome esatto che il file .ftl si aspetta
            dataModel.put("missioniInCorso", mieMissioni);
            
            // FIX: Costruiamo l'oggetto "utente" per il messaggio di benvenuto e lo stato
            Map<String, String> datiUtente = new HashMap<>();
            
            // Gestione null-safe per nome e cognome
            Object nomeObj = sessioneAttuale.getAttribute("nome");
            Object cognomeObj = sessioneAttuale.getAttribute("cognome");
            datiUtente.put("nome", nomeObj != null ? nomeObj.toString() : "Operatore");
            datiUtente.put("cognome", cognomeObj != null ? cognomeObj.toString() : "");

            // Calcolo logico dello stato: se la lista missioni ha qualcosa, è IMPEGNATO.
            if (mieMissioni != null && !mieMissioni.isEmpty()) {
                datiUtente.put("stato_attuale", "IMPEGNATO");
            } else {
                datiUtente.put("stato_attuale", "LIBERO");
            }
            dataModel.put("utente", datiUtente);

            // 4. Rendering magico con FreeMarker!
            TemplateManager.process("dashboard_operatore.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            System.err.println("Errore nel caricamento della dashboard operatore...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}