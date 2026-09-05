package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import java.io.PrintWriter;
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

@WebServlet(name = "GestioneRichiestaServletDallAdmin", urlPatterns = {"/GestioneRichiestaServletDallAdmin"})
public class GestioneRichiestaServletDallAdmin extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Non autorizzato\"}");
            return;
        }

        String idParam = request.getParameter("id");
        response.setContentType("application/json;charset=UTF-8");
        
        // connessione nativa
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PrintWriter out = response.getWriter()) {
            
            // query spezzata
            String sql = "SELECT id_richiesta, nome_segnalante, posizione, descrizione " +
                         "FROM richiesta_soccorso " +
                         "WHERE id_richiesta = ? AND stato = 'ATTIVA'";
                         
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idParam);
                try (ResultSet rs = stmt.executeQuery()) {
                    
                    if (rs.next()) {
                        int idRichiesta = rs.getInt("id_richiesta");
                        String nomeSegnalante = rs.getString("nome_segnalante");
                        String posizione = rs.getString("posizione");
                        String descrizione = rs.getString("descrizione");
                        
                        // 1. operatori liberi
                        StringBuilder operatoriJson = new StringBuilder("[");
                        String sqlOp = "SELECT id_utente, nome, cognome FROM utente " +
                                       "WHERE ruolo = 'OPERATORE' AND attivo = TRUE " +
                                       "AND id_utente NOT IN (" +
                                       "    SELECT id_utente FROM assegnazione_operatori_missione amm " +
                                       "    JOIN missione m ON amm.id_missione = m.id_missione " +
                                       "    WHERE m.stato = 'IN_CORSO'" +
                                       ")";
                                       
                        try (PreparedStatement stmtOp = conn.prepareStatement(sqlOp); 
                             ResultSet rsOp = stmtOp.executeQuery()) {
                            boolean first = true;
                            while (rsOp.next()) {
                                if (!first) operatoriJson.append(",");
                                operatoriJson.append("{\"id\":").append(rsOp.getInt("id_utente"))
                                             .append(",\"nomeCompleto\":\"").append(escapeJson(rsOp.getString("nome") + " " + rsOp.getString("cognome"))).append("\"}");
                                first = false;
                            }
                        }
                        operatoriJson.append("]");

                        // 2. mezzi liberi
                        StringBuilder mezziJson = new StringBuilder("[");
                        String sqlMz = "SELECT id_mezzo, nome, descrizione FROM mezzo " +
                                       "WHERE attivo = TRUE " +
                                       "AND id_mezzo NOT IN (" +
                                       "    SELECT id_mezzo FROM assegnazione_mezzi_missione amm " +
                                       "    JOIN missione m ON amm.id_missione = m.id_missione " +
                                       "    WHERE m.stato = 'IN_CORSO'" +
                                       ")";
                                       
                        try (PreparedStatement stmtMz = conn.prepareStatement(sqlMz); 
                             ResultSet rsMz = stmtMz.executeQuery()) {
                            boolean first = true;
                            while (rsMz.next()) {
                                if (!first) mezziJson.append(",");
                                mezziJson.append("{\"id\":").append(rsMz.getInt("id_mezzo"))
                                         .append(",\"nome\":\"").append(escapeJson(rsMz.getString("nome")))
                                         .append("\",\"descrizione\":\"").append(escapeJson(rsMz.getString("descrizione"))).append("\"}");
                                first = false;
                            }
                        }
                        mezziJson.append("]");

                        // 3. materiali liberi
                        StringBuilder materialiJson = new StringBuilder("[");
                        String sqlMat = "SELECT id_materiale, nome, descrizione FROM materiale " +
                                        "WHERE attivo = TRUE " +
                                        "AND id_materiale NOT IN (" +
                                        "    SELECT id_materiale FROM assegnazione_materiale_missione amm " +
                                        "    JOIN missione m ON amm.id_missione = m.id_missione " +
                                        "    WHERE m.stato = 'IN_CORSO'" +
                                        ")";
                                        
                        try (PreparedStatement stmtMat = conn.prepareStatement(sqlMat); 
                             ResultSet rsMat = stmtMat.executeQuery()) {
                            boolean first = true;
                            while (rsMat.next()) {
                                if (!first) materialiJson.append(",");
                                materialiJson.append("{\"id\":").append(rsMat.getInt("id_materiale"))
                                             .append(",\"nome\":\"").append(escapeJson(rsMat.getString("nome")))
                                             .append("\",\"descrizione\":\"").append(escapeJson(rsMat.getString("descrizione"))).append("\"}");
                                first = false;
                            }
                        }
                        materialiJson.append("]");

                        // build finale del payload
                        StringBuilder jsonFinal = new StringBuilder();
                        jsonFinal.append("{")
                                 .append("\"richiesta\":{")
                                 .append("\"idRichiesta\":").append(idRichiesta).append(",")
                                 .append("\"nomeSegnalante\":\"").append(escapeJson(nomeSegnalante)).append("\",")
                                 .append("\"posizione\":\"").append(escapeJson(posizione)).append("\",")
                                 .append("\"descrizione\":\"").append(escapeJson(descrizione)).append("\"")
                                 .append("},")
                                 .append("\"operatori\":").append(operatoriJson).append(",")
                                 .append("\"mezzi\":").append(mezziJson).append(",")
                                 .append("\"materiali\":").append(materialiJson)
                                 .append("}");

                        out.print(jsonFinal.toString());

                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"Richiesta non trovata o già in gestione.\"}");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore fetch dati per assegnazione missione");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("{\"error\": \"Errore interno del server.\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // check permessi
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\": false, \"error\": \"Non autorizzato\"}");
            return;
        }

        // input dal form inviato via ajax
        String idRichiesta = request.getParameter("id_richiesta");
        String[] operatoriScelti = request.getParameterValues("operatori");
        String caposquadraScelto = request.getParameter("caposquadra");
        String[] mezziScelti = request.getParameterValues("mezzi");
        String[] materialiScelti = request.getParameterValues("materiali");
        
        boolean avviato = false;

        // connessione nativa
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // stacco l'autocommit per gestire la transazione
            conn.setAutoCommit(false);
            
            try {
                String posizioneReale = "Posizione non specificata";
                String descrizioneReale = "Intervento di emergenza sul campo";
                
                String sqlGetRichiesta = "SELECT posizione, descrizione FROM richiesta_soccorso WHERE id_richiesta = ?";
                try (PreparedStatement stmtGet = conn.prepareStatement(sqlGetRichiesta)) {
                    stmtGet.setInt(1, Integer.parseInt(idRichiesta));
                    try (ResultSet rsReq = stmtGet.executeQuery()) {
                        if (rsReq.next()) {
                            posizioneReale = rsReq.getString("posizione");
                            descrizioneReale = rsReq.getString("descrizione");
                        }
                    }
                }

                // aggiorno stato richiesta
                String sqlUpdate = "UPDATE richiesta_soccorso SET stato = 'IN_CORSO' WHERE id_richiesta = ?";
                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setString(1, idRichiesta);
                    stmtUpdate.executeUpdate();
                }
                
                // creo la missione
                int idMissioneGenerato = 0;
                String sqlInsert = "INSERT INTO missione " +
                                   "(id_richiesta, obiettivo, posizione, stato) " +
                                   "VALUES (?, ?, ?, 'IN_CORSO')";
                
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmtInsert.setInt(1, Integer.parseInt(idRichiesta));
                    stmtInsert.setString(2, descrizioneReale);
                    stmtInsert.setString(3, posizioneReale);
                    stmtInsert.executeUpdate();
                    
                    try (ResultSet keys = stmtInsert.getGeneratedKeys()) {
                        if (keys.next()) idMissioneGenerato = keys.getInt(1);
                    }
                }
                
                // associo operatori
                if (operatoriScelti != null && idMissioneGenerato > 0) {
                    String sqlOpMissione = "INSERT INTO assegnazione_operatori_missione " +
                                           "(id_missione, id_utente, is_caposquadra) VALUES (?, ?, ?)";
                    try (PreparedStatement stmtOpM = conn.prepareStatement(sqlOpMissione)) {
                        for (String idOp : operatoriScelti) {
                            int isCapo = (idOp.equals(caposquadraScelto)) ? 1 : 0;
                            stmtOpM.setInt(1, idMissioneGenerato);
                            stmtOpM.setInt(2, Integer.parseInt(idOp));
                            stmtOpM.setInt(3, isCapo);
                            stmtOpM.executeUpdate();
                        }
                    }
                }

                // associo mezzi
                if (mezziScelti != null && idMissioneGenerato > 0) {
                    String sqlMezzoMissione = "INSERT INTO assegnazione_mezzi_missione " +
                                              "(id_missione, id_mezzo) VALUES (?, ?)";
                    try (PreparedStatement stmtMzm = conn.prepareStatement(sqlMezzoMissione)) {
                        for (String idMz : mezziScelti) {
                            stmtMzm.setInt(1, idMissioneGenerato);
                            stmtMzm.setInt(2, Integer.parseInt(idMz));
                            stmtMzm.executeUpdate();
                        }
                    }
                }

                // associo materiali
                if (materialiScelti != null && idMissioneGenerato > 0) {
                    String sqlMatMissione = "INSERT INTO assegnazione_materiale_missione " +
                                            "(id_missione, id_materiale) VALUES (?, ?)";
                    try (PreparedStatement stmtMatM = conn.prepareStatement(sqlMatMissione)) {
                        for (String idMat : materialiScelti) {
                            stmtMatM.setInt(1, idMissioneGenerato);
                            stmtMatM.setInt(2, Integer.parseInt(idMat));
                            stmtMatM.executeUpdate();
                        }
                    }
                }
                
                // tutto ok, chiudo la transazione
                conn.commit();
                avviato = true;
                
            } catch (Exception ex) {
                System.err.println("Errore transazione assegnazione missione, rollback in corso");
                conn.rollback();
                ex.printStackTrace();
            } finally {
                // ripristino stato connessione
                conn.setAutoCommit(true);
            }
            
        } catch (Exception e) {
            System.err.println("Errore db durante l'avvio della missione");
            e.printStackTrace();
        }

        // risposta ajax
        response.setContentType("application/json;charset=UTF-8");
        if (avviato) {
            response.getWriter().write("{\"success\": true, \"redirect\": \"" + request.getContextPath() + "/DashboardServlet\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"error\": \"Errore durante l'avvio dell'intervento.\"}");
        }
    }

    // helper escape json
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", " ")
                  .replace("\r", " ");
    }
}