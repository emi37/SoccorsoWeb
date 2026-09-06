<!doctype html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Assegnazione risorse - SoccorsoWeb</title>
    <!-- Aggiornato il percorso con il contextPath -->
    <link rel="stylesheet" href="${request.contextPath}/css/style.css" type="text/css"/>
</head>
<body class="flex-center-body">
    <main class="card-container-form">
        <header class="section-header text-left">
            <h2 class="section-title">Avvio missione #${(richiesta.id_richiesta)!(richiesta.id)!}</h2>
        </header>

        <section class="text-left mb-24">
            <p><b>Segnalante:</b> ${(richiesta.nome)!(richiesta.nome_segnalante)!""}</p>
            <p><b>Posizione dell'emergenza:</b> ${(richiesta.posizione)!""}</p>
            <p><b>Dettagli segnalazione:</b> ${(richiesta.descrizione)!""}</p>
        </section>

        <hr class="mb-24">

        <section class="text-left">
            <h3 class="section-title">Configurazione squadra d'intervento</h3>
            
<!-- Sostituisci la vecchia riga action con questa: -->
            <form action="GestioneRichiestaServletDallAdmin" method="POST">
                <input type="hidden" name="id_richiesta" value="${(richiesta.id_richiesta)!(richiesta.id)!}">
                
                <!-- 1. Operatori e Caposquadra -->
                <fieldset class="fieldset-antispam">
                    <legend class="legend-antispam">Seleziona gli operatori (e nomina un Caposquadra col pallino):</legend>
                    <div class="scrollable-list">
                        <#list operatori as operatore>
                            <div class="form-group-inline">
                                <!-- Checkbox per aggiungere l'operatore alla missione -->
                                <input type="checkbox" id="op_${operatore.id_utente!}" name="id_operatore" value="${operatore.id_utente!}">
                                
                                <!-- Variabile corretta in base al DAO: nome_completo -->
                                <label for="op_${operatore.id_utente!}">${(operatore.nome_completo)!(operatore.nome)!} ${(operatore.cognome)!""}</label>
                                
                                <!-- Radio per scegliere chi comanda la squadra -->
                                <input type="radio" name="id_caposquadra" value="${operatore.id_utente!}" title="Nomina come Caposquadra" required>
                                 <!--  <span>👑</span>   -->
                            </div>
                        <#else>
                            <p class="text-muted">Nessun operatore attualmente disponibile o libero.</p>
                        </#list>
                    </div>
                </fieldset>

                <!-- 2. Mezzi -->
                <fieldset class="fieldset-antispam">
                    <legend class="legend-antispam">Seleziona Mezzi di Soccorso Disponibili:</legend>
                    <div class="scrollable-list">
                        <#list mezzi as mezzo>
                            <div class="form-group-inline">
                                <input type="checkbox" id="mezzo_${(mezzo.id_mezzo)!(mezzo.id)!}" name="id_mezzo" value="${(mezzo.id_mezzo)!(mezzo.id)!}">
                                <label for="mezzo_${(mezzo.id_mezzo)!(mezzo.id)!}"><strong>${mezzo.nome!""}</strong> - ${mezzo.descrizione!""}</label>
                            </div>
                        <#else>
                            <p class="text-muted">Nessun automezzo disponibile in rimessa.</p>
                        </#list>
                    </div>
                </fieldset>

                <!-- 3. Materiali -->
                <fieldset class="fieldset-antispam">
                    <legend class="legend-antispam">Seleziona Attrezzature e Materiali di bordo:</legend>
                    <div class="scrollable-list">
                        <#list materiali as materiale>
                            <div class="form-group-inline">
                                <input type="checkbox" id="mat_${(materiale.id_materiale)!(materiale.id)!}" name="id_materiale" value="${(materiale.id_materiale)!(materiale.id)!}">
                                <label for="mat_${(materiale.id_materiale)!(materiale.id)!}"><strong>${materiale.nome!""}</strong> - ${materiale.descrizione!""}</label>
                            </div>
                        <#else>
                            <p class="text-muted">Nessun materiale disponibile in magazzino.</p>
                        </#list>
                    </div>
                </fieldset>

                <div class="text-center mt-20">
                    <button type="submit" class="btn-success-large">AVVIA INTERVENTO SUL CAMPO</button>
                </div>
            </form>
        </section>

        <hr class="mt-20 mb-24">

        <footer class="text-center">
            <a href="${request.contextPath}/DashboardServlet" class="link-back">&larr; Torna alla dashboard</a>
        </footer>
    </main>
</body>
</html>