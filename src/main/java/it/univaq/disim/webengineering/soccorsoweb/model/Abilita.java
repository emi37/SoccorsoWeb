/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "abilita")
public class Abilita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abilita")
    private Long idAbilita;

    @Column(nullable = false)
    private String nome;

    // Costruttore vuoto obbligatorio per JPA
    public Abilita() {
    }

    // --- Getter e Setter ---
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
