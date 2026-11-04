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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //   controllo che sia un operatore
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"OPERATORE".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idOperatore = Integer.parseInt(sessioneAttuale.getAttribute("id_utente").toString());

            // chiamo i DAO per recuperare le missioni
            MissioneDAO missioneDao = new MissioneDAO();
            List<Map<String, String>> mieMissioni = missioneDao.estraiMissioniAttivePerOperatore(idOperatore);
            // Recupero anche lo storico completo dell'operatore
            List<Map<String,String>> mioStorico = missioneDao.estraiMissioniPerOperatore((int) idOperatore);
            
            //Prepara i dati per freemarker
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("request", request);
            dataModel.put("missioniInCorso", mieMissioni);
            dataModel.put("storicoMissioni", mioStorico);
            
            // costruisce l'oggetto "utente" 
            Map<String, String> datiUtente = new HashMap<>();
            Object nomeObj = sessioneAttuale.getAttribute("nome");
            Object cognomeObj = sessioneAttuale.getAttribute("cognome");
            datiUtente.put("nome", nomeObj != null ? nomeObj.toString() : "Operatore");
            datiUtente.put("cognome", cognomeObj != null ? cognomeObj.toString() : "");

            //calcolo dello stato
            datiUtente.put("stato_attuale", (mieMissioni != null && !mieMissioni.isEmpty()) ? "IMPEGNATO" : "LIBERO");
            
            dataModel.put("utente", datiUtente);

            //estare le patenti e le abilità dell'operatore + quelle disponibili per le 2 tendine
            List<String> miePatenti = missioneDao.estraiPatentiOperatore(idOperatore);
            List<Map<String, String>> tutteLePatenti = missioneDao.estraiPatentiMancanti(idOperatore);
            dataModel.put("miePatenti", miePatenti);
            dataModel.put("tutteLePatenti", tutteLePatenti);

            List<String> mieAbilita = missioneDao.estraiAbilitaOperatore(idOperatore);
            List<Map<String, String>> tutteLeAbilita = missioneDao.estraiAbilitaMancanti(idOperatore);
            dataModel.put("mieAbilita", mieAbilita);
            dataModel.put("tutteLeAbilita", tutteLeAbilita);
            
            
            
            TemplateManager.process("dashboard_operatore.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"OPERATORE".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idOperatore = Integer.parseInt(sessioneAttuale.getAttribute("id_utente").toString());
            MissioneDAO missioneDao = new MissioneDAO();

            // Se l'operatore ha inviato il form della patente
            String idPatenteStr = request.getParameter("id_patente");
            if (idPatenteStr != null && !idPatenteStr.isEmpty()) {
                int idPatente = Integer.parseInt(idPatenteStr);
                missioneDao.aggiungiPatenteOperatore(idOperatore, idPatente);
            }

            // Se l'operatore ha inviato il form dell'abilità
            String idAbilitaStr = request.getParameter("id_abilita"); //gli passo l'etichetta e gli dico estrai il valore con getParameter
            if (idAbilitaStr != null && !idAbilitaStr.isEmpty()) {
                int idAbilita = Integer.parseInt(idAbilitaStr);
                missioneDao.aggiungiAbilitaOperatore(idOperatore, idAbilita); //gli passo il valore
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
    }
}

