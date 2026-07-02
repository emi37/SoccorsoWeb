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

    // Usiamo doGet perché cliccare un link in una mail è sempre una richiesta GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //1. prendiamo il token generato, che si nell indirizzo della pagina
        
        String token = request.getParameter("token");
        
        
        boolean validazioneRiuscita = false;

        // Se il token c'è e non è vuoto, lo cerco allora nel DB
        if (token != null && !token.trim().isEmpty()) {
            
            try (Connection conn = DBManager.getConnection()) {
                // 2. ora cerco la richiesta col token esatto che ho estratto dall indirizzo 
                // Se la troviamo, la mettiamo 'ATTIVA' e cancelliamo il token (NULL) per sicurezxa.
                String sql = "UPDATE richiesta_soccorso SET stato = 'ATTIVA', token_convalida = NULL "
                + "WHERE token_convalida = ? AND stato = 'IN_ATTESA'";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                                        int righeModificate = stmt.executeUpdate();
                    
                    if (righeModificate > 0) {
                        validazioneRiuscita = true; 
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 3.risult. a schermo
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Esito della convalida:</title></head>");
            out.println("<body style='font-family: Arial; text-align: center; padding-top: 50px;'>");
            
            if (validazioneRiuscita) {
                out.println("<h1 style='color: green;'>La sua richiesta è stata convalidata con successo ed è ora <b>ATTIVA</b> </h1>");
                out.println("<p>Stiamo preparando i soccorsi, saremo da voi il prima possibile</p>");
            } else {
                out.println("<h1 style='color: red;'>Errore nella convalida, riprovare la richiesta<a href='index.html'> qui </a></h1>");
                out.println("<p>Il link di convalida non è valido: probabilmente è scaduto, riprova.</p>");
            }
            out.println("<br><a href='index.html'>Torna al form iniziale </a>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}