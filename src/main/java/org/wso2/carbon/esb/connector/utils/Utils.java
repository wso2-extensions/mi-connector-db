package org.wso2.carbon.esb.connector.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.esb.connector.exception.InvalidConfigurationException;

import java.io.Writer;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLTransientException;

import java.sql.SQLWarning;

public class Utils {

    public static String getConnectionName(MessageContext messageContext) throws ConnectException {
        String connectionName = (String) messageContext.getProperty(Constants.CONNECTION_NAME);
        if (connectionName == null)
            throw new ConnectException("Connection name is not set");

        return getTenantSpecificConnectionName(connectionName, messageContext);
    }

    public static String getTenantSpecificConnectionName(String connectionName, MessageContext messageContext) {
        Object tenantDomain = messageContext.getProperty(Constants.TENANT_INFO_DOMAIN);
        if (tenantDomain != null)
            return String.format("%s@%s", connectionName, tenantDomain);

        return connectionName;
    }

    public static void setErrorPropertiesToMessage(MessageContext messageContext, Exception exception, Error error) {

        messageContext.setProperty(SynapseConstants.ERROR_CODE, error.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, exception.getMessage());
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, getStackTrace(exception));
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, exception);
    }

    private static String getStackTrace(Throwable throwable) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

    // CONNECTION_ERROR("701101", "DB:CONNECTIVITY"),
    // INVALID_INPUT("701102", "DB:INVALID_INPUT"),
    // INVALID_RESPONSE("701103", "DB:INVALID_RESPONSE"),
    // QUERY_EXECUTION_FAILURE("701104", "DB:QUERY_EXECUTION_FAILURE"),
    // DATA_TYPE_CONVERSION_ERROR("701105", "DB:DATA_TYPE_CONVERSION"),
    // CONNECTION_TIMEOUT("701106", "DB:CONNECTION_TIMEOUT"),
    // TRANSACTION_ERROR("701107", "DB:TRANSACTION_ERROR"),
    // PREPARED_STATEMENT_ERROR("701108", "DB:PREPARED_STATEMENT_ERROR"),
    // BATCH_UPDATE_ERROR("701109", "DB:BATCH_UPDATE_ERROR"),
    // RESOURCE_CLEANUP_ERROR("701110", "DB:RESOURCE_CLEANUP_ERROR"),
    // UNKNOWN_EXCEPTION("701111", "DB:UNKNOWN_EXCEPTION");

    public static Error getErrorCode(Exception exception) {

        if (exception instanceof ConnectException) {
            return Error.CONNECTION_ERROR;
        } else if (exception instanceof SQLException) {
            return Error.QUERY_EXECUTION_FAILURE;
        } else if (exception instanceof ClassNotFoundException) {
            return Error.CONNECTION_ERROR;
        } else if (exception instanceof NullPointerException) {
            return Error.INVALID_INPUT;
        } else if (exception instanceof IllegalArgumentException) {
            return Error.INVALID_INPUT;
        } else if (exception instanceof InvalidConfigurationException) {
            return Error.INVALID_INPUT;
        } else if (exception instanceof ArrayIndexOutOfBoundsException) {
            return Error.INVALID_INPUT;
        } else if (exception instanceof NumberFormatException) {
            return Error.DATA_TYPE_CONVERSION_ERROR;
        } else if (exception instanceof SQLTimeoutException) {
            return Error.CONNECTION_TIMEOUT;
        } else if (exception instanceof BatchUpdateException) {
            return Error.BATCH_UPDATE_ERROR;
        } else if (exception instanceof SQLTransactionRollbackException) {
            return Error.TRANSACTION_ERROR;
        } else if (exception instanceof SQLSyntaxErrorException) {
            return Error.QUERY_EXECUTION_FAILURE;
        } else if (exception instanceof SQLIntegrityConstraintViolationException) {
            return Error.QUERY_EXECUTION_FAILURE;
        } else if (exception instanceof SQLDataException) {
            return Error.DATA_TYPE_CONVERSION_ERROR;
        } else if (exception instanceof SQLFeatureNotSupportedException) {
            return Error.INVALID_INPUT;
        } else if (exception instanceof SQLNonTransientConnectionException) {
            return Error.CONNECTION_ERROR;
        } else if (exception instanceof SQLTransientConnectionException) {
            return Error.CONNECTION_TIMEOUT;
        } else if (exception instanceof SQLRecoverableException) {
            return Error.CONNECTION_ERROR;
        } else if (exception instanceof SQLInvalidAuthorizationSpecException) {
            return Error.INVALID_INPUT;
        } else if (exception instanceof SQLClientInfoException) {
            return Error.INVALID_INPUT;
        } else if (exception instanceof SQLNonTransientException) {
            return Error.QUERY_EXECUTION_FAILURE;
        } else if (exception instanceof SQLTransientException) {
            return Error.QUERY_EXECUTION_FAILURE;
        } else if (exception instanceof SQLWarning) {
            return Error.UNKNOWN_EXCEPTION;
        } else if (exception instanceof SQLException) {
            return Error.QUERY_EXECUTION_FAILURE;
        }

        return Error.UNKNOWN_EXCEPTION;
    }

    public static void setResultAsPayload(MessageContext msgContext, String operation, Object result) throws AxisFault {
        OMElement resultElement = generateOperationResult(msgContext, operation, result);
        if (resultElement == null) {
            throw new AxisFault("Unable to generate result element from the result object.");
        }

        SOAPBody soapBody = msgContext.getEnvelope().getBody();
        // Detaching first element (soapBody.getFirstElement().detach()) will be done by
        // following method anyway.
        JsonUtil.removeJsonPayload(((Axis2MessageContext) msgContext).getAxis2MessageContext());
        ((Axis2MessageContext) msgContext).getAxis2MessageContext().removeProperty(PassThroughConstants.NO_ENTITY_BODY);
        soapBody.addChild(resultElement);
    }

    /**
     * Generates an OMElement representation for the given result object
     * 
     * @param msgContext The message context
     * @param operation  The operation name for context
     * @param result     The result object to be converted to an OMElement
     * @return An OMElement representing the result. Returns an empty <result/>
     *         element if the input result is null and not an OMElement
     */
    public static OMElement generateOperationResult(MessageContext msgContext, String operation, Object result) {
        // return if already an OMElement
        if (result instanceof OMElement) {
            return (OMElement) result;
        }

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("", "");
        OMElement resultElement = factory.createOMElement("result", ns);
        resultElement.addAttribute("operation", operation, null);
        if (result != null) {
            resultElement.setText(result.toString());

        }

        return resultElement;
    }
}
