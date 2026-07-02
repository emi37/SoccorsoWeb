package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
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

@WebServlet(name = "GestioneRichiestaServletDallAdmin", urlPatterns = {"/GestioneRichiestaServletDallAdmin"})
public class GestioneRichiestaServletDallAdmin extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idParam = request.getParameter("id");
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Assegnazione risorse</title></head>");
            out.println("<body style='font-family: Arial; padding: 30px; background-color: #f8f9fa;'>");
            out.println("<div style='max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>");
            
            try (Connection conn = DBManager.getConnection()) {
                String sql = "SELECT id_richiesta, nome_segnalante, posizione, descrizione FROM richiesta_soccorso WHERE id_richiesta = ? AND stato = 'ATTIVA'";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, idParam);
                    try (ResultSet rs = stmt.executeQuery()) {
                        
                        if (rs.next()) {
                            out.println("<h2>Avvio missione #" + rs.getInt("id_richiesta") + "</h2>");
                            out.println("<p><b>Segnalante:</b> " + rs.getString("nome_segnalante") + "</p>");
                            out.println("<p><b>Posizione dell'emergenza:</b> " + rs.getString("posizione") + "</p>");
                            out.println("<p><b>Dettagli segnalazione:</b> " + rs.getString("descrizione") + "</p>");
                            out.println("<hr>");
                            
                            out.println("<h3>Configurazione squadra d'intervento</h3>");
                            out.println("<form action='" + request.getContextPath() + "/GestioneRichiestaServletDallAdmin' method='POST'>");
                            out.println("<input type='hidden' name='id_richiesta' value='" + idParam + "'>");
                            
                            out.println("<h4>Seleziona degli operatori liberi per la missione:</h4>");
                            String sqlOperatori = "SELECT id_utente, nome, cognome FROM utente WHERE ruolo = 'OPERATORE' AND attivo = TRUE "
                                                + "AND id_utente NOT IN (SELECT id_utente FROM assegnazione_operatori_missione amm JOIN missione m ON amm.id_missione = m.id_missione WHERE m.stato = 'IN_CORSO')";
                            try (PreparedStatement stmtOp = conn.prepareStatement(sqlOperatori);
                                 ResultSet rsOp = stmtOp.executeQuery()) {
                                while (rsOp.next()) {
                                    int idOp = rsOp.getInt("id_utente");
                                    String nomeOp = rsOp.getString("nome") + " " + rsOp.getString("cognome");
                                    out.println("<input type='checkbox' name='operatori' value='" + idOp + "'> " + nomeOp + "<br>");
                                }
                            }
                            out.println("<br>");

                            out.println("<h4>Seleziona Mezzi di Soccorso Disponibili:</h4>");
                            String sqlMezzi = "SELECT id_mezzo, nome, descrizione FROM mezzo WHERE attivo = TRUE "
                                            + "AND id_mezzo NOT IN (SELECT id_mezzo FROM assegnazione_mezzi_missione amm JOIN missione m ON amm.id_missione = m.id_missione WHERE m.stato = 'IN_CORSO')";
                            try (PreparedStatement stmtMz = conn.prepareStatement(sqlMezzi);
                                 ResultSet rsMz = stmtMz.executeQuery()) {
                                while (rsMz.next()) {
                                    int idMezzo = rsMz.getInt("id_mezzo");
                                    String nomeMezzo = rsMz.getString("nome") + " (" + rsMz.getString("descrizione") + ")";
                                    out.println("<input type='checkbox' name='mezzi' value='" + idMezzo + "'> " + nomeMezzo + "<br>");
                                }
                            }
                            out.println("<br><hr>");
                            
                            out.println("<button type='submit' style='background-color: #007bff; color: white; padding: 12px 24px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; width: 100%;'>FA PARTIRE I SOCCORSI SUL CAMPO</button>");
                            out.println("</form>");
                        } else {
                            out.println("<p style='color: red;'>Richiesta non trovata o gia' in gestione.</p>");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            out.println("<br><br><a href='" + request.getContextPath() + "/DashboardServlet'>Torna alla Dashboard</a>");
            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idRichiesta = request.getParameter("id_richiesta");
        String[] operatoriScelti = request.getParameterValues("operatori");
        String[] mezziScelti = request.getParameterValues("mezzi");
        
        boolean avviato = false;

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Aggiorna la richiesta portando lo stato a IN CORSO
                String sqlUpdate = "UPDATE richiesta_soccorso SET stato = 'IN_CORSO' WHERE id_richiesta = ?";
                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setString(1, idRichiesta);
                    stmtUpdate.executeUpdate();
                }
                
                // Creiamo il record della missione lasciando il report finale (voto e commento) a NULL
                int idMissioneGenerato = 0;
                String sqlInsert = "INSERT INTO missione (id_richiesta, obiettivo, posizione, stato, livello_successo, commenti, timestamp_fine) VALUES (?, 'Intervento di emergenza sul campo', 'Posizione segnalata dal cittadino', 'IN_CORSO', NULL, NULL, NULL)";
                
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmtInsert.setInt(1, Integer.parseInt(idRichiesta));
                    stmtInsert.executeUpdate();
                    
                    try (ResultSet keys = stmtInsert.getGeneratedKeys()) {
                        if (keys.next()) {
                            idMissioneGenerato = keys.getInt(1);
                        }
                    }
                }
                
                // Inseriamo le relazioni con gli operatori scelti
                if (operatoriScelti != null && idMissioneGenerato > 0) {
                    String sqlOpMissione = "INSERT INTO assegnazione_operatori_missione (id_missione, id_utente) VALUES (?, ?)";
                    try (PreparedStatement stmtOpM = conn.prepareStatement(sqlOpMissione)) {
                        for (String idOp : operatoriScelti) {
                            stmtOpM.setInt(1, idMissioneGenerato);
                            stmtOpM.setInt(2, Integer.parseInt(idOp));
                            stmtOpM.executeUpdate();
                        }
                    }
                }

                // Inseriamo le relazioni con i mezzi scelti
                if (mezziScelti != null && idMissioneGenerato > 0) {
                    String sqlMezzoMissione = "INSERT INTO assegnazione_mezzi_missione (id_missione, id_mezzo) VALUES (?, ?)";
                    try (PreparedStatement stmtMzm = conn.prepareStatement(sqlMezzoMissione)) {
                        for (String idMz : mezziScelti) {
                            stmtMzm.setInt(1, idMissioneGenerato);
                            stmtMzm.setInt(2, Integer.parseInt(idMz));
                            stmtMzm.executeUpdate();
                        }
                    }
                }
                
                conn.commit();
                avviato = true;
                
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            if (avviato) {
                out.println("<script>");
                out.println("alert('Squadre inviate. La missione é ora attiva sul campo.');");
                out.println("window.location.href='" + request.getContextPath() + "/DashboardServlet';");
                out.println("</script>");
            } else {
                out.println("<h2>Errore durante l'avvio della missione.</h2>");
            }
            out.println("</body></html>");
        }
    }
}