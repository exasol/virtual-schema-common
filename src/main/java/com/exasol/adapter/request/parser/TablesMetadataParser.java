package com.exasol.adapter.request.parser;

import com.exasol.adapter.metadata.ColumnMetadata;
import com.exasol.adapter.metadata.DataType;
import com.exasol.adapter.metadata.MetadataException;
import com.exasol.adapter.metadata.TableMetadata;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class TablesMetadataParser {
    private static final String INVOLVED_TABLES = "involvedTables";
    private static final String DATA_TYPE = "dataType";

    public List<TableMetadata> parse(final JsonObject tablesAsJson) throws MetadataException {
        final List<TableMetadata> tables = new ArrayList<>();
        final JsonArray jsonArray = tablesAsJson.getJsonArray(INVOLVED_TABLES);
        for (final JsonObject table : jsonArray.getValuesAs(JsonObject.class)) {
            final String tableName = table.getString("name", "");
            final String tableAdapterNotes = readAdapterNotes(table);
            final String tableComment = table.getString("comment", "");
            final List<ColumnMetadata> columns = new ArrayList<>();
            for (final JsonObject column : table.getJsonArray("columns").getValuesAs(JsonObject.class)) {
                columns.add(parseColumnMetadata(column));
            }
            tables.add(new TableMetadata(tableName, tableAdapterNotes, columns, tableComment));
        }
        return tables;
    }

    private ColumnMetadata parseColumnMetadata(final JsonObject column) {
        final String columnName = column.getString("name");
        final String adapterNotes = readAdapterNotes(column);
        final String comment = column.getString("comment", "");
        final String defaultValue = column.getString("default", "");
        final boolean isNullable = checkIfBooleanValueExists(column, "isNullable");
        final boolean isIdentity = checkIfBooleanValueExists(column, "isIdentity");
        final JsonObject dataType = column.getJsonObject(DATA_TYPE);
        final DataType type = getDataType(dataType);
        return new ColumnMetadata(columnName, adapterNotes, type, isNullable, isIdentity, defaultValue, comment);
    }

    private String readAdapterNotes(final JsonObject root) {
        if (root.containsKey("adapterNotes")) {
            final JsonValue notes = root.get("adapterNotes");
            if (notes.getValueType() == JsonValue.ValueType.STRING) {
                return ((JsonString) notes).getString();
            } else {
                return notes.toString();
            }
        }
        return "";
    }

    private boolean checkIfBooleanValueExists(final JsonObject column, final String bolleanName) {
        if (column.containsKey(bolleanName)) {
            return column.getBoolean(bolleanName);
        }
        return true;
    }

    private DataType getDataType(final JsonObject dataType) {
        final String typeName = dataType.getString("type").toUpperCase();
        final DataType type;
        switch (typeName) {
        case "DECIMAL":
            type = getDecimalDataType(dataType);
            break;
        case "DOUBLE":
            type = getDoubleDataType();
            break;
        case "VARCHAR":
            type = getVarcharDataType(dataType);
            break;
        case "CHAR":
            type = getCharDataType(dataType);
            break;
        case "BOOLEAN":
            type = getBooleanDataType();
            break;
        case "DATE":
            type = getDateDataType();
            break;
        case "TIMESTAMP":
            type = getTimestampDataType(dataType);
            break;
        case "INTERVAL":
            type = getIntervalDataType(dataType);
            break;
        case "GEOMETRY":
            type = getGeometryDataType(dataType);
            break;
        default:
            throw new RequestParserException("Unsupported data type encountered: " + typeName);
        }
        return type;
    }

    private DataType getGeometryDataType(final JsonObject dataType) {
        final int srid = dataType.getInt("srid");
        return DataType.createGeometry(srid);
    }

    private DataType getIntervalDataType(final JsonObject dataType) {
        final int precision = dataType.getInt("precision", 2);
        final DataType.IntervalType intervalType = intervalTypeFromString(dataType.getString("fromTo"));
        if (intervalType == DataType.IntervalType.DAY_TO_SECOND) {
            final int fraction = dataType.getInt("fraction", 3);
            return DataType.createIntervalDaySecond(precision, fraction);
        } else {
            return DataType.createIntervalYearMonth(precision);
        }
    }

    private DataType getTimestampDataType(final JsonObject dataType) {
        final boolean withLocalTimezone = dataType.getBoolean("withLocalTimeZone", false);
        return DataType.createTimestamp(withLocalTimezone);
    }

    private DataType getDateDataType() {
        return DataType.createDate();
    }

    private DataType getBooleanDataType() {
        return DataType.createBool();
    }

    private DataType getCharDataType(final JsonObject dataType) {
        final String charSet = dataType.getString("characterSet", "UTF8");
        return DataType.createChar(dataType.getInt("size"), charSetFromString(charSet));
    }

    private DataType getVarcharDataType(final JsonObject dataType) {
        final String charSet = dataType.getString("characterSet", "UTF8");
        return DataType.createVarChar(dataType.getInt("size"), charSetFromString(charSet));
    }

    private DataType getDoubleDataType() {
        return DataType.createDouble();
    }

    private DataType getDecimalDataType(final JsonObject dataType) {
        return DataType.createDecimal(dataType.getInt("precision"), dataType.getInt("scale"));
    }

    private static DataType.ExaCharset charSetFromString(final String charset) {
        if (charset.equals("UTF8")) {
            return DataType.ExaCharset.UTF8;
        } else if (charset.equals("ASCII")) {
            return DataType.ExaCharset.ASCII;
        } else {
            throw new RequestParserException("Unsupported charset encountered: " + charset);
        }
    }

    private static DataType.IntervalType intervalTypeFromString(final String intervalType) {
        if (intervalType.equals("DAY TO SECONDS")) {
            return DataType.IntervalType.DAY_TO_SECOND;
        } else if (intervalType.equals("YEAR TO MONTH")) {
            return DataType.IntervalType.YEAR_TO_MONTH;
        } else {
            throw new RequestParserException("Unsupported interval data type encountered: " + intervalType);
        }
    }
}
