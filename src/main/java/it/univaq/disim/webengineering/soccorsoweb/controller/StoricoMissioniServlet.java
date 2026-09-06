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

    @Override
    public void init() throws ServletException {
        // Solita prassi: chiamiamo il padre e via. Il driver è già carico.
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check permessi: lo storico lo facciamo vedere solo all'ADMIN
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // Invoco il nostro fidato DAO
            MissioneDAO missioneDao = new MissioneDAO();

            // Chiedo al database la lista di tutte le missioni già chiuse
            List<Map<String, String>> listaStorico = missioneDao.estraiStoricoMissioni();

            // Impacchetto i dati nella request per farli leggere a FreeMarker
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("missioni_archiviate", listaStorico);

           // Deleghiamo tutto il rendering al TemplateManager con il NUOVO NOME E POSIZIONE
            TemplateManager.process("storico_missioni.ftl", dataModel, response, getServletContext());
        } catch (Exception e) {
            // Rete di sicurezza ruspante
            System.err.println("Panico nel controller durante l'estrazione dello storico missioni...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Se mai qualcuno dovesse chiamare questa pagina in POST, la giriamo alla GET
        doGet(request, response);
    }
}