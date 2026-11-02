<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Area Operativa - SoccorsoWeb</title>
    <!-- Richiamiamo il tuo CSS universale -->
    <link rel="stylesheet" type="text/css" href="${request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        
        <!-- HEADER e TOPBAR -->
        <header class="topbar">
            <div>
                <h1 class="main-title">Centrale Operativa</h1>
                <p class="subtitle">Benvenuto, <b>${(utente.nome)!"Operatore"} ${(utente.cognome)!""}</b></p>
            </div>
            <div class="topbar-actions">
                <a href="${request.contextPath}/Logout" class="btn btn-danger">Logout</a>
            </div>
        </header>

        <!-- SEZIONE 1: STATO OPERATIVO -->
        <section class="card">
            <h2 class="section-title">Il tuo Stato attuale</h2>
            <p style="font-size: 16px;">
                Attualmente il tuo stato è: 
                <#if (utente.stato_attuale!"") == "LIBERO">
                    <span class="badge" style="background-color: #28a745; color: white;">LIBERO</span>
                    <br><br><span class="text-muted">Sei a disposizione della centrale. Mantieni il dispositivo acceso e attendi istruzioni.</span>
                <#else>
                    <span class="badge badge-danger">IMPEGNATO</span>
                    <br><br><span class="text-muted">Sei assegnato a un intervento in corso.</span>
                </#if>
            </p>
        </section>

        <!-- SEZIONE 2: MISSIONE ASSEGNATA -->
        <section class="card">
            <h2 class="section-title">La tua Missione in corso</h2>
            <div class="table-wrapper">
                <table>
                    <tr>
                        <th>ID Missione</th>
                        <th>Obiettivo / Emergenza</th>
                        <th>Posizione Segnalata</th>
                        <th>Azioni</th>
                    </tr>
                    
                    <#assign missioniTrovate = false>
                    <#if missioniInCorso??>
                        <#list missioniInCorso as m>
                            <#assign missioniTrovate = true>
                            <tr>
                                <td>#${m.id_missione!}</td>
                                <td>${m.obiettivo!}</td>
                                <td>${m.posizione!}</td>
                                <td>
                                    <!-- Il bottone porta l'operatore a leggere i dettagli dell'emergenza -->
                                    <a href="${request.contextPath}/DettaglioMissioneServlet?id_missione=${m.id_missione!}" class="btn btn-primary">Vedi Dettagli</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                    
                    <#if !missioniTrovate>
                        <tr>
                            <td colspan="4" class="text-center text-muted" style="padding: 20px;">
                                Nessuna missione attiva al momento.
                            </td>
                        </tr>
                    </#if>
                </table>
            </div>
        </section>
                

        <!-- SEZIONE 3: STORICO MISSIONI CHIUSE -->
        <section class="card">
            <h2 class="section-title">Storico delle mie missioni</h2>
            <p class="subtitle mb-15">Elenco delle missioni concluse a cui ho preso parte.</p>
            <div class="table-wrapper">
                <table>
                    <tr>
                        <th>ID Missione</th>
                        <th>Obiettivo</th>
                        <th>Posizione</th>
                        <th>Valutazione</th>
                        <th>Azioni</th>
                    </tr>
                    
                    <#assign storicoTrovato = false>
                    <#if storicoMissioni??>
                        <#list storicoMissioni as s>
                            <!-- Filtriamo per mostrare solo le missioni CHIUSE -->
                            <#if (s.stato!"") == "CHIUSA">
                                <#assign storicoTrovato = true>
                                <tr>
                                    <td>#${s.id_missione!}</td>
                                    <td>${s.obiettivo!}</td>
                                    <td>${s.posizione!}</td>
                                    <td>
                                        <span class="badge badge-secondary">${s.visualizzaVoto!}</span>
                                    </td>
                                    <td>
                                        <a href="${request.contextPath}/DettaglioMissioneServlet?id_missione=${s.id_missione!}" class="link-back">Rivedi Rapporto</a>
                                    </td>
                                </tr>
                            </#if>
                        </#list>
                    </#if>
                    
                    <#if !storicoTrovato>
                        <tr>
                            <td colspan="5" class="text-center text-muted pd-20">
                                Non hai svolto ancora nessuna missione.
                            </td>
                        </tr>
                    </#if>
                </table>
            </div>
        </section>

    </div>
</body>
</html>