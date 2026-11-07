package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.UtenteDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.NotificaService;
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

    // lunghezza massima di obiettivo e posizione di una missione
    private static final int LUNGHEZZA_MASSIMA = 255;
     
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
            String codiceErrore = request.getParameter("errore");
            if ("dati_mancanti".equals(codiceErrore)) {
                dataModel.put("errore", "Seleziona almeno il caposquadra (il pallino accanto a un operatore).");
            } else if ("non_disponibile".equals(codiceErrore)) {
                dataModel.put("errore", "Missione non creata: la richiesta e' gia' stata gestita oppure qualche operatore, mezzo o materiale scelto e' stato impegnato in un'altra missione. Ricontrolla le risorse libere e riprova.");
            }

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
        request.setCharacterEncoding("UTF-8");

        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))
                || sessioneAttuale.getAttribute("id_utente") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));
            // l'admin che avvia la missione e' quello della sessione (viene salvato al login)
            int idAdmin = Integer.parseInt(sessioneAttuale.getAttribute("id_utente").toString());

            String[] operatoriScelti = request.getParameterValues("id_operatore");
            String caposquadraScelto = request.getParameter("id_caposquadra");
            String[] mezziScelti = request.getParameterValues("id_mezzo");
            String[] materialiScelti = request.getParameterValues("id_materiale");

            // il caposquadra e' obbligatorio
            if (caposquadraScelto == null || caposquadraScelto.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/GestioneRichiestaServletDallAdmin?id_richiesta=" + idRichiesta + "&errore=dati_mancanti");
                return;
            }

            RichiestaSoccorsoDAO richiestaDao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
            Map<String, String> dettagliRichiesta = richiestaDao.estraiDettagliRichiesta(idRichiesta);
            if (dettagliRichiesta == null) {
                dettagliRichiesta = new HashMap<>();
            }

            // obiettivo e posizione: se l'admin li ha scritti nel form si usano quelli, altrimenti
            // si parte da quelli della richiesta (descrizione e indirizzo del segnalante)
            String obiettivo = valoreOppure(request.getParameter("obiettivo"),
                    dettagliRichiesta.getOrDefault("descrizione", "Intervento di soccorso"));
            String posizione = valoreOppure(request.getParameter("posizione"),
                    dettagliRichiesta.getOrDefault("posizione", "Posizione sconosciuta"));

            MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();
            int idMissione = missioneDao.creaMissioneInTransazione(
                    idRichiesta,
                    obiettivo,
                    posizione,
                    Integer.parseInt(caposquadraScelto.trim()),
                    operatoriScelti,
                    mezziScelti,
                    materialiScelti,
                    idAdmin
            );

            if (idMissione > 0) {
                // notifica (email) a tutti gli operatori coinvolti, con i dati per partecipare
                NotificaService.notificaOperatori(missioneDao.estraiContattiOperatoriMissione(idMissione),
                        "Nuova missione #" + idMissione + " assegnata",
                        "Sei stato assegnato alla missione #" + idMissione + ".\n\n"
                        + "Obiettivo: " + obiettivo + "\n"
                        + "Posizione: " + posizione + "\n\n"
                        + "Dettagli (accedendo al sito): " + NotificaService.baseUrl(request)
                        + "/DettaglioMissioneServlet?id_missione=" + idMissione);
                response.sendRedirect(request.getContextPath() + "/DashboardServlet?msg=missione_avviata");
                return;
            }

            // la missione non e' stata creata: richiesta gia' presa in carico o ignorata, oppure qualcuno
            // della squadra / un mezzo / un materiale e' stato assegnato a un'altra missione nel frattempo
            response.sendRedirect(request.getContextPath() + "/GestioneRichiestaServletDallAdmin?id_richiesta=" + idRichiesta + "&errore=non_disponibile");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante il POST per avviare la missione...", e);
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=critico");
        }
    }

    // usa il testo scritto dall'admin se non e' vuoto, altrimenti quello di riserva; massimo 255 caratteri
    private static String valoreOppure(String scelto, String riserva) {
        String testo = (scelto == null || scelto.trim().isEmpty()) ? riserva : scelto.trim();
        return testo.length() > LUNGHEZZA_MASSIMA ? testo.substring(0, LUNGHEZZA_MASSIMA) : testo;
    }
}
