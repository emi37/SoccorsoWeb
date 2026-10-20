package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.UtenteDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.Utenti;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se si prova ad accedere a /LoginServlet scrivendolo nella barra dell'URL (GET), 
        // lo rimbalziamo brutalmente alla pagina di login HTML.
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // prendo l'input dal form html o .ftl non ricordo
        String emailInserita = request.getParameter("email");
        String passwordInserita = request.getParameter("password");

        boolean loginEffettuato = false;
        String ruoloUtente = "";

        try {
            UtenteDAO utenteDao = new UtenteDAO();
            Utenti utenteTrovato = utenteDao.estraiUtentePerEmail(emailInserita);

            // Se il DAO ha trovato qualcuno con questa email, fa il check della password
            if (utenteTrovato != null) {

                //confronta la password in chiaro con l'hash salvato nel DB con quella libreria bcrypt
                if (BCrypt.checkpw(passwordInserita, utenteTrovato.getPassword())) {
} {
                    loginEffettuato = true;
                    ruoloUtente = utenteTrovato.getRuolo();

                    // Crea la sessione e ci salvo dentro i dati 
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

        if (loginEffettuato) {
            if ("ADMIN".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            } else if ("OPERATORE".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.html?errore=ruolo");
            }
        } else {
            // Se fallisce (email o pass sbagliate), rimando alla login con errore
            response.sendRedirect(request.getContextPath() + "/login.html?errore=credenziali");
        }
    }
}
