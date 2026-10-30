<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Dettaglio Missione - SoccorsoWeb</title>
    <!-- Collegamento al foglio di stile esterno -->
    <link rel="stylesheet" type="text/css" href="${(request.contextPath)!""}/css/style.css">
</head>
<body class="flex-center-body">

    <main class="page-content">
        <!-- INTESTAZIONE -->
        <header class="topbar">
            <div>
                <h1 class="main-title">Gestione Missione #${(missione.id_missione)!""}</h1>
                <p class="subtitle">Dettagli operativi e assegnazione rinforzi</p>
            </div>
            <nav class="topbar-actions">
                <a href="${(request.contextPath)!""}/DashboardServlet" class="btn">&larr; Torna alla Dashboard</a>
            </nav>
        </header>

        <!-- SEZIONE 1: DETTAGLI MISSIONE -->
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

        <!-- SEZIONE 2: RISORSE ASSEGNATE -->
        <section class="card">
            <h2 class="section-title">Risorse assegnate</h2>
            
            <h3>Automezzi sul posto</h3>
            <#if missione.mezzi?? && (missione.mezzi?size > 0)>
                <ul>
                    <#list missione.mezzi as mezzo>
                        <li>${mezzo}</li>
                    </#list>
                </ul>
            <#else>
                <p class="text-muted">Nessun mezzo registrato.</p>
            </#if>

            <h3>Materiali impiegati</h3>
            <#if missione.materiali?? && (missione.materiali?size > 0)>
                <ul>
                    <#list missione.materiali as mat>
                        <li>${mat}</li>
                    </#list>
                </ul>
            <#else>
                <p class="text-muted">Nessun materiale registrato.</p>
            </#if>
        </section>

        <!-- SEZIONE 3: MODIFICA MISSIONE E RINFORZI (Visibile solo se IN_CORSO) -->
        <#if (missione.stato!"") == "IN_CORSO">
        <section class="card">
            <h2 class="section-title">Modifica Missione e Invia Rinforzi</h2>
            <p class="text-muted">Aggiorna le coordinate o aggiungi risorse di supporto alla squadra già sul posto.</p>
            
            <form action="${(request.contextPath)!""}/DettaglioMissioneServlet" method="POST">
                <input type="hidden" name="id_missione" value="${(missione.id_missione)!""}">
                <input type="hidden" name="azione" value="modifica_missione">

                <div class="form-row-flexible mt-20">
                    <div>
                        <label for="obiettivo" class="form-label">Aggiorna Obiettivo:</label>
                        <input type="text" id="obiettivo" name="obiettivo" value="${(missione.obiettivo)!''}" class="form-control" required>
                    </div>
                    <div>
                        <label for="posizione" class="form-label">Aggiorna Posizione:</label>
                        <input type="text" id="posizione" name="posizione" value="${(missione.posizione)!''}" class="form-control" required>
                    </div>
                </div>

                <div class="form-row-flexible mt-20">
                    <!-- Rinforzi: Operatori -->
                    <fieldset class="fieldset-antispam">
                        <legend class="legend-antispam">Aggiungi Operatori Liberi:</legend>
                        <#if operatori_disponibili?? && (operatori_disponibili?size > 0)>
                            <#list operatori_disponibili as op>
                                <div class="mb-24">
                                    <label>
                                        <input type="checkbox" name="id_operatore" value="${(op.id_utente)!(op.id)!""}"> 
                                        ${(op.nome_completo)!(op.nome)!""} ${(op.cognome)!""}
                                    </label>
                                </div>
                            </#list>
                        <#else>
                            <p class="text-muted">Nessun operatore disponibile.</p>
                        </#if>
                    </fieldset>

                    <!-- Rinforzi: Mezzi -->
                    <fieldset class="fieldset-antispam">
                        <legend class="legend-antispam">Aggiungi Mezzi Liberi:</legend>
                        <#if mezzi_disponibili?? && (mezzi_disponibili?size > 0)>
                            <#list mezzi_disponibili as mez>
                                <div class="mb-24">
                                    <label>
                                        <input type="checkbox" name="id_mezzo" value="${(mez.id_mezzo)!(mez.id)!""}"> 
                                        ${(mez.nome)!""}
                                    </label>
                                </div>
                            </#list>
                        <#else>
                            <p class="text-muted">Nessun mezzo disponibile.</p>
                        </#if>
                    </fieldset>
                    
                    <!-- Rinforzi: Materiali -->
                    <fieldset class="fieldset-antispam">
                        <legend class="legend-antispam">Aggiungi Materiali Liberi:</legend>
                        <#if materiali_disponibili?? && (materiali_disponibili?size > 0)>
                            <#list materiali_disponibili as mat>
                                <div class="mb-24">
                                    <label>
                                        <input type="checkbox" name="id_materiale" value="${(mat.id_materiale)!(mat.id)!""}"> 
                                        ${(mat.nome)!""}
                                    </label>
                                </div>
                            </#list>
                        <#else>
                            <p class="text-muted">Nessun materiale disponibile.</p>
                        </#if>
                    </fieldset>
                </div>

                <div class="mt-20">
                    <button type="submit" class="btn btn-primary">Salva Modifiche Missione</button>
                </div>
            </form>
        </section>
        </#if>

    </main>
</body>
</html>