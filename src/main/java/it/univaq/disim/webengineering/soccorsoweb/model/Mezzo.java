/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mezzo")
public class Mezzo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mezzo")
    private Long idMezzo;

    @Column(nullable = false)
    private String nome;

    private String descrizione;

    // Costruttore vuoto obbligatorio per JPA
    public Mezzo() {
    }

    // --- Getter e Setter ---
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
