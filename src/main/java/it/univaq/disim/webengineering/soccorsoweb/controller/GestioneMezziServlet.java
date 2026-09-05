package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MezzoDAO;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@WebServlet("/GestioneMezzi")
public class GestioneMezziServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            MezzoDAO mezzoDao = new MezzoDAO();

            // se mi passano l'id da eliminare, faccio fare il soft-delete al DAO
            String idDaEliminareStr = request.getParameter("elimina");
            if (idDaEliminareStr != null) {
                int idMezzo = Integer.parseInt(idDaEliminareStr);
                mezzoDao.rimuoviMezzoSeLibero(idMezzo);

                // pulisco l'url facendo un redirect su se stessa
                response.sendRedirect("GestioneMezzi");
                return;
            }

            // mi faccio dare la lista già calcolata e impacchettata dal DAO
            List<Map<String, String>> listaMezzi = mezzoDao.estraiTuttiIMezziConStato();

            // passo i dati e renderizzo usando il template engine
            request.setAttribute("mezzi", listaMezzi);

            // TODO: Qui FreeMarker si prenderà il request.getAttribute("mezzi")
            // Per ora lasciamo il forward classico verso la view
            request.getRequestDispatcher("/WEB-INF/admin/gestione_mezzi.ftl").forward(request, response);

        } catch (Exception e) {
            System.err.println("Errore brutto nel controller della gestione mezzi...");
            e.printStackTrace();
        }
    }
}
