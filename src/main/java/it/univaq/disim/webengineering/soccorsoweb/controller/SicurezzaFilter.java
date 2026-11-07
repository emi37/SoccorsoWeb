package it.univaq.disim.webengineering.soccorsoweb.controller;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * Filtro che si applica a TUTTE le richieste del sito.
 *
 * 1) Protezione CSRF: un sito malevolo potrebbe far inviare al browser di un admin
 *    loggato un form verso il nostro sito (es. "chiudi missione", "ignora richiesta").
 *    Il browser allega i cookie di sessione, quindi il server non se ne accorgerebbe.
 *    Per le richieste che MODIFICANO dati (POST, PUT, DELETE, PATCH) controlliamo che
 *    provengano dal nostro stesso sito, usando gli header che il browser aggiunge da solo
 *    e che una pagina esterna non può falsificare (Sec-Fetch-Site, Origin, Referer).
 *
 * 2) Header di sicurezza sulle risposte (anti clickjacking, anti sniffing dei contenuti).
 */
@WebFilter(filterName = "SicurezzaFilter", urlPatterns = {"/*"})
public class SicurezzaFilter implements Filter {

    private static final Set<String> METODI_CHE_MODIFICANO = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // il sito non deve poter essere mostrato dentro un iframe di un'altra pagina
        response.setHeader("X-Frame-Options", "DENY");
        // il browser non deve "indovinare" il tipo dei contenuti
        response.setHeader("X-Content-Type-Options", "nosniff");
        // l'URL completo non viene mandato ad altri siti nel Referer
        response.setHeader("Referrer-Policy", "same-origin");

        if (METODI_CHE_MODIFICANO.contains(request.getMethod()) && !provieneDalNostroSito(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Richiesta da un altro sito non consentita");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean provieneDalNostroSito(HttpServletRequest request) {
        // 1) i browser moderni dicono da soli da dove parte la richiesta
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if ("same-origin".equals(fetchSite) || "none".equals(fetchSite)) {
            return true;
        }
        if ("cross-site".equals(fetchSite)) {
            return false;
        }

        // 2) altrimenti (o se "same-site", che può essere un'altra porta) confrontiamo
        //    host e porta di Origin, o in sua assenza di Referer, con quelli del nostro sito
        String hostNostro = request.getHeader("Host");
        String origine = request.getHeader("Origin");
        if (origine != null) {
            return stessoHost(origine, hostNostro);
        }
        String referer = request.getHeader("Referer");
        if (referer != null) {
            return stessoHost(referer, hostNostro);
        }

        // 3) nessun header: non è una richiesta fatta da una pagina web di un altro sito
        //    (un browser li aggiunge sempre sulle POST cross-site), ad esempio un client da riga di comando
        return true;
    }

    private boolean stessoHost(String url, String hostNostro) {
        if (hostNostro == null) {
            return false;
        }
        try {
            // "Origin: null" (contesti anonimi o sandbox) non ha un host e viene rifiutato
            String autorita = URI.create(url).getAuthority();
            return autorita != null && autorita.equalsIgnoreCase(hostNostro);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
