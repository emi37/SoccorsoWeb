package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.UtenteDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.Utenti;
import it.univaq.disim.webengineering.soccorsoweb.util.NotificaService;
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

    private static final Logger LOGGER = Logger.getLogger(CreaAdminEOperatoreServlet.class.getName());

    private static final java.util.regex.Pattern FORMATO_EMAIL
            = java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final java.security.SecureRandom GENERATORE = new java.security.SecureRandom();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        boolean inserimentoRiuscito = false;

        try {
            String nome = trimOrNull(request.getParameter("nome"));
            String cognome = trimOrNull(request.getParameter("cognome"));
            String email = trimOrNull(request.getParameter("email"));
            String password = request.getParameter("password");
            String ruolo = request.getParameter("ruolo");

            boolean valido = nome != null && nome.length() <= 100 && cognome != null && cognome.length() <= 100
                    && email != null && email.length() <= 150 && FORMATO_EMAIL.matcher(email).matches()
                    && ("ADMIN".equals(ruolo) || "OPERATORE".equals(ruolo))
                    && (password == null || password.isEmpty() || password.length() >= 8);

            if (valido) {
                // password lasciata vuota: la genera il sistema e la riceve l'utente per email.
                // Una password scelta dall'admin non viene mai scritta da nessuna parte.
                boolean generata = password == null || password.isEmpty();
                if (generata) {
                    byte[] casuali = new byte[9];
                    GENERATORE.nextBytes(casuali);
                    password = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(casuali);
                }

                Utenti nuovoUtente = new Utenti();
                nuovoUtente.setNome(nome);
                nuovoUtente.setCognome(cognome);
                nuovoUtente.setEmail(email);
                nuovoUtente.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                nuovoUtente.setRuolo(ruolo);

                UtenteDAO utenteDao = SoccorsoDataLayer.getInstance().getUtenteDAO();
                long idNuovoUtente = utenteDao.salvaNuovoUtente(nuovoUtente);

                if (idNuovoUtente > 0) {
                    inserimentoRiuscito = true;

                    // patenti e abilita' valgono per entrambi i ruoli (la specifica le prevede per tutti gli utenti)
                    String[] patenti = request.getParameterValues("patenti");
                    if (patenti != null) {
                        for (String id : patenti) {
                            utenteDao.collegaPatente(idNuovoUtente, Integer.parseInt(id));
                        }
                    }
                    String[] abilita = request.getParameterValues("abilita");
                    if (abilita != null) {
                        for (String id : abilita) {
                            utenteDao.collegaAbilita(idNuovoUtente, Integer.parseInt(id));
                        }
                    }

                    NotificaService.inviaEmail(email, "Account SoccorsoWeb creato",
                            "Ciao " + nome + ", e' stato creato il tuo account (" + ruolo + ").\n"
                            + "Accedi qui: " + NotificaService.baseUrl(request) + "/login.html\n"
                            + (generata ? "Password iniziale: " + password + "\nCambiala appena possibile."
                                    : "La password e' stata scelta dall'amministratore."));
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
        }

        if (inserimentoRiuscito) {
            response.sendRedirect(request.getContextPath() + "/GestioneOperatori");
        } else {
            response.sendRedirect(request.getContextPath() + "/CreazioneUtente_errore.html");
        }
    }

    private static String trimOrNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
