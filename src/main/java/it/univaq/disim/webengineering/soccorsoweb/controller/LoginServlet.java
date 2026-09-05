package it.univaq.disim.webengineering.soccorsoweb.controller;

import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    public void init() throws ServletException {
        // carico il driver una sola volta al boot
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL mancante");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // input dal form
        String emailInserita = request.getParameter("email");
        String passwordInserita = request.getParameter("password");

        boolean loginEffettuato = false;
        String ruoloUtente = "";

        // query spezzata 
        String sql = "SELECT id_utente, nome, password, ruolo " +
                     "FROM utente " +
                     "WHERE email = ? AND attivo = TRUE";            

        // connessione nativa
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, emailInserita);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashSalvato = rs.getString("password");
                    
                    // check jBcrypt
                    if (BCrypt.checkpw(passwordInserita, hashSalvato)) {
                        loginEffettuato = true;
                        
                        int idUtente = rs.getInt("id_utente");
                        String nomeUtente = rs.getString("nome");
                        ruoloUtente = rs.getString("ruolo");
                        
                        // inizializzo sessione per utente verificato
                        HttpSession session = request.getSession();
                        session.setAttribute("id_utente", idUtente);
                        session.setAttribute("nome", nomeUtente);
                        session.setAttribute("ruolo", ruoloUtente);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore query di login");
            e.printStackTrace();
        }

        // smistamento ruoli o gestione errore pulita senza html in java
        if (loginEffettuato) {
            if ("ADMIN".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            } else if ("OPERATORE".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
            }
        } else {
            // se fallisce, rimando alla login con la query string di errore 
            // (toccherà al frontend in JS, o alla jsp, leggere 'errore=1' e mostrare il box rosso)
            response.sendRedirect(request.getContextPath() + "/login.html?errore=1");
        }
    }
}