package it.univaq.disim.webengineering.soccorsoweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// indica che questa classe rappresenta un'entità del database
@Entity
// specifica il nome esatto della tabella all'interno del database
@Table(name = "missione")
public class Missione {

    // indica che questa variabile è la chiave primaria della tabella
    @Id
    // dice al database di autoincrementare l'id automaticamente (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // collega questa variabile al nome esatto della colonna nel db
    @Column(name = "id_missione")
    private Long idMissione;

    private String obiettivo;

    private String posizione;

    private String stato;

    @Column(name = "timestap_inizio")
    private LocalDateTime timestampInizio;

    @Column(name = "timestamp_fine")
    private LocalDateTime timestampFine;

    @Column(name = "livello_successo")
    private Integer livelloSuccesso;

    private String commenti;

    // definisce la relazione uno a uno
    @OneToOne
    // specifica la colonna usata come chiave esterna
    @JoinColumn(name = "id_richiesta")
    private RichiestaSoccorso richiesta;

    // definisce la relazione molti a molti
    @ManyToMany
    // specifica la tabella ponte da utilizzare nel db
    @JoinTable(
            name = "assegnazione_mezzi_missione",
            joinColumns = @JoinColumn(name = "id_missione"),
            inverseJoinColumns = @JoinColumn(name = "id_mezzo")
    )
    private List<Mezzo> mezzi = new ArrayList<>();

    // definisce la relazione molti a molti
    @ManyToMany
    // specifica la tabella ponte da utilizzare nel db
    @JoinTable(
            name = "assegnazione_materiale_missione",
            joinColumns = @JoinColumn(name = "id_missione"),
            inverseJoinColumns = @JoinColumn(name = "id_materiale")
    )
    private List<Materiale> materiali = new ArrayList<>();

    // definisce la relazione uno a molti e indica la variabile mappata
    @OneToMany(mappedBy = "missione")
    private List<AssegnazioneOperatoreMissione> squadra = new ArrayList<>();

    // definisce la relazione uno a molti e indica la variabile mappata
    @OneToMany(mappedBy = "missione")
    private List<AggiornamentoMissione> aggiornamenti = new ArrayList<>();

    public Missione() {
    }

    public Long getIdMissione() {
        return idMissione;
    }

    public void setIdMissione(Long idMissione) {
        this.idMissione = idMissione;
    }

    public String getObiettivo() {
        return obiettivo;
    }

    public void setObiettivo(String obiettivo) {
        this.obiettivo = obiettivo;
    }

    public String getPosizione() {
        return posizione;
    }

    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public LocalDateTime getTimestampInizio() {
        return timestampInizio;
    }

    public void setTimestampInizio(LocalDateTime timestampInizio) {
        this.timestampInizio = timestampInizio;
    }

    public LocalDateTime getTimestampFine() {
        return timestampFine;
    }

    public void setTimestampFine(LocalDateTime timestampFine) {
        this.timestampFine = timestampFine;
    }

    public Integer getLivelloSuccesso() {
        return livelloSuccesso;
    }

    public void setLivelloSuccesso(Integer livelloSuccesso) {
        this.livelloSuccesso = livelloSuccesso;
    }

    public String getCommenti() {
        return commenti;
    }

    public void setCommenti(String commenti) {
        this.commenti = commenti;
    }

    public RichiestaSoccorso getRichiesta() {
        return richiesta;
    }

    public void setRichiesta(RichiestaSoccorso richiesta) {
        this.richiesta = richiesta;
    }

    public List<Mezzo> getMezzi() {
        return mezzi;
    }

    public void setMezzi(List<Mezzo> mezzi) {
        this.mezzi = mezzi;
    }

    public List<Materiale> getMateriali() {
        return materiali;
    }

    public void setMateriali(List<Materiale> materiali) {
        this.materiali = materiali;
    }

    public List<AssegnazioneOperatoreMissione> getSquadra() {
        return squadra;
    }

    public void setSquadra(List<AssegnazioneOperatoreMissione> squadra) {
        this.squadra = squadra;
    }

    public List<AggiornamentoMissione> getAggiornamenti() {
        return aggiornamenti;
    }

    public void setAggiornamenti(List<AggiornamentoMissione> aggiornamenti) {
        this.aggiornamenti = aggiornamenti;
    }
}