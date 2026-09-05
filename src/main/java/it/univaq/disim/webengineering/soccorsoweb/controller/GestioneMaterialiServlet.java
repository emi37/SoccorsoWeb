package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MaterialeDAO;
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

    @Override
    public void init() throws ServletException {
        // Solita prassi: chiamiamo il padre
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Il nostro solito buttafuori: check permessi ruspante per bloccare i non-admin
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // Evochiamo il DAO
            MaterialeDAO materialeDao = new MaterialeDAO();

            // 1. GESTIONE CANCELLAZIONE (Soft-Delete)
            String idDaEliminareStr = request.getParameter("elimina");
            if (idDaEliminareStr != null && !idDaEliminareStr.trim().isEmpty()) {
                int idMateriale = Integer.parseInt(idDaEliminareStr);
                materialeDao.nascondiMaterialeSeLibero(idMateriale);
                response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
                return;
            }

            // 2. LETTURA DEI DATI 
            List<Map<String, String>> listaMateriali = materialeDao.estraiTuttiIMaterialiConStato();

            // 3. PREPARAZIONE DATI PER FREEMARKER (Come nella Dashboard)
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("materiali", listaMateriali); // La lista che la vista leggerà con <#list materiali as m>
            dataModel.put("request", request); // Cruciale per i link

            // 4. RENDERING TRAMITE TEMPLATE MANAGER
            TemplateManager.process("gestione_materiali.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            // Immancabile stampata a console se scoppia qualcosa
            System.err.println("Errore brutto nel controller della gestione materiali...");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}