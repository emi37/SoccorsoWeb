package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MissioneDAO;
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

@WebServlet(name = "StoricoMissioniServlet", urlPatterns = {"/StoricoMissioni"})
public class StoricoMissioniServlet extends HttpServlet {
    // la logica è: controlla se sei Admin -> prende i dati dal DAO -> li passa al file .ftl cosi li mostriamo in front-end 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //lo storico lo facciamo vedere solo all'admin
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // creo il  DAO
            MissioneDAO missioneDao = new MissioneDAO(); // prendo i dati estratti dalla query del DAO

            // prendo la lista di tutte di tutti i dati di tutte le missioni finite/concluse richiamando il metodo del DAO: estraiStoricoMissioni
            List<Map<String, String>> listaStorico = missioneDao.estraiStoricoMissioni();

            // prendo i dati nella request per freeMarker
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("missioni_archiviate", listaStorico);

            TemplateManager.process("storico_missioni.ftl", dataModel, response, getServletContext());
        } catch (Exception e) {
            //sicurezza
            System.err.println("erroree durante l'estrazione dello storico missioni...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}