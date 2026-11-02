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

// funzione per cercare una missione tramite fetch API
async function gestisciRicercaMissione(event) {
    //blocca il caricamento della pagina al submit
    event.preventDefault();

    const inputRicerca = document.getElementById('id_missione_cerca');
    const idMissione = inputRicerca.value.trim();

    try {
        const targetUrl = 'DettaglioMissioneServlet?id_missione=' + encodeURIComponent(idMissione);
        
        const response = await fetch(targetUrl, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error("Errore dal server: " + response.status);
        }
        
    } catch (e) {
        console.error("errore durante la ricerca della missione:", e);
    }
}

// funzione  per inviare un aggiornamento di una missione tramite fetch API
async function gestisciAggiornamentoMissione(event) {
    // blocca il caricamento della pagina al submit
    event.preventDefault();

    const idMissione = document.getElementById('id_missione').value.trim();
    const testoDescrittivo = document.getElementById('testo_descrittivo').value.trim();

    try {
        const formData = new FormData();
        formData.append('id_missione', idMissione);
        formData.append('testo_descrittivo', testoDescrittivo);

        const response = await fetch('DettaglioMissioneServlet', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error("Errore : " + response.status);
        }
        
        // svuota il campo di testo 
                document.getElementById('testo_descrittivo').value = '';

    } catch (e) {
        console.error("errore durante il salvataggio dell'aggiornamento:", e);
    }
}