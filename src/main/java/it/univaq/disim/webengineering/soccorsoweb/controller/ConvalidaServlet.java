package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ConvalidaServlet", urlPatterns = {"/ConvalidaServlet"})
public class ConvalidaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. prendiamo il token generato, che si trova nell'indirizzo della pagina
        String token = request.getParameter("token");

        boolean validazioneRiuscita = false;

        // Se il token c'è e non è vuoto, lo cerco nel DB
        if (token != null && !token.trim().isEmpty()) {

            try (Connection conn = DBManager.getConnection()) {
                // 2. AGGIORNAMENTO TIMESTAMP_CONVALIDA
                String sql = """
                    UPDATE richiesta_soccorso 
                    SET stato = 'ATTIVA', 
                        token_convalida = NULL,
                        timestamp_convalida = CURRENT_TIMESTAMP 
                    WHERE token_convalida = ? 
                      AND stato = 'IN_ATTESA'
                      AND timestamp_creazione >= NOW() - INTERVAL 10 MINUTE
                """;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                    int righeModificate = stmt.executeUpdate();

                    if (righeModificate > 0) {
                        validazioneRiuscita = true;
                    }
                }
                //Se qualcosa va storto, l’errore viene stampato nella console di Tomcat/NetBeans.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. Risultato a schermo (Sistemato con layout fluido e reattivo per mobile)
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='it'>");
            out.println("<head>");
            out.println("  <meta charset='UTF-8'>");
            // Inserito viewport per la visualizzazione corretta su smartphone
            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("  <title>Esito della convalida</title>");
            out.println("</head>");
            out.println("<body style='font-family: Arial, sans-serif; text-align: center; background-color: #f4f6f9; padding: 20px; margin: 0;'>");

            // Box contenitore fluido responsivo
            out.println("  <div style='max-width: 600px; width: 100%; margin: 60px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); box-sizing: border-box;'>");

            if (validazioneRiuscita) {
                out.println("<h1 style='color: #28a745; margin-top: 0;'>La sua richiesta è stata convalidata con successo!</h1>");
                out.println("<p style='font-size: 18px; color: #333;'>Stato attuale: <b style='background-color: #d4edda; color: #155724; padding: 4px 8px; border-radius: 4px;'>ATTIVA</b></p>");
                out.println("<p style='color: #666; line-height: 1.5;'>Stiamo preparando i soccorsi, la centrale operativa ha preso in carico la segnalazione.</p>");
            } else {
                out.println("<h1 style='color: #dc3545; margin-top: 0;'>Errore nella convalida</h1>");
                out.println("<p style='color: #666; line-height: 1.5;'>Il link di convalida non è valido: potrebbe essere già stato utilizzato oppure è <b>scaduto (valido solo per 10 minuti)</b>.</p>");
                out.println("<p style='color: #666;'>Per favore, effettui una nuova richiesta.</p>");
            }

            out.println("<hr style='border: 0; border-top: 1px solid #dee2e6; margin: 25px 0;'>");
            out.println("<a href='index.html' style='display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold; transition: background 0.2s;'>Torna al form iniziale</a>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
