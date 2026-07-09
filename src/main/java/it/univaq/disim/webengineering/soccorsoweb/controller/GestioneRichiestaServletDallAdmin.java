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
            out.println("<html lang='it'>");
            out.println("<head>");
            out.println("  <meta charset='UTF-8'>");
            out.println("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Assegnazione risorse complete</title></head>");
            out.println("<body style='font-family: Arial, sans-serif; padding: 15px; background-color: #f8f9fa; margin: 0;'>");
            
            out.println("<div style='max-width: 600px; width: 100%; margin: 20px auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); box-sizing: border-box;'>");
            
            try (Connection conn = DBManager.getConnection()) {
                String sql = "SELECT id_richiesta, nome_segnalante, posizione, descrizione FROM richiesta_soccorso WHERE id_richiesta = ? AND stato = 'ATTIVA'";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, idParam);
                    try (ResultSet rs = stmt.executeQuery()) {
                        
                        if (rs.next()) {
                            out.println("<h2 style='margin-top:0; color:#333;'>Avvio missione #" + rs.getInt("id_richiesta") + "</h2>");
                            out.println("<p><b>Segnalante:</b> " + rs.getString("nome_segnalante") + "</p>");
                            out.println("<p><b>Posizione dell'emergenza:</b> " + rs.getString("posizione") + "</p>");
                            out.println("<p><b>Dettagli segnalazione:</b> " + rs.getString("descrizione") + "</p>");
                            out.println("<hr style='border:0; border-top:1px solid #dee2e6; margin:20px 0;'>");
                            
                            out.println("<h3 style='color:#007bff;'>Configurazione squadra d'intervento</h3>");
                            out.println("<form action='" + request.getContextPath() + "/GestioneRichiestaServletDallAdmin' method='POST'>");
                            out.println("<input type='hidden' name='id_richiesta' value='" + idParam + "'>");
                            
                            // 1. SELEZIONE OPERATORI + ASSEGNAZIONE CAPOSQUADRA (Punto 4)
                            out.println("<h4 style='color:#495057; margin-bottom:10px;'>Seleziona gli operatori e nomina un Caposquadra:</h4>");
                            out.println("<div style='max-height: 200px; overflow-y: auto; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; margin-bottom: 15px; background-color: #fff;'>");
                            String sqlOperatori = "SELECT id_utente, nome, cognome FROM utente WHERE ruolo = 'OPERATORE' AND attivo = TRUE "
                                                + "AND id_utente NOT IN (SELECT id_utente FROM assegnazione_operatori_missione amm JOIN missione m ON amm.id_missione = m.id_missione WHERE m.stato = 'IN_CORSO')";
                            try (PreparedStatement stmtOp = conn.prepareStatement(sqlOperatori);
                                 ResultSet rsOp = stmtOp.executeQuery()) {
                                boolean haOp = false;
                                while (rsOp.next()) {
                                    haOp = true;
                                    int idOp = rsOp.getInt("id_utente");
                                    String nomeOp = rsOp.getString("nome") + " " + rsOp.getString("cognome");
                                    out.println("<div style='display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; border-bottom: 1px dashed #eee; padding-bottom: 4px;'>");
                                    out.println("  <label style='cursor:pointer;'><input type='checkbox' name='operatori' value='" + idOp + "'> " + nomeOp + "</label>");
                                    out.println("  <label style='font-size: 12px; color: #555; cursor:pointer;'><input type='radio' name='caposquadra' value='" + idOp + "' required> Caposquadra</label>");
                                    out.println("</div>");
                                }
                                if (!haOp) out.println("<p style='color:gray; margin:5px;'>Nessun operatore disponibile.</p>");
                            }
                            out.println("</div>");

                            // 2. SELEZIONE MEZZI
                            out.println("<h4 style='color:#495057; margin-bottom:10px;'>Seleziona Mezzi di Soccorso Disponibili:</h4>");
                            out.println("<div style='max-height: 180px; overflow-y: auto; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; margin-bottom: 15px;'>");
                            String sqlMezzi = "SELECT id_mezzo, nome, descrizione FROM mezzo WHERE attivo = TRUE "
                                            + "AND id_mezzo NOT IN (SELECT id_mezzo FROM assegnazione_mezzi_missione amm JOIN missione m ON amm.id_missione = m.id_missione WHERE m.stato = 'IN_CORSO')";
                            try (PreparedStatement stmtMz = conn.prepareStatement(sqlMezzi);
                                 ResultSet rsMz = stmtMz.executeQuery()) {
                                boolean haMezzi = false;
                                while (rsMz.next()) {
                                    haMezzi = true;
                                    int idMezzo = rsMz.getInt("id_mezzo");
                                    String nomeMezzo = rsMz.getString("nome") + " (" + rsMz.getString("descrizione") + ")";
                                    out.println("<label style='display:block; margin-bottom:8px; cursor:pointer;'><input type='checkbox' name='mezzi' value='" + idMezzo + "'> " + nomeMezzo + "</label>");
                                }
                                if (!haMezzi) out.println("<p style='color:gray; margin:5px;'>Nessun mezzo disponibile.</p>");
                            }
                            out.println("</div>");

                            // 3. SELEZIONE MATERIALI E ATTREZZATURE (Completamento Punto 4)
                            out.println("<h4 style='color:#495057; margin-bottom:10px;'>Seleziona Attrezzature e Materiali di bordo:</h4>");
                            out.println("<div style='max-height: 180px; overflow-y: auto; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; margin-bottom: 25px;'>");
                            String sqlMateriali = "SELECT id_materiale, nome, descrizione FROM materiale WHERE attivo = TRUE "
                                                + "AND id_materiale NOT IN (SELECT id_materiale FROM assegnazione_materiale_missione amm JOIN missione m ON amm.id_missione = m.id_missione WHERE m.stato = 'IN_CORSO')";
                            try (PreparedStatement stmtMat = conn.prepareStatement(sqlMateriali);
                                 ResultSet rsMat = stmtMat.executeQuery()) {
                                boolean haMat = false;
                                while (rsMat.next()) {
                                    haMat = true;
                                    int idMat = rsMat.getInt("id_materiale");
                                    String nomeMat = rsMat.getString("nome") + " - " + rsMat.getString("descrizione");
                                    out.println("<label style='display:block; margin-bottom:8px; cursor:pointer;'><input type='checkbox' name='materiali' value='" + idMat + "'> " + nomeMat + "</label>");
                                }
                                if (!haMat) out.println("<p style='color:gray; margin:5px;'>Nessuna attrezzatura libera disponibile.</p>");
                            }
                            out.println("</div>");
                            
                            out.println("<button type='submit' style='background-color: #28a745; color: white; padding: 14px 24px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; width: 100%; font-size:16px; box-shadow: 0 4px 6px rgba(40,167,69,0.1);'>AVVIA INTERVENTO SUL CAMPO</button>");
                            out.println("</form>");
                        } else {
                            out.println("<p style='color: red; font-weight:bold;'>Richiesta non trovata o già in gestione.</p>");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            out.println("<br><br><a href='" + request.getContextPath() + "/DashboardServlet' style='color:#007bff; text-decoration:none; font-weight:bold;'>&larr; Torna alla Dashboard</a>");
            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idRichiesta = request.getParameter("id_richiesta");
        String[] operatoriScelti = request.getParameterValues("operatori");
        String caposquadraScelto = request.getParameter("caposquadra");
        String[] mezziScelti = request.getParameterValues("mezzi");
        String[] materialiScelti = request.getParameterValues("materiali"); // Raccolta materiali
        
        boolean avviato = false;

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Recupero dinamico dei dati reali della richiesta
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

                // 1. Aggiorna la richiesta portando lo stato a IN CORSO
                String sqlUpdate = "UPDATE richiesta_soccorso SET stato = 'IN_CORSO' WHERE id_richiesta = ?";
                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setString(1, idRichiesta);
                    stmtUpdate.executeUpdate();
                }
                
                // 2. Creiamo il record della missione usando i dati REALI
                int idMissioneGenerato = 0;
                String sqlInsert = "INSERT INTO missione (id_richiesta, obiettivo, posizione, stato, livello_successo, commenti, timestamp_fine) VALUES (?, ?, ?, 'IN_CORSO', NULL, NULL, NULL)";
                
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmtInsert.setInt(1, Integer.parseInt(idRichiesta));
                    stmtInsert.setString(2, descrizioneReale);
                    stmtInsert.setString(3, posizioneReale);
                    stmtInsert.executeUpdate();
                    
                    try (ResultSet keys = stmtInsert.getGeneratedKeys()) {
                        if (keys.next()) {
                            idMissioneGenerato = keys.getInt(1);
                        }
                    }
                }
                
                // 3. Inseriamo gli operatori e settiamo il ruolo gerarchico del Caposquadra (Punto 4 completato!)
                if (operatoriScelti != null && idMissioneGenerato > 0) {
                    String sqlOpMissione = "INSERT INTO assegnazione_operatori_missione (id_missione, id_utente, is_caposquadra) VALUES (?, ?, ?)";
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

                // 4. Inseriamo le relazioni con i mezzi scelti
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

                // 5. Inseriamo le relazioni con i materiali selezionati (Punto 4 completato!)
                if (materialiScelti != null && idMissioneGenerato > 0) {
                    String sqlMatMissione = "INSERT INTO assegnazione_materiale_missione (id_missione, id_materiale) VALUES (?, ?)";
                    try (PreparedStatement stmtMatM = conn.prepareStatement(sqlMatMissione)) {
                        for (String idMat : materialiScelti) {
                            stmtMatM.setInt(1, idMissioneGenerato);
                            stmtMatM.setInt(2, Integer.parseInt(idMat));
                            stmtMatM.executeUpdate();
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
            out.println("<script>");
            if (avviato) {
                out.println("alert('Missione avviata con successo in totale sicurezza: Caposquadra e Materiali assegnati correttamente.');");
                out.println("window.location.href='" + request.getContextPath() + "/DashboardServlet';");
            } else {
                out.println("alert('Errore bloccante durante l\\'avvio dell\\'intervento.');");
                out.println("window.history.back();");
            }
            out.println("</script>");
        }
    }
}