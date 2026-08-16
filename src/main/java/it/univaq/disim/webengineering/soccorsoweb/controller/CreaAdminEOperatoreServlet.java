package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CreaAdminEOperatoreServlet", urlPatterns = {"/CreaAdminEOperatoreServlet"})
public class CreaAdminEOperatoreServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Protezione della sessione logica
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // Recupero parametri
        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String ruolo = request.getParameter("ruolo");

        boolean inserito = false;

        String sql = "INSERT INTO utente (nome, cognome, email, password, ruolo, attivo) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection conn = DBManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Cifratura della password con BCrypt
            String passwordCifrata = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            stmt.setString(3, email);
            stmt.setString(4, passwordCifrata);
            stmt.setString(5, ruolo);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                inserito = true;
            }
        } catch (Exception e) {
            // Se qualcosa va storto, l’errore viene stampato nella console di Tomcat
            e.printStackTrace();
        }

        // Applicazione rigorosa del pattern PRG con redirect verso le pagine HTML statiche
        if (inserito) {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_ok.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_errore.html");
        }
    }
}
