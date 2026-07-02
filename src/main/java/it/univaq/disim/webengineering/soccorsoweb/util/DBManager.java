package it.univaq.disim.webengineering.soccorsoweb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    
    // Stringa di connessione al database che abbiamo appena creato
    private static final String URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    
    // IMPORTANTISSIMO: Se su MySQL Workbench hai impostato una password per 'root', 
    // scrivila tra le virgolette qui sotto. Altrimenti lascia vuoto.
    private static final String USER = "root"; 
    private static final String PASSWORD = "12345"; 

    public static Connection getConnection() throws SQLException {
        try {
            // Carica il driver di MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ERRORE: Driver MySQL non trovato.");
            e.printStackTrace();
        }
        
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}