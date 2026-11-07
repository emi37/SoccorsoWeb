<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Gestione materiale</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <header class="topbar">
        <h1 class="main-title">Gestione materiale</h1>
        <nav class="topbar-actions">
            <a href="DashboardServlet" class="btn btn-primary">Torna alla Dashboard</a>
        </nav>
    </header>
    
    <div class="form-container form-container-box">
        <h2>Aggiungi un nuovo materiale</h2>
        <form action="AggiungiMateriale" method="POST">
            <label for="nome">Nome materiale:</label>
            <input type="text" id="nome" name="nome" required>
            
            <label for="descrizione">Descrizione:</label>
            <input type="text" id="descrizione" name="descrizione" required>
            
            <button type="submit" class="btn">Aggiungi materiale</button>
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
            <#list materiali as m>
            <tr>
                <td>${m.id_materiale!}</td>
                <td>${m.nome!}</td>
                <td>${m.descrizione!}</td>
                <td>${m.attivo!}</td>
                <td>          
                    <a href="GestioneMateriali?elimina=${(m.id_materiale)!}" class="btn btn-danger" onclick="return confirm('Sei sicuro di voler eliminare definitivamente questo materiale?');">Elimina</a>
                    <a href="StoricoRisorsa?tipo=materiale&id=${m.id_materiale!}" class="btn">Storico</a>
                    <details>
                        <summary>Modifica</summary>
                        <form action="ModificaRisorsa" method="POST">
                            <input type="hidden" name="tipo" value="materiale">
                            <input type="hidden" name="id" value="${m.id_materiale!}">
                            <input type="text" name="nome" value="${m.nome!""}" maxlength="100" required>
                            <input type="text" name="descrizione" value="${m.descrizione!""}" maxlength="255">
                            <button type="submit" class="btn btn-primary">Salva</button>
                        </form>
                    </details>
                </td>
            </tr>
            <#else>
            <tr>
                <td colspan="5" class="text-center text-muted">Nessun materiale disponibile al momento.</td>
            </tr>
            </#list>
        </tbody>
    </table>
</body>
</html>