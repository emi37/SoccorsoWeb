package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.AbilitaDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MissioneDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DashboardOperatoreServlet", urlPatterns = {"/DashboardOperatoreServlet"})
public class DashboardOperatoreServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Pulizia totale: il DBManager pensa al driver.
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi come un vero vigile urbano
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"OPERATORE".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) sessioneAttuale.getAttribute("id_utente");
        String nomeOperatore = (String) sessioneAttuale.getAttribute("nome");

        // Diciamo al browser che gli spariamo del JSON
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            // Istanzio i nostri due fantastici DAO
            AbilitaDAO abilitaDao = new AbilitaDAO();
            MissioneDAO missioneDao = new MissioneDAO();

            // NOTA: Usa metodi del DAO per leggere le stringhe (GROUP_CONCAT)
            // Se non li hai nel DAO, basta aggiungerli con una banale SELECT!
            String patentiCorrenti = abilitaDao.estraiPatentiFormatoStringa(idOperatore);
            String abilitaCorrenti = abilitaDao.estraiAbilitaFormatoStringa(idOperatore);

            // Mi faccio dare la lista delle missioni già pronta!
            List<Map<String, String>> missioni = missioneDao.estraiMissioniPerOperatore(idOperatore);

            // Buildo il JSON a mano e faccio l'escape INLINE per non creare metodi extra!
            StringBuilder json = new StringBuilder();
            json.append("{");

            String nomePulito = (nomeOperatore == null ? "" : nomeOperatore.replace("\\", "\\\\").replace("\"", "\\\""));
            json.append("\"nomeOperatore\": \"").append(nomePulito).append("\",");

            String patPulite = (patentiCorrenti == null ? "" : patentiCorrenti.replace("\\", "\\\\").replace("\"", "\\\""));
            json.append("\"pat\": \"").append(patPulite).append("\",");

            String abPulite = (abilitaCorrenti == null ? "" : abilitaCorrenti.replace("\\", "\\\\").replace("\"", "\\\""));
            json.append("\"ab\": \"").append(abPulite).append("\",");

            json.append("\"missioni\": [");

            boolean primoGiro = true;
            for (Map<String, String> singolaMissione : missioni) {
                if (!primoGiro) {
                    json.append(",");
                }
                primoGiro = false;

                String obPulito = singolaMissione.get("obiettivo") == null ? "" : singolaMissione.get("obiettivo").replace("\\", "\\\\").replace("\"", "\\\"");
                String posPulita = singolaMissione.get("posizione") == null ? "" : singolaMissione.get("posizione").replace("\\", "\\\\").replace("\"", "\\\"");

                json.append("{");
                json.append("\"id_missione\": ").append(singolaMissione.get("id_missione")).append(",");
                json.append("\"obiettivo\": \"").append(obPulito).append("\",");
                json.append("\"posizione\": \"").append(posPulita).append("\",");
                json.append("\"stato\": \"").append(singolaMissione.get("stato")).append("\",");
                json.append("\"visualizzaVoto\": \"").append(singolaMissione.get("visualizzaVoto")).append("\"");
                json.append("}");
            }

            json.append("]}");

            // sparo il json al client
            out.print(json.toString());

        } catch (Exception e) {
            System.err.println("Errore ruspante durante il fetch dati dell'operatore...");
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"OPERATORE".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int idOperatore = (int) sessioneAttuale.getAttribute("id_utente");

        // input dal form (patenti e abilità separate da virgola)
        String patentiRaw = request.getParameter("patenti");
        String abilitaRaw = request.getParameter("abilita");

        try {
            // Creo gli array al volo gestendo i null
            String[] arrayPatenti = (patentiRaw != null && !patentiRaw.trim().isEmpty()) ? patentiRaw.split(",") : new String[0];
            String[] arrayAbilita = (abilitaRaw != null && !abilitaRaw.trim().isEmpty()) ? abilitaRaw.split(",") : new String[0];

            // Magia totale: passo la palla al DAO che fa tutta la transazione in sicurezza!
            AbilitaDAO abilitaDao = new AbilitaDAO();
            abilitaDao.salvaAbilitaEPatentiOperatore(idOperatore, arrayPatenti, arrayAbilita);

        } catch (Exception e) {
            System.err.println("Panico durante l'aggiornamento delle competenze dell'operatore...");
            e.printStackTrace();
        }

        // pattern PRG (Post-Redirect-Get) per pulire lo stato ed evitare ricaricamenti molesti
        response.sendRedirect(request.getContextPath() + "/operatore/dashboard.html");
    }
}
