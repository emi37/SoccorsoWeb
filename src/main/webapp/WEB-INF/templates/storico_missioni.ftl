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
                            <th>Caposquadra</th>
                            <th class="col-dettagli">Dettagli operazione</th>
                            <th class="col-voto">Valutazione (0-5)</th>
                            <th>Relazione finale</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list missioni_archiviate![] as lista>
                            <tr>
                                <td>#${lista.id_missione!}</td>
                                <td>#${lista.id_richiesta!}</td>
                                <td>${lista.caposquadra!"Nessuno"}</td>
                                <td>${lista.obiettivo!""}</td>
                                <td>${lista.livello_successo!"-"} / 5</td>
                                <td>${lista.commenti!""}</td>
                            </tr>
                        <#else>
                            <tr>
                                <td colspan="6" class="text-center text-muted">L'archivio/lo storico delle missioni è vuoto.</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>