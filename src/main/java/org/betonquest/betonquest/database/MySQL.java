package org.betonquest.betonquest.database;

import lombok.CustomLog;
import org.betonquest.betonquest.BetonQuest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connects to and uses a MySQL database
 */
@SuppressWarnings("PMD.CommentRequired")
@CustomLog
public class MySQL extends Database {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;

    /**
     * Creates a new MySQL instance
     *
     * @param plugin   Plugin instance
     * @param hostname Name of the host
     * @param port     Port number
     * @param database Database name
     * @param username Username
     * @param password Password
     */
    public MySQL(final BetonQuest plugin, final String hostname, final String port, final String database, final String username, final String password) {
        super(plugin);
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    @Override
    public Connection openConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database + "?&useSSL=false", this.user, this.password);
        } catch (ClassNotFoundException | SQLException e) {
            LOG.warn("MySQL says: " + e.getMessage(), e);
        }
        return connection;
    }
}
