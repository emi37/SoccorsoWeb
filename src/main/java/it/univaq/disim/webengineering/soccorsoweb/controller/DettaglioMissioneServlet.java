package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.UtenteDAO;
import it.univaq.disim.webengineering.soccorsoweb.dao.AggiornamentoMissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.NotificaService;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "DettaglioMissioneServlet", urlPatterns = {"/DettaglioMissioneServlet"})
public class DettaglioMissioneServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DettaglioMissioneServlet.class.getName());

    private static final int MAX_LUNGHEZZA_AGGIORNAMENTO = 1000;
    private static final int MAX_LUNGHEZZA_CAMPO = 255;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || sessioneAttuale.getAttribute("ruolo") == null || sessioneAttuale.getAttribute("id_utente") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String ruoloLoggato = (String) sessioneAttuale.getAttribute("ruolo");
        if (!"ADMIN".equals(ruoloLoggato) && !"OPERATORE".equals(ruoloLoggato)) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        boolean admin = "ADMIN".equals(ruoloLoggato);
        String dashboardRitorno = admin ? "/DashboardServlet" : "/DashboardOperatoreServlet";

        try {
            int idDellaMissione = Integer.parseInt(request.getParameter("id_missione"));
            int idUtente = Integer.parseInt(sessioneAttuale.getAttribute("id_utente").toString());

            MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();

            // un operatore puo' vedere solo le missioni a cui partecipa (o ha partecipato)
            if (!admin && !missioneDao.operatoreCoinvoltoInMissione(idDellaMissione, idUtente)) {
                response.sendRedirect(request.getContextPath() + dashboardRitorno);
                return;
            }

            Map<String, Object> dettagliMissione = missioneDao.estraiDettagliCompleti(idDellaMissione);

            if (dettagliMissione != null && !dettagliMissione.isEmpty()) {
                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("request", request);
                dataModel.put("missione", dettagliMissione);
                dataModel.put("aggiornamenti", SoccorsoDataLayer.getInstance().getAggiornamentoMissioneDAO().estraiCronistoria(idDellaMissione));
                dataModel.put("ruolo_utente", ruoloLoggato);
                dataModel.put("dashboard_ritorno", dashboardRitorno);

                // le risorse libere servono solo al form di modifica, che vede solo l'admin
                if (admin) {
                    dataModel.put("operatori_disponibili", SoccorsoDataLayer.getInstance().getUtenteDAO().estraiOperatoriDisponibili());
                    dataModel.put("mezzi_disponibili", SoccorsoDataLayer.getInstance().getMezzoDAO().estraiMezziDisponibili());
                    dataModel.put("materiali_disponibili", SoccorsoDataLayer.getInstance().getMaterialeDAO().estraiMaterialiDisponibili());
                }

                // messaggi: scelti da un elenco fisso, mai testo preso dall'URL
                String codice = request.getParameter("errore");
                if ("aggiornamento_non_valido".equals(codice)) {
                    dataModel.put("errore", "L'aggiornamento deve contenere tra 1 e " + MAX_LUNGHEZZA_AGGIORNAMENTO + " caratteri.");
                } else if ("non_modificabile".equals(codice)) {
                    dataModel.put("errore", "Operazione non eseguita: la missione e' gia' conclusa oppure alcune risorse scelte non sono piu' libere.");
                } else if ("dati_non_validi".equals(codice)) {
                    dataModel.put("errore", "Obiettivo e posizione sono obbligatori (massimo " + MAX_LUNGHEZZA_CAMPO + " caratteri).");
                }
                String msg = request.getParameter("msg");
                if ("modificata".equals(msg)) {
                    dataModel.put("messaggio", "Missione modificata.");
                } else if ("aggiornamento_salvato".equals(msg)) {
                    dataModel.put("messaggio", "Aggiornamento aggiunto alla cronistoria.");
                }

                TemplateManager.process("dettaglio_missione.ftl", dataModel, response, getServletContext());
            } else {
                response.sendRedirect(request.getContextPath() + dashboardRitorno);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
            response.sendRedirect(request.getContextPath() + dashboardRitorno);
        }
    }

    // Le azioni di modifica sono riservate all'ADMIN (prima bastava essere loggati, anche da operatore).
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo")) || sessioneAttuale.getAttribute("id_utente") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String azione = request.getParameter("azione");
        int idDellaMissione;
        try {
            idDellaMissione = Integer.parseInt(request.getParameter("id_missione"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            return;
        }
        String ritorno = request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idDellaMissione;

        try {
            int idAdmin = Integer.parseInt(sessioneAttuale.getAttribute("id_utente").toString());
            MissioneDAO missioneDao = SoccorsoDataLayer.getInstance().getMissioneDAO();
            String statoAttuale = missioneDao.estraiStatoMissione(idDellaMissione);

            if (!"IN_CORSO".equals(statoAttuale)) {
                response.sendRedirect(ritorno + "&errore=non_modificabile");
                return;
            }

            if ("modifica_missione".equals(azione)) {
                String nuovoObiettivo = request.getParameter("obiettivo");
                String nuovaPosizione = request.getParameter("posizione");
                if (nuovoObiettivo == null || nuovoObiettivo.trim().isEmpty() || nuovoObiettivo.trim().length() > MAX_LUNGHEZZA_CAMPO
                        || nuovaPosizione == null || nuovaPosizione.trim().isEmpty() || nuovaPosizione.trim().length() > MAX_LUNGHEZZA_CAMPO) {
                    response.sendRedirect(ritorno + "&errore=dati_non_validi");
                    return;
                }
                String[] nuoviOperatori = request.getParameterValues("id_operatore");
                String[] nuoviMezzi = request.getParameterValues("id_mezzo");
                String[] nuoviMateriali = request.getParameterValues("id_materiale");

                boolean ok = missioneDao.modificaMissioneInCorso(
                        idDellaMissione, nuovoObiettivo.trim(), nuovaPosizione.trim(),
                        nuoviOperatori, nuoviMezzi, nuoviMateriali
                );
                if (ok) {
                    NotificaService.notificaOperatori(missioneDao.estraiContattiOperatoriMissione(idDellaMissione),
                            "Missione #" + idDellaMissione + " modificata",
                            "La missione #" + idDellaMissione + " e' stata modificata.\n"
                            + "Obiettivo: " + nuovoObiettivo.trim() + "\nPosizione: " + nuovaPosizione.trim() + "\n"
                            + "Dettagli: " + NotificaService.baseUrl(request) + "/DettaglioMissioneServlet?id_missione=" + idDellaMissione);
                    response.sendRedirect(ritorno + "&msg=modificata");
                } else {
                    response.sendRedirect(ritorno + "&errore=non_modificabile");
                }
                return;
            }

            if ("aggiungi_aggiornamento".equals(azione)) {
                String testo = request.getParameter("testo");
                if (testo == null || testo.trim().isEmpty() || testo.trim().length() > MAX_LUNGHEZZA_AGGIORNAMENTO) {
                    response.sendRedirect(ritorno + "&errore=aggiornamento_non_valido");
                    return;
                }
                AggiornamentoMissioneDAO aggiornamentoDao = SoccorsoDataLayer.getInstance().getAggiornamentoMissioneDAO();
                if (aggiornamentoDao.salvaNuovoAggiornamento(idDellaMissione, idAdmin, testo.trim())) {
                    NotificaService.notificaOperatori(missioneDao.estraiContattiOperatoriMissione(idDellaMissione),
                            "Nuovo aggiornamento sulla missione #" + idDellaMissione,
                            testo.trim() + "\n\nDettagli: " + NotificaService.baseUrl(request)
                            + "/DettaglioMissioneServlet?id_missione=" + idDellaMissione);
                    response.sendRedirect(ritorno + "&msg=aggiornamento_salvato");
                } else {
                    response.sendRedirect(ritorno + "&errore=non_modificabile");
                }
                return;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
        }

        response.sendRedirect(ritorno);
    }
}
