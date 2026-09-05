package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.AggiornamentoMissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.util.GestioneEmail;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DettaglioMissioneServlet", urlPatterns = {"/DettaglioMissioneServlet"})
public class DettaglioMissioneServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Regola d'oro: niente caricamento JDBC qua dentro, ci pensa il DBManager
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Controllo accessi ruspante: entra solo chi è ADMIN
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // Prendo l'ID dalla query string (es. ?id_missione=5)
            int idDellaMissione = Integer.parseInt(request.getParameter("id_missione"));

            // Istanzio i due DAO per estrarre i dati
            MissioneDAO missioneDao = new MissioneDAO();
            AggiornamentoMissioneDAO aggiornamentoDao = new AggiornamentoMissioneDAO();

            // Sfrutto il mega-metodo appena creato nel DAO per avere tutte le info in un colpo solo
            Map<String, Object> dettagliMissione = missioneDao.estraiDettagliCompleti(idDellaMissione);

            // Mi faccio dare la lista degli aggiornamenti temporali
            List<Map<String, String>> cronistoria = aggiornamentoDao.estraiCronistoria(idDellaMissione);

            // Se la missione esiste davvero, procedo con FreeMarker
            if (dettagliMissione != null && !dettagliMissione.isEmpty()) {

                // Piazzo i dati nella request pronti per essere letti dal template engine
                request.setAttribute("missione", dettagliMissione);
                request.setAttribute("timeline", cronistoria);

                // Rimbalzo la palla al file .ftl (FreeMarker farà lui tutto l'HTML!)
                request.getRequestDispatcher("/WEB-INF/template/dettaglio_missione.ftl").forward(request, response);

            } else {
                // Se c'è un errore o non trova nulla, lo rispedisco alla dashboard
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            }

        } catch (Exception e) {
            // Salto nella rete di sicurezza
            System.err.println("Panico nel controller durante la lettura dei dettagli missione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check permessi anche in fase di salvataggio (non ci fidiamo mai del client)
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idMissioneStringa = request.getParameter("id_missione");

        try {
            int idDellaMissione = Integer.parseInt(idMissioneStringa);
            String messaggioDescrittivo = request.getParameter("testo_descrittivo");
            int idAdminLoggato = (int) sessioneAttuale.getAttribute("id_utente");

            MissioneDAO missioneDao = new MissioneDAO();
            AggiornamentoMissioneDAO aggiornamentoDao = new AggiornamentoMissioneDAO();

            // Chiedo al DAO se la missione è ancora 'IN_CORSO' prima di aggiungere un nuovo log
            String statoAttuale = missioneDao.estraiStatoMissione(idDellaMissione);

            if ("IN_CORSO".equals(statoAttuale)) {

                // L'admin ha scritto un messaggio, lo salvo tramite il DAO
                boolean salvatoOk = aggiornamentoDao.salvaNuovoAggiornamento(idDellaMissione, idAdminLoggato, messaggioDescrittivo);

            }

        } catch (Exception e) {
            System.err.println("Disastro totale durante l'inserimento di un log missione...");
            e.printStackTrace();
        }

        // Pattern PRG puro: ricarico la Servlet in GET passandogli l'ID per pulire l'URL
        response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissioneStringa);
    }
}
