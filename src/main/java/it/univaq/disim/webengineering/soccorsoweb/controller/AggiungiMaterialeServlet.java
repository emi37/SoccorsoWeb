package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.Materiale;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AggiungiMateriale")
public class AggiungiMaterialeServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
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
            // Estraiamo i parametri che ci ha spedito il form HTML
            String nomeInserito = request.getParameter("nome");
            String descrizioneInserita = request.getParameter("descrizione");

            //  non salva roba vuota
            if (nomeInserito != null && !nomeInserito.trim().isEmpty()) {

                Materiale nuovoMateriale = new Materiale();
                nuovoMateriale.setNome(nomeInserito);
                nuovoMateriale.setDescrizione(descrizioneInserita);

                // Salvataggio tramite DAO
                MaterialeDAO materialeDao = new MaterialeDAO();
                materialeDao.salvaNuovoMateriale(nuovoMateriale);
            }
        } catch (Exception e) {
            System.err.println("Panico nel controller durante l'aggiunta del materiale...");
            e.printStackTrace();
        }

        // Redirect  verso la lista
        response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
    }
}