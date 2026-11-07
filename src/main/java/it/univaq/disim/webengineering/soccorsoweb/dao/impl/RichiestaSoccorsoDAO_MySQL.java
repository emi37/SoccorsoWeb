package it.univaq.disim.webengineering.soccorsoweb.dao.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import it.univaq.disim.webengineering.soccorsoweb.dao.RichiestaSoccorsoDAO;

import it.univaq.disim.webengineering.soccorsoweb.model.RichiestaSoccorso;
import it.univaq.disim.webengineering.soccorsoweb.util.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichiestaSoccorsoDAO_MySQL implements RichiestaSoccorsoDAO {

    private static final Logger LOGGER = Logger.getLogger(RichiestaSoccorsoDAO_MySQL.class.getName());

    // inserisce una nuova richiesta in stato IN_ATTESA dal form di rchiesta
    @Override
    public boolean salvaNuovaRichiesta(RichiestaSoccorso richiesta) {
        String query = "INSERT INTO richiesta_soccorso "
                + "(nome_segnalante, email_segnalante, posizione, descrizione, ip_origine, token_convalida, stato, timestamp_creazione) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'IN_ATTESA', NOW())";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setString(1, richiesta.getNomeSegnalante());
            statement.setString(2, richiesta.getEmailSegnalante());
            statement.setString(3, richiesta.getPosizione());
            statement.setString(4, richiesta.getDescrizione());
            statement.setString(5, richiesta.getIpOrigine());
            statement.setString(6, richiesta.getTokenConvalida());

            return statement.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante il salvataggio della richiesta : " + e.getMessage(), e);
        }
        return false;
    }

    // anti-spam: true se la stessa email (o lo stesso IP) ha già inviato una richiesta negli ultimi N minuti
    @Override
    public boolean esisteRichiestaRecente(String email, String ipOrigine, int minuti) {
        String query = "SELECT COUNT(*) FROM richiesta_soccorso "
                + "WHERE (email_segnalante = ? OR ip_origine = ?) "
                + "  AND timestamp_creazione >= NOW() - INTERVAL ? MINUTE";

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, ipOrigine);
            statement.setInt(3, minuti);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante il controllo anti-spam: " + e.getMessage(), e);
        }
        // se il controllo fallisce non blocchiamo una possibile vera emergenza
        return false;
    }

    // convalida il token via email e attiva la richiesta (se non convalidi entro 10 min la richiesta rimane in stato di attesa sul DB)
    @Override
    public boolean svuotaTokenConvalida(String tokenUsato) {
        String query = "UPDATE richiesta_soccorso "
                + "SET stato = 'ATTIVA', "
                + "    token_convalida = NULL, "
                + "    timestamp_convalida = CURRENT_TIMESTAMP "
                + "WHERE token_convalida = ? "
                + "  AND stato = 'IN_ATTESA' "
                + "  AND timestamp_creazione >= NOW() - INTERVAL 10 MINUTE";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setString(1, tokenUsato);
            
            return statement.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore db durante la convalida del token: " + e.getMessage(), e);
        }
        return false;
    }

    // estrae le richieste con lo stato "ATTIVA" (viene richiamato dalla dashboard dell'admin)
    @Override
    public List< Map<String, String> > estraiRichiesteAttive() {
        List< Map<String, String> > listaRichieste = new ArrayList<>();
        
        String query = "SELECT id_richiesta, nome_segnalante, email_segnalante, posizione, descrizione "
                + "FROM richiesta_soccorso WHERE stato = 'ATTIVA' ORDER BY timestamp_convalida ASC";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query); 
             ResultSet resultSet = statement.executeQuery()) {
             
            while (resultSet.next()) {
                Map< String, String > mappaRichiesta = new HashMap<>();
                mappaRichiesta.put("id", String.valueOf(resultSet.getInt("id_richiesta")));
                mappaRichiesta.put("segnalante", resultSet.getString("nome_segnalante"));
                mappaRichiesta.put("email", resultSet.getString("email_segnalante"));
                mappaRichiesta.put("posizione", resultSet.getString("posizione"));
                mappaRichiesta.put("descrizione", resultSet.getString("descrizione"));
                
                listaRichieste.add(mappaRichiesta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione delle richieste attive: " + e.getMessage(), e);
        }
        return listaRichieste;
    }

    // scarta la richiesta impostandogli lo stato sul DB in "IGNORATA "
    @Override
    public boolean aggiornaStatoRichiestaInIgnorata(int idRichiestaDaScartare) {
        // si puo' ignorare solo una richiesta ancora ATTIVA (non una gia' in corso o chiusa)
        String query = "UPDATE richiesta_soccorso SET stato = 'IGNORATA' WHERE id_richiesta = ? AND stato = 'ATTIVA'";

        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setInt(1, idRichiestaDaScartare);
            
            return statement.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'aggiornamento dello stato ignorata: " + e.getMessage(), e);
        }
        return false;
    }

    // estrae i dettagli di una richiesta(la richieta estratta è quella con id_richiesta = ?)
    @Override
    public Map< String, String > estraiDettagliRichiesta(int idRichiesta) {
        Map< String, String > dettagli = new HashMap<>();
        
        String query = "SELECT nome_segnalante, posizione, descrizione, timestamp_creazione " +
                       "FROM richiesta_soccorso WHERE id_richiesta = ?";
                       
        try (Connection connessione = DBManager.getConnection(); 
             PreparedStatement statement = connessione.prepareStatement(query)) {
             
            statement.setInt(1, idRichiesta);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    dettagli.put("id_richiesta", String.valueOf(idRichiesta));
                    dettagli.put("nome", resultSet.getString("nome_segnalante"));
                    dettagli.put("posizione", resultSet.getString("posizione"));
                    dettagli.put("descrizione", resultSet.getString("descrizione"));
                    dettagli.put("data_ora", resultSet.getTimestamp("timestamp_creazione").toString());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante l'estrazione dei dettagli della richiesta: " + e.getMessage(), e);
        }
        return dettagli;
    }

    // ---- pulizia: elimina le richieste mai convalidate dal segnalante ----
    @Override
    public int eliminaRichiesteNonConvalidate(int minuti) {
        String query = "DELETE FROM richiesta_soccorso WHERE stato = 'IN_ATTESA' "
                + "AND timestamp_creazione < NOW() - INTERVAL ? MINUTE";
        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query)) {
            statement.setInt(1, minuti);
            return statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore durante la pulizia delle richieste non convalidate", e);
        }
        return 0;
    }

    // ---- archivio richieste: filtro per stato e per intervallo di date, con paginazione ----
    private static final java.util.Set<String> STATI_CONSULTABILI = java.util.Set.of("ATTIVA", "IN_CORSO", "CHIUSA", "IGNORATA");

    // costruisce la parte WHERE (stato + date) e riempie la lista dei parametri nello stesso ordine
    private void costruisciFiltro(StringBuilder where, java.util.List<Object> parametri,
                                  String stato, java.time.LocalDate da, java.time.LocalDate a) {
        where.append(" WHERE r.stato = ?");
        parametri.add(stato);
        if (da != null) {
            where.append(" AND r.timestamp_creazione >= ?");
            parametri.add(java.sql.Timestamp.valueOf(da.atStartOfDay()));
        }
        if (a != null) {
            // "fino al giorno a incluso" = prima della mezzanotte del giorno dopo
            where.append(" AND r.timestamp_creazione < ?");
            parametri.add(java.sql.Timestamp.valueOf(a.plusDays(1).atStartOfDay()));
        }
    }

    @Override
    public int contaRichieste(String stato, java.time.LocalDate da, java.time.LocalDate a) {
        if (stato == null || !STATI_CONSULTABILI.contains(stato)) {
            return 0;
        }
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM richiesta_soccorso r");
        java.util.List<Object> parametri = new ArrayList<>();
        costruisciFiltro(query, parametri, stato, da, a);

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query.toString())) {
            for (int k = 0; k < parametri.size(); k++) {
                statement.setObject(k + 1, parametri.get(k));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore nel conteggio delle richieste", e);
        }
        return 0;
    }

    @Override
    public List<Map<String, String>> cercaRichieste(String stato, java.time.LocalDate da, java.time.LocalDate a,
                                                    int pagina, int perPagina) {
        List<Map<String, String>> risultati = new ArrayList<>();
        if (stato == null || !STATI_CONSULTABILI.contains(stato) || pagina < 1 || perPagina < 1) {
            return risultati;
        }

        StringBuilder query = new StringBuilder(
                "SELECT r.id_richiesta, r.nome_segnalante, r.email_segnalante, r.posizione, r.descrizione, "
                + "r.stato, r.timestamp_creazione, m.id_missione "
                + "FROM richiesta_soccorso r LEFT JOIN missione m ON m.id_richiesta = r.id_richiesta");
        java.util.List<Object> parametri = new ArrayList<>();
        costruisciFiltro(query, parametri, stato, da, a);
        query.append(" ORDER BY r.timestamp_creazione DESC, r.id_richiesta DESC LIMIT ? OFFSET ?");
        parametri.add(perPagina);
        parametri.add((pagina - 1) * perPagina);

        try (Connection connessione = DBManager.getConnection();
             PreparedStatement statement = connessione.prepareStatement(query.toString())) {
            for (int k = 0; k < parametri.size(); k++) {
                statement.setObject(k + 1, parametri.get(k));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, String> riga = new HashMap<>();
                    riga.put("id", String.valueOf(resultSet.getInt("id_richiesta")));
                    riga.put("segnalante", resultSet.getString("nome_segnalante"));
                    riga.put("email", resultSet.getString("email_segnalante"));
                    riga.put("posizione", resultSet.getString("posizione"));
                    riga.put("descrizione", resultSet.getString("descrizione"));
                    riga.put("stato", resultSet.getString("stato"));
                    java.sql.Timestamp data = resultSet.getTimestamp("timestamp_creazione");
                    riga.put("data", data == null ? "" : data.toLocalDateTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    int idMissione = resultSet.getInt("id_missione");
                    if (!resultSet.wasNull()) {
                        riga.put("id_missione", String.valueOf(idMissione));
                    }
                    risultati.add(riga);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "errore nella ricerca delle richieste", e);
        }
        return risultati;
    }

}
