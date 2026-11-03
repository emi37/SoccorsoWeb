<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Dettaglio Missione - SoccorsoWeb</title>
    <link rel="stylesheet" type="text/css" href="${(request.contextPath)!""}/css/style.css">
</head>
<body class="flex-center-body">

    <main class="page-content">
        <header class="topbar">
            <div>
                <h1 class="main-title">Gestione Missione #${(missione.id_missione)!""}</h1>
                <p class="subtitle">Dettagli operativi e assegnazione rinforzi</p>
            </div>
            <nav class="topbar-actions">
                <a href="${(request.contextPath)!""}/DashboardServlet" class="btn"> Torna alla Dashboard</a>
            </nav>
        </header>

        <section class="card">
            <h2 class="section-title">Dettagli missione</h2>
            <div class="table-wrapper">
                <table>
                    <tbody>
                        <tr>
                            <th>ID missione</th>
                            <td>${(missione.id_missione)!""}</td>
                        </tr>
                        <tr>
                            <th>Obiettivo</th>
                            <td>${(missione.obiettivo)!""}</td>
                        </tr>
                        <tr>
                            <th>Posizione</th>
                            <td>${(missione.posizione)!""}</td>
                        </tr>
                        <tr>
                            <th>Stato</th>
                            <td>
                                <#if (missione.stato!"") == "IN_CORSO">
                                    <span class="badge-status in-corso">IN_CORSO</span>
                                <#else>
                                    <span class="badge-status chiusa">${(missione.stato)!""}</span>
                                </#if>
                            </td>
                        </tr>
                        <tr>
                            <th>Inizio intervento</th>
                            <td>${(missione.inizio)!""}</td>
                        </tr>
                        <tr>
                            <th>Caposquadra</th>
                            <td>${(missione.caposquadra)!"Non specificato"}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="card">
            <h2 class="section-title">Risorse assegnate</h2>
            
            <h3>Automezzi sul posto</h3>
            <ul>
                <#list missione.mezzi as mezzo>
                    <li>${mezzo}</li>
                <#else>
                    <li class="text-muted">Nessun mezzo registrato.</li>
                </#list>
            </ul>

            <h3>Materiali impiegati</h3>
            <ul>
                <#list missione.materiali as mat>
                    <li>${mat}</li>
                <#else>
                    <li class="text-muted">Nessun materiale registrato.</li>
                </#list>
            </ul>
        </section>

        <#if (missione.stato!"") == "IN_CORSO">
        <section class="card">
            <h2 class="section-title">Modifica Missione</h2>
            <p class="text-muted">Aggiorna le informazioni della missione.</p>
            
            <form action="${(request.contextPath)!""}/DettaglioMissioneServlet" method="POST">
                <input type="hidden" name="id_missione" value="${(missione.id_missione)!""}">
                <input type="hidden" name="azione" value="modifica_missione">

                <div class="form-row-flexible mt-20">
                    <div>
                        <label for="obiettivo" class="form-label">Aggiorna obiettivo:</label>
                        <input type="text" id="obiettivo" name="obiettivo" value="${(missione.obiettivo)!''}" class="form-control" required>
                    </div>
                    <div>
                        <label for="posizione" class="form-label">Aggiorna posizione:</label>
                        <input type="text" id="posizione" name="posizione" value="${(missione.posizione)!''}" class="form-control" required>
                    </div>
                </div>

                <div class="form-row-flexible mt-20">
                    
                    <fieldset class="fieldset-antispam">
                        <legend class="legend-antispam">Aggiungi Operatori:</legend>
                        <#list operatori_disponibili as op>
                            <div class="mb-24">
                                <label>
                                    <input type="checkbox" name="id_operatore" value="${op.id_utente}"> 
                                    ${(op.nome_completo)!(op.nome)!""} ${(op.cognome)!""}
                                </label>
                            </div>
                        <#else>
                            <p class="text-muted">Nessun operatore disponibile.</p>
                        </#list>
                    </fieldset>
 
                    <fieldset class="fieldset-antispam">
                        <legend class="legend-antispam">Aggiungi Mezzi liberi:</legend>
                        <#list mezzi_disponibili as mez>
                            <div class="mb-24">
                                <label>
                                    <input type="checkbox" name="id_mezzo" value="${mez.id_mezzo}"> 
                                    ${(mez.nome)!""}
                                </label>
                            </div>
                        <#else>
                            <p class="text-muted">Nessun mezzo disponibile.</p>
                        </#list>
                    </fieldset>
                    
                    <fieldset class="fieldset-antispam">
                        <legend class="legend-antispam">Aggiungi Materiali liberi:</legend>
                        <#list materiali_disponibili as mat>
                            <div class="mb-24">
                                <label>
                                    <input type="checkbox" name="id_materiale" value="${mat.id_materiale}"> 
                                    ${(mat.nome)!""}
                                </label>
                            </div>
                        <#else>
                            <p class="text-muted">Nessun materiale disponibile.</p>
                        </#list>
                    </fieldset>
                </div>

                <div class="mt-20">
                    <button type="submit" class="btn btn-primary">Salva modifiche</button>
                </div>
            </form>
        </section>
        </#if>

    </main>
</body>
</html>