<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Archivio richieste - SoccorsoWeb</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container mt-20">
        <div class="topbar">
            <h1 class="main-title">Archivio richieste di soccorso</h1>
            <div class="topbar-actions">
                <a href="DashboardServlet" class="btn btn-primary">Torna al pannello di controllo</a>
            </div>
        </div>

        <div class="card">
            <form action="Richieste" method="GET" class="form-row-flexible">
                <div>
                    <label for="stato" class="form-label">Stato:</label>
                    <select id="stato" name="stato" class="form-control">
                        <option value="">Tutti</option>
                        <#list stati as s>
                            <option value="${s}"<#if s == filtro_stato> selected</#if>>${s}</option>
                        </#list>
                    </select>
                </div>
                <div>
                    <label for="da" class="form-label">Dal:</label>
                    <input type="date" id="da" name="da" class="form-control" value="${filtro_da}">
                </div>
                <div>
                    <label for="a" class="form-label">Al:</label>
                    <input type="date" id="a" name="a" class="form-control" value="${filtro_a}">
                </div>
                <div>
                    <button type="submit" class="btn btn-primary">Filtra</button>
                </div>
            </form>
        </div>

        <div class="card">
            <p class="text-muted">Richieste trovate: ${totale}</p>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th class="col-id">ID</th>
                            <th>Data</th>
                            <th>Segnalante</th>
                            <th>Posizione</th>
                            <th>Descrizione</th>
                            <th>Stato</th>
                            <th>Missione</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list richieste![] as r>
                            <tr>
                                <td>#${r.id!}</td>
                                <td>${r.data!""}</td>
                                <td>${r.segnalante!""} (${r.email!""})</td>
                                <td>${r.posizione!""}</td>
                                <td>${r.descrizione!""}</td>
                                <td>${r.stato!""}</td>
                                <td>
                                    <#if r.id_missione??>
                                        <a href="DettaglioMissioneServlet?id_missione=${r.id_missione}" class="btn">Apri #${r.id_missione}</a>
                                    <#else>
                                        -
                                    </#if>
                                </td>
                            </tr>
                        <#else>
                            <tr>
                                <td colspan="7" class="text-center text-muted">Nessuna richiesta trovata.</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>

            <p class="mt-20">
                <#if (pagina > 1)>
                    <a class="btn" href="Richieste?stato=${filtro_stato?url}&da=${filtro_da?url}&a=${filtro_a?url}&pagina=${pagina - 1}">&laquo; Precedente</a>
                </#if>
                Pagina ${pagina} di ${pagine}
                <#if (pagina < pagine)>
                    <a class="btn" href="Richieste?stato=${filtro_stato?url}&da=${filtro_da?url}&a=${filtro_a?url}&pagina=${pagina + 1}">Successiva &raquo;</a>
                </#if>
            </p>
        </div>
    </div>
</body>
</html>
