package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "abilita")
public class Abilita {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "id_abilita")
    private Long idAbilita;

    @Column(nullable = false)
    private String nome;
    
    
    public Abilita() {
    }

    public Long getIdAbilita() {
        return idAbilita;
    }

    public void setIdAbilita(Long idAbilita) {
        this.idAbilita = idAbilita;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}