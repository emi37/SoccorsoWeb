/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "richiesta_soccorso")
public class RichiestaSoccorso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_richiesta")
    private Long idRichiesta;

    @Column(nullable = false)
    private String descrizione;

    @Column(nullable = false)
    private String posizione;

    @Column(name = "nome_segnalante", nullable = false)
    private String nomeSegnalante;

    @Column(name = "email_segnalante", nullable = false)
    private String emailSegnalante;

    @Column(name = "ip_origine", nullable = false)
    private String ipOrigine;

    @Column(name = "token_convalida")
    private String tokenConvalida;

    @Column(nullable = false)
    private String stato;

    @Column(name = "foto_path")
    private String fotoPath;

    @Column(name = "timestamp_creazione", nullable = false)
    private LocalDateTime timestampCreazione;

    @Column(name = "timestamp_convalida")
    private LocalDateTime timestampConvalida;

    // @Lob indica che questo campo contiene dati di grandi dimensioni (es. BLOB)
    @Lob
    private byte[] foto;

    // Costruttore vuoto obbligatorio per JPA
    public RichiestaSoccorso() {
    }

    // --- Getter e Setter ---
    public Long getIdRichiesta() {
        return idRichiesta;
    }

    public void setIdRichiesta(Long idRichiesta) {
        this.idRichiesta = idRichiesta;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getPosizione() {
        return posizione;
    }

    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    public String getNomeSegnalante() {
        return nomeSegnalante;
    }

    public void setNomeSegnalante(String nomeSegnalante) {
        this.nomeSegnalante = nomeSegnalante;
    }

    public String getEmailSegnalante() {
        return emailSegnalante;
    }

    public void setEmailSegnalante(String emailSegnalante) {
        this.emailSegnalante = emailSegnalante;
    }

    public String getIpOrigine() {
        return ipOrigine;
    }

    public void setIpOrigine(String ipOrigine) {
        this.ipOrigine = ipOrigine;
    }

    public String getTokenConvalida() {
        return tokenConvalida;
    }

    public void setTokenConvalida(String tokenConvalida) {
        this.tokenConvalida = tokenConvalida;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public LocalDateTime getTimestampCreazione() {
        return timestampCreazione;
    }

    public void setTimestampCreazione(LocalDateTime timestampCreazione) {
        this.timestampCreazione = timestampCreazione;
    }

    public LocalDateTime getTimestampConvalida() {
        return timestampConvalida;
    }

    public void setTimestampConvalida(LocalDateTime timestampConvalida) {
        this.timestampConvalida = timestampConvalida;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }
}
