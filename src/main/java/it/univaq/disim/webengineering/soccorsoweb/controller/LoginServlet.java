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

        //1 dati del form
        String emailInserita = request.getParameter("email");
        String passwordInserita = request.getParameter("password");

        boolean loginEffettuato = false;
        String nomeUtente = "";
        String ruoloUtente = "";

        // 2 interrogazione DB
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
                            
                            // creiamo la sessione dell'utente che si logga
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

        // 5. Reindirizzamento o errore
        if (loginEffettuato) {
            if ("ADMIN".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardServlet");
            } else if ("OPERATORE".equals(ruoloUtente)) {
                response.sendRedirect(request.getContextPath() + "/DashboardOperatoreServlet");
            }
            return;
        } else  {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head><title>Accesso negato.</title></head>");
                out.println("<body style='font-family: Arial; text-align: center; padding-top: 50px;'>");
                out.println("<h1 style='color: red;'>❌ Accesso negato</h1>");
                out.println("<p>Email o password errate, riprova.</p>");
                out.println("<br><a href='login.html'>Torna alla schermata di login</a>");
                out.println("</body></html>");
            }
        }
    }
}