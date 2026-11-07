package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.UtenteDAO;
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

    private static final Logger LOGGER = Logger.getLogger(GestioneRichiestaServletDallAdmin.class.getName());
     
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
            // questo e' l'id della richiesta dove l utente ha cliccato, posso vedere questo id 
            // anche nella barra dell' URL in alto 

            RichiestaSoccorsoDAO richiestaDao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
            UtenteDAO utenteDao = SoccorsoDataLayer.getInstance().getUtenteDAO();
            MezzoDAO mezzoDao = SoccorsoDataLayer.getInstance().getMezzoDAO();
            MaterialeDAO materialeDao = SoccorsoDataLayer.getInstance().getMaterialeDAO();

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

                        //il data model è il contenitore grosso che contiene tutte le liste dei dati da mostrare tramite " gestione_richiesta.ftl "
                        //cosi' da mostrare tutti i dati estratti all'utente finale 
            TemplateManager.process("gestione_richiesta.ftl", dataModel, response, getServletContext());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della pagina ...", e);
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

            // devo selezionare il caposquadra
            if (caposquadraScelto != null && !caposquadraScelto.trim().isEmpty()) {
                
                RichiestaSoccorsoDAO richiestaDao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
                Map<String, String> dettagliRichiesta = richiestaDao.estraiDettagliRichiesta(idRichiesta);
                                                    //se trova la descrizione dentro la mappa mostra quella, altriement dice di default"Intervento di soccorso"                 
                String obiettivo = dettagliRichiesta.getOrDefault("descrizione", "Intervento di soccorso");
                String posizione = dettagliRichiesta.getOrDefault("posizione", "Posizione sconosciuta");

                //estrazione dell'ID dell'admin, la servlet salva l'id dell'admin una volta che fail login cosi da sapere che è lui
                Object objId = sessioneAttuale.getAttribute("id_utente");
                int idAdmin = 1; // ID di default 
                if (objId != null) {
                    idAdmin = Integer.parseInt(objId.toString());
                }

                MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();
                boolean avvioOk = missioneDao.creaMissioneInTransazione(
                        idRichiesta, 
                        obiettivo, 
                        posizione, 
                        Integer.parseInt(caposquadraScelto), 
                        operatoriScelti, 
                        mezziScelti, 
                        materialiScelti, 
                        idAdmin //passo tutti i parametri che la servlet ha ricevuto dal file ftl, al metodo creaMissioneInTransazione... 
                );
                
                if (avvioOk) {
                    response.sendRedirect(request.getContextPath() + "/DashboardServlet?msg=missione_avviata");
                    return;
                }
            }
            
            // Se fallisce, stampo l'errore nell'URL e non va solo a vuoto 
            response.sendRedirect(request.getContextPath() + "/GestioneRichiestaServletDallAdmin?id_richiesta=" + idRichiesta + "&errore=dati_mancanti");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante il POST per avviare la missione...", e);
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=critico");
        }
    }
}