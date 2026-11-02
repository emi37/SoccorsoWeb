document.addEventListener('DOMContentLoaded', () => {
    const formAssegnazione = document.getElementById('formAssegnazione');
    if (formAssegnazione) {
        // avvia il controllo dei dati quando viene inviato il form
        formAssegnazione.addEventListener('submit', validaAssegnazione);
    }

    // prende la scelta del caposquadra e fa chiama la funzione qui sotto
    const radioCaposquadra = document.querySelectorAll('input[name="caposquadra"]');
    radioCaposquadra.forEach(radio => {
        radio.addEventListener('change', autoSelezionaOperatore);
    });
});

// spunta la checkbox/il pallino dell'operatore scelto come caposquadra
function autoSelezionaOperatore(event) {
    const idCapo = event.target.value;
    const checkOperatore = document.querySelector('input[name="operatori"][value="' + idCapo + '"]');
    
    if (checkOperatore) {
        checkOperatore.checked = true;
    }
}

// blocca l'invio del form al DB se mancano i dati obbligatori
function validaAssegnazione(event) {
    // recupera solo le checkbox che l'utente ha spuntato
    const operatoriSelezionati = document.querySelectorAll('input[name="operatori"]:checked');
    const mezziSelezionati = document.querySelectorAll('input[name="mezzi"]:checked');
    
    // se non c'è nessun operatore, blocca l'evento di submit, perché deve esserci almeno un operatore in una squadra
    if (operatoriSelezionati.length === 0) {
        event.preventDefault();
        alert("Errore: devi selezionare almeno un operatore per la squadra.");
        return;
    }

    // se non c'è nessun mezzo, blocca l'evento di submit, lo stesso di sopra
    if (mezziSelezionati.length === 0) {
        event.preventDefault();
        alert("Errore: devi assegnare almeno un mezzo di soccorso per avviare l'intervento.");
        return;
    }
}