package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MaterialeDAO;
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

@WebServlet(name = "GestioneMaterialiServlet", urlPatterns = {"/GestioneMateriali"})
public class GestioneMaterialiServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(GestioneMaterialiServlet.class.getName());


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // controllo se sei admin
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // DAO
            MaterialeDAO materialeDao = SoccorsoDataLayer.getInstance().getMaterialeDAO();

            // 1. GESTIONE CANCELLAZIONE 
            String idDaEliminareStr = request.getParameter("elimina");
            if (idDaEliminareStr != null && !idDaEliminareStr.trim().isEmpty()) {
                int idMateriale = Integer.parseInt(idDaEliminareStr);
                materialeDao.nascondiMaterialeSeLibero(idMateriale);
                response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
                return;
            }

            // 2. lettura dei dati estratti 
            List<Map<String, String>> listaMateriali = materialeDao.estraiTuttiIMaterialiConStato();

            // preparo i dati per il freemarker
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("materiali", listaMateriali); //è la lista che la vista leggerà con <#list materiali as m>
            dataModel.put("request", request);

            TemplateManager.process("gestione_materiali.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            
            LOGGER.log(Level.SEVERE, "Errore della gestione materiali...", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}  