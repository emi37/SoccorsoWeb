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

        @Override    // controllo classico per vedere se è admin
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        boolean inserimentoRiuscito = false;

        try {
            String nomeInserito = request.getParameter("nome");
            String cognomeInserito = request.getParameter("cognome");
            String emailInserita = request.getParameter("email");
            String passwordInserita = request.getParameter("password");
            String ruoloScelto = request.getParameter("ruolo");

            if (emailInserita != null && passwordInserita != null && ruoloScelto != null) {
                String passwordCifrata = BCrypt.hashpw(passwordInserita, BCrypt.gensalt());

                Utenti nuovoUtente = new Utenti();
                nuovoUtente.setNome(nomeInserito);
                nuovoUtente.setCognome(cognomeInserito);
                nuovoUtente.setEmail(emailInserita);
                nuovoUtente.setPassword(passwordCifrata);
                nuovoUtente.setRuolo(ruoloScelto);

                UtenteDAO utenteDao = new UtenteDAO();
                long idNuovoUtente = utenteDao.salvaNuovoUtente(nuovoUtente);

                if (idNuovoUtente > 0) {
                    inserimentoRiuscito = true;

                    if ("OPERATORE".equals(ruoloScelto)) {
                        String[] patentiSelezionate = request.getParameterValues("patenti");
                        if (patentiSelezionate != null) {
                            for (String idPatenteStr : patentiSelezionate) {
                                utenteDao.collegaPatente(idNuovoUtente, Integer.parseInt(idPatenteStr));
                            }
                        }

                        String[] abilitaSelezionate = request.getParameterValues("abilita");
                        if (abilitaSelezionate != null) {
                            for (String idAbilitaStr : abilitaSelezionate) {
                                utenteDao.collegaAbilita(idNuovoUtente, Integer.parseInt(idAbilitaStr));
                            }
                            }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (inserimentoRiuscito) {
            response.sendRedirect(request.getContextPath() + "/GestioneOperatori");
        } else {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_errore.html");
        }
    }
}