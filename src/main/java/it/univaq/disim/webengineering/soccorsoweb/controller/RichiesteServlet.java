package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Archivio di tutte le richieste di soccorso, con filtro per stato e per periodo e paginazione. */
@WebServlet(name = "RichiesteServlet", urlPatterns = {"/Richieste"})
public class RichiesteServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RichiesteServlet.class.getName());
    private static final int PER_PAGINA = 10;
    private static final List<String> STATI = List.of("ATTIVA", "IN_CORSO", "CHIUSA", "IGNORATA");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessione = request.getSession(false);
        if (sessione == null || !"ADMIN".equals(sessione.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // i filtri arrivano dall'URL: valori non validi vengono semplicemente ignorati
        String stato = request.getParameter("stato");
        if (stato == null || !STATI.contains(stato)) {
            stato = null;
        }
        LocalDate da = leggiData(request.getParameter("da"));
        LocalDate a = leggiData(request.getParameter("a"));
        int pagina = 1;
        try {
            pagina = Math.max(1, Integer.parseInt(request.getParameter("pagina")));
        } catch (Exception e) {
            // resta la prima pagina
        }

        try {
            var dao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
            int totale = dao.contaRichieste(stato, da, a);
            int pagine = Math.max(1, (totale + PER_PAGINA - 1) / PER_PAGINA);
            pagina = Math.min(pagina, pagine);

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("richieste", dao.cercaRichieste(stato, da, a, pagina, PER_PAGINA));
            dataModel.put("stati", STATI);
            dataModel.put("filtro_stato", stato == null ? "" : stato);
            dataModel.put("filtro_da", da == null ? "" : da.toString());
            dataModel.put("filtro_a", a == null ? "" : a.toString());
            dataModel.put("pagina", pagina);
            dataModel.put("pagine", pagine);
            dataModel.put("totale", totale);
            TemplateManager.process("richieste.ftl", dataModel, response, getServletContext());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nell'archivio richieste", e);
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }

    private static LocalDate leggiData(String testo) {
        try {
            return (testo == null || testo.isBlank()) ? null : LocalDate.parse(testo.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
