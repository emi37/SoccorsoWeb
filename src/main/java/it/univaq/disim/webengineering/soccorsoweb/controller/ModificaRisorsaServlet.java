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

/** Modifica nome e descrizione di un mezzo o di un materiale (solo ADMIN, solo POST). */
@WebServlet(name = "ModificaRisorsaServlet", urlPatterns = {"/ModificaRisorsa"})
public class ModificaRisorsaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ModificaRisorsaServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession sessione = request.getSession(false);
        if (sessione == null || !"ADMIN".equals(sessione.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String tipo = request.getParameter("tipo");
        String ritorno = "materiale".equals(tipo) ? "/GestioneMateriali" : "/GestioneMezzi";
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String nome = request.getParameter("nome");
            String descrizione = request.getParameter("descrizione");
            if (nome != null && !nome.trim().isEmpty() && nome.trim().length() <= 100
                    && descrizione != null && descrizione.trim().length() <= 255) {
                if ("mezzo".equals(tipo)) {
                    SoccorsoDataLayer.getInstance().getMezzoDAO().modificaMezzo(id, nome.trim(), descrizione.trim());
                } else if ("materiale".equals(tipo)) {
                    SoccorsoDataLayer.getInstance().getMaterialeDAO().modificaMateriale(id, nome.trim(), descrizione.trim());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nella modifica risorsa", e);
        }
        response.sendRedirect(request.getContextPath() + ritorno);
    }
}
