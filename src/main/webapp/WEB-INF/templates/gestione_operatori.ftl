<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Gestione Operatori e Amministratori - SoccorsoWeb</title>
    <link rel="stylesheet" type="text/css" href="${request.contextPath}/css/style.css">

</head>
<body>

    <main class="page-content">
        <header class="topbar">
            <h1 class="main-title">Gestione Operatori e Amministratori</h1>
            <nav class="topbar-actions">
                <a href="DashboardServlet" class="btn btn-primary">Torna a Dashboard</a>
            </nav>
        </header>
        
        <section class="card">
            <h2>Aggiungi un nuovo Utente</h2>
            <form action="CreaAdminEOperatoreServlet" method="POST">
                
                <div class="form-row-flexible">
                    <div>
                        <label for="campoNome" class="form-label">Nome:</label>
                        <input type="text" id="campoNome" name="nome" class="form-control" required>
                    </div>
                    <div>
                        <label for="campoCognome" class="form-label">Cognome:</label>
                        <input type="text" id="campoCognome" name="cognome" class="form-control" required>
                    </div>
                    <div>
                        <label for="campoEmail" class="form-label">Email:</label>
                        <input type="email" id="campoEmail" name="email" class="form-control" required>
                    </div>
                    <div>
                        <label for="campoPassword" class="form-label">Password:</label>
                        <input type="password" id="campoPassword" name="password" class="form-control" minlength="8" placeholder="Lascia vuoto per generarla automaticamente">
                    </div>
                    <div>
                        <label for="sceltaRuolo" class="form-label">Ruolo:</label>
                        <select name="ruolo" id="sceltaRuolo" class="form-control" required onchange="gestisciVisibilitaCompetenze()">
                            <option value="OPERATORE">Operatore</option>
                            <option value="ADMIN">Amministratore</option>
                        </select>
                    </div>
                </div>

                <div id="sezioneCompetenze" class="skills-collapsible">
                    <h3>Seleziona Abilità (Solo se Operatore):</h3>
                    <div class="multi-column-checkboxes">
                        <#list abilita![] as a>
                            <label>
                                <input type="checkbox" name="abilita" value="${a.id_abilita!}"> 
                                ${a.nome!}
                            </label><br>
                        </#list>
                    </div>

                    <h3>Seleziona patenti (Solo se Operatore):</h3>
                    <div class="multi-column-checkboxes">
                        <#list patenti![] as p>
                            <label>
                                <input type="checkbox" name="patenti" value="${p.id_patente!}"> 
                                ${p.codice!}
                            </label><br>
                        </#list>
                    </div>
                </div>

                <div class="mt-20">
                    <button type="submit" class="btn btn-primary">Crea utente</button>
                </div>
            </form>
        </section>

        <details class="tendina-container">
            <summary class="tendina-titolo">Lista Operatori (Clicca per espandere/comprimere)</summary>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Cognome</th>
                            <th>Email</th>
                            <th>Stato Operativo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#assign operatoriTrovati = false>
                        <#list operatori![] as o>
                            <#if (o.ruolo!"") == "OPERATORE">
                                <#assign operatoriTrovati = true>
                                <tr>
                                    <td>#${o.id_utente!}</td>
                                    <td>${o.nome!}</td>
                                    <td>${o.cognome!}</td>
                                    <td>${o.email!}</td>
                                    <td>
                                        <#if (o.stato_attuale!"") == "LIBERO">
                                            <span class="status-badge-free">LIBERO</span>
                                        <#else>
                                            <span class="status-badge-busy">IMPEGNATO</span>
                                        </#if>
                                        <a href="StoricoRisorsa?tipo=operatore&id=${o.id_utente!}" class="btn">Storico</a>
                                    </td>
                                </tr>
                            </#if>
                        </#list>
                        <#if !operatoriTrovati>
                            <tr>
                                <td colspan="5" class="text-center text-muted">Nessun operatore registrato in archivio.</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </details>

        <details class="tendina-container">
            <summary class="tendina-titolo">Lista Amministratori (Clicca per espandere/comprimere)</summary>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Cognome</th>
                            <th>Email</th>
                            <th>Ruolo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#assign adminTrovati = false>
                        <#list operatori![] as o>
                            <#if (o.ruolo!"") == "ADMIN">
                                <#assign adminTrovati = true>
                                <tr>
                                    <td>#${o.id_utente!}</td>
                                    <td>${o.nome!}</td>
                                    <td>${o.cognome!}</td>
                                    <td>${o.email!}</td>
                                    <td><span class="role-badge-admin">ADMIN</span></td>
                                </tr>
                            </#if>
                        </#list>
                        <#if !adminTrovati>
                            <tr>
                                <td colspan="5" class="text-center text-muted">Nessun amministratore presente.</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </details>
        
    </main>

    <script>
        function gestisciVisibilitaCompetenze() {
            const tendinaRuolo = document.getElementById("sceltaRuolo");
            const blocco = document.getElementById("sezioneCompetenze");
            
            if (tendinaRuolo.value !== "OPERATORE") {
                blocco.style.display = "none";
            } else {
                blocco.style.display = "block";
            }
        }

        document.addEventListener("DOMContentLoaded", gestisciVisibilitaCompetenze);
    </script>
</body>
</html>