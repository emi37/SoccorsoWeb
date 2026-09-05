package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ConvalidaServlet", urlPatterns = {"/ConvalidaServlet"})
public class ConvalidaServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico il driver una sola volta all'avvio
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // tiro giù il token dalla query string
        String token = request.getParameter("token");
        boolean validazioneRiuscita = false;

        // procedo solo se il token è valorizzato
        if (token != null && !token.trim().isEmpty()) {

            // query concatenata col + come da vecchie abitudini, senza blocchi monolitici
            String sql = "UPDATE richiesta_soccorso " +
                         "SET stato = 'ATTIVA', " +
                         "    token_convalida = NULL, " +
                         "    timestamp_convalida = CURRENT_TIMESTAMP " +
                         "WHERE token_convalida = ? " +
                         "  AND stato = 'IN_ATTESA' " +
                         "  AND timestamp_creazione >= NOW() - INTERVAL 10 MINUTE";

            // connessione nativa
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                // bind parametri
                stmt.setString(1, token);
                
                // eseguo e controllo le righe affette (affectedRows)
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    validazioneRiuscita = true;
                }
                
            } catch (Exception e) {
                System.err.println("Errore db durante la convalida del token");
                e.printStackTrace();
            }
        }

        // preparo i dati in mappa
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("validazioneRiuscita", validazioneRiuscita);

        // TODO: Invocazione template engine (es. Freemarker)
        // Per ora facciamo un redirect in base all'esito per chiudere il flusso
        if (validazioneRiuscita) {
            response.sendRedirect(request.getContextPath() + "/convalida.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/errore_convalida.html");
        }
    }
}