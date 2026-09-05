<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Gestione Operatori</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h1>Gestione Operatori e Amministratori</h1>

    <!-- Form Creazione -->
    <div class="form-container" style="margin-bottom: 20px; padding: 15px; border: 1px solid #ccc;">
        <h2>Aggiungi un nuovo Utente</h2>
        <form action="CreaAdminEOperatoreServlet" method="POST">
            <label>Nome:</label>
            <input type="text" name="nome" required>
            
            <label>Cognome:</label>
            <input type="text" name="cognome" required>
            
            <label>Email:</label>
            <input type="email" name="email" required>
            
            <label>Password:</label>
            <input type="password" name="password" required>
            
            <label>Ruolo:</label>
            <select name="ruolo" required>
                <option value="OPERATORE">Operatore</option>
                <option value="ADMIN">Amministratore</option>
            </select>

            <br><br>
            <h3>Seleziona Abilità (Solo se Operatore):</h3>
            <#if abilita?? && abilita?size &gt; 0>
                <#list abilita as a>
                    <input type="checkbox" name="abilita" value="${a.id_abilita!}"> ${a.nome!}<br>
                </#list>
            </#if>

            <br>
            <h3>Seleziona Patenti (Solo se Operatore):</h3>
            <#if patenti?? && patenti?size &gt; 0>
                <#list patenti as p>
                    <input type="checkbox" name="patenti" value="${p.id_patente!}"> ${p.codice!}<br>
                </#list>
            </#if>

            <br><br>
            <button type="submit" class="btn">Crea Utente</button>
        </form>
    </div>

    <!-- Tabella Visualizzazione -->
    <h2>Lista Operatori</h2>
    <table border="1">
        <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Cognome</th>
            <th>Email</th>
            <th>Stato (Attivo)</th>
        </tr>
        <#if operatori?? && operatori?size &gt; 0>
            <#list operatori as o>
            <tr>
                <td>${o.id_utente!}</td>
                <td>${o.nome!}</td>
                <td>${o.cognome!}</td>
                <td>${o.email!}</td>
                <td>${o.attivo!}</td>
            </tr>
            </#list>
        <#else>
            <tr><td colspan="5">Nessun operatore disponibile al momento.</td></tr>
        </#if>
    </table>
    
    <br>
    <a href="DashboardServlet" class="btn">Torna alla Dashboard</a>
</body>
</html>