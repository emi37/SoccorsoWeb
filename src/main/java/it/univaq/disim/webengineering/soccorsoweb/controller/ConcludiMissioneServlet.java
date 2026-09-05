package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MissioneDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ConcludiMissioneServlet", urlPatterns = {"/ConcludiMissioneServlet"})
public class ConcludiMissioneServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    // Nota: Ho omesso il doGet perché prima conteneva l'estrazione dati che ora 
    // farà la DettaglioMissioneServlet. Manteniamo solo la POST come da tue regole.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // prelevo i dati dal post inviato dal form HTML
            int idMissione = Integer.parseInt(request.getParameter("id_missione"));
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));
            int livelloSuccesso = Integer.parseInt(request.getParameter("voto_successo"));
            String commenti = request.getParameter("commenti");
            int idAdminLoggato = (int) sessioneAttuale.getAttribute("id_utente");

            // Deleghiamo tutta la mostruosità della transazione (update, update, insert) al DAO
            MissioneDAO missioneDao = new MissioneDAO();
            boolean terminataOk = missioneDao.chiudiMissioneInTransazione(idMissione, idRichiesta, livelloSuccesso, commenti, idAdminLoggato);

            // Setto il messaggio per la UI 
            sessioneAttuale = request.getSession(true);
            if (terminataOk) {
                sessioneAttuale.setAttribute("messaggioEsito", "Intervento concluso con successo. Report archiviato.");
                response.sendRedirect(request.getContextPath() + "/DettaglioMissioneServlet?id_missione=" + idMissione);
            } else {
                sessioneAttuale.setAttribute("messaggioEsito", "Errore salvataggio report.");
                response.sendRedirect(request.getContextPath() + "/ConcludiMissioneServlet?id_missione=" + idMissione);
            }

        } catch (Exception e) {
            System.err.println("Errore ruspante durante l'elaborazione della chiusura missione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }
}
