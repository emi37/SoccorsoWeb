package it.univaq.disim.webengineering.soccorsoweb.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Punto unico di accesso al database.
 *
 * Le connessioni non vengono più aperte con DriverManager a ogni richiesta, ma
 * prelevate da un connection pool gestito dal container (Tomcat) e raggiunto
 * tramite JNDI. La configurazione (URL, utente, password, dimensione del pool)
 * sta in META-INF/context.xml, il riferimento in WEB-INF/web.xml.
 *
 * Chiamare close() su una Connection non la chiude davvero: la restituisce al
 * pool. Va quindi SEMPRE chiusa (try-with-resources), altrimenti il pool si
 * esaurisce.
 */
public class DBManager {

    // nome JNDI della risorsa: "java:comp/env/" + res-ref-name del web.xml
    private static final String JNDI_NAME = "java:comp/env/jdbc/soccorsoweb";

    // il DataSource viene cercato una sola volta, alla prima richiesta
    private static volatile DataSource dataSource;

    private DBManager() {
    }

    private static DataSource getDataSource() throws SQLException {
        DataSource ds = dataSource;
        if (ds == null) {
            synchronized (DBManager.class) {
                ds = dataSource;
                if (ds == null) {
                    try {
                        ds = (DataSource) new InitialContext().lookup(JNDI_NAME);
                    } catch (NamingException e) {
                        throw new SQLException("DataSource '" + JNDI_NAME + "' non trovato: "
                                + "controllare Resource in META-INF/context.xml e resource-ref in WEB-INF/web.xml", e);
                    }
                    dataSource = ds;
                }
            }
        }
        return ds;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
