package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.Mezzo;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AggiungiMezzo")
public class AggiungiMezzoServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Il caricamento del driver MySQL ora lo fa il DBManager, 
        // ma teniamo il metodo init pulito chiamando il padre come da regola
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // recupero la sessione senza crearne di nuove a vuoto
        HttpSession sessioneAttuale = request.getSession(false);

        // check permessi
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            // estraggo i parametri inviati dal client
            String nomeInserito = request.getParameter("nome");
            String descrizioneInserita = request.getParameter("descrizione");

            if (nomeInserito != null && !nomeInserito.trim().isEmpty()) {

                // Assemblo l'entità
                Mezzo nuovoMezzo = new Mezzo();
                nuovoMezzo.setNome(nomeInserito);
                nuovoMezzo.setDescrizione(descrizioneInserita);

                // Chiamo il DAO che fa il lavoro sporco col database
                MezzoDAO mezzoDao = new MezzoDAO();
                mezzoDao.salvaNuovoMezzo(nuovoMezzo);
            }
        } catch (Exception e) {
            // solita stampata ignorante a console
            System.err.println("Panico nel controller durante l'aggiunta del mezzo...");
            e.printStackTrace();
        }

        // riporto l'utente alla vista generale (PRG pattern)
        response.sendRedirect(request.getContextPath() + "/GestioneMezzi");
    }
}
