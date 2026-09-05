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
        // Chiamata alla superclasse d'ordinanza. 
        // Il caricamento del driver MySQL ora se lo smazza il DBManager in autonomia!
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Recuperiamo la sessione senza crearne una nuova a vuoto
        HttpSession sessioneAttuale = request.getSession(false);

        // Check permessi: il vigile urbano blocca chi non è ADMIN
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            // Estraiamo i parametri che ci ha spedito il form HTML
            String nomeInserito = request.getParameter("nome");
            String descrizioneInserita = request.getParameter("descrizione");

            // Check ruspante per non salvare roba vuota
            if (nomeInserito != null && !nomeInserito.trim().isEmpty()) {

                // Ricostruiamo l'oggetto di dominio
                Materiale nuovoMateriale = new Materiale();
                nuovoMateriale.setNome(nomeInserito);
                nuovoMateriale.setDescrizione(descrizioneInserita);
                // Il campo "attivo" viene impostato a 1 (true) direttamente dalla query nel DAO

                // Magia: deleghiamo tutta l'interazione col DB alla classe DAO
                MaterialeDAO materialeDao = new MaterialeDAO();
                materialeDao.salvaNuovoMateriale(nuovoMateriale);
            }
        } catch (Exception e) {
            // Solita stampata brutale a console per il debug se qualcosa va a fuoco
            System.err.println("Panico nel controller durante l'aggiunta del materiale...");
            e.printStackTrace();
        }

        // Finito il giro, ributtiamo l'utente sulla lista pulendo l'URL (Pattern PRG)
        response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
    }
}
