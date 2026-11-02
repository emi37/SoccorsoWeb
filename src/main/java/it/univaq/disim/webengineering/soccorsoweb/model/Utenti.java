package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "utenti")
public class Utenti {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "id_utente")
    private Long idUtente;

    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String ruolo;
    private boolean attivo;

    // definisce la relazione molti a molti
    @ManyToMany
    // specifica la tabella ponte da utilizzare nel db
    @JoinTable(
            name = "utente_patente",
            joinColumns = @JoinColumn(name = "id_utente"),
            inverseJoinColumns = @JoinColumn(name = "id_patente")
    )
    private List<Patente> patenti = new ArrayList<>();

    // definisce la relazione molti a molti
    @ManyToMany
    // specifica la tabella ponte da utilizzare nel db
    @JoinTable(
            name = "utente_abilita",
            joinColumns = @JoinColumn(name = "id_utente"),
            inverseJoinColumns = @JoinColumn(name = "id_abilita")
    )
    private List<Abilita> abilita = new ArrayList<>();

    public Utenti() {
    }

    public Long getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Long idUtente) {
        this.idUtente = idUtente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }

    public List<Patente> getPatenti() {
        return patenti;
    }

    public void setPatenti(List<Patente> patenti) {
        this.patenti = patenti;
    }

    public List<Abilita> getAbilita() {
        return abilita;
    }

    public void setAbilita(List<Abilita> abilita) {
        this.abilita = abilita;
    }
}