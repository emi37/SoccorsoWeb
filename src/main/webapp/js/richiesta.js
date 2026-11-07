fetch("CaptchaServlet")
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            document.getElementById("domandaCaptcha").textContent = data.domanda;
        })
        .catch(function () {
            document.getElementById("domandaCaptcha").textContent =
                    "Errore nel caricamento del captcha";
        });

// mostra un messaggio chiaro quando il server rimanda qui con ?errore=...
(function () {
    var messaggi = {
        dati_mancanti: "Compila tutti i campi obbligatori.",
        dati_non_validi: "Alcuni dati non sono validi: controlla l'indirizzo e-mail e la lunghezza dei testi.",
        captcha_mancante: "Il controllo anti-spam è scaduto: rispondi alla nuova domanda e invia di nuovo.",
        captcha_vuoto: "Rispondi alla domanda anti-spam.",
        captcha_errato: "La risposta alla domanda anti-spam è sbagliata: riprova con la nuova domanda.",
        captcha_non_valido: "La risposta alla domanda anti-spam deve essere un numero.",
        troppe_richieste: "Hai già inviato una richiesta da pochi minuti con questa e-mail o da questa connessione. Attendi qualche minuto prima di inviarne un'altra.",
        salvataggio: "Non è stato possibile salvare la richiesta. Riprova tra poco.",
        rendering: "La richiesta è stata salvata, ma non è stato possibile mostrare la conferma."
    };

    var codice = new URLSearchParams(window.location.search).get("errore");
    if (codice) {
        var box = document.getElementById("messaggioErrore");
        // textContent (e non innerHTML): il testo non viene mai interpretato come HTML
        box.textContent = messaggi[codice] || "Si è verificato un errore: riprova.";
        box.hidden = false;
    }
})();
