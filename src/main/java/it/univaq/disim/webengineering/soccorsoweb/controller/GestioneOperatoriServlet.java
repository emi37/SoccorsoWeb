/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.controller;

/**
 *
 * @author Filippo
 */

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.UtenteDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "GestioneOperatoriServlet", urlPatterns = {"/GestioneOperatori"})
public class GestioneOperatoriServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Blocco sicurezza ADMIN
        HttpSession sessioneAttuale = request.getSession(false); //serve a dire di cercare se la sessione attuale ha un admin logato o meno, se rimango a false,non sono admin e non posso entrare
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html"); //la session o è null o piena cioe è admin
            return;
        }

        try {
            UtenteDAO utenteDao = new UtenteDAO();
            Map<String, Object> dataModel = new HashMap<>();
            
            // Passiamo a FreeMarker le 3 liste che servono per la pagina
            dataModel.put("operatori", utenteDao.estraiTuttiGliOperatoriConStato());
            dataModel.put("abilita", utenteDao.estraiTutteLeAbilita());
            dataModel.put("patenti", utenteDao.estraiTutteLePatenti());
            dataModel.put("request", request);

            TemplateManager.process("gestione_operatori.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            System.err.println("Errore nel caricamento della dashboard operatori...");
            e.printStackTrace();
        }
    }
}    