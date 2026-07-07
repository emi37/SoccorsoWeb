<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>gestione parco mezzi - admin</title>
    <style>
        body { font-family: sans-serif; margin: 20px; background-color: #f4f6f9; }
        h1, h2 { color: #333; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; background: white; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #0056b3; color: white; }
        .stato-libero { color: green; font-weight: bold; }
        .stato-impegnato { color: orange; font-weight: bold; }
        .btn-elimina { color: red; text-decoration: none; font-weight: bold; }
        .btn-disabled { color: #aaa; text-decoration: none; cursor: not-allowed; }
        .form-container { background: white; padding: 20px; border-radius: 5px; margin-bottom: 20px; border: 1px solid #ddd; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input, .form-group textarea { width: 100%; padding: 8px; box-sizing: border-box; }
        .btn-invia { background-color: #28a745; color: white; padding: 10px 15px; border: none; cursor: pointer; font-size: 16px; }
    </style>

    <script>
        function confermaEliminazione(event, nomeMezzo) {
            var conferma = confirm("sei sicuro di voler rimuovere il mezzo '" + nomeMezzo + "' dal parco attivo?");
            if (!conferma) {
                event.preventDefault();
            }
        }
    </script>
</head>
<body>

    <h1>Pannello gestione mezzi</h1>
    
    <p><a href="${pageContext.request.contextPath}/DashboardServlet">← torna al pannello di controllo</a></p>
    
    <hr>

    <div class="form-container">
        <h2>aggiungi un nuovo mezzo</h2>
        <form action="${pageContext.request.contextPath}/AggiungiMezzo" method="post">
            <div class="form-group">
                <label for="nome">nome del mezzo:</label>
                <input type="text" id="nome" name="nome" required placeholder="es. ambulanza b3, automedica fast2...">
            </div>
            <div class="form-group">
                <label for="descrizione">descrizione delle dotazioni:</label>
                <textarea id="descrizione" name="descrizione" rows="3" required placeholder="inserisci i dettagli del veicolo..."></textarea>
            </div>
            <button type="submit" class="btn-invia">Salva nuovo mezzo</button>
        </form>
    </div>

    <h2>Elenco nuovi mezzi</h2>
    <table>
        <thead>
            <tr>
                <th>id</th>
                <th>nome mezzo</th>
                <th>descrizione</th>
                <th>stato operativo</th>
                <th>azioni</th>
            </tr>
        </thead>
        <tbody>
            <%
                ArrayList<Map<String, String>> mezzi = (ArrayList<Map<String, String>>) request.getAttribute("mezzi");
                if (mezzi != null && !mezzi.isEmpty()) {
                    for (Map<String, String> m : mezzi) {
            %>
            <tr>
                <td><%= m.get("id") %></td>
                <td><strong><%= m.get("nome") %></strong></td>
                <td><%= m.get("descrizione") %></td>
                <td>
                    <% if ("LIBERO".equals(m.get("stato"))) { %>
                        <span class="stato-libero">libero</span>
                    <% } else { %>
                        <span class="stato-impegnato">impegnato in missione</span>
                    <% } %>
                </td>
                <td>
                    <% if ("LIBERO".equals(m.get("stato"))) { %>
                        <a href="${pageContext.request.contextPath}/GestioneMezzi?elimina=<%= m.get("id") %>" 
                           class="btn-elimina" 
                           onclick="confermaEliminazione(event, '<%= m.get("nome") %>')">rimuovi</a>
                    <% } else { %>
                        <span class="btn-disabled" title="non puoi rimuovere un mezzo impegnato">bloccato</span>
                    <% } %>
                </td>
            </tr>
            <% 
                    }
                } else {
            %>
            <tr>
                <td colspan="5" style="text-align: center;">nessun mezzo attivo registrato nel sistema.</td>
            </tr>
            <% } %>
        </tbody>
    </table>

</body>
</html>