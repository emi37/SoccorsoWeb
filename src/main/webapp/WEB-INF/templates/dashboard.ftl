<!DOCTYPE html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <title>Dashboard Amministratore - SoccorsoWeb</title>
        <link rel="stylesheet" type="text/css" href="css/style.css">
    </head>
    <body>
        <header class="topbar">
            <div>
                <h1>Dashboard Amministratore</h1>
                <p class="subtitle">
                    Gestione richieste, missioni e risorse di SoccorsoWeb
                    <#if nome_utente??> — Benvenuto, <strong>${nome_utente} ${cognome_utente!""}</strong></#if>
                </p>
            </div>

            <nav class="topbar-actions">
                <a href="GestioneOperatori" class="btn">Gestione operatori e admin</a>
                <a href="GestioneMezzi" class="btn">Gestione mezzi</a>
                <a href="GestioneMateriali" class="btn">Gestione materiali</a>
                <a href="StoricoMissioni" class="btn">Storico missioni</a>
                <a href="Logout" class="btn btn-danger">Logout</a>
            </nav>
        </header>

        <main class="container">
            <!-- SEZIONE RICHIESTE ATTIVE -->
            <section class="card">
                <div class="section-header">
                    <div>
                        <h2>Richieste attive</h2>
                        <p>Richieste confermate via email e pronte per essere trasformate in missioni o archiviate.</p>
                    </div>
                </div>

                <div class="table-wrapper">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Segnalante</th>
                                <th>Email</th>
                                <th>Posizione</th>
                                <th>Descrizione</th>
                                <th>Azioni</th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list richieste as r>
                            <#assign idRic = (r.id)!(r.idRichiesta)!(r.id_richiesta)!"" />
                            <tr>
                                <td>#${idRic}</td>
                                <td>${(r.segnalante)!(r.nomeSegnalante)!(r.nome_segnalante)!""}</td>
                                <td>${(r.email)!(r.emailSegnalante)!(r.email_segnalante)!""}</td>
                                <td>${(r.posizione)!""}</td>
                                <td>${(r.descrizione)!""}</td>
                                <td>
                                    <a href="GestioneRichiestaServletDallAdmin?id_richiesta=${idRic}" class="btn btn-primary">Avvia Missione</a>
                                    <a href="IgnoraRichiestaServlet?id_richiesta=${idRic}" 
                                       class="btn btn-danger" 
                                       onclick="return confirm('Sei sicuro di voler ignorare questa richiesta di soccorso?');">Ignora</a>
                                </td>
                            </tr>
                            <#else>
                            <tr>
                                <td colspan="6" class="text-center text-muted">Nessuna richiesta di soccorso in attesa. Ottimo lavoro!</td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </section>

            <!-- SEZIONE MISSIONI IN CORSO -->
            <section class="card">
                <div class="section-header">
                    <div>
                        <h2>Missioni in corso</h2>
                        <p>Missioni attive sul territorio non ancora concluse.</p>
                    </div>
                </div>

                <div class="table-wrapper">
                    <table>
                        <thead>
                            <tr>
                                <th>ID missione</th>
                                <th>ID richiesta</th>
                                <th>Obiettivo</th>
                                <th>Posizione</th>
                                <th>Inizio / Stato</th>
                                <th>Azioni</th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list missioni as m>
                            <#assign idMiss = (m.id_missione)!(m.idMissione)!"" />
                            <#assign idRicM = (m.id_richiesta)!(m.idRichiesta)!"" />
                            <tr>
                                <td>#${idMiss}</td>
                                <td>#${idRicM}</td>
                                <td>${m.obiettivo!""}</td>
                                <td>${m.posizione!""}</td>
                                <td>
                                    <span class="badge-status in-corso">${(m.stato)!"IN CORSO"}</span>
                                    <#if m.data_inizio??><br><small class="text-muted">${m.data_inizio}</small></#if>
                                </td>
                                <td>
                                    <a href="DettaglioMissioneServlet?id_missione=${idMiss}" class="btn">Gestisci</a> 
                                    <br> <br>
                                    <a href="ConcludiMissioneServlet?id_missione=${idMiss}&amp;id_richiesta=${idRicM}" class="btn btn-primary">Concludi</a>
                                </td>
                            </tr>
                            <#else>
                            <tr>
                                <td colspan="6" class="text-center text-muted">Nessuna missione attualmente in corso sul territorio.</td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </section>

            <!-- SEZIONE AZIONI RAPIDE 
            <section class="card">
                <h2>Azioni rapide</h2>
                <div class="quick-actions">
                    <a href="GestioneOperatori" class="action-card">
                        <strong>Operatori e Admin</strong>
                        <span>Registra nuovi operatori, gestisci patenti e abilità.</span>
                    </a>
                    <a href="GestioneMezzi" class="action-card">
                        <strong>Mezzi</strong>
                        <span>Aggiungi, consulta o rimuovi mezzi disponibili.</span>
                    </a>
                    <a href="GestioneMateriali" class="action-card">
                        <strong>Materiali</strong>
                        <span>Gestisci materiali e risorse operative.</span>
                    </a>
                    <a href="StoricoMissioni" class="action-card">
                        <strong>Storico</strong>
                        <span>Consulta le missioni concluse.</span>
                    </a>
                </div>
            </section> -->
        </main>
    </body>
</html>