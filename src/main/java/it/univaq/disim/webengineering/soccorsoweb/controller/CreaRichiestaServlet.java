package it.univaq.disim.webengineering.soccorsoweb.controller;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
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

    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String nome = request.getParameter("nome_segnalante");
        String emailSegnalante = request.getParameter("email_segnalante");
        String posizione = request.getParameter("posizione");
        String descrizione = request.getParameter("descrizione");
        String captchaInserito = request.getParameter("captcha");
        String ipOrigine = request.getRemoteAddr();
        if (nome == null || nome.isBlank()
                || emailSegnalante == null || emailSegnalante.isBlank()
                || posizione == null || posizione.isBlank()
                || descrizione == null || descrizione.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=dati_mancanti");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("captchaRisultato") == null) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_mancante");
            return;
        }

        if (captchaInserito == null || captchaInserito.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/richiesta.html?errore=captcha_vuoto");
            return;
        }

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

        session.removeAttribute("captchaRisultato");
        String nomeFileSalvato = null;
        try {
            Part filePart = request.getPart("foto");
            if (filePart != null && filePart.getSize() > 0) {
                String nomeOriginale = Paths.get(filePart.getSubmittedFileName())
                        .getFileName()
                        .toString();
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
            nomeFileSalvato = null;
        }
        String tokenConvalida = UUID.randomUUID().toString();
        boolean salvato = false;
        try (Connection conn = DBManager.getConnection()) {

            String sql = """
                INSERT INTO richiesta_soccorso
                (descrizione, posizione, nome_segnalante, email_segnalante, ip_origine, token_convalida, stato, foto)
                VALUES (?, ?, ?, ?, ?, ?, 'IN_ATTESA', ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (salvato) {
            String linkConvalida = request.getScheme()
                    + "://"
                    + request.getServerName()
                    + ":"
                    + request.getServerPort()
                    + request.getContextPath()
                    + "/ConvalidaServlet?token="
                    + tokenConvalida;

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