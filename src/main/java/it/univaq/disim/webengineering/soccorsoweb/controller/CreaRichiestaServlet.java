package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import it.univaq.disim.webengineering.soccorsoweb.dao.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.RichiestaSoccorso;
import it.univaq.disim.webengineering.soccorsoweb.util.NotificaService;
import it.univaq.disim.webengineering.soccorsoweb.util.TemplateManager; 
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HexFormat;
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

    private static final Logger LOGGER = Logger.getLogger(CreaRichiestaServlet.class.getName());

    // tempo minimo tra due richieste dalla stessa email o dallo stesso IP
    private static final int FINESTRA_ANTISPAM_MINUTI = 5;

    // limiti coerenti con le colonne del database (nome 100, email 150, posizione 255)
    private static final int MAX_NOME = 100;
    private static final int MAX_EMAIL = 150;
    private static final int MAX_POSIZIONE = 255;
    private static final int MAX_DESCRIZIONE = 2000;

    // controllo di forma volutamente semplice: qualcosa@dominio.estensione, senza spazi
    private static final java.util.regex.Pattern FORMATO_EMAIL
            = java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // un solo generatore crittograficamente sicuro, riusato per tutte le richieste
    private static final SecureRandom GENERATORE_SICURO = new SecureRandom();

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

        // eliminiamo gli spazi ai lati, poi controlliamo lunghezze e formato dell'email
        nomeInserito = nomeInserito.trim();
        emailInserita = emailInserita.trim();
        posizioneInserita = posizioneInserita.trim();
        descrizioneInserita = descrizioneInserita.trim();

        if (nomeInserito.length() > MAX_NOME
                || emailInserita.length() > MAX_EMAIL
                || posizioneInserita.length() > MAX_POSIZIONE
                || descrizioneInserita.length() > MAX_DESCRIZIONE
                || !FORMATO_EMAIL.matcher(emailInserita).matches()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=dati_non_validi");
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
        // il captcha è monouso: lo consumiamo a ogni tentativo, giusto o sbagliato,
        // così non si può indovinare riprovando all'infinito la stessa domanda
        // (la pagina del form ne richiede uno nuovo ogni volta che viene ricaricata)
        sessioneAttuale.removeAttribute("captchaRisultato");
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

        // anti-spam: niente richieste consecutive dalla stessa email o dallo stesso IP
        RichiestaSoccorsoDAO controlloSpamDao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
        if (controlloSpamDao.esisteRichiestaRecente(emailInserita, ipOrigine, FINESTRA_ANTISPAM_MINUTI)) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=troppe_richieste");
            return;
        }

        // token di convalida: 128 bit casuali generati con SecureRandom (32 caratteri esadecimali),
        // quindi impossibile da indovinare (prima erano solo 1 milione di valori con Random)
        byte[] byteCasuali = new byte[16];
        GENERATORE_SICURO.nextBytes(byteCasuali);
        String tokenConvalida = HexFormat.of().formatHex(byteCasuali);

        boolean salvataggioRiuscito = false;

        try {
            RichiestaSoccorso nuovaRichiesta = new RichiestaSoccorso();
            nuovaRichiesta.setNomeSegnalante(nomeInserito);
            nuovaRichiesta.setEmailSegnalante(emailInserita);
            nuovaRichiesta.setPosizione(posizioneInserita);
            nuovaRichiesta.setDescrizione(descrizioneInserita);
            nuovaRichiesta.setIpOrigine(ipOrigine);
            nuovaRichiesta.setTokenConvalida(tokenConvalida);

            RichiestaSoccorsoDAO richiestaDao = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO();
            salvataggioRiuscito = richiestaDao.salvaNuovaRichiesta(nuovaRichiesta);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
        }

        if (salvataggioRiuscito) {
            String linkConvalida = request.getScheme() + "://" + request.getServerName() + ":"
                    + request.getServerPort() + request.getContextPath()
                    + "/ConvalidaServlet?token=" + tokenConvalida;

            NotificaService.inviaEmail(emailInserita, "Conferma la tua richiesta di soccorso",
                    "Ciao " + nomeInserito + ", conferma la richiesta cliccando qui:\n" + linkConvalida);

            try {
                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("email", emailInserita);
                dataModel.put("token", tokenConvalida);
                dataModel.put("request", request); 

                TemplateManager.process("richiesta_inviata.ftl", dataModel, response, getServletContext());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Errore imprevisto", e);
                response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=rendering");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=salvataggio");
        }
    }
}