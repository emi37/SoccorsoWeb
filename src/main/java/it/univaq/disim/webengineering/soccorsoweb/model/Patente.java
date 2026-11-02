package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "patente")
public class Patente {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "id_patente")
    private Long idPatente;

    @Column(nullable = false)
    private String codice;

    public Patente() {
    }

    public Long getIdPatente() {
        return idPatente;
    }

    public void setIdPatente(Long idPatente) {
        this.idPatente = idPatente;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }
}