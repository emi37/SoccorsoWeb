package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "assegnazione_operatori_missioni")
public class AssegnazioneOperatoreMissione {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // definisce la relazione molti a uno
    @ManyToOne
    // specifica la colonna usata come chiave esterna
    @JoinColumn(name = "id_missione", nullable = false)
    private Missione missione;

    // definisce la relazione molti a uno
    @ManyToOne
    // specifica la colonna usata come chiave esterna
    @JoinColumn(name = "id_utente", nullable = false)
    private Utenti utente;

    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "is_caposquadra", nullable = false)
    private boolean isCaposquadra;

    public AssegnazioneOperatoreMissione() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Missione getMissione() {
        return missione;
    }

    public void setMissione(Missione missione) {
        this.missione = missione;
    }

    public Utenti getUtente() {
        return utente;
    }

    public void setUtente(Utenti utente) {
        this.utente = utente;
    }

    public boolean isCaposquadra() {
        return isCaposquadra;
    }

    public void setCaposquadra(boolean isCaposquadra) {
        this.isCaposquadra = isCaposquadra;
    }
}