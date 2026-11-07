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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se si prova ad accedere a /LoginServlet scrivendolo nella barra dell'URL (GET), 
        // lo rimbalziamo brutalmente alla pagina di login HTML.
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // prendo l'input dal form html del login
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

                    loginEffettuato = true;
                    ruoloUtente = utenteTrovato.getRuolo();

                    // Crea la sessione e ci salvo dentro i dati 
                    // anti session-fixation: se esisteva già una sessione anonima (ad esempio quella
                    // creata dal captcha) la buttiamo via e ne creiamo una NUOVA con un id diverso,
                    // così un id di sessione noto in anticipo da un attaccante non vale più dopo il login
                    HttpSession sessioneVecchia = request.getSession(false);
                    if (sessioneVecchia != null) {
                        sessioneVecchia.invalidate();
                    }
                    HttpSession sessioneAttuale = request.getSession(true);
                            //request.getSession(true); chiede la sessione dell utente al server, che viene inizializzata in questo file servlet e dice: 
                            //se non c'e' allora creala, metto true solo nella servlet del login perche se non esiste la devo creare, invece in tutte le altre servlet devo
                            // false perceh se non esite non la vadomamcreare, ma semplicemente restituisco null e l utente non puo entrare nella pagina perceh non ha i permessi
                    sessioneAttuale.setAttribute("id_utente", utenteTrovato.getIdUtente());
                    sessioneAttuale.setAttribute("nome", utenteTrovato.getNome());
                    sessioneAttuale.setAttribute("ruolo", ruoloUtente);
                }
            }
        } catch (Exception e) {
            System.err.println("errore durante la fase di login...");
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
            // Se fallisce (email o pass sbagliate), rimando al login con errore
            response.sendRedirect(request.getContextPath() + "/login.html?errore=credenziali") ;
        }
    }
}
  