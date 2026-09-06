<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Chiusura Intervento - SoccorsoWeb</title>
    <link rel="stylesheet" type="text/css" href="${request.contextPath}/css/style.css">
</head>
<body class="flex-center-body">
    <main class="card-container-form">
        <header class="section-header text-left">
            <h2 class="section-title">Chiusura report missione #${(missione.id_missione)!""}</h2>
        </header>

        <section class="text-left mb-24">
            <p><b>ID Richiesta collegata:</b> #${(missione.id_richiesta)!""}</p>
            <p><b>Dettaglio operazione:</b> ${(missione.obiettivo)!"Nessun dettaglio specificato"}</p>
            <p><b>Posizione:</b> ${(missione.posizione)!"Posizione non specificata"}</p>
        </section>

        <hr class="mb-24">

        <section class="text-left">
            <h3 class="section-title">Compila Relazione Finale</h3>
            
            <form action="${request.contextPath}/ConcludiMissioneServlet" method="POST">
                <!-- ID Nascosti che servono alla Servlet per chiudere la missione nel DB -->
                <input type="hidden" name="id_missione" value="${(missione.id_missione)!""}">
                <input type="hidden" name="id_richiesta" value="${(missione.id_richiesta)!""}">
                
                <div class="form-group">
                    <label for="voto_successo">Valutazione Intervento (0-5):</label>
                    <input type="number" id="voto_successo" name="voto_successo" min="0" max="5" required class="input-field">
                </div>

                <div class="form-group">
                    <label for="commenti">Relazione e commenti operativi:</label>
                    <textarea id="commenti" name="commenti" rows="5" required placeholder="Descrivi com'è andato l'intervento..." class="input-field"></textarea>
                </div>

                <div class="text-center mt-20">
                    <button type="submit" class="btn btn-success">ARCHIVIA MISSIONE E LIBERA SQUADRA</button>
                </div>
            </form>
        </section>

        <hr class="mt-20 mb-24">

        <footer class="text-center">
            <a href="${request.contextPath}/DashboardServlet" class="btn">&larr; Annulla e torna alla dashboard</a>
        </footer>
    </main>
</body>
</html>