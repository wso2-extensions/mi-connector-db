package org.wso2.carbon.esb.connector.deploy;

import org.apache.synapse.config.AbstractSynapseObserver;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.libraries.model.Library;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.esb.connector.utils.Constants;

/**
 * Listen for library un deploy events
 * and cleanup connections created by the connector.
 */
public class ConnectorUndeployObserver extends AbstractSynapseObserver {

    private SynapseConfiguration synapseConfiguration;
    private String connectorName = Constants.CONNECTOR_NAME;

    public ConnectorUndeployObserver(SynapseConfiguration synapseConfiguration) {
        this.synapseConfiguration = synapseConfiguration;
    }

    @Override
    public void synapseLibraryRemoved(Library library) {
        String libraryPath = library.getFileName();
        // No need to use File.separator here since libraryPath we are getting from
        // synapse is formatted with "/"
        String libraryName = libraryPath.substring(libraryPath.lastIndexOf("/"));
        if (libraryName.contains(Constants.CONNECTOR_LIBRARY_NAME)
                && library.getPackage().equals(Constants.CONNECTOR_LIBRARY_PACKAGE_TYPE)) {
            if (log.isDebugEnabled()) {
                log.debug("Database Connector is being un deployed. Closing all db connections created.");
            }
            ConnectionHandler.getConnectionHandler().shutdownConnections(Constants.CONNECTOR_NAME);
            synapseConfiguration.unregisterObserver(this);
        }
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConnectorUndeployObserver that = (ConnectorUndeployObserver) o;
        return connectorName.equals(that.connectorName);
    }

    public int hashCode() {
        return connectorName.hashCode();
    }
}
