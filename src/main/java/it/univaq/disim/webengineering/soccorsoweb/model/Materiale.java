/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "materiale")
public class Materiale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materiale")
    private Long idMateriale;

    @Column(nullable = false)
    private String nome;

    private String descrizione;

    @Column(nullable = false)
    private boolean attivo;

    // Costruttore vuoto obbligatorio per JPA
    public Materiale() {
    }

    // --- Getter e Setter ---
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
