package it.univaq.disim.webengineering.soccorsoweb.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("Avvio risoluzione e test connessione...");
        
        try (Connection conn = DBManager.getConnection()) {
            System.out.println("1. Connessione stabilita con successo!");
            
            // OSTACOLO IDENTIFICATO: Il vecchio hash non combaciava.
            // SOLUZIONE: Generiamo un hash fresco, locale e perfetto per la parola "admin".
            String passwordInChiaro = "admin";
            String nuovoHash = BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt());
            
            // Aggiorniamo forzatamente il database con il nuovo hash
            String sqlUpdate = "UPDATE utente SET password = ? WHERE email = 'admin@soccorsoweb.it'";
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                stmtUpdate.setString(1, nuovoHash);
                stmtUpdate.executeUpdate();
                System.out.println("2. Password dell'Admin rigenerata e aggiornata nel DB!");
            }
            
            // AGGIORNAMENTO NATIVO ANCHE PER GLI OPERATORI
            String passOperatoreInChiaro = "operatore";
            String hashOperatore = BCrypt.hashpw(passOperatoreInChiaro, BCrypt.gensalt());
            String sqlUpdateOp = "UPDATE utente SET password = ? WHERE ruolo = 'OPERATORE'";
            try (PreparedStatement stmtUpdateOp = conn.prepareStatement(sqlUpdateOp)) {
                stmtUpdateOp.setString(1, hashOperatore);
                int righeOp = stmtUpdateOp.executeUpdate();
                System.out.println("2b. Password rigenerata per " + righeOp + " Operatori nel DB!");
            }
            
            // VERIFICA RIFLESSIVA: Andiamo a rileggere il dato e controlliamo se ora passa il test
            String sqlSelect = "SELECT email, password FROM utente WHERE ruolo = 'ADMIN'";
            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect);
                 ResultSet rs = stmtSelect.executeQuery()) {
                
                if (rs.next()) {
                    String emailTrovata = rs.getString("email");
                    String hashTrovato = rs.getString("password");
                    
                    System.out.println("3. Trovato Admin: " + emailTrovata);
                    
                    // Verifica finale di BCrypt
                    if (BCrypt.checkpw(passwordInChiaro, hashTrovato)) {
                        System.out.println("4. TEST SUPERATO : password riconosciuta");
                    } else {
                        System.out.println("4. TEST FALLITO: errore.");
                    }
                }
            }

            // VERIFICA AGGIUNTIVA PER UN OPERATORE DEL GRUPPO
            String sqlSelectOp = "SELECT email, password FROM utente WHERE ruolo = 'OPERATORE' LIMIT 1";
            try (PreparedStatement stmtSelectOp = conn.prepareStatement(sqlSelectOp);
                 ResultSet rsOp = stmtSelectOp.executeQuery()) {
                
                if (rsOp.next()) {
                    String emailOp = rsOp.getString("email");
                    String hashOp = rsOp.getString("password");
                    
                    System.out.println("3b. Verifica Operatore Campione: " + emailOp);
                    
                    if (BCrypt.checkpw(passOperatoreInChiaro, hashOp)) {
                        System.out.println("4b. TEST OPERATORI SUPERATO: password riconosciuta dal server!");
                    } else {
                        System.out.println("4b. TEST OPERATORI FALLITO: errore hash.");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("ERRORE DURANTE IL TEST:");
            e.printStackTrace();
        }
    }
}