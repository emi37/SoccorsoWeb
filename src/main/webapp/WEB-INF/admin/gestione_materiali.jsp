<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>gestione materiali - admin</title>
    <style>
        body { font-family: sans-serif; margin: 0; padding: 15px; background-color: #f4f6f9; }
        h1, h2 { color: #333; }
        
        /* Contenitore principale fluido */
        .main-container { max-width: 1000px; width: 100%; margin: 0 auto; box-sizing: border-box; }
        
        /* Struttura responsive per la tabella */
        .table-responsive { width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; border-radius: 4px; border: 1px solid #ddd; margin-top: 20px; }
        table { width: 100%; border-collapse: collapse; background: white; min-width: 650px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #0056b3; color: white; }
        
        .stato-libero { color: green; font-weight: bold; }
        .stato-impegnato { color: orange; font-weight: bold; }
        .btn-elimina { color: red; text-decoration: none; font-weight: bold; }
        .btn-disabled { color: #aaa; text-decoration: none; cursor: not-allowed; }
        
        .form-container { background: white; padding: 20px; border-radius: 5px; margin-bottom: 20px; border: 1px solid #ddd; box-sizing: border-box; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input, .form-group textarea { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
        .form-group textarea { resize: vertical; }
        .btn-invia { background-color: #28a745; color: white; padding: 10px 15px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; width: 100%; max-width: 200px; font-weight: bold; }
    </style>

    <script>
        function confermaEliminazione(event, nomeMateriale) {
            var conferma = confirm("sei sicuro di voler rimuovere il materiale '" + nomeMateriale + "' dalle dotazioni attive?");
            if (!conferma) {
                event.preventDefault();
            }
        }
    </script>
</head>
<body>

    <div class="main-container">
        <h1>Pannello gestione materiali</h1>
        <p><a href="${pageContext.request.contextPath}/DashboardServlet" style="color: #007bff; text-decoration: none; font-weight: bold;">← torna al pannello di controllo</a></p>
        
        <hr style="border: 0; border-top: 1px solid #dee2e6; margin: 20px 0;">

        <div class="form-container">
            <h2>aggiungi materiale o attrezzatura</h2>
            <form action="${pageContext.request.contextPath}/AggiungiMateriale" method="post">
                <div class="form-group">
                    <label for="nome">nome del materiale:</label>
                    <input type="text" id="nome" name="nome" required placeholder="es. kit rianimazione avanzato, estintore co2...">
                </div>
                <div class="form-group">
                    <label for="descrizione">descrizione e specifiche:</label>
                    <textarea id="descrizione" name="descrizione" rows="3" required placeholder="inserisci i dettagli tecnici..."></textarea>
                </div>
                <button type="submit" class="btn-invia">Salva nuovo materiale</button>
            </form>
        </div>

        <h2>Elenco nuovo materiale</h2>
        
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>id</th>
                        <th>nome materiale</th>
                        <th>descrizione</th>
                        <th>stato operativo</th>
                        <th>azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        ArrayList<Map<String, String>> materiali = (ArrayList<Map<String, String>>) request.getAttribute("materiali");
                        if (materiali != null && !materiali.isEmpty()) {
                            for (Map<String, String> m : materiali) {
                    %>
                    <tr>
                        <td><%= m.get("id") %></td>
                        <td><strong><%= m.get("nome") %></strong></td>
                        <td><%= m.get("descrizione") %></td>
                        <td>
                            <% if ("LIBERO".equals(m.get("stato"))) { %>
                                <span class="stato-libero">libero</span>
                            <% } else { %>
                                <span class="stato-impegnato">assegnato a una missione</span>
                            <% } %>
                        </td>
                        <td>
                            <% if ("LIBERO".equals(m.get("stato"))) { %>
                                <a href="${pageContext.request.contextPath}/GestioneMateriali?elimina=<%= m.get("id") %>" 
                                   class="btn-elimina" 
                                   onclick="confermaEliminazione(event, '<%= m.get("nome") %>')">rimuovi</a>
                            <% } else { %>
                                <span class="btn-disabled" title="materiale attualmente sul campo">bloccato</span>
                            <% } %>
                        </td>
                    </tr>
                    <% 
                            }
                        } else {
                    %>
                    <tr>
                        <td colspan="5" style="text-align: center; color: gray;">nessun materiale attivo censito nel sistema.</td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

</body>
</html>