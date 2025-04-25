package org.wso2.carbon.esb.connector.utils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.synapse.SynapseException;

public class Statement {

    private String sqlString = null;
    private final List<Parameter> parameters = new ArrayList<Parameter>();
    private final Map<String, String> resultsMap = new HashMap<String, String>();

    public Statement(String sqlString) {
        this.sqlString = sqlString;
    }

    public void addParameter(String type, String value) {
        parameters.add(new Parameter(type, value));
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getSqlString() {
        return sqlString;
    }

    public void addResult(String key, String value) {
        resultsMap.put(key, value);
    }

    public Map<String, String> getResultsMap() {
        return resultsMap;
    }

    public static class Parameter {
        // private String name;
        private int type;
        private String value;

        Parameter(String type, String value) {
            this.value = value;

            // match java.sql.Types to the type string
            switch (type) {
                case "CHAR":
                    this.type = Types.CHAR;
                    break;
                case "VARCHAR":
                    this.type = Types.VARCHAR;
                    break;
                case "LONGVARCHAR":
                    this.type = Types.LONGVARCHAR;
                    break;
                case "NUMERIC":
                    this.type = Types.NUMERIC;
                    break;
                case "DECIMAL":
                    this.type = Types.DECIMAL;
                    break;
                case "BIT":
                    this.type = Types.BIT;
                    break;
                case "TINYINT":
                    this.type = Types.TINYINT;
                    break;
                case "SMALLINT":
                    this.type = Types.SMALLINT;
                    break;
                case "INT":
                case "INTEGER":
                    this.type = Types.INTEGER;
                    break;
                case "BIGINT":
                    this.type = Types.BIGINT;
                    break;
                case "REAL":
                    this.type = Types.REAL;
                    break;
                case "FLOAT":
                    this.type = Types.FLOAT;
                    break;
                case "DOUBLE":
                    this.type = Types.DOUBLE;
                    break;
                case "DATE":
                    this.type = Types.DATE;
                    break;
                case "TIME":
                    this.type = Types.TIME;
                    break;
                case "TIMESTAMP":
                    this.type = Types.TIMESTAMP;
                    break;
                case "BINARY":
                    this.type = Types.BINARY;
                    break;
                default:
                    throw new SynapseException("Unknown or unsupported JDBC type : " + type);
            }
        }

        public int getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    public String getQuery() {
        return sqlString;
    }

}