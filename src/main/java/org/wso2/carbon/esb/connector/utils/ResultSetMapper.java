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

package org.wso2.carbon.esb.connector.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import javax.xml.namespace.QName;

/**
 * Utility class to convert ResultSet to different formats like JSON, XML, CSV, and Text.
 */
public class ResultSetMapper {
    private static final Logger log = LoggerFactory.getLogger(ResultSetMapper.class);
    private static final int INITIAL_STRING_BUILDER_CAPACITY = 1024;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Object mapToFormat(ResultSet rs, String format) throws Exception {
        if (rs == null) {
            log.warn("Null ResultSet provided for conversion");
            return null;
        }

        try {
            switch (format.toLowerCase()) {
                case Constants.FORMAT_JSON:
                    return convertToJSON(rs);
                case Constants.FORMAT_XML:
                    return convertToXML(rs);
                case Constants.FORMAT_CSV:
                    return convertToCSV(rs);
                case Constants.FORMAT_TEXT:
                    return convertToText(rs);
                default:
                    log.warn("Unsupported format: {}. Using JSON as default.", format);
                    return convertToJSON(rs);
            }
        } catch (SQLException e) {
            throw new Exception("SQL error while converting ResultSet to " + format + " format", e);
        } catch (Exception e) {
            throw new Exception("Error while converting ResultSet to " + format + " format", e);
        }
    }

    private Object convertToJSON(ResultSet rs) throws SQLException {
        ArrayNode jsonArray = objectMapper.createArrayNode();

        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        String[] columnNames = new String[columnCount];

        // cache colmn names
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metadata.getColumnLabel(i + 1);
        }

        while (rs.next()) {
            ObjectNode jsonRow = objectMapper.createObjectNode();
            for (int i = 0; i < columnCount; i++) {
                String columnName = columnNames[i];
                Object value = rs.getObject(i + 1);

                if (value == null) {
                    jsonRow.putNull(columnName);
                } else if (value instanceof Number) {
                    if (value instanceof Integer) {
                        jsonRow.put(columnName, (Integer) value);
                    } else if (value instanceof Long) {
                        jsonRow.put(columnName, (Long) value);
                    } else if (value instanceof Double || value instanceof Float) {
                        jsonRow.put(columnName, ((Number) value).doubleValue());
                    } else {
                        jsonRow.put(columnName, value.toString());
                    }
                } else if (value instanceof Boolean) {
                    jsonRow.put(columnName, (Boolean) value);
                } else if (value instanceof Date) {
                    jsonRow.put(columnName, ((Date) value).getTime());
                } else {
                    jsonRow.put(columnName, value.toString());
                }
            }
            jsonArray.add(jsonRow);
        }

        return jsonArray;
    }

    private Object convertToXML(ResultSet rs) throws SQLException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement rootElement = factory.createOMElement(new QName("rows"));

        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        String[] columnNames = new String[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metadata.getColumnLabel(i + 1);
        }

        while (rs.next()) {
            OMElement rowElement = factory.createOMElement(new QName("row"));
            rootElement.addChild(rowElement);

            for (int i = 0; i < columnCount; i++) {
                OMElement colElement = factory.createOMElement(new QName(columnNames[i]));
                Object value = rs.getObject(i + 1);
                if (value != null) {
                    colElement.setText(value.toString());
                }
                rowElement.addChild(colElement);
            }
        }

        return rootElement;
    }

    private Object convertToCSV(ResultSet rs) throws SQLException {
        try {
            StringWriter writer = new StringWriter(INITIAL_STRING_BUILDER_CAPACITY);
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();

            // Prepare header
            String[] headers = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                headers[i] = metadata.getColumnLabel(i + 1);
            }

            // load class csvPrinter
            Class.forName("org.apache.commons.csv.CSVPrinter");
    
            // CSVPrinter csvPrinter = CSVFormat.DEFAULT.withHeader(headers).print(writer);
            try (CSVPrinter csvPrinter = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
                // Add data rows
                while (rs.next()) {
                    Object[] rowData = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        rowData[i] = rs.getObject(i + 1);
                    }
                    csvPrinter.printRecord(rowData);
                }
                csvPrinter.flush();
            }

            return writer.toString();
        } catch (Exception e) {
            log.error("Error converting to CSV: {}", e.getMessage(), e);
            throw new SQLException("Failed to convert to CSV format", e);
        }
    }

    private Object convertToText(ResultSet rs) throws SQLException {
        StringBuilder textBuilder = new StringBuilder(INITIAL_STRING_BUILDER_CAPACITY);

        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();

        // get column names and determine column widths
        String[] columnNames = new String[columnCount];
        int[] columnWidths = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metadata.getColumnLabel(i + 1);
            columnWidths[i] = columnNames[i].length();
        }

        // get column widths
        rs.beforeFirst(); // Reset cursor if supported
        while (rs.next()) {
            for (int i = 0; i < columnCount; i++) {
                Object value = rs.getObject(i + 1);
                int valueLength = value == null ? 4 : value.toString().length(); // "null" length is 4
                if (valueLength > columnWidths[i]) {
                    columnWidths[i] = valueLength;
                }
            }
        }

        rs.beforeFirst();

        // print header
        for (int i = 0; i < columnCount; i++) {
            textBuilder.append(padRight(columnNames[i], columnWidths[i]));
            if (i < columnCount - 1) {
                textBuilder.append(" | ");
            }
        }
        textBuilder.append('\n');

        // print separator
        for (int i = 0; i < columnCount; i++) {
            textBuilder.append("-".repeat(columnWidths[i]));
            if (i < columnCount - 1) {
                textBuilder.append("-+-");
            }
        }
        textBuilder.append('\n');

        // print data
        while (rs.next()) {
            for (int i = 0; i < columnCount; i++) {
                Object value = rs.getObject(i + 1);
                String displayValue = value == null ? "" : value.toString();
                textBuilder.append(padRight(displayValue, columnWidths[i]));
                if (i < columnCount - 1) {
                    textBuilder.append(" | ");
                }
            }
            textBuilder.append('\n');
        }

        return textBuilder.toString();
    }

    private String padRight(String s, int width) {
        if (s == null) {
            s = "";
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String getTargetFormat(MessageContext messageContext) {
        Object format = ConnectorUtils.lookupTemplateParamater(messageContext, Constants.FORMAT);
        return format != null ? format.toString() : Constants.FORMAT_JSON;
    }
}