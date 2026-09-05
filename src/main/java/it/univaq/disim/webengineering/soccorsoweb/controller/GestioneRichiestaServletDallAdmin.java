package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.AssegnazioneOperatoreMissioneDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MaterialeDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MezzoDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.RichiestaSoccorsoDAO;
import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.UtenteDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GestioneRichiestaServletDallAdmin", urlPatterns = {"/GestioneRichiestaServletDallAdmin"})
public class GestioneRichiestaServletDallAdmin extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Classico init pulito.
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check permessi ruspante: se non sei ADMIN, non passi.
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // Prendo l'ID della richiesta dalla query string (es. ?id_richiesta=3)
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));

            // Istanzio l'esercito di DAO necessari per popolare la vista
            RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
            UtenteDAO utenteDao = new UtenteDAO();
            MezzoDAO mezzoDao = new MezzoDAO();
            MaterialeDAO materialeDao = new MaterialeDAO();

            // 1. Estrapolo i dati base della richiesta per mostrarli all'admin
            Map<String, String> dettagliRichiesta = richiestaDao.estraiDettagliRichiesta(idRichiesta);
            
            // 2. Mi faccio dare dai DAO tutte le liste delle risorse attualmente LIBERE e ATTIVE
            List<Map<String, String>> operatoriLiberi = utenteDao.estraiOperatoriDisponibili();
            List<Map<String, String>> mezziLiberi = mezzoDao.estraiMezziDisponibili();
            List<Map<String, String>> materialiLiberi = materialeDao.estraiMaterialiDisponibili();

            // 3. Piazzo tutto nel bagagliaio della request per FreeMarker
            request.setAttribute("richiesta", dettagliRichiesta);
            request.setAttribute("operatori", operatoriLiberi);
            request.setAttribute("mezzi", mezziLiberi);
            request.setAttribute("materiali", materialiLiberi);

            // Deleghiamo la costruzione dell'HTML (le famose <select>) al template .ftl!
            request.getRequestDispatcher("/WEB-INF/template/gestione_richiesta.ftl").forward(request, response);

        } catch (Exception e) {
            System.err.println("Errore catastrofico durante il caricamento della pagina di assegnazione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check permessi anche sulla POST (la sicurezza prima di tutto)
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // Estrapolo l'id della richiesta 
            int idRichiesta = Integer.parseInt(request.getParameter("id_richiesta"));
            
            // Uso getParameterValues per prendere gli ARRAY di dati multipli spuntati dall'Admin nel form
            String[] operatoriScelti = request.getParameterValues("operatori");
            String caposquadraScelto = request.getParameter("caposquadra");
            String[] mezziScelti = request.getParameterValues("mezzi");
            String[] materialiScelti = request.getParameterValues("materiali");

            // Controllo ruspante: non puoi avviare una missione senza almeno un operatore
            if (operatoriScelti != null && operatoriScelti.length > 0 && caposquadraScelto != null) {
                
                // Magia pura: evoco il DAO delle transazioni e gli passo gli array crudi!
                // Si smazzerà lui l'autocommit a false, gli insert multipli e gli eventuali rollback
                AssegnazioneOperatoreMissioneDAO assegnazioneDao = new AssegnazioneOperatoreMissioneDAO();
                boolean avvioOk = assegnazioneDao.avviaMissioneConRisorse(idRichiesta, operatoriScelti, caposquadraScelto, mezziScelti, materialiScelti);
                
                if (avvioOk) {
                    // Pattern PRG: se tutto va bene, rispediamo l'admin alla dashboard pulendo l'URL
                    response.sendRedirect(request.getContextPath() + "/DashboardServlet?msg=missione_avviata");
                    return;
                }
            }
            
            // Se mancano dati o la transazione fallisce, rimbalzo sulla stessa pagina mostrando errore
            response.sendRedirect(request.getContextPath() + "/GestioneRichiestaServletDallAdmin?id_richiesta=" + idRichiesta + "&errore=1");

        } catch (Exception e) {
            System.err.println("Panico totale durante la POST per avviare la missione...");
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?errore=critico");
        }
    }
}