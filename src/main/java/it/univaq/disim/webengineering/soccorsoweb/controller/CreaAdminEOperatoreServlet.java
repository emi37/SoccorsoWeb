package it.univaq.disim.webengineering.soccorsoweb.controller;

import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CreaAdminEOperatoreServlet", urlPatterns = {"/CreaAdminEOperatoreServlet"})
public class CreaAdminEOperatoreServlet extends HttpServlet {

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // check permessi: prendo la sessione corrente se esiste
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // input dal form
        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String ruolo = request.getParameter("ruolo");

        boolean inserito = false;

        // query spezzata per comodità
        String sql = "INSERT INTO utente " +
                     "(nome, cognome, email, password, ruolo, attivo) " +
                     "VALUES (?, ?, ?, ?, ?, 1)";

        // connessione nativa e statement nel try-with-resources per chiusura automatica
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // hasho la password (assicurati di avere la lib jbcrypt inclusa)
            String passwordCifrata = BCrypt.hashpw(password, BCrypt.gensalt());

            // bind parametri
            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            stmt.setString(3, email);
            stmt.setString(4, passwordCifrata);
            stmt.setString(5, ruolo);

            // eseguo insert e vedo se ha scritto almeno una riga
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                inserito = true;
            }
        } catch (Exception e) {
            System.err.println("Errore inserimento nuovo utente a db");
            e.printStackTrace();
        }

        // redirect PRG pulito 
        if (inserito) {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_ok.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/creazione_utente_errore.html");
        }
    }
}