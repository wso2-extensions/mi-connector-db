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

public enum Error {

    CONNECTION_ERROR("701101", "DB:CONNECTIVITY"),
    INVALID_INPUT("701102", "DB:INVALID_INPUT"),
    INVALID_RESPONSE("701103", "DB:INVALID_RESPONSE"),
    QUERY_EXECUTION_FAILURE("701104", "DB:QUERY_EXECUTION_FAILURE"),
    DATA_TYPE_CONVERSION_ERROR("701105", "DB:DATA_TYPE_CONVERSION"),
    CONNECTION_TIMEOUT("701106", "DB:CONNECTION_TIMEOUT"),
    TRANSACTION_ERROR("701107", "DB:TRANSACTION_ERROR"),
    PREPARED_STATEMENT_ERROR("701108", "DB:PREPARED_STATEMENT_ERROR"),
    BATCH_UPDATE_ERROR("701109", "DB:BATCH_UPDATE_ERROR"),
    RESOURCE_CLEANUP_ERROR("701110", "DB:RESOURCE_CLEANUP_ERROR"),
    SQL_SYNTAX_ERROR("701111", "DB:SQL_SYNTAX_ERROR"),
    UNKNOWN_EXCEPTION("701112", "DB:UNKNOWN_EXCEPTION");

    private final String code;
    private final String message;

    /**
     * Create an error code.
     *
     * @param code    error code represented by number
     * @param message error message
     */
    Error(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getErrorCode() {
        return this.code;
    }

    public String getErrorDetail() {
        return this.message;
    }
}
