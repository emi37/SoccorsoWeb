package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Dati del form
        String emailInserita = request.getParameter("email");
        String passwordInserita = request.getParameter("password");

        boolean loginEffettuato = false;
        String nomeUtente = "";
        String ruoloUtente = "";

        // 2. Interrogazione DB
        try (Connection conn = DBManager.getConnection()) {
            // Controlliamo che l'email esista E che l'utente sia attivo
            String sql = "SELECT id_utente, nome, password, ruolo FROM utente WHERE email = ? AND attivo = TRUE";            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, emailInserita);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String hashSalvato = rs.getString("password");
                        
                        if (BCrypt.checkpw(passwordInserita, hashSalvato)) {
                            loginEffettuato = true;
                            
                            int idUtente = rs.getInt("id_utente");
                            nomeUtente = rs.getString("nome");
                            ruoloUtente = rs.getString("ruolo");
                            
                            // Creiamo la sessione dell'utente che si logga
                            HttpSession session = request.getSession();
                            session.setAttribute("id_utente", idUtente);
                            session.setAttribute("nome", nomeUtente);
                            session.setAttribute("ruolo", ruoloUtente);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Reindirizzamento o schermata di errore responsive
        if (loginEffettuato) {
            if ("ADMIN".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            } else if ("OPERATORE".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
            }
            return;
        } else {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html lang='it'>");
                out.println("<head>");
                out.println("  <meta charset='UTF-8'>");
                out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                out.println("  <title>Accesso negato</title>");
                out.println("</head>");
                out.println("<body style='font-family: Arial, sans-serif; text-align: center; background-color: #f4f6f9; padding: 20px; margin: 0;'>");
                
                // Box di errore responsive flessibile
                out.println("  <div style='max-width: 500px; width: 100%; margin: 60px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); box-sizing: border-box;'>");
                out.println("    <h1 style='color: #dc3545; margin-top: 0;'>❌ Accesso negato</h1>");
                out.println("    <p style='color: #666; line-height: 1.5;'>Email o password errate. Verifica le credenziali inserite e riprova.</p>");
                out.println("    <hr style='border: 0; border-top: 1px solid #dee2e6; margin: 25px 0;'>");
                out.println("    <a href='login.html' style='display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Torna al Login</a>");
                out.println("  </div>");
                
                out.println("</body></html>");
            }
        }
    }
}