package it.univaq.disim.webengineering.soccorsoweb.controller;

import it.univaq.disim.webengineering.soccorsoweb.controller.DAO.RichiestaSoccorsoDAO;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "IgnoraRichiestaServlet", urlPatterns = {"/IgnoraRichiestaServlet"})
public class IgnoraRichiestaServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Solita chiamata al padre. Il driver MySQL se lo gestisce DBManager in background.
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Controllo di sicurezza: se non sei ADMIN loggato, torni al login.
        HttpSession sessioneAttuale = request.getSession(false);
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // 2. Pesco l'id della richiesta da ignorare dalla query string (es. ?id_richiesta=8)
        String idDaIgnorareStr = request.getParameter("id_richiesta");

        try {
            // Controllo ruspante per evitare NullPointer o stringhe vuote
            if (idDaIgnorareStr != null && !idDaIgnorareStr.trim().isEmpty()) {

                int idRichiesta = Integer.parseInt(idDaIgnorareStr);

                // 3. Magia del DAO: delego a lui la query di UPDATE
                RichiestaSoccorsoDAO richiestaDao = new RichiestaSoccorsoDAO();
                richiestaDao.aggiornaStatoRichiestaInIgnorata(idRichiesta);
            }
        } catch (Exception e) {
            // Rete di sicurezza in console se l'ID passato non è un numero o il DB va offline
            System.err.println("Panico nel controller durante lo scarto della richiesta...");
            e.printStackTrace();
        }

        // 4. Pattern PRG: Finito il giro, rimbalziamo l'admin sulla dashboard per pulire l'URL
        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Se in futuro modifichi l'HTML usando un <form method="POST"> invece di un link, 
        // questa Servlet non andrà in crash (Errore 405), ma girerà la pratica direttamente al doGet!
        doGet(request, response);
    }
}
