/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author edoar
 */
public class GestioneEmail {

    public static void notificaOperatoriAssegnati(
            Connection connessione,
            int idMissione,
            String oggetto,
            String messaggio) throws SQLException {
        /*
         * Query SQL:
         * prende email, nome e cognome degli operatori
         * assegnati alla missione indicata.
         *
         * La JOIN collega:
         * - utente
         * - assegnazione_operatori_missione
         *
         * Così troviamo solo gli operatori collegati a quella missione.
         */

        String sql = """
            SELECT u.email, u.nome, u.cognome
            FROM utente u
            JOIN assegnazione_operatori_missione aom
                ON u.id_utente = aom.id_utente
            WHERE aom.id_missione = ?
        """;
        try (PreparedStatement statement = connessione.prepareStatement(sql)) {
            statement.setInt(1, idMissione);

            try (ResultSet risultato = statement.executeQuery()) {
                boolean trovatiOperatori = false;

                while (risultato.next()) {
                    trovatiOperatori = true;

                    /*
                     * Leggiamo i dati dell'operatore dal database.
                     */
                    String email = risultato.getString("email");
                    String nome = risultato.getString("nome");
                    String cognome = risultato.getString("cognome");

                    System.out.println("======================================");
                    System.out.println("EMAIL SIMULATA A OPERATORE");
                    System.out.println("Destinatario: " + nome + " " + cognome + " <" + email + ">");
                    System.out.println("Oggetto: " + oggetto);
                    System.out.println("Messaggio:");
                    System.out.println(messaggio);
                    System.out.println("======================================");
                }
                /*
                 * Se la query non ha trovato operatori,
                 * stampiamo un messaggio di controllo in console.
                 */

                if (!trovatiOperatori) {
                    System.out.println("Nessun operatore assegnato alla missione #" + idMissione);
                }
            }
        }
    }
}
