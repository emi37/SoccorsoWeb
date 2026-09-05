package it.univaq.disim.webengineering.soccorsoweb.controller;

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
    public void init() throws ServletException {
        // carico il driver jdbc solo all'avvio
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
        
        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect("login.html");
            return;
        }

        // check se stiamo cancellando un materiale
        String idElimina = request.getParameter("elimina");
        if (idElimina != null) {
            // soft-delete: lo nascondo (attivo=0) ma lo tengo per lo storico
            // non lo tocco se è impegnato in una missione IN_CORSO
            String queryDelete = "UPDATE materiale SET attivo = 0 WHERE id_materiale = ? AND id_materiale NOT IN (" +
                                 "SELECT id_materiale FROM assegnazione_materiale_missione amm " +
                                 "JOIN missione mis ON amm.id_missione = mis.id_missione WHERE mis.stato = 'IN_CORSO')";
            
            // connessione nativa per l'update
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(queryDelete)) {
                
                ps.setInt(1, Integer.parseInt(idElimina));
                ps.executeUpdate();
                
            } catch (Exception e) {
                System.err.println("Errore durante l'eliminazione del materiale");
                e.printStackTrace();
            }
            
            // pattern PRG per evitare casini coi refresh
            response.sendRedirect("GestioneMateriali");
            return;
        }

        ArrayList<Map<String, String>> listaMateriali = new ArrayList<>();
        
        // query materiali e subquery per vedere se è attualmente a bordo di un mezzo (IN_CORSO)
        String query = "SELECT m.id_materiale, m.nome, m.descrizione, " +
                       "CASE WHEN EXISTS (" +
                       "    SELECT 1 FROM assegnazione_materiale_missione amm " +
                       "    JOIN missione mis ON amm.id_missione = mis.id_missione " +
                       "    WHERE amm.id_materiale = m.id_materiale AND mis.stato = 'IN_CORSO'" +
                       ") THEN 'IMPEGNATO' ELSE 'LIBERO' END AS stato_attuale " +
                       "FROM materiale m WHERE m.attivo = 1";

        // connessione nativa per la select
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
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
        } catch (Exception e) {
            System.err.println("Errore fetch lista materiali");
            e.printStackTrace();
        }

        // passo i dati alla vista
        request.setAttribute("materiali", listaMateriali);
        request.getRequestDispatcher("/WEB-INF/admin/gestione_materiali.jsp").forward(request, response);
    }
}