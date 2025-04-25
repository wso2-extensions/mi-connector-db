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

package org.wso2.carbon.esb.connector.connections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.connector.core.pool.ConnectionFactory;
import org.wso2.carbon.esb.connector.pojo.ConnectionConfiguration;

public class DBConnectionFactory implements ConnectionFactory {
    private ConnectionConfiguration connectionConfiguration;
    private static final Log log = LogFactory.getLog(DBConnectionFactory.class);

    public DBConnectionFactory(ConnectionConfiguration connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    @Override
    public Object makeObject() throws Exception {
        DBHandler dbHandler = new DBHandler(connectionConfiguration);
        return dbHandler;
    }

    @Override
    public void destroyObject(Object connection) throws Exception {
        ((DBHandler) connection).close();
    }

    @Override
    public boolean validateObject(Object connection) {
        try {
            DBHandler DBHandlerConnection = (DBHandler) connection;
            return DBHandlerConnection.validateConnection();
        } catch (Throwable e) {
            log.error("Error while validating the connection", e);
            return false;
        }
    }

    @Override
    public void activateObject(Object o) throws Exception {
        // Nothing to do here
    }

    @Override
    public void passivateObject(Object o) throws Exception {
        // Nothing to do here
    }
}
