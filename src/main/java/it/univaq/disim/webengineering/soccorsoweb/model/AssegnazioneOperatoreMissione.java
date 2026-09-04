/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "assegnazione_operatori_missioni")
public class AssegnazioneOperatoreMissione {

   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne
    @JoinColumn(name = "id_missione", nullable = false)
    private Missione missione;

    
    @ManyToOne
    @JoinColumn(name = "id_utente", nullable = false)
    private Utenti utente;

    
    @Column(name = "is_caposquadra", nullable = false)
    private boolean isCaposquadra;

    
    public AssegnazioneOperatoreMissione() {
    }

    // --- Getter e Setter ---
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
