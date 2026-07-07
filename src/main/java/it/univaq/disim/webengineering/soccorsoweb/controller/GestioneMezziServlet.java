package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.io.IOException;
import java.sql.Connection;
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

@WebServlet("/GestioneMezzi")
public class GestioneMezziServlet extends HttpServlet {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // controllo della sicurezza della sessione dell'admin
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        // gestione della cancellazione logica 
        String idElimina = request.getParameter("elimina");
        if (idElimina != null) {
            rimuoviMezzo(Integer.parseInt(idElimina));
            response.sendRedirect("GestioneMezzi");
            return;
        }

        ArrayList<Map<String, String>> listaMezzi = new ArrayList<>();
        
        // estrazione mezzi con calcolo dinamico dello stato occupato
        String query = "SELECT m.id_mezzo, m.nome, m.descrizione, " +
                       "CASE WHEN EXISTS (" +
                       "    SELECT 1 FROM assegnazione_mezzi_missione amm " +
                       "    JOIN missione mis ON amm.id_missione = mis.id_missione " +
                       "    WHERE amm.id_mezzo = m.id_mezzo AND mis.stato = 'IN_CORSO'" +
                       ") THEN 'IMPEGNATO' ELSE 'LIBERO' END AS stato_attuale " +
                       "FROM mezzo m WHERE m.attivo = 1";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DBManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, String> mezzo = new HashMap<>();
                    mezzo.put("id", String.valueOf(rs.getInt("id_mezzo")));
                    mezzo.put("nome", rs.getString("nome"));
                    mezzo.put("descrizione", rs.getString("descrizione"));
                    mezzo.put("stato", rs.getString("stato_attuale"));
                    listaMezzi.add(mezzo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("mezzi", listaMezzi);
        request.getRequestDispatcher("/WEB-INF/admin/gestione_mezzi.jsp").forward(request, response);
    }

    private void rimuoviMezzo(int id) {
        // disattivazione per tutelare lo storico
        String query = "UPDATE mezzo SET attivo = 0 WHERE id_mezzo = ? AND id_mezzo NOT IN (" +
                       "SELECT id_mezzo FROM assegnazione_mezzi_missione amm " +
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