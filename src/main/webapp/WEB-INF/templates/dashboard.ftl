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
<!-- SEZIONE MISSIONI IN CORSO -->
<section class="card">
    <h2 class="section-title">Missioni in corso</h2>
    
    
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th class="col-id">ID missione</th>
                    <th class="col-id">ID richiesta</th>
                    <th>Caposquadra</th>
                    <th class="col-dettagli">Obiettivo</th>
                    <th>Posizione</th>
                    <th>Inizio / Stato</th>
                    <th>Azioni</th>
                </tr>
            </thead>
            <tbody>
                <#if missioni?? && missioni?size &gt; 0>
                    <#list missioni as m>
                        <#assign idMiss = (m.id_missione)!(m.idMissione)!"" />
                        <#assign idRicM = (m.id_richiesta)!(m.idRichiesta)!"" />
                        
                        <tr>
                            <td>#${idMiss}</td>
                            <td>#${idRicM}</td>
                            
                            <!-- IL DATO DEL CAPOSQUADRA -->
                            <td>${m.caposquadra!"Non assegnato"}</td>
                            
                            <td>${m.obiettivo!""}</td>
                            <td>${m.posizione!""}</td>
                            <td>
                                ${(m.stato)!"IN CORSO"}
                                <#if m.data_inizio??>
                                    <br><small class="text-muted">${m.data_inizio}</small>
                                </#if>
                            </td>
                            <td>
                                <a href="DettaglioMissioneServlet?id_missione=${idMiss}" class="btn btn-primary d-block mb-5">Gestisci</a>
                                <a href="ConcludiMissioneServlet?id_missione=${idMiss}" class="btn btn-primary d-block">Concludi</a>
                            </td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="7" class="text-center text-muted pd-20">
                            Nessuna missione attualmente in corso.
                        </td>
                    </tr>
                </#if>
            </tbody>
        </table>
    </div>
</section>
        </main>
    </body>
</html>