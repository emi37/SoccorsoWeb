package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/GestioneMateriali")
public class GestioneMaterialiServlet extends HttpServlet {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // controllo sicurezza sessione amministratore
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        // gestione cancellazione logica se invocata dal pannello
        String idElimina = request.getParameter("elimina");
        if (idElimina != null) {
            rimuoviMateriale(Integer.parseInt(idElimina));
            response.sendRedirect("GestioneMateriali");
            return;
        }

        ArrayList<Map<String, String>> listaMateriali = new ArrayList<>();
        
        // estrazione attrezzature con verifica legame missioni attive
        String query = "SELECT m.id_materiale, m.nome, m.descrizione, " +
                       "CASE WHEN EXISTS (" +
                       "    SELECT 1 FROM assegnazione_materiale_missione amm " +
                       "    JOIN missione mis ON amm.id_missione = mis.id_missione " +
                       "    WHERE amm.id_materiale = m.id_materiale AND mis.stato = 'IN_CORSO'" +
                       ") THEN 'IMPEGNATO' ELSE 'LIBERO' END AS stato_attuale " +
                       "FROM materiale m WHERE m.attivo = 1";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DBManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, String> materiale = new HashMap<>();
                    materiale.put("id", String.valueOf(rs.getInt("id_materiale")));
                    materiale.put("nome", rs.getString("nome"));
                    materiale.put("descrizione", rs.getString("descrizione"));
                    materiale.put("stato", rs.getString("stato_attuale"));
                    listaMateriali.add(materiale);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("materiali", listaMateriali);
        request.getRequestDispatcher("/WEB-INF/admin/gestione_materiali.jsp").forward(request, response);
    }

    private void rimuoviMateriale(int id) {
        // disattivazione sicura per preservare i vincoli dello storico d esame
        String query = "UPDATE materiale SET attivo = 0 WHERE id_materiale = ? AND id_materiale NOT IN (" +
                       "SELECT id_materiale FROM assegnazione_materiale_missione amm " +
                       "JOIN missione mis ON amm.id_missione = mis.id_missione WHERE mis.stato = 'IN_CORSO')";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


