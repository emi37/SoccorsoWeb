/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "patente")
public class Patente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_patente")
    private Long idPatente;

    @Column(nullable = false)
    private String codice;

    // Costruttore vuoto obbligatorio per JPA
    public Patente() {
    }

    // --- Getter e Setter ---
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
