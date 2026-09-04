document.addEventListener('DOMContentLoaded', () => {
    const formChiusura = document.getElementById('formChiusuraMissione');

    if (formChiusura) {
        formChiusura.addEventListener('submit', chiudiOperazione);
    }
});

async function chiudiOperazione(event) {
    // blocco il submit nativo così non ricarico la pagina perdendo contesto
    event.preventDefault();

    // mi tiro giù i dati dai campi del form, anche quelli hidden
    let idMissione = document.getElementById('id_missione').value;
    let idRichiesta = document.getElementById('id_richiesta').value;
    let votoSuccesso = document.getElementById('voto').value;
    let relazione = document.getElementById('commenti').value.trim();

    try {
        // preparo il pacchetto dati per la POST
        let formData = new FormData();
        formData.append('id_missione', idMissione);
        formData.append('id_richiesta', idRichiesta);
        formData.append('voto_successo', votoSuccesso);
        formData.append('commenti', relazione);

        let response = await fetch('ConcludiMissioneServlet', {
            method: 'POST',
            body: formData
        });

        // se la servlet sbotta, lancio l'eccezione
        if (!response.ok) {
            throw new Error("Errore HTTP dalla servlet: " + response.status);
        }

        console.log("Missione chiusa con successo e risorse liberate.");
        alert("Intervento chiuso correttamente. Ottimo lavoro!");
        
        // volendo qua potremmo fare un redirect pulito alla dashboard
        window.location.href = "DashboardServlet";

    } catch (e) {
        // classica gestione brutale in console
        console.error("Errore fatale durante la chiusura della missione...");
        console.error(e);
        alert("Impossibile registrare la chiusura. Dai un'occhiata alla console di rete.");
    }
}