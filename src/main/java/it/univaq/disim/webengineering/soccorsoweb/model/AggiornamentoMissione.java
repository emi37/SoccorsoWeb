package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "aggiornamento_missione")
public class AggiornamentoMissione {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "id_aggiornamento")
    private Long idAggiornamento;

    // definisce la relazione molti a uno
    @ManyToOne
    // specifica la colonna usata come chiave esterna
    @JoinColumn(name = "id_missione")
    private Missione missione;

    // definisce la relazione molti a uno
    @ManyToOne
    // specifica la colonna usata come chiave esterna
    @JoinColumn(name = "id_admin")
    private Utenti admin;

    @Column(name = "testo_descritto")
    private String testoDescritto;

    @Column(name = "timestamp_inserimento")
    private LocalDateTime timestampInserimento;

    public AggiornamentoMissione() {
    }

    public Long getIdAggiornamento() {
        return idAggiornamento;
    }

    public void setIdAggiornamento(Long idAggiornamento) {
        this.idAggiornamento = idAggiornamento;
    }

    public Missione getMissione() {
        return missione;
    }

    public void setMissione(Missione missione) {
        this.missione = missione;
    }

    public Utenti getAdmin() {
        return admin;
    }

    public void setAdmin(Utenti admin) {
        this.admin = admin;
    }

    public String getTestoDescritto() {
        return testoDescritto;
    }

    public void setTestoDescritto(String testoDescritto) {
        this.testoDescritto = testoDescritto;
    }

    public LocalDateTime getTimestampInserimento() {
        return timestampInserimento;
    }

    public void setTimestampInserimento(LocalDateTime timestampInserimento) {
        this.timestampInserimento = timestampInserimento;
    }
}