<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Dashboard Operatore - SoccorsoWeb</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
</head>
<body>
    <header class="topbar">
        <div>
            <h1>Area Operativa</h1>
            <p class="subtitle">Benvenuto nella tua dashboard, Operatore!</p>
        </div>

        <nav class="topbar-actions">
            <!-- Pulsante di disconnessione in rosso -->
            <a href="/SoccorsoWeb/Logout"class="btn btn-danger" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Logout</a>
        </nav>
    </header>

    <main class="container mt-20">
        <!-- SEZIONE LE MIE MISSIONI -->
        <section class="card">
            <div class="section-header">
                <div>
                    <h2>Le tue missioni attive</h2>
                    <p>Elenco degli interventi in corso a cui sei stato assegnato dalla centrale.</p>
                </div>
            </div>

            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>ID Missione</th>
                            <th>ID Richiesta</th>
                            <th>Obiettivo</th>
                            <th>Posizione</th>
                            <th>Stato</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Il controllo FreeMarker per vedere se la Servlet ha passato la lista -->
                        <#if mie_missioni?? && mie_missioni?size &gt; 0>
                            <#list mie_missioni as m>
                            <tr>
                                <td><strong>#${m.id_missione!}</strong></td>
                                <td>#${m.id_richiesta!}</td>
                                <td>${m.obiettivo!""}</td>
                                <td>${m.posizione!""}</td>
                                <td><span style="color: #007bff; font-weight: bold;">${m.stato!}</span></td>
                                <td>
                                    <!-- Il tasto magico che apre la pagina "Dettaglio Missione" che avevi già fatto! -->
                                    <a href="DettaglioMissioneServlet?id_missione=${m.id_missione!}" class="btn btn-primary">Apri / Aggiorna</a>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                            <tr>
                                <td colspan="6" class="text-center text-muted" style="padding: 30px;">
                                    Attualmente non sei assegnato a nessuna missione in corso. Resta in attesa di comunicazioni dalla centrale.
                                </td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

    <!-- Footer di sistema -->
    <footer class="message text-center mt-20">
        <p>SoccorsoWeb - Sistema di Gestione Emergenze</p>
    </footer>
</body>
</html>
