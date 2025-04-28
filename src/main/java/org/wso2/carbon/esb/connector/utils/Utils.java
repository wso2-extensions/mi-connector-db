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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.JsonObject;
import org.apache.axis2.AxisFault;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.data.connector.ConnectorResponse;
import org.apache.synapse.data.connector.DefaultConnectorResponse;
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
import java.util.Map;

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

    public static void setErrorPropertiesToMessage(MessageContext messageContext, Error error) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, error.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, error.getErrorDetail());
        // Axis2MessageContext axis2smc = (Axis2MessageContext) messageContext;
        // org.apache.axis2.context.MessageContext axis2MessageCtx =
        // axis2smc.getAxis2MessageContext();
        // axis2MessageCtx.setProperty(SynapseConstants.STATUS_CODE,
        // ResponseConstants.HTTP_STATUS_500);
    }

    private static String getStackTrace(Throwable throwable) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

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

    public static void handleConnectorResponse(MessageContext messageContext, String responseVariable,
            Boolean overwriteBody, JsonObject payload, Map<String, Object> attributes) {

        ConnectorResponse response = new DefaultConnectorResponse();
        if (overwriteBody != null && overwriteBody) {
            org.apache.axis2.context.MessageContext axisMsgCtx = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            String jsonString = payload.toString();
            try {
                JsonUtil.getNewJsonPayload(axisMsgCtx, jsonString, true, true);
            } catch (AxisFault e) {
                throw new RuntimeException("Error while setting JSON payload", e);
            }
            axisMsgCtx.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                    Constants.JSON_CONTENT_TYPE);
            axisMsgCtx.setProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE,
                    Constants.JSON_CONTENT_TYPE);
        } else {
            response.setPayload(payload);
            response.setAttributes(attributes);
            messageContext.setVariable(responseVariable, response);
        }
    }

    public JsonObject generateOperationResult(MessageContext msgContext, boolean resultStatus, Error error) {
        JsonObject jsonResult = new JsonObject();

        jsonResult.addProperty("success", resultStatus);

        if (error != null) {
            setErrorPropertiesToMessage(msgContext, error);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("code", error.getErrorCode());
            errorJson.addProperty("message", error.getErrorDetail());
            jsonResult.add("error", errorJson);
        }

        return jsonResult;
    }

    public static JsonObject generateErrorResult(MessageContext messageContext, Exception exception, Error error) {
        JsonObject jsonResult = new JsonObject();

        if (error != null) {
            setErrorPropertiesToMessage(messageContext, exception, error);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("code", error.getErrorCode());
            errorJson.addProperty("message", error.getErrorDetail());
            jsonResult.add("error", errorJson);
        }

        return jsonResult;
    }

}
