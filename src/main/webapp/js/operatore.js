document.addEventListener("DOMContentLoaded", function() {
    // Usa l'URL corretto della tua Servlet (es. aggiungendo eventuale slash o percorso)
    fetch('DashboardOperatoreServlet')
        .then(response => {
            if (!response.ok) {
                throw new Error("Errore di rete nella risposta del server");
            }
            return response.json();
        })
        .then(data => {
            // 1. Nome Operatore (CORRETTO con i backtick)
            const saluto = document.getElementById("saluto-operatore");
            if (saluto && data.nomeOperatore) {
                saluto.textContent = `Pannello operatore: ${data.nomeOperatore}`;
            }

            // 2. Tabella Missioni
            const tbody = document.getElementById("missioni-tbody");
            if (tbody) {
                tbody.innerHTML = "";
                if (data.missioni && data.missioni.length > 0) {
                    data.missioni.forEach(m => {
                        const tr = document.createElement("tr");
                        tr.innerHTML = `
                            <td><strong>#${m.id_missione}</strong></td>
                            <td>${m.obiettivo}</td>
                            <td>${m.posizione}</td>
                            <td>${m.stato}</td>
                            <td><strong>${m.visualizzaVoto}</strong></td>
                        `;
                        tbody.appendChild(tr);
                    });
                } else {
                    const tr = document.createElement("tr");
                    tr.innerHTML = '<td colspan="5" class="text-center">Non sei ancora stato assegnato a nessuna missione.</td>';
                    tbody.appendChild(tr);
                }
            }

            // 3. Patenti Select (Richiede che data.patenti sia un array)
            const patSelect = document.getElementById("patenti-select");
            if (patSelect) {
                patSelect.innerHTML = "";
                if (data.patenti && data.patenti.length > 0) {
                    data.patenti.forEach(pat => {
                        const opt = document.createElement("option");
                        opt.textContent = pat;
                        patSelect.appendChild(opt);
                    });
                } else {
                    const opt = document.createElement("option");
                    opt.textContent = "Nessuna patente presente a sistema";
                    patSelect.appendChild(opt);
                }
            }

            // 4. Abilitazioni Select (Richiede che data.abilita sia un array)
            const abSelect = document.getElementById("abilita-select");
            if (abSelect) {
                abSelect.innerHTML = "";
                if (data.abilita && data.abilita.length > 0) {
                    data.abilita.forEach(ab => {
                        const opt = document.createElement("option");
                        opt.textContent = ab;
                        abSelect.appendChild(opt);
                    });
                } else {
                    const opt = document.createElement("option");
                    opt.textContent = "Nessuna specializzazione presente a sistema";
                    abSelect.appendChild(opt);
                }
            }
        })
        .catch(error => {
            console.error("Errore nel caricamento dei dati operatore:", error);
        });
});