<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Gestione mezzi</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <header class="topbar">
        <h1 class="main-title">Gestione mezzi</h1>
        <nav class="topbar-actions">
            <a href="DashboardServlet" class="btn btn-primary">Torna alla dashboard</a>
        </nav>
    </header>
    
    <div class="form-container form-container-box">
        <h2>Aggiungi un nuovo mezzo</h2>
        <form action="AggiungiMezzo" method="POST">
            <label for="nome">Nome mezzo (es. Ambulanza A1):</label>
            <input type="text" id="nome" name="nome" required>
            
            <label for="descrizione">Descrizione:</label>
            <input type="text" id="descrizione" name="descrizione" required>
            
            <button type="submit" class="btn">Aggiungi mezzo</button>
        </form>
    </div>
    
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Descrizione</th>
                <th>Stato (Attivo)</th>
                <th>Azioni</th>
            </tr>
        </thead>
        <tbody>
            <#list mezzi as m>
            <tr>
                <td>${m.id_mezzo!}</td>
                <td>${m.nome!}</td>
                <td>${m.descrizione!}</td>
                <td>${m.attivo!}</td>
                <td>
                        <a href="GestioneMezzi?elimina=${m.id_mezzo!}" class="btn btn-danger" onclick="return confirm('Sei sicuro di voler eliminare definitivamente questo mezzo?');">Elimina</a>
                    <a href="StoricoRisorsa?tipo=mezzo&id=${m.id_mezzo!}" class="btn">Storico</a>
                    <details>
                        <summary>Modifica</summary>
                        <form action="ModificaRisorsa" method="POST">
                            <input type="hidden" name="tipo" value="mezzo">
                            <input type="hidden" name="id" value="${m.id_mezzo!}">
                            <input type="text" name="nome" value="${m.nome!""}" maxlength="100" required>
                            <input type="text" name="descrizione" value="${m.descrizione!""}" maxlength="255">
                            <button type="submit" class="btn btn-primary">Salva</button>
                        </form>
                    </details>
                </td>
            </tr>
            <#else>
            <tr>
                <td colspan="4" class="text-center text-muted">Nessun mezzo disponibile al momento.</td>
            </tr>
            </#list>
        </tbody>
    </table>
</body>
</html>