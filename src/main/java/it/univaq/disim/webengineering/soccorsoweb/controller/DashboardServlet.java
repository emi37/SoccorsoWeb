package it.univaq.disim.webengineering.soccorsoweb.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/DashboardServlet"})
public class DashboardServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        // Solita chiamata al padre. Non serve caricare driver o altro qui!
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Prendo la sessione se esiste, senza crearne una a vuoto
        HttpSession sessioneAttuale = request.getSession(false);

        // Controllo accessi ruspante: se non sei ADMIN o non sei loggato, torni alla porta
        if (sessioneAttuale == null || !"ADMIN".equals(sessioneAttuale.getAttribute("ruolo"))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        // Se passi il controllo, ti sparo sulla dashboard statica (in attesa di agganciarci FreeMarker)
        response.sendRedirect(request.getContextPath() + "/dashboard.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Se per qualche strano motivo dovesse arrivare una POST a questa pagina,
        // per non far crashare il server col classico errore 405, la giriamo alla GET.
        doGet(request, response);
    }
}
