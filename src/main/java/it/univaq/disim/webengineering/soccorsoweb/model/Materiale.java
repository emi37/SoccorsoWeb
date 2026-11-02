package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "materiale")
public class Materiale {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "id_materiale")
    private Long idMateriale;

    @Column(nullable = false)
    private String nome;

    private String descrizione;

    @Column(nullable = false)
    private boolean attivo;

    public Materiale() {
    }

    public Long getIdMateriale() {
        return idMateriale;
    }

    public void setIdMateriale(Long idMateriale) {
        this.idMateriale = idMateriale;
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

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }
}