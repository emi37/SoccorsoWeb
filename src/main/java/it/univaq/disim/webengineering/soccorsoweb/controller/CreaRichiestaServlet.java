package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.RichiestaSoccorso;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager; 
import java.io.IOException;
import java.util.Random;
import java.util.HashMap; 
import java.util.Map;   
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CreaRichiestaServlet", urlPatterns = {"/CreaRichiestaServlet"})
public class CreaRichiestaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String nomeInserito = request.getParameter("nome_segnalante");
        String emailInserita = request.getParameter("email_segnalante");
        String posizioneInserita = request.getParameter("posizione");
        String descrizioneInserita = request.getParameter("descrizione");
        String captchaInserito = request.getParameter("captcha");
        String ipOrigine = request.getRemoteAddr();

        if (nomeInserito == null || nomeInserito.trim().isEmpty()
                || emailInserita == null || emailInserita.trim().isEmpty()
                || posizioneInserita == null || posizioneInserita.trim().isEmpty()
                || descrizioneInserita == null || descrizioneInserita.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=dati_mancanti");
            return;
        }

        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || sessioneAttuale.getAttribute("captchaRisultato") == null) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_mancante");
            return;
        }

        if (captchaInserito == null || captchaInserito.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_vuoto");
            return;
        }

        int captchaCorretto = (int) sessioneAttuale.getAttribute("captchaRisultato");
        try {
            // parseInt trasforma una String in un int
            int valoreInserito = Integer.parseInt(captchaInserito);
            if (valoreInserito != captchaCorretto) {
                response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_errato");
                return;
            }
        } catch (Exception e) { 
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_non_valido");
            return;
        }

        sessioneAttuale.removeAttribute("captchaRisultato");

        // genera numero per il token
        Random random = new Random();
        int numeroACaso = random.nextInt(1000000); 
        String tokenConvalida = "TOKEN_" + numeroACaso;
        
        boolean salvataggioRiuscito = false;

        try {
            RichiestaSoccorso nuovaRichiesta = new RichiestaSoccorso();
            nuovaRichiesta.setNomeSegnalante(nomeInserito);
            nuovaRichiesta.setEmailSegnalante(emailInserita);
            nuovaRichiesta.setPosizione(posizioneInserita);
            nuovaRichiesta.setDescrizione(descrizioneInserita);
            nuovaRichiesta.setIpOrigine(ipOrigine);
            nuovaRichiesta.setTokenConvalida(tokenConvalida);

            RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
            salvataggioRiuscito = richiestaDao.salvaNuovaRichiesta(nuovaRichiesta);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (salvataggioRiuscito) {
            String linkConvalida = request.getScheme() + "://" + request.getServerName() + ":"
                    + request.getServerPort() + request.getContextPath()
                    + "/ConvalidaServlet?token=" + tokenConvalida;

            System.out.println("=====================================");
            System.out.println("SIMULAZIONE EMAIL DI CONVALIDA");
            System.out.println("Destinatario: " + emailInserita);
            System.out.println("Ciao " + nomeInserito + ", conferma la richiesta cliccando qui:");
            System.out.println(linkConvalida);
            System.out.println("=====================================");

            try {
                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("email", emailInserita);
                dataModel.put("token", tokenConvalida);
                dataModel.put("request", request); 

                TemplateManager.process("richiesta_inviata.ftl", dataModel, response, getServletContext());
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=rendering");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=salvataggio");
        }
    }
}