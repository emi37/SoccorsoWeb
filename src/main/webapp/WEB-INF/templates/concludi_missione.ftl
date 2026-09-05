
<!doctype html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <!-- Risolto il percorso del CSS usando il context path -->
        <link rel="stylesheet" type="text/css" href="${request.contextPath}/css/style.css">
        <title>Chiusura Intervento - SoccorsoWeb</title>
    </head>
    
    <body class="flex-center-body">
        <main class="card-container">
            <!-- INIZIO LOGICA FREEMARKER: Controllo se l'oggetto missione esiste -->
            <#if missione??>
                
                <header>
                    <h1 class="main-title">Chiusura report missione #${missione.id_missione}</h1>
                </header>

                <section>
                    <p><strong>ID Richiesta collegata:</strong> ${missione.id_richiesta}</p>
                    <p><strong>Dettaglio operazione:</strong> ${missione.obiettivo}</p>
                </section>

                <hr>

                <section>
                    <!-- Form con action aggiornata col context path -->
                    <form id="formChiusuraMissione" action="${request.contextPath}/ConcludiMissioneServlet" method="POST">
                        <input type="hidden" id="id_missione" name="id_missione" value="${missione.id_missione}">
                        <input type="hidden" id="id_richiesta" name="id_richiesta" value="${missione.id_richiesta}">

                        <fieldset>
                            <legend>Dettagli Chiusura Intervento</legend>

                            <p class="form-group">
                                <label class="form-label" for="voto">Valutazione esito intervento (0-5):</label>
                                <input class="form-control" type="number" id="voto" name="voto_successo" min="0" max="5" value="5" required>
                            </p>

                            <p class="form-group">
                                <label class="form-label" for="commenti">Relazione finale della squadra:</label>
                                <textarea class="form-control" id="commenti" name="commenti" rows="5" required placeholder="Scrivi qui come si è concluso il soccorso sul posto..."></textarea>
                            </p>
                        </fieldset>

                        <br>
                        <button class="btn" type="submit">REGISTRA CHIUSURA E LIBERA RISORSE</button>
                    </form>
                </section>
                
            <#else>
                
                <!-- Questo blocco viene mostrato SOLO SE la missione non viene trovata -->
                <section>
                    <h2 class="text-danger">⚠️ Attenzione</h2>
                    <p><strong>Missione non trovata o già conclusa.</strong></p>
                </section>
                
            </#if>
            <!-- FINE LOGICA FREEMARKER -->

            <hr>

            <footer>
                <a href="${request.contextPath}/DashboardServlet" class="btn-primary">&larr; Annulla e torna alla Dashboard</a>
            </footer>
        </main>
        
        <!-- Script agganciato con percorso assoluto corretto -->
        <script src="${request.contextPath}/js/chiusura_intervento.js"></script>
    </body>
</html>