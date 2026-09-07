<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Gestione Operatori</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div style="padding: 20px;">
        <h1>Gestione Operatori e Amministratori</h1>

        <!-- FORM CREAZIONE -->
        <div class="form-container" style="margin-bottom: 30px; padding: 20px; border: 1px solid #ccc; border-radius: 8px; background-color: #fff;">
            <h2>Aggiungi un nuovo Utente</h2>
            <form action="CreaAdminEOperatoreServlet" method="POST">
                
                <div style="display: flex; gap: 15px; flex-wrap: wrap; margin-bottom: 15px;">
                    <div>
                        <label>Nome:</label><br>
                        <input type="text" name="nome" required>
                    </div>
                    <div>
                        <label>Cognome:</label><br>
                        <input type="text" name="cognome" required>
                    </div>
                    <div>
                        <label>Email:</label><br>
                        <input type="email" name="email" required>
                    </div>
                    <div>
                        <label>Password:</label><br>
                        <input type="password" name="password" required>
                    </div>
                    <div>
                        <label>Ruolo:</label><br>
                        <select name="ruolo" id="sceltaRuolo" required>
                            <option value="OPERATORE">Operatore</option>
                            <option value="ADMIN">Amministratore</option>
                        </select>
                    </div>
                </div>

                <!-- SEZIONE A SCOMPARSA (Abilità e Patenti) -->
                <div id="sezioneCompetenze" style="border-top: 1px solid #eee; padding-top: 15px; margin-top: 15px;">
                    <h3>Seleziona Abilità (Solo se Operatore):</h3>
                    <#if abilita??>
                        <div style="column-count: 2;">
                        <#list abilita as a>
                            <label><input type="checkbox" name="abilita" value="${a.id_abilita!}"> ${a.nome!}</label><br>
                        </#list>
                        </div>
                    </#if>

                    <br>
                    <h3>Seleziona Patenti (Solo se Operatore):</h3>
                    <#if patenti??>
                        <div style="column-count: 2;">
                        <#list patenti as p>
                            <label><input type="checkbox" name="patenti" value="${p.id_patente!}"> ${p.codice!}</label><br>
                        </#list>
                        </div>
                    </#if>
                </div>

                <br>
                <button type="submit" class="btn btn-primary" style="padding: 10px 20px; font-size: 16px;">Crea Utente</button>
            </form>
        </div>

        <!-- Tendina 1: OPERATORI (Chiusa di default) -->
        <details class="tendina-container">
            <summary class="tendina-titolo">🚑 Lista Operatori (Clicca per espandere/comprimere)</summary>
            <div class="table-wrapper">
                <table>
                    <tr>
                        <th>ID</th>
                        <th>Nome</th>
                        <th>Cognome</th>
                        <th>Email</th>
                        <th>Stato Operativo</th>
                    </tr>
                    <#assign operatoriTrovati = false>
                    <#if operatori??>
                        <#list operatori as o>
                            <!-- Filtro esatto per chi ha Ruolo = OPERATORE -->
                            <#if (o.ruolo!"") == "OPERATORE">
                                <#assign operatoriTrovati = true>
                                <tr>
                                    <td>${o.id_utente!}</td>
                                    <td>${o.nome!}</td>
                                    <td>${o.cognome!}</td>
                                    <td>${o.email!}</td>
                                    <td>
                                        <#if (o.stato_attuale!"") == "LIBERO">
                                            <strong style="color: green;">LIBERO</strong>
                                        <#else>
                                            <strong style="color: red;">IMPEGNATO</strong>
                                        </#if>
                                    </td>
                                </tr>
                            </#if>
                        </#list>
                    </#if>
                    <#if !operatoriTrovati>
                        <tr><td colspan="5">Nessun operatore in archivio.</td></tr>
                    </#if>
                </table>
            </div>
        </details>

        <!-- Tendina 2: AMMINISTRATORI (Chiusa di default) -->
        <details class="tendina-container">
            <summary class="tendina-titolo">👨‍💻 Lista Amministratori (Clicca per espandere/comprimere)</summary>
            <div class="table-wrapper">
                <table>
                    <tr>
                        <th>ID</th>
                        <th>Nome</th>
                        <th>Cognome</th>
                        <th>Email</th>
                        <th>Ruolo</th>
                    </tr>
                    <#assign adminTrovati = false>
                    <#if operatori??>
                        <#list operatori as o>
                            <!-- Filtro esatto per chi ha Ruolo = ADMIN -->
                            <#if (o.ruolo!"") == "ADMIN">
                                <#assign adminTrovati = true>
                                <tr>
                                    <td>${o.id_utente!}</td>
                                    <td>${o.nome!}</td>
                                    <td>${o.cognome!}</td>
                                    <td>${o.email!}</td>
                                    <td><strong style="color: blue;">ADMIN</strong></td>
                                </tr>
                            </#if>
                        </#list>
                    </#if>
                    <#if !adminTrovati>
                        <tr><td colspan="5">Nessun amministratore presente.</td></tr>
                    </#if>
                </table>
            </div>
        </details>
        
        <br>
        <a href="DashboardServlet" class="btn" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">&larr; Torna alla Dashboard</a>
    </div>

    <!-- SCRIPT PER FAR COMPARIRE/SCOMPARIRE LE COMPETENZE -->
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            var selectRuolo = document.getElementById("sceltaRuolo");
            var sezioneCompetenze = document.getElementById("sezioneCompetenze");

            function aggiornaVista() {
                if (selectRuolo.value === "ADMIN") {
                    sezioneCompetenze.style.display = "none";
                } else {
                    sezioneCompetenze.style.display = "block";
                }
            }

            aggiornaVista();
            selectRuolo.addEventListener("change", aggiornaVista);
        });
    </script>
</body>
</html>