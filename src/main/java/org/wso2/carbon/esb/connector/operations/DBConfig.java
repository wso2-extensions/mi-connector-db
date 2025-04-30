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

package org.wso2.carbon.esb.connector.operations;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;

import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.esb.connector.deploy.ConnectorUndeployObserver;
import org.wso2.carbon.esb.connector.exception.InvalidConfigurationException;
import org.wso2.carbon.esb.connector.pojo.ConnectionConfiguration;
import org.wso2.carbon.esb.connector.utils.Constants;
import org.wso2.carbon.esb.connector.utils.Utils;
import org.wso2.carbon.esb.connector.utils.Error;
import org.wso2.carbon.esb.connector.connections.DBConnectionFactory;
import org.wso2.carbon.esb.connector.connections.DBHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConfig extends AbstractConnector implements ManagedLifecycle {

    private static final Logger log = LoggerFactory.getLogger(DBConfig.class);

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        SynapseConfiguration synapseConfig = synapseEnvironment.getSynapseConfiguration();
        synapseConfig.registerObserver(new ConnectorUndeployObserver(synapseConfig));
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Destroy method of Config init is not supposed to be called");
    }

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {

        log.debug("Initializing database connector");
        String connectionName = (String) getParameter(messageContext, Constants.CONNECTION_NAME);
        String tenantSpecificConnectionName = Utils.getTenantSpecificConnectionName(connectionName, messageContext);

        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            ConnectionConfiguration configuration = getConnectionConfigFromContext(messageContext);

            // check if there is a connection in the message context
            Object connectionObj = messageContext.getProperty(Constants.DB_CONNECTION + "_" + connectionName);
            if (connectionObj != null && connectionObj instanceof DBHandler) {
                log.debug("Connection found in message context: " + connectionObj);
                return;
            }

            try {
                handler.createConnection(Constants.CONNECTOR_NAME, tenantSpecificConnectionName,
                        new DBConnectionFactory(configuration), configuration.getConfiguration(), messageContext);
            } catch (NoSuchMethodError e) {
                handler.createConnection(Constants.CONNECTOR_NAME, tenantSpecificConnectionName,
                        new DBConnectionFactory(configuration), configuration.getConfiguration());
            }

            log.debug("Connection created: " + tenantSpecificConnectionName);

        } catch (Exception e) {
            handleError(messageContext, e, Error.CONNECTION_ERROR, "Error initializing database connector");
        }
    }

    private ConnectionConfiguration getConnectionConfigFromContext(MessageContext msgContext)
            throws InvalidConfigurationException {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(msgContext);

        connectionConfig.setConnectionName((String) getParameter(msgContext, Constants.CONNECTION_NAME));
        connectionConfig.setUrl((String) getParameter(msgContext, Constants.CONNECTION_URL));
        connectionConfig.setUsername((String) getParameter(msgContext, Constants.USERNAME));
        connectionConfig.setPassword((String) getParameter(msgContext, Constants.PASSWORD));
        connectionConfig.setDriverClassName((String) getParameter(msgContext, Constants.DRIVER_CLASS));
        connectionConfig.setDriverPath((String) getParameter(msgContext, Constants.DRIVER_PATH));
        connectionConfig.setProtocol((String) getParameter(msgContext, Constants.PROTOCOL));
        connectionConfig.setMaxFailureRetryCount((String) getParameter(msgContext, Constants.MAX_FAILURE_RETRY_COUNT));

        if (msgContext.getProperty(Constants.POOL_CONNECTION_AGED_TIMEOUT) != null) {
            try {
                connectionConfig.setPoolConnectionAgedTimeout(
                        Long.parseLong((String) getParameter(msgContext, Constants.POOL_CONNECTION_AGED_TIMEOUT)));
            } catch (NumberFormatException e) {
                handleError(msgContext, e, Error.INVALID_INPUT, "Invalid value for poolConnectionAgedTimeout");
            }
        } else {
            connectionConfig.setPoolConnectionAgedTimeout(30 * 60 * 1000L);
        }

        connectionConfig.setConfiguration(ConnectorUtils.getPoolConfiguration(msgContext));
        return connectionConfig;
    }

    private void handleError(MessageContext msgCtx, Exception e, Error error, String errorDetail) {
        Utils.setErrorPropertiesToMessage(msgCtx, e, error);
        handleException(Constants.GENERAL_ERROR_MSG + errorDetail, e, msgCtx);
    }

}
