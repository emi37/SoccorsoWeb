document.addEventListener('DOMContentLoaded', () => {
    caricaStoricoMissioni();
});

async function caricaStoricoMissioni() {
    let tbodyStorico = document.getElementById('storicoTableBody');

    try {
        // chiamo l'endpoint che mi deve restituire lo storico in json
        let response = await fetch('StoricoMissioniServlet', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error("Errore HTTP fetch storico: " + response.status);
        }

        let listaMissioni = await response.json();

        // piallo il messaggio "Caricamento in corso..."
        tbodyStorico.innerHTML = '';

        // gestisco il caso in cui il db non abbia missioni chiuse
        if (listaMissioni.length === 0) {
            tbodyStorico.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Nessuna missione conclusa presente nel database.</td></tr>';
            return;
        }

        // monto la tabella iterando sull'array
        listaMissioni.forEach(missione => {
            let riga = document.createElement('tr');
            
            // solita concatenazione manuale pura
            riga.innerHTML = 
                '<td>' + missione.id_missione + '</td>' +
                '<td>' + missione.id_richiesta + '</td>' +
                '<td>' + (missione.obiettivo ? missione.obiettivo : 'ND') + '</td>' +
                '<td>' + (missione.esito !== undefined && missione.esito !== null ? missione.esito : 'ND') + ' / 5</td>' +
                '<td>' + (missione.commenti ? missione.commenti : 'Nessuna relazione fornita') + '</td>';
                
            tbodyStorico.appendChild(riga);
        });

    } catch (e) {
        // gestione errore ruspante ma pulita: uso la classe text-error invece dello style inline
        console.error("Si è rotto qualcosa durante il caricamento dello storico...");
        console.error(e);
        tbodyStorico.innerHTML = '<tr><td colspan="5" class="text-center text-error">Impossibile recuperare i dati. Dai un occhio alla console di rete.</td></tr>';
    }
}