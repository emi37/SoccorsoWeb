package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet(name = "CreaRichiestaServlet", urlPatterns = {"/CreaRichiestaServlet"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 5,       // 5MB
    maxRequestSize = 1024 * 1024 * 10    // 10MB
)
public class CreaRichiestaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Imposta la codifica corretta per evitare caratteri strani con il multipart
        request.setCharacterEncoding("UTF-8");

        // Recupero dei parametri standard tramite getParameter
        String nome = request.getParameter("nome_segnalante");
        String email = request.getParameter("email_segnalante");
        String posizione = request.getParameter("posizione");
        String descrizione = request.getParameter("descrizione");
        String captcha = request.getParameter("captcha");
        String ipOrigine = request.getRemoteAddr();

        // Controllo captcha: 3 + 4 = 7
        if (captcha == null || !captcha.trim().equals("7")) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html><html lang='it'><head><meta charset='UTF-8'><title>Captcha errato</title></head>");
                out.println("<body style='font-family: Arial; text-align: center; padding-top: 50px;'>");
                out.println("<h1 style='color: red;'>Captcha errato</h1><p>La risposta al controllo anti-spam non è corretta.</p>");
                out.println("<br><a href='index.html'>Torna al form</a></body></html>");
            }
            return;
        }

        // Gestione salvataggio fisico della Foto opzionale
        String nomeFileSalvato = null;
        try {
            Part filePart = request.getPart("foto");
            if (filePart != null && filePart.getSize() > 0) {
                String subNomeFile = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                nomeFileSalvato = UUID.randomUUID().toString() + "_" + subNomeFile;
                
                String percorsoUpload = getServletContext().getRealPath("") + File.separator + "uploads";
                File directoryUpload = new File(percorsoUpload);
                if (!directoryUpload.exists()) {
                    directoryUpload.mkdir();
                }
                filePart.write(percorsoUpload + File.separator + nomeFileSalvato);
            }
        } catch (Exception ex) {
            nomeFileSalvato = null;
        }

        String tokenConvalida = UUID.randomUUID().toString();
        boolean salvato = false;

        try (Connection conn = DBManager.getConnection()) {

            /* COMMENTATO TEMPORANEMENTE TUTTO LO SCUDO ANTISPAM PER I TEST LOCALHOST   */
            String sqlSpamMisto = """
                SELECT COUNT(*)
                FROM richiesta_soccorso
                WHERE (email_segnalante = ? OR ip_origine = ?)
                  AND (timestamp_creazione >= NOW() - INTERVAL 10 MINUTE OR stato IN ('ATTIVA', 'IN_CORSO'))
            """;

            try (PreparedStatement spam_statement = conn.prepareStatement(sqlSpamMisto)) {
                spam_statement.setString(1, email);
                spam_statement.setString(2, ipOrigine);

                try (ResultSet spam_risultato = spam_statement.executeQuery()) {
                    if (spam_risultato.next() && spam_risultato.getInt(1) > 0) {
                        response.setContentType("text/html;charset=UTF-8");
                        try (PrintWriter out = response.getWriter()) {
                            out.println("<!DOCTYPE html><html lang='it'><head><meta charset='UTF-8'><title>Richiesta bloccata</title></head>");
                            out.println("<body style='font-family: Arial; text-align: center; padding-top: 50px;'>");
                            out.println("<h1 style='color: red;'>Richiesta bloccata</h1><p>Hai già una segnalazione attiva o hai provato a inviare un duplicato negli ultimi 10 minuti dal tuo indirizzo IP o e-mail.</p>");
                            out.println("<br><a href='index.html'>Torna al form</a></body></html>");
                        }
                        return;
                    }
                }
            }
           /* */

            // Inserimento della richiesta includendo la colonna della foto
            String sql = "INSERT INTO richiesta_soccorso (descrizione, posizione, nome_segnalante, email_segnalante, ip_origine, token_convalida, stato, foto) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'IN_ATTESA', ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, descrizione);
                stmt.setString(2, posizione);
                stmt.setString(3, nome);
                stmt.setString(4, email);
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

        // Output simulazione email
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html><head><title>Simulazione casella e-mail</title></head>");
            out.println("<body style='font-family: Arial; padding: 40px; background-color: #f4f4f9;'>");
            if (salvato) {
                out.println("<div style='border: 2px dashed #0056b3; padding: 20px; background-color: white; max-width: 600px; margin: auto;'>");
                out.println("<h3 style='color: #0056b3;'>📩 Abbiamo inviato un e-mail a: " + email + "</h3>");
                out.println("<p>Ciao <b>" + nome + "</b>,</p>");
                out.println("<p>Abbiamo ricevuto la sua richiesta di soccorso la quale <b>è ancora in attesa di convalida</b>.</p>");
                out.println("<p>Per confermarla e renderla visibile alla centrale operativa, clicca sul pulsante qui sotto:</p>");

                String linkConvalida = request.getContextPath() + "/ConvalidaServlet?token=" + tokenConvalida;
                out.println("<br><a href='" + linkConvalida + "' style='background-color: #28a745; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>CONVALIDA RICHIESTA</a></div>");
            } else {
                out.println("<h2 style='color: red;'>Errore: impossibile salvare la richiesta nel database.</h2>");
            }
            out.println("</body></html>");
        }
    }
}