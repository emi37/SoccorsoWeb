package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.MaterialeDAO;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@WebServlet(name = "GestioneMaterialiServlet", urlPatterns = {"/GestioneMateriali"})
public class GestioneMaterialiServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Solita prassi: chiamiamo il padre e lasciamo che il DBManager gestisca i driver
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Il nostro solito buttafuori: check permessi ruspante per bloccare i non-admin
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        try {
            // Evochiamo il DAO
            MaterialeDAO materialeDao = new MaterialeDAO();

            // 1. GESTIONE CANCELLAZIONE (Soft-Delete)
            // Se nell'URL ci passano il parametro ?elimina=ID, facciamo la cancellazione logica
            String idDaEliminareStr = request.getParameter("elimina");
            if (idDaEliminareStr != null && !idDaEliminareStr.trim().isEmpty()) {
                int idMateriale = Integer.parseInt(idDaEliminareStr);

                // Il DAO fa l'update e nasconde il materiale (se non è usato in missioni in corso)
                materialeDao.nascondiMaterialeSeLibero(idMateriale);

                // Pattern PRG: ricarico la pagina pulendo la query string per evitare doppi click
                response.sendRedirect(request.getContextPath() + "/GestioneMateriali");
                return;
            }

            // 2. LETTURA DEI DATI 
            // Mi faccio dare la lista dei materiali già calcolata col loro stato (LIBERO o IMPEGNATO)
            List<Map<String, String>> listaMateriali = materialeDao.estraiTuttiIMaterialiConStato();

            // Schiaffo la lista nella request per farla leggere al template engine
            request.setAttribute("materiali", listaMateriali);

            // Deleghiamo a FreeMarker la creazione dell'HTML passando per la cartella protetta WEB-INF
            request.getRequestDispatcher("/WEB-INF/admin/gestione_materiali.ftl").forward(request, response);

        } catch (Exception e) {
            // Immancabile stampata a console se scoppia qualcosa
            System.err.println("Errore brutto nel controller della gestione materiali...");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se per caso arriva una POST in questa pagina (es. da un form strambo),
        // per non far cacciare un errore 405 dal server la giriamo comodamente alla GET
        doGet(request, response);
    }
}
