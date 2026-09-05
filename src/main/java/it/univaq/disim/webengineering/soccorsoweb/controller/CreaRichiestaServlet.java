package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.model.RichiestaSoccorso;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet(name = "CreaRichiestaServlet", urlPatterns = {"/CreaRichiestaServlet"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 10
)
public class CreaRichiestaServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Solita chiamata pulita al padre, il driver JDBC se lo gestisce il DBManager
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // estraggo i dati dal form html
        String nomeInserito = request.getParameter("nome_segnalante");
        String emailInserita = request.getParameter("email_segnalante");
        String posizioneInserita = request.getParameter("posizione");
        String descrizioneInserita = request.getParameter("descrizione");
        String captchaInserito = request.getParameter("captcha");
        String ipOrigine = request.getRemoteAddr();

        // validazione ruspante degli input obbligatori
        if (nomeInserito == null || nomeInserito.isBlank()
                || emailInserita == null || emailInserita.isBlank()
                || posizioneInserita == null || posizioneInserita.isBlank()
                || descrizioneInserita == null || descrizioneInserita.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=dati_mancanti");
            return;
        }

        // check sessione per il captcha
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || sessioneAttuale.getAttribute("captchaRisultato") == null) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_mancante");
            return;
        }

        if (captchaInserito == null || captchaInserito.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_vuoto");
            return;
        }

        // verifica matematica del captcha calcolato dalla CaptchaServlet
        int captchaCorretto = (int) sessioneAttuale.getAttribute("captchaRisultato");
        try {
            int valoreInserito = Integer.parseInt(captchaInserito);
            if (valoreInserito != captchaCorretto) {
                response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_errato");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_non_valido");
            return;
        }

        // brucio il captcha appena usato per evitare che lo riciclino
        sessioneAttuale.removeAttribute("captchaRisultato");

        String nomeFileSalvato = null;

        // gestione dell'upload file (la foto dell'emergenza)
        try {
            Part parteFoto = request.getPart("foto");
            if (parteFoto != null && parteFoto.getSize() > 0) {
                String nomeOriginale = Paths.get(parteFoto.getSubmittedFileName()).getFileName().toString();

                // ci piazzo un uuid per evitare che due file con lo stesso nome si sovrascrivano
                nomeFileSalvato = UUID.randomUUID().toString() + "_" + nomeOriginale;
                String percorsoUpload = getServletContext().getRealPath("") + File.separator + "uploads";
                File cartellaUpload = new File(percorsoUpload);

                if (!cartellaUpload.exists()) {
                    cartellaUpload.mkdirs();
                }

                // salvo fisicamente l'immagine nella cartella del server
                parteFoto.write(percorsoUpload + File.separator + nomeFileSalvato);
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'upload del file, lo ignoro e vado avanti...");
            e.printStackTrace();
            nomeFileSalvato = null;
        }

        // genero un token finto e brutale per la convalida via mail
        String tokenConvalida = UUID.randomUUID().toString();
        boolean salvataggioRiuscito = false;

        try {
            // Assemblo l'oggetto di dominio per passarlo pulito al DAO
            RichiestaSoccorso nuovaRichiesta = new RichiestaSoccorso();
            nuovaRichiesta.setNomeSegnalante(nomeInserito);
            nuovaRichiesta.setEmailSegnalante(emailInserita);
            nuovaRichiesta.setPosizione(posizioneInserita);
            nuovaRichiesta.setDescrizione(descrizioneInserita);
            nuovaRichiesta.setIpOrigine(ipOrigine);
            nuovaRichiesta.setTokenConvalida(tokenConvalida);

        } catch (Exception e) {
            System.err.println("Panico nel controller durante il salvataggio della richiesta nel DB...");
            e.printStackTrace();
        }

        if (salvataggioRiuscito) {
            // assemblo il link di validazione per la mail finta
            String linkConvalida = request.getScheme() + "://" + request.getServerName() + ":"
                    + request.getServerPort() + request.getContextPath()
                    + "/ConvalidaServlet?token=" + tokenConvalida;

            // log ruspante per emulare invio email come da direttive
            System.out.println("=====================================");
            System.out.println("SIMULAZIONE EMAIL DI CONVALIDA");
            System.out.println("Destinatario: " + emailInserita);
            System.out.println("Ciao " + nomeInserito + ", conferma la richiesta cliccando qui:");
            System.out.println(linkConvalida);
            System.out.println("=====================================");

            // Pattern PRG
            response.sendRedirect(request.getContextPath() + "/richiesta-inviata.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=salvataggio");
        }
    }
}
