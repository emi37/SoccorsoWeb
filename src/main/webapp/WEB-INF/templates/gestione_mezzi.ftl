


<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Gestione Mezzi</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h1>Gestione Mezzi</h1>

    <!-- Inizio Form Aggiunta Mezzo -->
    <div class="form-container" style="margin-bottom: 20px; padding: 15px; border: 1px solid #ccc;">
        <h2>Aggiungi un nuovo Mezzo</h2>
        <form action="AggiungiMezzo" method="POST">
            <label for="nome">Nome Mezzo (es. Ambulanza A1):</label>
            <input type="text" id="nome" name="nome" required>
            
            <label for="descrizione">Descrizione:</label>
            <input type="text" id="descrizione" name="descrizione" required>
            
            <button type="submit" class="btn">Aggiungi Mezzo</button>
        </form>
    </div>
    <br>
    <!-- Fine Form Aggiunta Mezzo -->
    
    <table border="1">
        <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Descrizione</th>
            <th>Stato (Attivo)</th>
            <th>Azioni</th>
        </tr>
        
        <#if mezzi?? && mezzi?size &gt; 0>
            <#list mezzi as m>
            <tr>
                <td>${m.id_mezzo!}</td>
                <td>${m.nome!}</td>
                <td>${m.descrizione!}</td>
                <td>${m.attivo!}</td> 
                <td>
                    <a href="GestioneMezzi?elimina=${m.id_mezzo!}">Nascondi</a>
                    <a href="StoricoMezzo?id=${m.id_mezzo!}">Vedi Storico</a>
                </td>
            </tr>
            </#list>
        <#else>
            <tr><td colspan="5">Nessun mezzo disponibile al momento.</td></tr>
        </#if>
    </table>
    
    <br>
    <a href="DashboardServlet" class="btn">Torna alla Dashboard</a>
</body>
</html>