package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.UtenteDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.Utenti;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Init pulitissimo. Il DBManager carica il driver per conto suo.
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se qualcuno prova ad accedere a /LoginServlet scrivendolo nella barra dell'URL (GET), 
        // lo rimbalziamo brutalmente alla pagina di login HTML.
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Tiriamo giù l'input dal form HTML
        String emailInserita = request.getParameter("email");
        String passwordInserita = request.getParameter("password");

        boolean loginEffettuato = false;
        String ruoloUtente = "";

        try {
            // Niente più SQL in giro, chiamo solo il DAO!
            UtenteDAO utenteDao = new UtenteDAO();
            Utenti utenteTrovato = utenteDao.estraiUtentePerEmail(emailInserita);

            // Se il DAO ha trovato qualcuno con questa email, procedo col check della password
            if (utenteTrovato != null) {

                // Magia di jBcrypt: confronta la password in chiaro con l'hash salvato nel DB
                if (passwordInserita.equals(utenteTrovato.getPassword())) {
                    loginEffettuato = true;
                    ruoloUtente = utenteTrovato.getRuolo();

                    // Creiamo la sessione (passando true) e ci salviamo dentro i dati utili
                    HttpSession sessioneAttuale = request.getSession(true);
                    sessioneAttuale.setAttribute("id_utente", utenteTrovato.getIdUtente());
                    sessioneAttuale.setAttribute("nome", utenteTrovato.getNome());
                    sessioneAttuale.setAttribute("ruolo", ruoloUtente);
                }
            }
        } catch (Exception e) {
            System.err.println("Panico nel controller durante la fase di login...");
            e.printStackTrace();
        }

        // Smistamento da vigile urbano: decido dove mandarti in base a chi sei
        if (loginEffettuato) {
            if ("ADMIN".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            } else if ("OPERATORE".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
            } else {
                // Sicurezza: ruolo non riconosciuto
                response.sendRedirect(request.getContextPath() + "/login.html?errore=ruolo");
            }
        } else {
            // Se fallisce (email o pass sbagliate), rimando alla login con errore
            response.sendRedirect(request.getContextPath() + "/login.html?errore=credenziali");
        }
    }
}
