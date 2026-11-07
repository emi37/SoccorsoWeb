/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
/**
 *
 * @author Filippo
 */

import it.univaq.disim.webengineering.soccorsoweb.dao.UtenteDAO;
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

    private static final Logger LOGGER = Logger.getLogger(GestioneOperatoriServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Blocco sicurezza ADMIN
        HttpSession sessioneAttuale = request.getSession(false); 
        //request.getSession(false); chiede la sessione dell utente al server, che inizia in login servlet e dice: se c'e' allora dammela, se non c'è allora restituisci null e basta, in questo caso false (e true) è un'istruzione che dice cosa deve fare e basta
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html"); //se non esiste la sessione oppure non è admin, reindirizzo alla pagina di login, in oratica non seiin grado di accedere a questa pagina  per amministratori
            return;
        }

        try {
            UtenteDAO utenteDao = SoccorsoDataLayer.getInstance().getUtenteDAO();
            Map<String, Object> dataModel = new HashMap<>();
            
            // Passiamo a FreeMarker le 3 liste che servono per la pagina
            dataModel.put("operatori", utenteDao.estraiTuttiGliOperatoriConStato());
            dataModel.put("abilita", utenteDao.estraiTutteLeAbilita());
            dataModel.put("patenti", utenteDao.estraiTutteLePatenti());
            dataModel.put("request", request);

            TemplateManager.process("gestione_operatori.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento della dashboard operatori...", e);
        }
    }
}    