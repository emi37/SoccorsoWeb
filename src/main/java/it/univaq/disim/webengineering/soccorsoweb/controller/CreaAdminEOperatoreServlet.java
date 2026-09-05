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

@WebServlet(name = "CreaAdminEOperatoreServlet", urlPatterns = {"/CreaAdminEOperatoreServlet"})
public class CreaAdminEOperatoreServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Solita chiamata al padre. Il DBManager pensa a caricare il driver JDBC.
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi: prendo la sessione corrente senza crearne una nuova
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        boolean inserimentoRiuscito = false;

        try {
            // Tiro giù i parametri inviati dal form HTML
            String nomeInserito = request.getParameter("nome");
            String cognomeInserito = request.getParameter("cognome");
            String emailInserita = request.getParameter("email");
            String passwordInserita = request.getParameter("password");
            String ruoloScelto = request.getParameter("ruolo");

            // Controllo ruspante per assicurarmi che ci siano i dati fondamentali
            if (emailInserita != null && passwordInserita != null && ruoloScelto != null) {

                // Hasho la password qui nel controller con jBcrypt prima di passarla al DAO
                String passwordCifrata = BCrypt.hashpw(passwordInserita, BCrypt.gensalt());

                // Assemblo l'oggetto Utente (il Modello)
                Utenti nuovoUtente = new Utenti();
                nuovoUtente.setNome(nomeInserito);
                nuovoUtente.setCognome(cognomeInserito);
                nuovoUtente.setEmail(emailInserita);
                nuovoUtente.setPassword(passwordCifrata);
                nuovoUtente.setRuolo(ruoloScelto);

                // Chiamo il DAO e gli faccio fare la INSERT sporca nel database
                UtenteDAO utenteDao = new UtenteDAO();
                inserimentoRiuscito = utenteDao.salvaNuovoUtente(nuovoUtente);
            }

        } catch (Exception e) {
            // Stampata d'ordinanza a console per beccare le eccezioni al volo
            System.err.println("Panico nel controller durante la creazione del nuovo utente...");
            e.printStackTrace();
        }

        // Pattern PRG: niente forward, ma un bel redirect pulito in base all'esito
        if (inserimentoRiuscito) {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_ok.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_errore.html");
        }
    }
}
