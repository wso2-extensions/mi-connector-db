/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.connector.operations;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.InlineExpressionUtil;
import org.apache.synapse.util.xpath.SynapseExpression;

import org.jaxen.JaxenException;
import org.json.JSONArray;
import org.json.JSONException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.esb.connector.connections.DBHandler;
import org.wso2.carbon.esb.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.esb.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.esb.connector.utils.Constants;
import org.wso2.carbon.esb.connector.utils.Error;
import org.wso2.carbon.esb.connector.utils.ResultSetMapper;
import org.wso2.carbon.esb.connector.utils.Statement;
import org.wso2.carbon.esb.connector.utils.Utils;

public class ExecuteQuery extends AbstractConnector {
    private static final Logger log = LoggerFactory.getLogger(ExecuteQuery.class);

    private String operation = "";
    private String queryTimeout = "";
    private String fetchSize = "";
    private String maxRows = "";
    private String resultPropertyName = "";
    private String columns = "";

    // select specific
    private String limit = "";
    private String offset = "";

    // execute query specific
    private String parameters = "";

    private boolean isNewTransaction = false; // if a new transaction is started
    private boolean isNewConnection = false; // if a new connection is created
    private boolean isOngoingTransaction = false; // if a transaction is already in progress

    private final Map<String, SynapseExpression> inlineExpressionCache = new ConcurrentHashMap<>();

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(String queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public String getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(String fetchSize) {
        this.fetchSize = fetchSize;
    }

    public void setMaxRows(String maxRows) {
        this.maxRows = maxRows;
    }

    public String getMaxRows() {
        return maxRows;
    }

    public String getResultPropertyName() {
        return resultPropertyName;
    }

    public void setResultPropertyName(String resultPropertyName) {
        this.resultPropertyName = resultPropertyName;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {

        String query = (String) getParameter(messageContext, Constants.QUERY);
        String preparedStmt = (String) getParameter(messageContext, Constants.PREPARED_STMT);
        String columnTypes = (String) getParameter(messageContext, Constants.COLUMN_TYPES);
        String columnNames = (String) getParameter(messageContext, Constants.COLUMN_NAMES);

        String includeResultTo = (String) getParameter(messageContext, Constants.INCLUDE_RESULT_TO);
        String resultPropertyName = (String) getParameter(messageContext, Constants.RESULT_PROPERTY_NAME);

        String queryTimeout = (String) getParameter(messageContext, Constants.QUERY_TIMEOUT);
        String fetchSize = (String) getParameter(messageContext, Constants.FETCH_SIZE);
        String maxRows = (String) getParameter(messageContext, Constants.MAX_ROWS);
        String transactionIsolation = (String) getParameter(messageContext, Constants.TRANSACTION_ISOLATION);

        String queryType = (String) getParameter(messageContext, Constants.QUERY_TYPE);
        String queryData = (String) getParameter(messageContext, Constants.QUERY_DATA);

        // select specific
        String limit = (String) getParameter(messageContext, Constants.LIMIT);
        String offset = (String) getParameter(messageContext, Constants.OFFSET);

        // execute query specific
        String parameters = (String) getParameter(messageContext, Constants.PARAMETERS);
        String isPreparedStatement = (String) getParameter(messageContext, Constants.IS_PREPARED_STATEMENT);
        String isResultSet = (String) getParameter(messageContext, Constants.IS_RESULT_SET);

        Connection conn = null;
        DBHandler dbHandlerConnection = null;
        ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
        String connectionName = Utils.getConnectionName(messageContext);

        boolean isSelect = false;
        boolean querySuccess = false;

        // TODO handle mutiple queries in a single stetmen

        try {
            dbHandlerConnection = prepareTransactionEnvironment(messageContext, handler, connectionName,
                    transactionIsolation);
            conn = dbHandlerConnection.getJDBCConnection();

            switch (operation) {
                case Constants.OPERATION_SELECT:
                    isSelect = true;
                    break;
                case Constants.OPERATION_INSERT:
                case Constants.OPERATION_UPDATE:
                case Constants.OPERATION_DELETE:
                case Constants.OPERATION_CALL:
                    break;
                case Constants.OPERATION_EXECUTE_QUERY:
                    isSelect = Boolean.parseBoolean(isResultSet);
                    break;
                case Constants.BEGIN_TRANSACTION:
                    String isolationLevel = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                            "isolationLevel");
                    if (isolationLevel == null || isolationLevel.isEmpty()) {
                        isolationLevel = "TRANSACTION_NONE";
                    }

                    dbHandlerConnection.startTransaction(isolationLevel);
                    querySuccess = true;
                    return;

                case Constants.COMMIT_TRANSACTION:
                    dbHandlerConnection.commitTransaction();
                    querySuccess = true;
                    return;

                case Constants.ROLLBACK_TRANSACTION:
                    dbHandlerConnection.rollbackTransaction();
                    querySuccess = true;
                    return;

                default:
                    throw new InvalidConfigurationException("Operation not supported: " + operation);
            }

            // at least one of query or preparedStmt should be set
            if (StringUtils.isEmpty(query) && StringUtils.isEmpty(preparedStmt)) {
                throw new InvalidConfigurationException("Query or prepared statement is required");
            }

            // if operation is execute query and isPreparedStatement is set to true, then
            // preparedStmt = query
            if (operation.equalsIgnoreCase(Constants.OPERATION_EXECUTE_QUERY)
                    && Boolean.parseBoolean(isPreparedStatement)) {
                preparedStmt = query;
            }

            Statement stmnt = null;
            try {
                if (StringUtils.isEmpty(preparedStmt)) {
                    // query = InlineExpressionUtil.replaceDynamicValues(messageContext, query);
                    query = processExpression(query, messageContext);
                    stmnt = new Statement(query);
                } else {
                    stmnt = new Statement(preparedStmt);

                    // if offline query, get data from the param manager
                    if (!StringUtils.isEmpty(queryType) && queryType.equalsIgnoreCase(Constants.QUERY_TYPE_OFFLINE)) {

                        if (StringUtils.isEmpty(queryData)) {
                            throw new InvalidConfigurationException("Query data is required for offline query");
                        }
                        processOfflineQuery(messageContext, stmnt, queryData);

                    } else if (operation.equalsIgnoreCase(Constants.OPERATION_EXECUTE_QUERY)) {

                        if (StringUtils.isEmpty(parameters)) {
                            throw new InvalidConfigurationException("Parameters are required for execute query");
                        }
                        processExecuteQueryParameters(messageContext, stmnt, parameters);

                    } else {

                        String[] columnNamesArray = null, columnTypesArray = null;
                        if (!StringUtils.isEmpty(columnNames) && !StringUtils.isEmpty(columnTypes)) {
                            columnNamesArray = columnNames.split(",");
                            columnTypesArray = columnTypes.split(",");

                            if (columnNamesArray.length != columnTypesArray.length) {
                                throw new InvalidConfigurationException(
                                        "Number of column names and types do not match");
                            }
                        }

                        processColumnParameters(messageContext, stmnt, columnNamesArray, columnTypesArray);
                    }

                    // if a select query add parameters limit and offset if set
                    if (operation.equalsIgnoreCase(Constants.OPERATION_SELECT)) {
                        if (!StringUtils.isEmpty(limit)) {
                            stmnt.addParameter("INTEGER", limit);
                        }

                        if (!StringUtils.isEmpty(offset)) {
                            stmnt.addParameter("INTEGER", offset);
                        }
                    }

                }
            } catch (StringIndexOutOfBoundsException e) {
                handleError(messageContext, e, Error.INVALID_INPUT,
                        "Malformed value string in message context: " + e.getMessage());
            } catch (JSONException e) {
                handleError(messageContext, e, Error.INVALID_INPUT,
                        "Malformed JSON in query data: " + e.getMessage());
            } catch (Exception e) {
                handleError(messageContext, e, Error.INVALID_INPUT,
                        "Error processing statement parameters: " + e.getMessage());
            }

            Object result = null;
            try (PreparedStatement ps = getPreparedStatement(conn, stmnt)) {

                // Set query parameters
                // ps.setMaxRows(Integer.parseInt(maxRows));
                setIntProperty(ps, "setQueryTimeout", queryTimeout);
                setIntProperty(ps, "setFetchSize", fetchSize);
                setIntProperty(ps, "setMaxRows", maxRows);

                if (isSelect) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.isBeforeFirst()) {
                            log.info("No results found for query: " + query);
                        } else {
                            ResultSetMapper resultSetMapper = new ResultSetMapper();
                            result = resultSetMapper.mapToFormat(rs, ResultSetMapper.getTargetFormat(messageContext));
                            log.info("Result: " + result);
                        }
                    }
                } else {
                    int affected = ps.executeUpdate();
                    result = affected;
                    log.info("Affected rows: " + affected);

                    // if insert get the last inserted id
                    if (operation.equalsIgnoreCase(Constants.OPERATION_INSERT)) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                result = rs.getLong(1);
                                log.info("Last inserted id: " + result);
                            }
                        }
                    }
                }
            }

            if (!StringUtils.isEmpty(includeResultTo)) {
                if (includeResultTo.equalsIgnoreCase(Constants.MESSAGE_BODY)) {
                    Utils.setResultAsPayload(messageContext, operation, result);
                } else if (includeResultTo.equalsIgnoreCase(Constants.MESSAGE_PROPERTY)) {
                    // messageContext.setProperty(resultPropertyName, result);
                    OMElement resultEle = Utils.generateOperationResult(messageContext, operation, result);
                    messageContext.setProperty(resultPropertyName, resultEle);

                    log.info("Result:" + resultEle.toString());
                } else {
                    throw new InvalidConfigurationException("Invalid includeResultTo value: " + includeResultTo);
                }
            }

            querySuccess = true;

        } catch (SQLSyntaxErrorException e) {
            handleError(messageContext, e, Error.SQL_SYNTAX_ERROR, "Error executing query: " + e.getMessage());
        } catch (SQLTimeoutException e) {
            handleError(messageContext, e, Error.CONNECTION_TIMEOUT, "Query execution timed out: " + e.getMessage());
        } catch (SQLException e) {

            String errorMsg = String.format("Database error executing query: %s. Error code: %d, SQL State: %s", query,
                    e.getErrorCode(), e.getSQLState());
            handleError(messageContext, e, Error.QUERY_EXECUTION_FAILURE, errorMsg);
        } catch (Exception e) {
            handleError(messageContext, e, Utils.getErrorCode(e), "Error executing query: " + e.getMessage());
        } finally {

            try {
                if (querySuccess) {
                    log.info("Query Successful");
                    if (isNewTransaction) {
                        dbHandlerConnection.commitTransaction();
                    }
                } else {
                    log.info("Query Failed");
                    if (isNewTransaction || isOngoingTransaction) {
                        log.info("Rolling back transaction");
                        dbHandlerConnection.rollbackTransaction();
                    }

                    // if isNewConnection, rollback any transactions in the initial connection
                    if (isNewConnection) {
                        log.info("Rolling back the initial connection");
                        DBHandler oldConnection = (DBHandler) handler.getConnection(Constants.CONNECTOR_NAME,
                                connectionName);
                        if (oldConnection != null) {
                            if (oldConnection.isTransactionStarted()) {
                                oldConnection.rollbackTransaction();
                            }

                            // return the connection to connection pool
                            if (handler.getStatusOfConnection(Constants.CONNECTOR_NAME, connectionName)) {
                                handler.returnConnection(Constants.CONNECTOR_NAME, connectionName, oldConnection);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                handleError(messageContext, e, Utils.getErrorCode(e), "Error cleaning up: " + e.getMessage());
            }

            if (isNewConnection) {
                dbHandlerConnection.close();
            } else if (dbHandlerConnection != null) {

                // if there is an active transaction do not return the connection to the pool
                if (dbHandlerConnection.isTransactionStarted()) {
                    log.info("Transaction is still active, not returning connection to pool");

                    // instead of returning to the pool, set the connection to the message context
                    // write the connection to the message context with the connectionname
                    messageContext.setProperty(Constants.DB_CONNECTION + "_" + connectionName,
                            dbHandlerConnection);

                } else if (handler.getStatusOfConnection(Constants.CONNECTOR_NAME, connectionName)) {
                    handler.returnConnection(Constants.CONNECTOR_NAME, connectionName, dbHandlerConnection);
                    log.info("Returning connection to pool");
                }

            }
        }
    }

    /**
     * Sets an integer property on the PreparedStatement using reflection.
     *
     * @param ps         The PreparedStatement object.
     * @param methodName The name of the method to invoke (e.g., "setQueryTimeout").
     * @param value      The value to set as a string.
     * @throws SQLException If an error occurs while setting the property.
     */
    private void setIntProperty(PreparedStatement ps, String methodName, String value) throws SQLException {
        if (StringUtils.isNotEmpty(value)) {
            try {
                int intValue = Integer.parseInt(value);
                ps.getClass().getMethod(methodName, int.class).invoke(ps, intValue);
            } catch (NumberFormatException e) {
                handleError(null, e, Error.INVALID_INPUT,
                        "Invalid value for " + methodName + ": " + value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                handleError(null, e, Error.INVALID_INPUT,
                        "Error invoking method " + methodName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Creates a PreparedStatement from the given connection and statement.
     * 
     * @param conn The JDBC connection.
     * @param stmt The statement object containing the SQL query and parameters.
     */
    private PreparedStatement getPreparedStatement(Connection conn, Statement stmt) throws SQLException, Exception {
        PreparedStatement ps = conn.prepareStatement(stmt.getQuery());
        List<Statement.Parameter> parameters = stmt.getParameters();

        int columnNum = 1;
        for (Statement.Parameter param : parameters) {
            if (param == null) {
                continue;
            }

            String value = param.getValue();

            switch (param.getType()) {
                // according to J2SE 1.5 /docs/guide/jdbc/getstart/mapping.html
                // https://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR: {
                    if (value != null && value.length() != 0) {
                        ps.setString(columnNum, value);
                    } else {
                        ps.setString(columnNum, null);
                    }
                    break;
                }
                case Types.NUMERIC:
                case Types.DECIMAL: {
                    if (value != null && value.length() != 0) {
                        ps.setBigDecimal(columnNum, new BigDecimal(value));
                    } else {
                        ps.setBigDecimal(columnNum, null);
                    }
                    break;
                }
                case Types.BIT: {
                    if (value != null && value.length() != 0) {
                        ps.setBoolean(columnNum, Boolean.parseBoolean(value));
                    } else {
                        ps.setNull(columnNum, Types.BIT);
                    }
                    break;
                }
                case Types.TINYINT: {
                    if (value != null && value.length() != 0) {
                        ps.setByte(columnNum, Byte.parseByte(value));
                    } else {
                        ps.setNull(columnNum, Types.TINYINT);
                    }
                    break;
                }
                case Types.SMALLINT: {
                    if (value != null && value.length() != 0) {
                        ps.setShort(columnNum, Short.parseShort(value));
                    } else {
                        ps.setNull(columnNum, Types.SMALLINT);
                    }
                    break;
                }
                case Types.INTEGER: {
                    if (value != null && value.length() != 0) {
                        ps.setInt(columnNum, Integer.parseInt(value));
                    } else {
                        ps.setNull(columnNum, Types.INTEGER);
                    }
                    break;
                }
                case Types.BIGINT: {
                    if (value != null && value.length() != 0) {
                        ps.setLong(columnNum, Long.parseLong(value));
                    } else {
                        ps.setNull(columnNum, Types.BIGINT);
                    }
                    break;
                }
                case Types.REAL: {
                    if (value != null && value.length() != 0) {
                        ps.setFloat(columnNum, Float.parseFloat(value));
                    } else {
                        ps.setNull(columnNum, Types.REAL);
                    }
                    break;
                }
                case Types.FLOAT: {
                    if (value != null && value.length() != 0) {
                        ps.setDouble(columnNum, Double.parseDouble(value));
                    } else {
                        ps.setNull(columnNum, Types.FLOAT);
                    }
                    break;
                }
                case Types.DOUBLE: {
                    if (value != null && value.length() != 0) {
                        ps.setDouble(columnNum, Double.parseDouble(value));
                    } else {
                        ps.setNull(columnNum, Types.DOUBLE);
                    }
                    break;
                }
                // skip BINARY, VARBINARY and LONGVARBINARY
                case Types.DATE: {
                    if (value != null && value.length() != 0) {
                        ps.setDate(columnNum, Date.valueOf(value));
                    } else {
                        ps.setNull(columnNum, Types.DATE);
                    }
                    break;
                }
                case Types.TIME: {
                    if (value != null && value.length() != 0) {
                        ps.setTime(columnNum, Time.valueOf(value));
                    } else {
                        ps.setNull(columnNum, Types.TIME);
                    }
                    break;
                }
                case Types.TIMESTAMP: {
                    if (value != null && value.length() != 0) {
                        ps.setTimestamp(columnNum, Timestamp.valueOf(value));
                    } else {
                        ps.setNull(columnNum, Types.TIMESTAMP);
                    }
                    break;
                }
                // skip CLOB, BLOB, ARRAY, DISTINCT, STRUCT, REF, JAVA_OBJECT
                default: {
                    String msg = "Trying to set an unsupported JDBC Type : " + param.getType() +
                            " against column : " + columnNum + " and statement : " +
                            stmt.getQuery() +
                            " used by a DB connector (see java.sql.Types for valid type values)";
                    throw new SQLException(msg);
                }
            }

            columnNum++;
        }
        return ps;
    }

    /**
     * Extract paramters from the param manager values
     * matching with the columnnames and types list
     */
    private void processColumnParameters(MessageContext messageContext, Statement stmnt,
            String[] columnNamesArray, String[] columnTypesArray)
            throws InvalidConfigurationException {

        if (columnNamesArray == null || columnTypesArray == null) {
            return;
        }

        for (int i = 0; i < columnNamesArray.length; i++) {
            String key = operation.toLowerCase() + ":" + columnNamesArray[i].trim();
            Object propertyValue = messageContext.getProperty(key);

            if (propertyValue == null) {
                throw new InvalidConfigurationException(
                        "Missing value for column: " + columnNamesArray[i].trim());
            }

            // key: insert:EmployeeNumber Value: Value {name ='null', keyValue ='30'}
            // insert:EmployeeNumber Value: Value {name ='null', expression =:id}

            String valueString = propertyValue.toString();
            String value = null;
            if (valueString.contains("keyValue =")) {
                value = valueString.substring(valueString.indexOf("keyValue ='") + 11,
                        valueString.lastIndexOf("'"));
            } else if (valueString.contains("expression =")) {
                value = valueString.substring(valueString.indexOf("expression =") + 12,
                        valueString.lastIndexOf("}"));
                value = processExpression("${" + value + "}", messageContext);

            } else {
                throw new InvalidConfigurationException(
                        "Invalid value format for column: " + columnNamesArray[i].trim());
            }

            stmnt.addParameter(columnTypesArray[i].trim(), value);
            // log.info("Column name: {} column value: {} column type: {}", columnNamesArray[i].trim(), value,
            //         columnTypesArray[i].trim());
        }
    }

    /**
     * Extract parameters from the param manager for offline query
     */
    private void processOfflineQuery(MessageContext messageContext, Statement stmnt, String queryData)
            throws InvalidConfigurationException, JSONException {
        queryData = processExpression(queryData, messageContext);
        JSONArray columnsArray = new JSONArray(queryData);
        if (columnsArray == null || columnsArray.length() == 0) {
            handleError(messageContext,
                    new InvalidConfigurationException("Query data is empty"),
                    Error.INVALID_INPUT, "Query data is empty");
        }

        for (int i = 0; i < columnsArray.length(); i++) {
            JSONArray columnItem = columnsArray.getJSONArray(i);
            if (columnItem.length() == 3) {
                String columnName = columnItem.getString(0).trim();
                String columnValue = columnItem.getString(1).trim();
                String columnType = columnItem.getString(2).trim();

                stmnt.addParameter(columnType, columnValue);
                // log.info("Column name: {} column value: {} column type: {}", columnName, columnValue,
                //         columnType);
            }
        }

    }

    /**
     * Extract parameters from the param manager
     */
    private void processExecuteQueryParameters(MessageContext messageContext, Statement stmnt,
            String parameters) throws InvalidConfigurationException, JSONException {
        parameters = processExpression(parameters, messageContext);
        JSONArray columnsArray = new JSONArray(parameters);
        if (columnsArray == null || columnsArray.length() == 0) {
            handleError(messageContext,
                    new InvalidConfigurationException("Query data is empty"),
                    Error.INVALID_INPUT, "Query data is empty");
        }

        for (int i = 0; i < columnsArray.length(); i++) {
            JSONArray columnItem = columnsArray.getJSONArray(i);
            if (columnItem.length() == 2) {
                String columnValue = columnItem.getString(0).trim();
                String columnType = columnItem.getString(1).trim();

                stmnt.addParameter(columnType, columnValue);
                log.info("Column value: {} column type: {}", columnValue, columnType);
            }
        }

    }

    /**
     * Processes the given expression using InlineExpressionUtil
     */
    private String processExpression(String expression, MessageContext synCtx) {
        String result = null;
        try {
            result = InlineExpressionUtil.processInLineSynapseExpressionTemplate(synCtx, expression,
                    inlineExpressionCache);
            log.debug("Evaluated expression: " + result);

        } catch (JaxenException e) {
            handleError(synCtx, e, Error.INVALID_INPUT, "Error processing expression: " + e.getMessage());
        }
        return result;
    }

    /**
     * Prepares the transaction environment by creating or reusing a connection and starting a transaction if needed.
     */
    private DBHandler prepareTransactionEnvironment(MessageContext messageContext, ConnectionHandler handler,
            String connectionName, String transactionIsolation) throws SQLException, ConnectException {

        DBHandler dbHandlerConnection = null;
        Connection conn = null;

        // check if there is a connection in the message context
        Object connectionObj = messageContext.getProperty(Constants.DB_CONNECTION + "_" + connectionName);
        if (connectionObj != null && connectionObj instanceof DBHandler) {
            messageContext.setProperty(Constants.DB_CONNECTION + "_" + connectionName, null);

            log.info("Using connection from message context");
            conn = ((DBHandler) connectionObj).getJDBCConnection();
            if (conn != null && !conn.isClosed()) {
                dbHandlerConnection = (DBHandler) connectionObj;
            } else {
                dbHandlerConnection = new DBHandler(((DBHandler) connectionObj).getConfig());
                conn = dbHandlerConnection.getJDBCConnection();
            }
        } else {
            // check if there is a connection in the connection pool
            dbHandlerConnection = (DBHandler) handler.getConnection(Constants.CONNECTOR_NAME, connectionName);
            conn = dbHandlerConnection.getJDBCConnection();

            // check if connection is valid else create a new connection
            if (conn == null || conn.isClosed()) {
                dbHandlerConnection = new DBHandler(dbHandlerConnection.getConfig());
                conn = dbHandlerConnection.getJDBCConnection();
            }
        }

        // check if transactionIsolation is set to something other than TRANSACTION_NONE
        if (!StringUtils.isEmpty(transactionIsolation)
                && !transactionIsolation.equalsIgnoreCase("TRANSACTION_NONE")) {

            // if there is already a transaction in progress, create a new connection
            if (dbHandlerConnection.isTransactionStarted()) {

                ConnectionConfiguration newConfig = dbHandlerConnection.getConfig();

                // return the current connection
                if (dbHandlerConnection != null
                        && handler.getStatusOfConnection(Constants.CONNECTOR_NAME, connectionName)) {
                    messageContext.setProperty(Constants.DB_CONNECTION + "_" + connectionName,
                            dbHandlerConnection);

                    // handler.returnConnection(Constants.CONNECTOR_NAME, connectionName,
                    // dbHandlerConnection);
                }

                dbHandlerConnection = new DBHandler(newConfig);
                // conn = dbHandlerConnection.getJDBCConnection();
                isNewConnection = true;

                log.info("Creating new connection for transaction isolation level: " + transactionIsolation);
            }

            dbHandlerConnection.startTransaction(transactionIsolation);
            isNewTransaction = true;
        }

        isOngoingTransaction = dbHandlerConnection.isTransactionStarted();

        return dbHandlerConnection;
    }

    private void handleError(MessageContext msgCtx, Exception e, Error error, String errorDetail) {
        Utils.setErrorPropertiesToMessage(msgCtx, e, error);
        handleException(Constants.GENERAL_ERROR_MSG + errorDetail, e, msgCtx);
    }

}
