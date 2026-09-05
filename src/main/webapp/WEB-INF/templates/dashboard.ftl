   

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
                <p class="subtitle">Gestione richieste, missioni e risorse di SoccorsoWeb</p>
            </div>

            <nav class="topbar-actions">
                <a href="gestione-mezzi.html" class="btn">Gestione mezzi</a>
                <a href="gestione-materiali.html" class="btn">Gestione materiali</a>
                <a href="storico-missioni.html" class="btn">Storico missioni</a>
                <a href="LogoutServlet" class="btn btn-danger">Logout</a>
            </nav>
        </header>

        <main class="container">
            <!-- SEZIONE RICHIESTE ATTIVE -->
            <section class="card">
                <div class="section-header">
                    <div>
                        <h2>Richieste attive</h2>
                        <p>Richieste confermate e pronte per essere trasformate in missioni.</p>
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
                                <tr>
                                    <td>#${r.id_richiesta!}</td>
                                    <td>${r.nome_segnalante!""}</td>
                                    <td>${r.email_segnalante!""}</td>
                                    <td>${r.posizione!""}</td>
                                    <td>${r.descrizione!""}</td>
                                    <td>
                                        <a href="GestioneRichiestaServletDallAdmin?id_richiesta=${r.id_richiesta!}" class="btn btn-primary">Avvia Missione</a>
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
                        <p>Missioni già create e non ancora concluse.</p>
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
                                <th>Stato</th>
                                <th>Azioni</th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list missioni as m>
                                <tr>
                                    <td>#${m.id_missione!}</td>
                                    <td>#${m.id_richiesta!}</td>
                                    <td>${m.obiettivo!""}</td>
                                    <td>${m.posizione!""}</td>
                                    <td><span class="badge-status in-corso">${m.stato!""}</span></td>
                                    <td>
                                        <a href="ConcludiMissioneServlet?id_missione=${m.id_missione!}" class="btn btn-success">Concludi</a>
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

            <section class="card">
                <h2>Azioni rapide</h2>
                <div class="quick-actions">
                    <a href="gestione-mezzi.html" class="action-card">
                        <strong>Mezzi</strong>
                        <span>Aggiungi, consulta o rimuovi mezzi disponibili.</span>
                    </a>
                    <a href="gestione-materiali.html" class="action-card">
                        <strong>Materiali</strong>
                        <span>Gestisci materiali e risorse operative.</span>
                    </a>
                    <a href="storico-missioni.html" class="action-card">
                        <strong>Storico</strong>
                        <span>Consulta le missioni concluse.</span>
                    </a>
                </div>
            </section>
        </main>
    </body>
</html>


