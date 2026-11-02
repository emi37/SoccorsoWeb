package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "mezzo")
public class Mezzo {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "id_mezzo")
    private Long idMezzo;

    @Column(nullable = false)
    private String nome;

    private String descrizione;

    public Mezzo() {
    }

    public Long getIdMezzo() {
        return idMezzo;
    }

    public void setIdMezzo(Long idMezzo) {
        this.idMezzo = idMezzo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}