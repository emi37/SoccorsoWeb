package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "CreaRichiestaServlet", urlPatterns = {"/CreaRichiestaServlet"})
public class CreaRichiestaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1 raccoglie i dati del form HTML
        String nome = request.getParameter("nome_segnalante"); 
        String email = request.getParameter("email_segnalante");
        String posizione = request.getParameter("posizione");
        String descrizione = request.getParameter("descrizione");
        String ipOrigine = request.getRemoteAddr();

        String tokenConvalida = UUID.randomUUID().toString(); 

        //3 salva la richiesta nel db, con stato di default 'IN_ATTESA'
        boolean salvato = false;
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO richiesta_soccorso (descrizione, posizione, nome_segnalante, email_segnalante, ip_origine, token_convalida, stato) "
                       + "VALUES (?, ?, ?, ?, ?, ?, 'IN_ATTESA')";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, descrizione);
                stmt.setString(2, posizione);
                stmt.setString(3, nome);
                stmt.setString(4, email);
                stmt.setString(5, ipOrigine);
                stmt.setString(6, tokenConvalida);
                
                int righeInserite = stmt.executeUpdate();
                if (righeInserite > 0) {
                    salvato = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Simulazione casella e mail</title></head>");
            out.println("<body style='font-family: Arial; padding: 40px; background-color: #f4f4f9;'>");

            if (salvato) {
                out.println("<div style='border: 2px dashed #0056b3; padding: 20px; background-color: white; max-width: 600px; margin: auto;'>");
                out.println("<h3 style='color: #0056b3;'>📩 Abbiamo inviato un email a: " + email + "</h3>");
                out.println("<p>Ciao <b>" + nome + "</b>,</p>");
                out.println("<p>Abbiamo ricevuto la sua richiesta di soccorso la quale <b>è ancora in attesa di convalida</b>.</p>");
                out.println("<p>Per confermarla e renderla visibile alla centrale operativa, clicca sul pulsante qui sotto:</p>");
                
                String linkConvalida = request.getContextPath() + "/ConvalidaServlet?token=" + tokenConvalida;
                
                out.println("<br><a href='" + linkConvalida + "' style='background-color: #28a745; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>CONVALIDA RICHIESTA</a><br><br>");
                
               
            } else {
                out.println("<h2 style='color: red;'>Errore : impossibile salvare la richiesta nel database, riprovare.</h2>");
            }
            out.println("</body>");
            out.println("</html>");
        }
    }
}                                    