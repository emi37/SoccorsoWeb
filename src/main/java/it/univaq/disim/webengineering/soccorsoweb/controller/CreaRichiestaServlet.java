package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico il driver una volta sola all'avvio
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL mancante");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        request.setCharacterEncoding("UTF-8");
        
        // estraggo i dati dal form
        String nome = request.getParameter("nome_segnalante");
        String emailSegnalante = request.getParameter("email_segnalante");
        String posizione = request.getParameter("posizione");
        String descrizione = request.getParameter("descrizione");
        String captchaInserito = request.getParameter("captcha");
        String ipOrigine = request.getRemoteAddr();
        
        // validazione base input obbligatori
        if (nome == null || nome.isBlank()
                || emailSegnalante == null || emailSegnalante.isBlank()
                || posizione == null || posizione.isBlank()
                || descrizione == null || descrizione.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=dati_mancanti");
            return;
        }

        // check sessione captcha
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("captchaRisultato") == null) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_mancante");
            return;
        }

        if (captchaInserito == null || captchaInserito.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_vuoto");
            return;
        }

        // verifica valore captcha
        int captchaCorretto = (int) session.getAttribute("captchaRisultato");
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

        // brucio il captcha per evitare riutilizzi
        session.removeAttribute("captchaRisultato");
        
        String nomeFileSalvato = null;
        
        // gestione file upload (multipart)
        try {
            Part filePart = request.getPart("foto");
            if (filePart != null && filePart.getSize() > 0) {
                String nomeOriginale = Paths.get(filePart.getSubmittedFileName())
                        .getFileName()
                        .toString();
                
                // aggiungo uuid per evitare collisioni nomi
                nomeFileSalvato = UUID.randomUUID().toString() + "_" + nomeOriginale;
                String percorsoUpload = getServletContext().getRealPath("")
                        + File.separator
                        + "uploads";
                File directoryUpload = new File(percorsoUpload);

                if (!directoryUpload.exists()) {
                    directoryUpload.mkdirs();
                }
                filePart.write(percorsoUpload + File.separator + nomeFileSalvato);
            }
        } catch (Exception e) {
            System.err.println("Errore upload file, ignoro e vado avanti");
            e.printStackTrace();
            nomeFileSalvato = null;
        }
        
        String tokenConvalida = UUID.randomUUID().toString();
        boolean salvato = false;
        
        // query concatenata col +
        String sql = "INSERT INTO richiesta_soccorso " +
                     "(descrizione, posizione, nome_segnalante, email_segnalante, ip_origine, token_convalida, stato, foto) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'IN_ATTESA', ?)";

        // apro connessione e preparo lo statement
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            // bind parametri
            stmt.setString(1, descrizione);
            stmt.setString(2, posizione);
            stmt.setString(3, nome);
            stmt.setString(4, emailSegnalante);
            stmt.setString(5, ipOrigine);
            stmt.setString(6, tokenConvalida);
            stmt.setString(7, nomeFileSalvato);
            
            int righeInserite = stmt.executeUpdate();
            if (righeInserite > 0) {
                salvato = true;
            }
            
        } catch (Exception e) {
            System.err.println("Errore inserimento richiesta a db");
            e.printStackTrace();
        }
        
        if (salvato) {
            // assemblo il link di validazione per la mail finta
            String linkConvalida = request.getScheme()
                    + "://"
                    + request.getServerName()
                    + ":"
                    + request.getServerPort()
                    + request.getContextPath()
                    + "/ConvalidaServlet?token="
                    + tokenConvalida;

            // log per emulare invio email
            System.out.println("=====================================");
            System.out.println("SIMULAZIONE EMAIL DI CONVALIDA");
            System.out.println("Destinatario: " + emailSegnalante);
            System.out.println("Ciao " + nome + ", conferma la richiesta cliccando qui:");
            System.out.println(linkConvalida);
            System.out.println("=====================================");
            
            response.sendRedirect(request.getContextPath() + "/richiesta-inviata.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=salvataggio");
        }
    }
}