package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/DashboardServlet"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Controllo di sicurezza sulla sessione
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("id_utente") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // Se un OPERATORE prova ad accedere alla dashboard Admin, lo reindirizziamo alla sua area
        String ruolo = (String) session.getAttribute("ruolo");
        if (!"ADMIN".equalsIgnoreCase(ruolo)) {
            response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
            return;
        }

        // 2. Recupero dei dati tramite i DAO e li metto in una lista
        RichiestaSoccorsoDAO richiestaDao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
        MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();

        List<Map<String, String>> listaRichiesteAttive = richiestaDao.estraiRichiesteAttive();
        List<Map<String, String>> listaMissioniInCorso = missioneDao.estraiMissioniInCorso();

        // 3. Preparazione del datamodel per freeMarker
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("richieste", listaRichiesteAttive);
        dataModel.put("missioni", listaMissioniInCorso);
        dataModel.put("request", request);

        // Passiamo anche i dati dell'amministratore loggato s
        dataModel.put("nome_utente", session.getAttribute("nome"));
        dataModel.put("cognome_utente", session.getAttribute("cognome"));

        TemplateManager.process("dashboard.ftl", dataModel, response, getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}