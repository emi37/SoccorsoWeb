package it.univaq.disim.webengineering.soccorsoweb.listener;

import it.univaq.disim.webengineering.soccorsoweb.dao.SoccorsoDataLayer;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parte e si ferma insieme all'applicazione (come i listener visti a lezione).
 * Ogni 10 minuti elimina le richieste di soccorso mai convalidate dal segnalante
 * (specifica: le richieste non convalidate devono essere eliminate dopo un po' di tempo).
 */
@WebListener
public class PulizieProgrammateListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(PulizieProgrammateListener.class.getName());

    // dopo quanti minuti una richiesta non convalidata viene eliminata
    private static final int SCADENZA_MINUTI = 60;

    private ScheduledExecutorService pianificatore;

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        pianificatore = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "pulizia-richieste");
            t.setDaemon(true);
            return t;
        });
        pianificatore.scheduleWithFixedDelay(() -> {
            try {
                int eliminate = SoccorsoDataLayer.getInstance().getRichiestaSoccorsoDAO()
                        .eliminaRichiesteNonConvalidate(SCADENZA_MINUTI);
                if (eliminate > 0) {
                    LOGGER.info("Eliminate " + eliminate + " richieste non convalidate");
                }
            } catch (Exception e) {
                // un errore non deve fermare le esecuzioni successive
                LOGGER.log(Level.WARNING, "Pulizia richieste non riuscita", e);
            }
        }, 1, 10, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        if (pianificatore != null) {
            pianificatore.shutdownNow();
        }
    }
}
