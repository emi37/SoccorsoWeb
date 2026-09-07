<!doctype html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <title>Dettaglio Missione - SoccorsoWeb</title>
        <!-- I path a css/js restano relativi alla root pubblica del server -->
        <link rel="stylesheet" type="text/css" href="${request.contextPath}/css/style.css">
    </head>

    <body>
        <div class="container">
            <header class="topbar">
                <div>
                    <h1>SoccorsoWeb</h1>
                    <p class="subtitle">Gestione Missione e Operazioni</p>
                </div>

                <nav class="topbar-actions">
                    <!-- Navigazione Intelligente basata sul ruolo -->
                    <#if (request.getSession().getAttribute("ruolo")!"") == "ADMIN">
                        <a href="${request.contextPath}/DashboardServlet" class="btn">Dashboard Admin</a>
                    <#else>
                        <a href="${request.contextPath}/DashboardOperatoreServlet" class="btn">Area Operativa</a>
                    </#if>
                    <a href="${request.contextPath}/homeFinale.html" class="btn">Home</a>
                    <a href="${request.contextPath}/Logout" class="btn btn-danger">Logout</a>
                </nav>
            </header>

            <main>
                <div class="card">
                    <h2>Dettaglio missione</h2>
                    <p>In questa pagina è possibile consultare una missione e inserire un aggiornamento operativo.</p>
                </div>

                <!-- INIZIO INIEZIONE DATI FREEMARKER: Info Base -->
                <div class="card">
                    <h2>Informazioni missione</h2>
                    <div class="table-wrapper">
                        <table>
                            <tbody>
                                <tr>
                                    <th>ID missione</th>
                                    <td>${missione.id_missione!}</td>
                                </tr>
                                <tr>
                                    <th>Obiettivo</th>
                                    <td>${missione.obiettivo!}</td>
                                </tr>
                                <tr>
                                    <th>Posizione</th>
                                    <td>${missione.posizione!}</td>
                                </tr>
                                <tr>
                                    <th>Stato</th>
                                    <td><strong>${missione.stato!}</strong></td>
                                </tr>
                                <tr>
                                    <th>Inizio intervento</th>
                                    <td>${missione.inizio!}</td>
                                </tr>
                                <tr>
                                    <th>Caposquadra</th>
                                    <td>${missione.caposquadra!}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- INIEZIONE LISTE FREEMARKER: Mezzi e Materiali CORRETTA -->
                <div class="card">
                    <h2>Risorse assegnate</h2>

                    <h3>Automezzi sul posto</h3>
                    <ul>
                        <!-- Cerchiamo dentro 'missione' e verifichiamo che la lista non sia vuota -->
                        <#if missione.mezzi?? && missione.mezzi?has_content>
                            <!-- Stampiamo direttamente la stringa di testo passata dal Java -->
                            <#list missione.mezzi as mezzoStringa>
                                <li>${mezzoStringa}</li>
                            </#list>
                        <#else>
                            <li class="text-muted">Nessun automezzo associato a questa missione.</li>
                        </#if>
                    </ul>

                    <h3>Attrezzature e materiali</h3>
                    <ul>
                        <#if missione.materiali?? && missione.materiali?has_content>
                            <#list missione.materiali as materialeStringa>
                                <li>${materialeStringa}</li>
                            </#list>
                        <#else>
                            <li class="text-muted">Nessun materiale associato a questa missione.</li>
                        </#if>
                    </ul>
                </div>

                <!-- SE LA MISSIONE E' IN CORSO, MOSTRO IL FORM -->
              <!-- SE LA MISSIONE E' IN CORSO, MOSTRO IL FORM (ATTUALMENTE NASCOSTO) -->
                <#-- 
                <#if (missione.stato!"") == "IN_CORSO">
                <div class="card">
                    <h2>Aggiungi aggiornamento missione</h2>
                    <form id="formAggiornaMissione" action="${request.contextPath}/DettaglioMissioneServlet" method="post">
                        <div class="form-group">
                            <label class="form-label" for="id_missione">ID missione</label>
                            <input type="text" id="id_missione" name="id_missione" class="form-control" value="${missione.id_missione!}" readonly required>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="testo_descrittivo">Messaggio</label>
                            <textarea id="testo_descrittivo" name="testo_descrittivo" rows="6" class="form-control" required></textarea>
                        </div>

                        <div>
                            <input type="submit" value="Invia aggiornamento" class="btn">
                            <input type="reset" value="Cancella dati" class="btn btn-danger">
                        </div>
                    </form>
                </div>
                </#if>
                -->

                <!-- INIEZIONE TIMELINE FREEMARKER (ATTUALMENTE NASCOSTA) -->
                <#-- 
                <div class="card">
                    <h2>Cronistoria eventi</h2>
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Data e ora</th>
                                    <th>Operatore</th>
                                    <th>Messaggio</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#if timeline??>
                                    <#list timeline as evento>
                                    <tr>
                                        <td>${evento.data!}</td>
                                        <td>${evento.operatore!}</td>
                                        <td>${evento.testo!}</td>
                                    </tr>
                                    <#else>
                                    <tr>
                                        <td colspan="3" class="text-muted text-center" style="padding: 15px;">Nessun aggiornamento registrato.</td>
                                    </tr>
                                    </#list>
                                <#else>
                                    <tr>
                                        <td colspan="3" class="text-muted text-center" style="padding: 15px;">Nessun aggiornamento registrato.</td>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>
                -->

            </main>

            <footer class="message">
                <p>SoccorsoWeb</p>
            </footer>

        </div>
        
        <!-- Il tuo JS -->
        <script src="${request.contextPath}/js/missioni.js"></script>
    </body>
</html>