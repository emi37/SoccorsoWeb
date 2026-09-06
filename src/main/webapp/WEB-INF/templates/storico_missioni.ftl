<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Storico Interventi Conclusi - SoccorsoWeb</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container mt-20">

        <div class="topbar">
            <h1 class="main-title">Storico delle missioni concluse</h1>
            <div class="topbar-actions">
                <a href="DashboardServlet" class="btn btn-primary">Torna al pannello di controllo</a>
            </div>
        </div>

        <div class="card">
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th class="col-id">ID missione</th>
                            <th class="col-id">ID richiesta</th>
                            <th class="col-dettagli">Dettagli operazione</th>
                            <th class="col-voto">Valutazione (0-5)</th>
                            <th>Relazione finale</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#if missioni_archiviate?? && missioni_archiviate?size &gt; 0>
                            <#list missioni_archiviate as m>
                            <tr>
                                <td>#${m.id_missione!}</td>
                                <td>#${m.id_richiesta!}</td>
                                <td>${m.obiettivo!""}</td>
                                <td>${m.livello_successo!"-"} / 5</td>
                                <td>${m.commenti!""}</td>
                            </tr>
                            </#list>
                        <#else>
                            <tr>
                                <td colspan="5" class="text-center text-muted">Nessuna missione in storico al momento. L'archivio è vuoto.</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>