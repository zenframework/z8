package org.zenframework.z8.server.db;

import org.zenframework.z8.server.exceptions.db.UnknownDataTypeException;
import org.zenframework.z8.server.types.*;

import java.sql.SQLException;

public class DbUtil {
    static public void addParameter(Statement statement, int posParam, FieldType type, primary value) throws SQLException {
        switch(type) {
        case Guid:
            statement.setGuid(posParam, (guid)value);
            break;
        case Boolean:
            statement.setBoolean(posParam, (bool)value);
            break;
        case Integer:
            statement.setInteger(posParam, (integer)value);
            break;
        case String:
            statement.setString(posParam, (string)value);
            break;
        case Date:
            statement.setDate(posParam, (date)value);
            break;
        case Datetime:
            statement.setDatetime(posParam, (datetime)value);
            break;
        case Datespan:
            statement.setDatespan(posParam, (datespan)value);
            break;
        case Decimal:
            statement.setDecimal(posParam, (decimal)value);
            break;
        case Text:
            string string = value != null ? (string)value : new string();
            statement.setBinary(posParam, new binary(string.getBytes(statement.charset())));
            break;
        case Binary:
            statement.setBinary(posParam, (binary)value);
            break;
        case Null:
            statement.setNull(posParam);
            break;
        default:
            throw new UnknownDataTypeException(type);
        }
    }
}
