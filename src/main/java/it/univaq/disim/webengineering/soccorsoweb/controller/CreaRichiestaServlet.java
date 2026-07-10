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
        String emailSegnalante = request.getParameter("email_segnalante"); 
        String posizione = request.getParameter("posizione");
        String descrizione = request.getParameter("descrizione");
        String captcha = request.getParameter("captcha");
        String ipOrigine = request.getRemoteAddr();

        // Controllo captcha: 3 + 4 = 7 (Sistemato con layout fluido responsive)
        if (captcha == null || !captcha.trim().equals("7")) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html lang='it'>");
                out.println("<head>");
                out.println("  <meta charset='UTF-8'>");
                out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                out.println("  <title>Captcha errato</title>");
                out.println("</head>");
                out.println("<body style='font-family: Arial, sans-serif; text-align: center; background-color: #f4f6f9; padding: 20px; margin: 0;'>");
                out.println("  <div style='max-width: 500px; width: 100%; margin: 60px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); box-sizing: border-box;'>");
                out.println("    <h1 style='color: #dc3545; margin-top: 0;'>Controllo anti-spam fallito</h1>");
                out.println("    <p style='color: #666; line-height: 1.5;'>La risposta al controllo matematico non è corretta.</p>");
                out.println("    <hr style='border: 0; border-top: 1px solid #dee2e6; margin: 20px 0;'>");
                out.println("    <a href='index.html' style='display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Torna al form</a>");
                out.println("  </div>");
                out.println("</body></html>");
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

            /* ==========================================
               CONTROLLO IP E ANTISPAM COMMENTATO
               ==========================================
            String sqlSpamMisto = """
                SELECT COUNT(*)
                FROM richiesta_soccorso
                WHERE (email_segnalante = ? OR ip_origine = ?)
                  AND (timestamp_creazione >= NOW() - INTERVAL 10 MINUTE OR stato IN ('ATTIVA', 'IN_CORSO'))
            """;

            try (PreparedStatement spam_statement = conn.prepareStatement(sqlSpamMisto)) {
                spam_statement.setString(1, emailSegnalante);
                spam_statement.setString(2, ipOrigine);

                try (ResultSet spam_risultato = spam_statement.executeQuery()) {
                    if (spam_risultato.next() && spam_risultato.getInt(1) > 0) {
                        response.setContentType("text/html;charset=UTF-8");
                        try (PrintWriter out = response.getWriter()) {
                            out.println("<!DOCTYPE html><html lang='it'><head>");
                            out.println("  <meta charset='UTF-8'>");
                            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                            out.println("  <title>Richiesta bloccata</title></head>");
                            out.println("<body style='font-family: Arial, sans-serif; text-align: center; background-color: #f4f6f9; padding: 20px; margin: 0;'>");
                            out.println("  <div style='max-width: 500px; width: 100%; margin: 60px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); box-sizing: border-box;'>");
                            out.println("    <h1 style='color: #dc3545; margin-top: 0;'>Richiesta bloccata</h1>");
                            out.println("    <p style='color: #666; line-height: 1.5;'>Hai già una segnalazione attiva o hai inviato un duplicato negli ultimi 10 minutes dal tuo indirizzo IP o e-mail.</p>");
                            out.println("    <hr style='border: 0; border-top: 1px solid #dee2e6; margin: 20px 0;'>");
                            out.println("    <a href='index.html' style='display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Torna al form</a>");
                            out.println("  </div>");
                            out.println("</body></html>");
                        }
                        return;
                    }
                }
            }
            ========================================== */

            // Inserimento diretto della richiesta nel database
            String sql = "INSERT INTO richiesta_soccorso (descrizione, posizione, nome_segnalante, email_segnalante, ip_origine, token_convalida, stato, foto) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'IN_ATTESA', ?)";

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

        // Output simulazione email (Reso fluido e moderno per mobile)
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html lang='it'><head>");
            out.println("  <meta charset='UTF-8'>");
            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("  <title>Simulazione casella e-mail</title></head>");
            out.println("<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f9; margin: 0;'>");
            
            if (salvato) {
                out.println("  <div style='border: 2px dashed #0056b3; padding: 25px; background-color: white; max-width: 600px; width: 100%; margin: 40px auto; border-radius: 6px; box-sizing: border-box;'>");
                out.println("    <h3 style='color: #0056b3; margin-top: 0;'>📩 Abbiamo inviato un'e-mail a: " + emailSegnalante + "</h3>");
                out.println("    <p style='color: #333;'>Ciao <b>" + nome + "</b>,</p>");
                out.println("    <p style='color: #555; line-height: 1.5;'>Abbiamo ricevuto la tua richiesta di soccorso, la quale <b>è in attesa di convalida</b> per certificare l'autenticità del segnalante.</p>");
                out.println("    <p style='color: #555;'>Per confermarla definitivamente e inoltrarla alla centrale operativa, clicca sul pulsante qui sotto:</p>");

                String linkConvalida = request.getContextPath() + "/ConvalidaServlet?token=" + tokenConvalida;
                out.println("    <br><a href='" + linkConvalida + "' style='background-color: #28a745; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block; width: 100%; text-align: center; box-sizing: border-box; font-size: 16px;'>CONVALIDA RICHIESTA</a>");
                out.println("  </div>");
            } else {
                out.println("  <div style='max-width: 600px; width: 100%; margin: 40px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); text-align: center; box-sizing: border-box;'>");
                out.println("    <h2 style='color: #dc3545; margin-top: 0;'>Errore di sistema</h2>");
                out.println("    <p style='color: #666;'>Impossibile salvare la richiesta nel database. Riprova più tardi.</p>");
                out.println("  </div>");
            }
            out.println("</body></html>");
        }
    }
}