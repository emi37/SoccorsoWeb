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
            // Recupero l'ID dell'operatore loggato dalla sessione
            long idOperatore = (long) sessioneAttuale.getAttribute("id_utente");

            // 2. Chiamo i DAO per recuperare i dati che servono all'operatore
            MissioneDAO missioneDao = new MissioneDAO();

            // Mi faccio dare la lista delle missioni IN CORSO a cui questo specifico operatore è stato assegnato
            List<Map<String, String>> mieMissioni = missioneDao.estraiMissioniAttivePerOperatore(idOperatore);
            // 3. Prepara i dati per FreeMarker usando una Mappa (dataModel)
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("mie_missioni", mieMissioni);
// Se serve, puoi passare anche i dati dell'operatore: dataModel.put("nome", sessioneAttuale.getAttribute("nome"));

// 4. Rendering magico con FreeMarker!
            TemplateManager.process("dashboard_operatore.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            System.err.println("Errore brutto nel caricamento della dashboard operatore...");
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
