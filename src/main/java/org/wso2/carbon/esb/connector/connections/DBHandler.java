package org.wso2.carbon.esb.connector.connections;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;

import org.wso2.carbon.connector.core.connection.ConnectionConfig;
import org.wso2.carbon.connector.core.connection.Connection;
import org.wso2.carbon.esb.connector.pojo.ConnectionConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBHandler implements Connection {

    private Connection connection;
    private java.sql.Connection conn;
    private boolean transactionStarted = false;
    private ConnectionConfiguration config;

    private static final Logger log = LoggerFactory.getLogger(DBHandler.class);

    /**
     * Create a new DBHandler object. This will contain a new JDBC Connection
     * configured with the provided settings.
     */
    public DBHandler(ConnectionConfiguration connectionConfiguration) throws SQLException {
        try {
            config = connectionConfiguration;
            conn = getNewConnection(connectionConfiguration);
            log.debug("Connection created successfully: " + conn);

        } catch (SQLSyntaxErrorException e) {
            log.error("SQL syntax error: " + e.getMessage());
            throw new SQLException("SQL syntax error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Class not found: " + e.getMessage());
            throw new SQLException("Class not found: " + e.getMessage());
        } catch (SQLTimeoutException e) {
            log.error("Query timeout: " + e.getMessage());
            throw new SQLException("Query timeout: " + e.getMessage());
        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage());
            throw new SQLException("SQL error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error executing query: " + e.getMessage());
            throw new SQLException("Error executing query: " + e.getMessage());
        }
    }

    public static java.sql.Connection getNewConnection(ConnectionConfiguration config)
            throws SQLException, ClassNotFoundException, Exception {
        java.sql.Connection connection = null;

        // Class.forName("com.mysql.cj.jdbc.Driver");
        Class.forName(config.getDriverClassName());
        log.info("Loading database driver: " + config.getDriverClassName());
        connection = DriverManager.getConnection(config.getUrl(), config.getUsername(),
                config.getPassword());

        return connection;
    }

    // transaction handling take isolation level as argument
    public void startTransaction(String isolationLevel) throws SQLException {
        if (transactionStarted) {
            throw new SQLException(
                    "Transaction already started. Cannot start another transaction without committing or rolling back the current one.");
        }

        try {
            conn.setAutoCommit(false);
            switch (isolationLevel) {
                case "TRANSACTION_NONE":
                    conn.setTransactionIsolation(java.sql.Connection.TRANSACTION_NONE);
                    break;
                case "TRANSACTION_READ_UNCOMMITTED":
                    conn.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
                    break;
                case "TRANSACTION_READ_COMMITTED":
                    conn.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
                    break;
                case "TRANSACTION_REPEATABLE_READ":
                    conn.setTransactionIsolation(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
                    break;
                case "TRANSACTION_SERIALIZABLE":
                    conn.setTransactionIsolation(java.sql.Connection.TRANSACTION_SERIALIZABLE);
                    break;
                default:
                    throw new SQLException("Invalid isolation level: " + isolationLevel);
            }

            transactionStarted = true;
            log.info("Transaction started with isolation level: " + conn.getTransactionIsolation());
        } catch (SQLException e) {
            log.error("Error starting transaction: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException re) {
                log.error("Error rolling back transaction: " + re.getMessage());
            }
            throw new SQLException("Failed to start transaction: " + e.getMessage());
        }
    }

    public void commitTransaction() throws SQLException {
        if (!transactionStarted) {
            throw new SQLException("Transaction not started.");
        }

        try {
            conn.commit();
            transactionStarted = false;
            log.info("Transaction committed.");
        } catch (SQLException e) {
            log.error("Error committing transaction: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException re) {
                log.error("Error rolling back transaction: " + re.getMessage());
            }
            throw new SQLException("Failed to commit transaction: " + e.getMessage());
        }
    }

    public void rollbackTransaction() throws SQLException {
        if (!transactionStarted) {
            throw new SQLException("Transaction not started.");
        }

        try {
            conn.rollback();
            transactionStarted = false;
            log.info("Transaction rolled back.");
        } catch (SQLException e) {
            log.error("Error rolling back transaction: " + e.getMessage());
            throw new SQLException("Failed to rollback transaction: " + e.getMessage());
        }
    }

    public ConnectionConfiguration getConfig() {
        return config;
    }

    public Connection getConnection() {
        return connection;
    }

    // validateConnection
    public boolean validateConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                log.debug("Connection is valid.");
                return true;
            } else {
                log.error("Connection is not valid.");
                return false;
            }
        } catch (SQLException e) {
            log.error("Error validating connection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the JDBC Connection object configured for the connection.
     * 
     * @return JDBC Connection
     */
    public java.sql.Connection getJDBCConnection() {
        return conn;
    }

    public boolean isTransactionStarted() {
        return transactionStarted;
    }

    @Override
    public void connect(ConnectionConfig config) {
        throw new UnsupportedOperationException("Connection is already established.");
    }

    @Override
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                log.info("Closing connection");

                // if transaction is still open, rollback
                if (transactionStarted) {
                    log.info("Rolling back transaction");
                    conn.rollback();
                    transactionStarted = false;
                }

                conn.close();
            }
        } catch (SQLException e) {
            log.error("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}