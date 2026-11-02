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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessioneAttuale = request.getSession(false);

        
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
           
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            //prendo i parametri inseriti nel form 
            String nomeInserito = request.getParameter("nome");
            String descrizioneInserita = request.getParameter("descrizione");
            
            if (nomeInserito != null && !nomeInserito.trim().isEmpty()) {
                
                //creo il nuovo materiale e ci metto i dati messi nel form 
                Materiale nuovoMateriale = new Materiale();
                nuovoMateriale.setNome(nomeInserito);
                nuovoMateriale.setDescrizione(descrizioneInserita);

                // prendo il nuovo materiale inserito e tramite il metodo salvaNuovoMateriale del DAO lo salvo nel db
                MaterialeDAO materialeDao = new MaterialeDAO();
                materialeDao.salvaNuovoMateriale(nuovoMateriale);
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta del nuovo materiale, riprovare");
            e.printStackTrace();
        }
        
        response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
    }
}