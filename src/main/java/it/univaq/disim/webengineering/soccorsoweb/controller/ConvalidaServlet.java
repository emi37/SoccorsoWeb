package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.sql.Connection;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Recupero del token generato dalla query string
        String token = request.getParameter("token");
        boolean validazioneRiuscita = false;

        // Se il token c'è e non è vuoto, lo cerco nel DB per aggiornare lo stato
        if (token != null && !token.trim().isEmpty()) {

            try (Connection conn = DBManager.getConnection()) {
                // 2. Esecuzione UPDATE e convalida tramite JDBC
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
            } catch (Exception e) {
                // Se qualcosa va storto, l'errore viene stampato nella console
                e.printStackTrace();
            }
        }

        // 3. Preparazione dei dati per il livello View
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("validazioneRiuscita", validazioneRiuscita);

      
    }
}
