<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Storico - SoccorsoWeb</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container mt-20">
        <div class="topbar">
            <h1 class="main-title">${titolo}</h1>
            <div class="topbar-actions">
                <a href="${ritorno}" class="btn btn-primary">Indietro</a>
            </div>
        </div>

        <div class="card">
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th class="col-id">Missione</th>
                            <th>Obiettivo</th>
                            <th>Posizione</th>
                            <th>Inizio</th>
                            <th>Fine</th>
                            <th>Stato</th>
                            <th>Esito</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list storico![] as m>
                            <tr>
                                <td><a href="DettaglioMissioneServlet?id_missione=${m.id_missione!}">#${m.id_missione!}</a></td>
                                <td>${m.obiettivo!""}</td>
                                <td>${m.posizione!""}</td>
                                <td>${m.inizio!""}</td>
                                <td>${m.fine!"-"}</td>
                                <td>${m.stato!""}</td>
                                <td>${m.esito!"-"}</td>
                            </tr>
                        <#else>
                            <tr>
                                <td colspan="7" class="text-center text-muted">Nessuna missione registrata.</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>
