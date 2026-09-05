<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Gestione Materiali</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h1>Gestione Materiali</h1>

    <!-- Inizio Form Aggiunta Materiale -->
    <div class="form-container" style="margin-bottom: 20px; padding: 15px; border: 1px solid #ccc;">
        <h2>Aggiungi un nuovo Materiale</h2>
        <form action="AggiungiMateriale" method="POST">
            <label for="nome">Nome Materiale:</label>
            <input type="text" id="nome" name="nome" required>
            
            <label for="descrizione">Descrizione:</label>
            <input type="text" id="descrizione" name="descrizione" required>
            
            <button type="submit" class="btn">Aggiungi Materiale</button>
        </form>
    </div>
    <br>
    <!-- Fine Form Aggiunta Materiale -->
    
    <table border="1">
        <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Descrizione</th>
            <th>Stato (Attivo)</th>
            <th>Azioni</th>
        </tr>
        
        <#if materiali?? && materiali?size &gt; 0>
            <#list materiali as m>
            <tr>
                <td>${m.id_materiale!}</td>
                <td>${m.nome!}</td>
                <td>${m.descrizione!}</td>
                <td>${m.attivo!}</td> 
                <td>
                    <a href="GestioneMateriali?elimina=${m.id_materiale!}">Nascondi</a>
                    <!-- Bottone per lo storico (richiesto dalle specifiche) -->
                    <a href="StoricoMateriale?id=${m.id_materiale!}">Vedi Storico</a>
                </td>
            </tr>
            </#list>
        <#else>
            <tr><td colspan="5">Nessun materiale disponibile al momento.</td></tr>
        </#if>
    </table>
    
    <br>
    <a href="DashboardServlet" class="btn">Torna alla Dashboard</a>
</body>
</html>