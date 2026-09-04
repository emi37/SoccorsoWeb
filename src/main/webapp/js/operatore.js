// faccio partire tutto solo quando l'HTML è stato elaborato dal browser
document.addEventListener('DOMContentLoaded', () => {
    caricaMissioniAssegnate();
    caricaCompetenzeAttuali();

    // aggancio il form di aggiornamento profilo per bloccare il submit nativo
    const formProfilo = document.getElementById('formAggiornaProfilo');
    if (formProfilo) {
        formProfilo.addEventListener('submit', aggiornaCompetenze);
    }
});

async function caricaMissioniAssegnate() {
    let tbodyMissioni = document.getElementById('missioni-tbody');
    
    try {
        // chiamo la servlet per farmi dare le missioni dell'operatore loggato
        let response = await fetch('ListaMissioniOperatoreServlet', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
            throw new Error("Il server ha sbroccato sulle missioni: " + response.status);
        }

        let listaMissioni = await response.json();
        
        // piallo il messaggio di caricamento testuale
        tbodyMissioni.innerHTML = '';

        // gestisco il caso vuoto
        if (listaMissioni.length === 0) {
            tbodyMissioni.innerHTML = '<tr><td colspan="5" class="text-center">Nessuna missione assegnata al momento. Goditi il riposo.</td></tr>';
            return;
        }

        // ciclo l'array e monto l'HTML riga per riga
        listaMissioni.forEach(missione => {
            let tr = document.createElement('tr');
            
            // concatenazione manuale del markup
            tr.innerHTML = 
                '<td>' + missione.id_missione + '</td>' +
                '<td>' + missione.obiettivo + '</td>' +
                '<td>' + missione.posizione + '</td>' +
                '<td>' + missione.stato + '</td>' +
                '<td>' + (missione.esito !== undefined && missione.esito !== null ? missione.esito : 'In corso') + '</td>';
                
            tbodyMissioni.appendChild(tr);
        });

    } catch (e) {
        // logging brutale per il debug
        console.error("Errore fatale nel caricamento delle missioni...");
        console.error(e);
        tbodyMissioni.innerHTML = '<tr><td colspan="5" class="text-center" style="color: red;">Impossibile recuperare i dati. Dai un occhio alla console.</td></tr>';
    }
}

async function caricaCompetenzeAttuali() {
    let selectPatenti = document.getElementById('patenti-select');
    let selectAbilita = document.getElementById('abilita-select');

    try {
        // tiro giù il JSON con i dati dell'operatore (es. nome, liste patenti e abilità)
        let response = await fetch('DettaglioOperatoreServlet', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
            throw new Error("Errore HTTP sul recupero profilo: " + response.status);
        }

        let profilo = await response.json();

        // svuoto le tendine
        selectPatenti.innerHTML = '';
        selectAbilita.innerHTML = '';

        // personalizzo il saluto in alto se il server mi manda il nome
        if (profilo.nome) {
            document.getElementById('saluto-operatore').textContent = "Pannello operatore - " + profilo.nome;
        }

        // popolo le patenti
        if (profilo.patenti && profilo.patenti.length > 0) {
            profilo.patenti.forEach(patente => {
                let opt = document.createElement('option');
                opt.value = patente;
                opt.textContent = patente;
                selectPatenti.appendChild(opt);
            });
        } else {
            selectPatenti.innerHTML = '<option value="">Nessuna patente registrata</option>';
        }

        // popolo le specializzazioni
        if (profilo.abilita && profilo.abilita.length > 0) {
            profilo.abilita.forEach(abilita => {
                let opt = document.createElement('option');
                opt.value = abilita;
                opt.textContent = abilita;
                selectAbilita.appendChild(opt);
            });
        } else {
            selectAbilita.innerHTML = '<option value="">Nessuna specializzazione registrata</option>';
        }

    } catch (e) {
        console.error("Errore durante il recupero delle competenze...");
        console.error(e);
        selectPatenti.innerHTML = '<option value="">Errore di caricamento</option>';
        selectAbilita.innerHTML = '<option value="">Errore di caricamento</option>';
    }
}

async function aggiornaCompetenze(event) {
    // blocco il refresh standard, facciamo tutto in asincrono
    event.preventDefault();

    let patentiInserite = document.getElementById('patenti').value.trim();
    let abilitaInserite = document.getElementById('abilita').value.trim();

    // se è tutto vuoto non chiamo nemmeno il server
    if (!patentiInserite && !abilitaInserite) {
        alert("Inserisci almeno una competenza o patente prima di salvare.");
        return;
    }

    try {
        let formData = new FormData();
        formData.append('patenti', patentiInserite);
        formData.append('abilita', abilitaInserite);

        let response = await fetch('DashboardOperatoreServlet', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error("Errore salvataggio competenze. Stato: " + response.status);
        }

        console.log("Dati profilo spediti con successo al DB.");
        alert("Profilo aggiornato correttamente.");

        // pulisco i campi di input
        document.getElementById('patenti').value = '';
        document.getElementById('abilita').value = '';

        // ricarico le tendine al volo per far vedere le novità senza ricaricare la pagina
        caricaCompetenzeAttuali();

    } catch (e) {
        console.error("Salvataggio fallito");
        console.error(e);
        alert("Impossibile aggiornare il profilo.");
    }
}