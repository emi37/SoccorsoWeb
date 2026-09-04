// Appena l'HTML ha finito di caricare, andiamo a pescare i dati dal server
document.addEventListener('DOMContentLoaded', () => {
    caricaRichiesteAttive();
    caricaMissioniInCorso();
});

async function caricaRichiesteAttive() {
    let tbodyRichieste = document.getElementById('tabellaRichieste');

    try {
        // chiamo la servlet che mi deve sputare fuori il JSON delle richieste
        let response = await fetch('RichiesteAttiveServlet', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error("Errore HTTP sulla servlet delle richieste: " + response.status);
        }

        let listaRichieste = await response.json();
        
        // svuoto la scritta "Caricamento in corso..."
        tbodyRichieste.innerHTML = '';

        // se non c'è nulla da fare, lo scriviamo chiaro
        if (listaRichieste.length === 0) {
            tbodyRichieste.innerHTML = '<tr><td colspan="6">Nessuna richiesta in coda al momento.</td></tr>';
            return;
        }

        // ciclo l'array e costruisco il DOM riga per riga
        listaRichieste.forEach(richiesta => {
            let riga = document.createElement('tr');
            
            // concateno stringhe classiche per assemblare l'HTML
            riga.innerHTML = 
                '<td>' + richiesta.id + '</td>' +
                '<td>' + richiesta.segnalante + '</td>' +
                '<td>' + richiesta.email + '</td>' +
                '<td>' + richiesta.posizione + '</td>' +
                '<td>' + richiesta.descrizione + '</td>' +
                '<td><a href="crea-missione.html?id_richiesta=' + richiesta.id + '" class="btn">Crea Missione</a></td>';
                
            tbodyRichieste.appendChild(riga);
        });

    } catch (e) {
        // gestione errore ruspante ma sicura
        console.error("Si è rotto qualcosa durante il caricamento delle richieste attive...");
        console.error(e);
        tbodyRichieste.innerHTML = '<tr><td colspan="6" style="color: red;">Impossibile recuperare le richieste. Dettagli in console.</td></tr>';
    }
}

async function caricaMissioniInCorso() {
    let tbodyMissioni = document.getElementById('tabellaMissioni');

    try {
        // stessa roba per le missioni: chiamo la servlet in GET
        let response = await fetch('MissioniInCorsoServlet', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error("Errore HTTP sulla servlet delle missioni: " + response.status);
        }

        let listaMissioni = await response.json();
        
        // faccio piazza pulita del segnaposto
        tbodyMissioni.innerHTML = '';

        if (listaMissioni.length === 0) {
            tbodyMissioni.innerHTML = '<tr><td colspan="6">Nessuna operazione sul campo attualmente attiva.</td></tr>';
            return;
        }

        // monto la tabella missioni
        listaMissioni.forEach(missione => {
            let riga = document.createElement('tr');
            
            riga.innerHTML = 
                '<td>' + missione.id_missione + '</td>' +
                '<td>' + missione.id_richiesta + '</td>' +
                '<td>' + missione.obiettivo + '</td>' +
                '<td>' + missione.posizione + '</td>' +
                '<td>' + missione.data_inizio + '</td>' +
                '<td><a href="dettaglio-missione.html?id_missione=' + missione.id_missione + '" class="btn">Apri Report</a></td>';
                
            tbodyMissioni.appendChild(riga);
        });

    } catch (e) {
        // come sopra, stampo l'errore per il debug e aggiorno la UI in modo non catastrofico
        console.error("Si è rotto qualcosa durante il caricamento delle missioni in corso...");
        console.error(e);
        tbodyMissioni.innerHTML = '<tr><td colspan="6" style="color: red;">Impossibile recuperare le missioni. Dettagli in console.</td></tr>';
    }
}

// funzioncina di utility se volessimo stampare messaggi per l'admin a fondo pagina
function mostraMessaggio(testo, tipoErrore = false) {
    let pMessaggio = document.getElementById('messaggio');
    if (!pMessaggio) return;
    
    pMessaggio.textContent = testo;
    pMessaggio.style.color = tipoErrore ? 'red' : 'green';
}