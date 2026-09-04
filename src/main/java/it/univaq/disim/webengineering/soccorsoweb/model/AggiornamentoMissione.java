/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aggiornamento_missione")
public class AggiornamentoMissione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aggiornamento")
    private Long idAggiornamento;

    // Relazione N:1 verso la Missione (lato proprietario della relazione)
    @ManyToOne
    @JoinColumn(name = "id_missione")
    private Missione missione;

    // Relazione N:1 verso l'Utente (l'admin che ha inserito l'aggiornamento)
    @ManyToOne
    @JoinColumn(name = "id_admin")
    private Utenti admin;

    @Column(name = "testo_descritto")
    private String testoDescritto;

    @Column(name = "timestamp_inserimento")
    private LocalDateTime timestampInserimento;

    // Costruttore vuoto obbligatorio per JPA
    public AggiornamentoMissione() {
    }

    // --- Getter e Setter ---
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
