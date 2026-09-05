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
                
                // CORREZIONE ERRORE: Ora riceviamo l'ID (long), non più un boolean
                long idNuovoUtente = utenteDao.salvaNuovoUtente(nuovoUtente);

                if (idNuovoUtente > 0) {
                    inserimentoRiuscito = true;

                    // Se è un operatore, salviamo anche le sue patenti e abilità
                    if ("OPERATORE".equals(ruoloScelto)) {
                        
                        // Estraiamo gli array di checkbox dal form HTML
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
            System.err.println("Panico nel controller durante la creazione del nuovo utente...");
            e.printStackTrace();
        }

        // Redirect in base all'esito
        if (inserimentoRiuscito) {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_ok.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_errore.html");
        }
    }
}