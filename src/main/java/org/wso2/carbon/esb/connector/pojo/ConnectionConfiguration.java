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

package org.wso2.carbon.esb.connector.pojo;

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.pool.Configuration;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.esb.connector.exception.InvalidConfigurationException;

/**
 * Configuration parameters used to
 * establish a connection to the file server
 */
public class ConnectionConfiguration {

    private String connectionName;

    private Configuration configuration;

    private int maxFailureRetryCount = 0;
    private long poolConnectionAgedTimeout;

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String driverPath;
    private String protocol;

    public ConnectionConfiguration(MessageContext messageContext) {

        this.configuration = ConnectorUtils.getPoolConfiguration(messageContext);
        // Set default values
        this.configuration.setExhaustedAction("WHEN_EXHAUSTED_BLOCK");
        this.configuration.setTestOnBorrow(true);
        // MYSQL default max connections: 151
        this.configuration.setMaxActiveConnections(150);

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(url)) {
            this.url = url;
        } else {
            throw new InvalidConfigurationException("Mandatory parameter 'url' is not set.");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(username)) {
            this.username = username;
        } else {
            throw new InvalidConfigurationException("Mandatory parameter 'username' is not set.");
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(password)) {
            this.password = password;
        } else {
            throw new InvalidConfigurationException("Mandatory parameter 'password' is not set.");
        }
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(driverClassName)) {
            this.driverClassName = driverClassName;
        } else {
            throw new InvalidConfigurationException("Mandatory parameter 'driverClassName' is not set.");
        }
    }

    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) throws InvalidConfigurationException {
        this.driverPath = driverPath;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(protocol)) {
            this.protocol = protocol;
        } else {
            throw new InvalidConfigurationException("Mandatory parameter 'protocol' is not set.");
        }
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(connectionName)) {
            this.connectionName = connectionName;
        } else {
            throw new InvalidConfigurationException("Mandatory parameter 'connectionName' is not set.");
        }
    }

    public int getMaxFailureRetryCount() {
        return maxFailureRetryCount;
    }

    public void setMaxFailureRetryCount(String maxFailureRetryCount) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(maxFailureRetryCount)) {
            if (!StringUtils.isNumeric(maxFailureRetryCount)) {
                throw new InvalidConfigurationException("Parameter 'maxFailureRetryCount' should be a number.");
            }
            this.maxFailureRetryCount = Integer.parseInt(maxFailureRetryCount);
        }
    }

    public int getMaxActiveConnections() {

        return configuration.getMaxActiveConnections();
    }

    public void setMaxActiveConnections(int maxActiveConnections) {

        this.configuration.setMaxActiveConnections(maxActiveConnections);
    }

    public int getMaxIdleConnections() {

        return configuration.getMaxIdleConnections();
    }

    public void setMaxIdleConnections(int maxIdleConnections) {

        this.configuration.setMaxIdleConnections(maxIdleConnections);
    }

    public long getMaxWaitTime() {

        return configuration.getMaxWaitTime();
    }

    public void setMaxWaitTime(long maxWaitTime) {

        this.configuration.setMaxWaitTime(maxWaitTime);
    }

    public long getMinEvictionTime() {

        return configuration.getMinEvictionTime();
    }

    public void setMinEvictionTime(long minEvictionTime) {

        this.configuration.setMinEvictionTime(minEvictionTime);
    }

    public long getEvictionCheckInterval() {

        return configuration.getEvictionCheckInterval();
    }

    public void setEvictionCheckInterval(long evictionCheckInterval) {

        this.configuration.setEvictionCheckInterval(evictionCheckInterval);
    }

    public String getExhaustedAction() {

        return configuration.getExhaustedAction();
    }

    public void setExhaustedAction(String exhaustedAction) {

        this.configuration.setExhaustedAction(exhaustedAction);
    }

    public Configuration getConfiguration() {

        return configuration;
    }

    public void setConfiguration(Configuration configuration) {

        this.configuration = configuration;
    }

    public long getPoolConnectionAgedTimeout() {
        return poolConnectionAgedTimeout;
    }

    public void setPoolConnectionAgedTimeout(long poolConnectionAgedTimeout) {
        this.configuration.setPoolConnectionAgedTimeout(poolConnectionAgedTimeout);
        this.poolConnectionAgedTimeout = poolConnectionAgedTimeout;
    }

    public int getRetryCount() {
        return configuration.getRetryCount();
    }

    public void setRetryCount(int retryCount) {
        this.configuration.setRetryCount(retryCount);
    }

}
