// faccio partire tutto solo quando il DOM è bello che pronto
document.addEventListener('DOMContentLoaded', () => {
    try {
        gestisciRedirectAutomatico();
    } catch (e) {
        // se esplode il setup iniziale, sputo l'errore a video ma la pagina HTML resta navigabile
        console.err("Errore fatale nell'avvio del timer di redirect...");
        console.error(e);
    }
});

function gestisciRedirectAutomatico() {
    // mi vado a pescare i riferimenti a video
    let spanCountdown = document.getElementById('countdown');
    let btnRitorno = document.getElementById('btnRitorno');

    // controllo preventivo per evitare null pointer exception dei poveri
    if (!spanCountdown || !btnRitorno) {
        throw new Error("Elementi del DOM mancanti. Impossibile attaccare il timer.");
    }

    let tempoRimanente = 5;

    // faccio frullare il timer scalandolo di uno ogni secondo
    let intervalloCountdown = setInterval(() => {
        tempoRimanente--;
        spanCountdown.textContent = tempoRimanente;

        if (tempoRimanente <= 0) {
            // il tempo è scaduto: sego l'intervallo dalla memoria e sparo l'utente alla home
            clearInterval(intervalloCountdown);
            console.log("Redirect automatico triggerato.");
            window.location.href = 'index.html';
        }
    }, 1000);

    // gestisco il caso in cui l'utente si stufa di aspettare e clicca da solo
    btnRitorno.addEventListener('click', (event) => {
        // blocco la navigazione standard per fare pulizia prima
        event.preventDefault();
        
        // ammazzo il timer in background sennò continua a girare a vuoto
        clearInterval(intervalloCountdown);
        console.log("L'utente ha forzato il ritorno manuale.");
        
        // redirect manuale
        window.location.href = 'index.html';
    });
}