document.addEventListener('DOMContentLoaded', () => {
    const formRicerca = document.getElementById('formCercaMissione');
    const formAggiornamento = document.getElementById('formAggiornaMissione');

    if (formRicerca) {
        formRicerca.addEventListener('submit', gestisciRicercaMissione);
    }

    if (formAggiornamento) {
        formAggiornamento.addEventListener('submit', gestisciAggiornamentoMissione);
    }
});

async function gestisciRicercaMissione(event) {
    // blocco il submit nativo sennò mi ricarica la pagina a vuoto
    event.preventDefault();

    // tiro fuori l'ID inserito
    const inputRicerca = document.getElementById('id_missione_cerca');
    let idMissione = inputRicerca.value.trim();

    try {
        // preparo l'url per la GET
        let targetUrl = 'DettaglioMissioneServlet?id_missione=' + encodeURIComponent(idMissione);
        
        let response = await fetch(targetUrl, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error("Codice anomalo dal server: " + response.status);
        }

        // let payloadMissione = await response.json();
        // TODO: mappare i dati sulla UI per aggiornare la tabella
        
        console.log("Missione trovata e dati scaricati.");
    } catch (e) {
        // classica gestione manuale dell'errore
        console.error("Errore durante la ricerca della missione...");
        console.error(e);
        // non blocchiamo l'operatività ma avvisiamo l'operatore
    }
}

async function gestisciAggiornamentoMissione(event) {
    // blocco il submit standard
    event.preventDefault();

    // estraggo i valori puliti dai campi del form
    let idMissione = document.getElementById('id_missione').value.trim();
    let testoDescrittivo = document.getElementById('testo_descrittivo').value.trim();

    try {
        // assemblo il payload per la POST
        let formData = new FormData();
        formData.append('id_missione', idMissione);
        formData.append('testo_descrittivo', testoDescrittivo);

        let response = await fetch('DettaglioMissioneServlet', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error("Il server ha risposto picche: " + response.status);
        }

        console.log("Aggiornamento salvato con successo sul DB.");
        
        // pulisco il campo di testo per comodità dell'operatore
        document.getElementById('testo_descrittivo').value = '';

    } catch (e) {
        // gestione errore brutale ma efficace
        console.error("Errore in inserimento aggiornamento...");
        console.error(e);
    }
}