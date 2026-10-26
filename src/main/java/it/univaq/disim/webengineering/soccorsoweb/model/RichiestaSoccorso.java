package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "richiesta_soccorso")
public class RichiestaSoccorso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_richiesta")
    private int idRichiesta;

    @Column(name = "descrizione", nullable = false, columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "posizione", nullable = false)
    private String posizione;

    @Column(name = "nome_segnalante", nullable = false, length = 100)
    private String nomeSegnalante;

    @Column(name = "email_segnalante", nullable = false, length = 150)
    private String emailSegnalante;

    @Column(name = "foto_path")
    private String fotoPath;

    @Column(name = "ip_origine", length = 45)
    private String ipOrigine;

    @Column(name = "token_convalida", unique = true)
    private String tokenConvalida;

    @Column(name = "stato", nullable = false, length = 30)
    private String stato = "IN_ATTESA";

    @Column(name = "timestamp_invio", insertable = false, updatable = false)
    private Timestamp timestampInvio;

    // Costruttore vuoto obbligatorio per JPA
    public RichiestaSoccorso() {
    }

    // --- Getter e Setter ---

    public int getIdRichiesta() {
        return idRichiesta;
    }

    public void setIdRichiesta(int idRichiesta) {
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

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
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

    public Timestamp getTimestampInvio() {
        return timestampInvio;
    }

    public void setTimestampInvio(Timestamp timestampInvio) {
        this.timestampInvio = timestampInvio;
    }
}