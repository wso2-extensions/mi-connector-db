/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.connector.utils;

public final class Constants {

    public static final String CONNECTOR_NAME = "db";

    public static final String CONNECTION_NAME = "name";
    public static final String PROTOCOL = "connectionType";

    public static final String TENANT_INFO_DOMAIN = "tenant.info.domain";
    public static final String GENERAL_ERROR_MSG = "DB connector encountered an error: ";

    public static final String QUERY_TYPE = "queryType";
    public static final String QUERY_TYPE_OFFLINE = "offline";
    public static final String QUERY_TYPE_ONLINE = "online";

    public static final String QUERY_DATA = "columns";

    // Operation types
    public static final String OPERATION = "operation";
    public static final String OPERATION_SELECT = "SELECT";
    public static final String OPERATION_INSERT = "INSERT";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_BATCH = "BATCH";
    public static final String OPERATION_CALL = "CALL";
    public static final String OPERATION_EXECUTE_QUERY = "EXECUTE_QUERY";

    public static final String DB_CONNECTION = "dbConnection";

    // transaction types
    public static final String BEGIN_TRANSACTION = "BEGIN_TRANSACTION";
    public static final String COMMIT_TRANSACTION = "COMMIT_TRANSACTION";
    public static final String ROLLBACK_TRANSACTION = "ROLLBACK_TRANSACTION";

    // advanced parameters
    public static final String QUERY_TIMEOUT = "queryTimeout";
    public static final String FETCH_SIZE = "fetchSize";
    public static final String MAX_ROWS = "maxRows";
    public static final String TRANSACTION_ISOLATION = "transactionIsolation";
    
    public static final CharSequence CONNECTOR_LIBRARY_NAME = "db-connector";
    public static final String CONNECTOR_LIBRARY_PACKAGE_TYPE = "org.wso2.carbon.esb.connector";

    public static final String CONNECTION_URL = "dbUrl";
    public static final String USERNAME = "dbUser";
    public static final String PASSWORD = "dbPassword";
    public static final String DRIVER_CLASS = "driverClass";
    public static final String DRIVER_PATH = "driverPath";

    public static final String CONNECTION_ID = "connection.id";

    public static final String MAX_FAILURE_RETRY_COUNT = "maxFailureRetryCount";
    public static final String RETRY_COUNT = "retryCount";
    public static final String POOL_CONNECTION_AGED_TIMEOUT = "poolConnectionAgedTimeout";

    // Connection pool configuration
    public static final String MAX_POOL_SIZE = "pool.maxSize";
    public static final String MIN_POOL_SIZE = "pool.minSize";
    public static final String MAX_IDLE_TIME = "pool.maxIdleTime";
    public static final String MAX_LIFETIME = "pool.maxLifetime";

    // Query parameters
    public static final String QUERY = "query";
    public static final String PREPARED_STMT = "preparedStmt";
    public static final String COLUMN_TYPES = "columnTypes";
    public static final String COLUMN_NAMES = "columnNames";

    // execute query parameters
    public static final String PARAMETERS = "parameters";
    public static final String IS_PREPARED_STATEMENT = "isPreparedStatement"; 
    public static final String IS_RESULT_SET = "isResultSet";
    
    public static final String INCLUDE_RESULT_TO = "includeResultTo";
    public static final String RESULT_PROPERTY_NAME = "resultPropertyName";

    public static final String RESPONSE_VARIABLE = "responseVariable";
    public static final String OVERWRITE_BODY = "overwriteBody";
    
    public static final String MESSAGE_BODY = "Message Body";
    public static final String MESSAGE_PROPERTY = "Message Property";

    public static final String PROCEDURE_NAME = "procedure.name";
    public static final String TABLE_NAME = "table.name";
    public static final String COLUMNS = "columns";
    public static final String WHERE_CLAUSE = "where.clause";
    public static final String ORDER_BY = "order.by";
    public static final String GROUP_BY = "group.by";
    public static final String HAVING = "having";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";

    public static final String FORMAT = "format";
    public static final String RESULT = "result";
    public static final String AFFECTED_ROWS = "affected.rows";
    public static final String RESULT_FORMAT = "result.format";
    public static final String FORMAT_JSON = "json";
    public static final String FORMAT_XML = "xml";
    public static final String FORMAT_CSV = "csv";
    public static final String FORMAT_TEXT = "text";

    public static final String JSON_CONTENT_TYPE = "application/json";

    private Constants() {
        
    }
}
