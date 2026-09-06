package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.UtenteDAO;
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

@WebServlet(name = "GestioneRichiestaServletDallAdmin", urlPatterns = {"/GestioneRichiestaServletDallAdmin"})
public class GestioneRichiestaServletDallAdmin extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));

            RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
            UtenteDAO utenteDao = new UtenteDAO();
            MezzoDAO mezzoDao = new MezzoDAO();
            MaterialeDAO materialeDao = new MaterialeDAO();

            Map<String, String> dettagliRichiesta = richiestaDao.estraiDettagliRichiesta(idRichiesta);
            List<Map<String, String>> operatoriLiberi = utenteDao.estraiOperatoriDisponibili();
            List<Map<String, String>> mezziLiberi = mezzoDao.estraiMezziDisponibili();
            List<Map<String, String>> materialiLiberi = materialeDao.estraiMaterialiDisponibili();

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("richiesta", dettagliRichiesta);
            dataModel.put("operatori", operatoriLiberi);
            dataModel.put("mezzi", mezziLiberi);
            dataModel.put("materiali", materialiLiberi);
            dataModel.put("request", request);

            TemplateManager.process("gestione_richiesta.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            System.err.println("Errore catastrofico durante il caricamento della pagina di assegnazione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));
            
            String[] operatoriScelti = request.getParameterValues("id_operatore");
            String caposquadraScelto = request.getParameter("id_caposquadra");
            String[] mezziScelti = request.getParameterValues("id_mezzo");
            String[] materialiScelti = request.getParameterValues("id_materiale");

            // Ora basta che sia selezionato almeno il Caposquadra!
            if (caposquadraScelto != null && !caposquadraScelto.trim().isEmpty()) {
                
                RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
                Map<String, String> dettagliRichiesta = richiestaDao.estraiDettagliRichiesta(idRichiesta);
                
                // Fallback sicuri in caso non trovino i dati dalla mappa
                String obiettivo = dettagliRichiesta.getOrDefault("descrizione", "Intervento di soccorso");
                String posizione = dettagliRichiesta.getOrDefault("posizione", "Posizione sconosciuta");

                // Estrazione sicura dell'ID Admin (Niente più ClassCastException silenziose)
                Object objId = sessioneAttuale.getAttribute("id_utente");
                int idAdmin = 1; // ID di default in caso di emergenza
                if (objId != null) {
                    idAdmin = Integer.parseInt(objId.toString());
                }

                MissioneDAO missioneDao = new MissioneDAO();
                boolean avvioOk = missioneDao.creaMissioneInTransazione(
                        idRichiesta, 
                        obiettivo, 
                        posizione, 
                        Integer.parseInt(caposquadraScelto), 
                        operatoriScelti, 
                        mezziScelti, 
                        materialiScelti, 
                        idAdmin
                );
                
                if (avvioOk) {
                    response.sendRedirect(request.getContextPath() + "/DashboardServlet?msg=missione_avviata");
                    return;
                }
            }
            
            // Se fallisce, stampiamo l'errore nell'URL così te ne accorgi e non va solo a vuoto
            response.sendRedirect(request.getContextPath() + "/GestioneRichiestaServletDallAdmin?id_richiesta=" + idRichiesta + "&errore=dati_mancanti");

        } catch (Exception e) {
            System.err.println("Panico totale durante la POST per avviare la missione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=critico");
        }
    }
}