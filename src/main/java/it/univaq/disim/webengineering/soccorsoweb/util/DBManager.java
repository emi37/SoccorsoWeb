package it.univaq.disim.webengineering.soccorsoweb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    
    //stringa per connettere Net al db
    private static final String URL = "jdbc:mysql://localhost:3306/soccorsoweb_db";
    
    // IMPORTANTISSIMO: password di MySQL Workbench per l'user 'root' è 12345, 
    private static final String USER = "root"; 
    private static final String PASSWORD = "12345"; 

    public static Connection getConnection() throws SQLException {
        try {
            // Carica il driver di MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Errore: driver MySQL non trovato");
            e.printStackTrace();
        }
        
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}   