/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

// aspettiamo che il DOM sia caricato per evitare null pointer
document.addEventListener('DOMContentLoaded', () => {
    try {
        const formAssegnazione = document.getElementById('formAssegnazione');
        if (formAssegnazione) {
            formAssegnazione.addEventListener('submit', validaAssegnazione);
        }

        // mi vado a pescare tutti i radio button del caposquadra per agganciargli un listener
        const radioCaposquadra = document.querySelectorAll('input[name="caposquadra"]');
        radioCaposquadra.forEach(radio => {
            radio.addEventListener('change', autoSelezionaOperatore);
        });

    } catch (e) {
        console.error("Errore fatale nell'inizializzazione degli script di assegnazione...");
        console.error(e);
    }
});

function autoSelezionaOperatore(event) {
    // tiro fuori l'ID dell'operatore appena nominato capo
    let idCapo = event.target.value;

    // cerco la checkbox generica corrispondente allo stesso operatore
    let checkOperatore = document.querySelector('input[name="operatori"][value="' + idCapo + '"]');
    
    // se l'ho trovata, la spunto in automatico (il capo deve far parte della squadra per forza)
    if (checkOperatore) {
        checkOperatore.checked = true;
    }
}

function validaAssegnazione(event) {
    // mi prendo tutte le checkbox spuntate per fare la conta
    let operatoriSelezionati = document.querySelectorAll('input[name="operatori"]:checked');
    let mezziSelezionati = document.querySelectorAll('input[name="mezzi"]:checked');
    
    // controllo base: non si parte se non c'è almeno un tizio e una macchina
    if (operatoriSelezionati.length === 0) {
        event.preventDefault();
        alert("Errore: devi selezionare almeno un operatore per comporre la squadra.");
        return;
    }

    if (mezziSelezionati.length === 0) {
        event.preventDefault();
        alert("Errore: devi assegnare almeno un mezzo di soccorso per l'intervento.");
        return;
    }

    // il caposquadra non lo controllo perché il campo radio ha già l'attributo 'required' nativo dell'HTML
    
    console.log("Validazione superata, lascio passare la POST al server.");
    // NB: non faccio preventDefault() qui, quindi il form fa il suo lavoro nativo
}