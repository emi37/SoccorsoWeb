package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MezzoDAO;
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

@WebServlet(name = "GestioneMezziServlet", urlPatterns = {"/GestioneMezzi"})
public class GestioneMezziServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check permessi: blocca chi non è ADMIN
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            MezzoDAO mezzoDao = new MezzoDAO();

            // 1. GESTIONE CANCELLAZIONE (Soft-Delete)
            String idDaEliminareStr = request.getParameter("elimina");
            if (idDaEliminareStr != null && !idDaEliminareStr.trim().isEmpty()) {
                int idMezzo = Integer.parseInt(idDaEliminareStr);
                mezzoDao.rimuoviMezzoSeLibero(idMezzo);

                // Pattern PRG: pulisco l'URL facendo un redirect pulito
                response.sendRedirect(request.getContextPath() + "/GestioneMezzi");
                return;
            }

            // 2. LETTURA DEI DATI 
            List<Map<String, String>> listaMezzi = mezzoDao.estraiTuttiIMezziConStato();

            // 3. PREPARAZIONE DATI PER FREEMARKER
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("mezzi", listaMezzi); // La lista che la vista leggerà con <#list mezzi as m>
            dataModel.put("request", request);

            // 4. RENDERING TRAMITE TEMPLATE MANAGER (Punta alla cartella templates in automatico)
            TemplateManager.process("gestione_mezzi.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            System.err.println("Errore brutto nel controller della gestione mezzi...");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se arriva una POST, la giriamo alla GET per sicurezza
        doGet(request, response);
    }
}