package it.univaq.disim.webengineering.soccorsoweb.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Invio delle notifiche per email (creazione/aggiornamento/chiusura missione, nuovi utenti).
 *
 * La specifica ammette di non configurare un vero server di posta e di "generare il contenuto
 * della mail a scopo illustrativo": qui la mail viene COMPOSTA per intero e scritta nel log del
 * server (alla voce SIMULAZIONE EMAIL). Tutto il resto del sito usa solo questa classe, quindi
 * per spedire email vere basta sostituire il corpo del metodo inviaEmail (ad esempio con
 * Jakarta Mail) senza toccare nessuna servlet.
 */
public final class NotificaService {

    private static final Logger LOGGER = Logger.getLogger(NotificaService.class.getName());

    private NotificaService() {
    }

    // punto UNICO in cui una mail "parte": qui va sostituita la simulazione con un invio reale
    public static void inviaEmail(String destinatario, String oggetto, String corpo) {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            return;
        }
        LOGGER.info("\n=====================================\n"
                + "SIMULAZIONE EMAIL\n"
                + "A: " + destinatario + "\n"
                + "Oggetto: " + oggetto + "\n\n"
                + corpo + "\n"
                + "=====================================");
    }

    // manda la stessa notifica a tutti gli operatori indicati (liste di contatti: nome, cognome, email)
    public static void notificaOperatori(List<Map<String, String>> contatti, String oggetto, String corpo) {
        if (contatti == null) {
            return;
        }
        for (Map<String, String> contatto : contatti) {
            String nominativo = (contatto.getOrDefault("nome", "") + " " + contatto.getOrDefault("cognome", "")).trim();
            String ruolo = "true".equals(contatto.get("caposquadra")) ? "\nSei il CAPOSQUADRA di questa missione." : "";
            inviaEmail(contatto.get("email"), oggetto, "Ciao " + nominativo + "," + ruolo + "\n\n" + corpo);
        }
    }

    // indirizzo base del sito (http://host:porta/contesto), per costruire i link nelle mail
    public static String baseUrl(HttpServletRequest request) {
        String schema = request.getScheme();
        int porta = request.getServerPort();
        boolean portaDiDefault = ("http".equals(schema) && porta == 80) || ("https".equals(schema) && porta == 443);
        return schema + "://" + request.getServerName() + (portaDiDefault ? "" : ":" + porta) + request.getContextPath();
    }
}
