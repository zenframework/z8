package org.zenframework.z8.server.db;

import java.sql.SQLException;

import org.zenframework.z8.server.exceptions.db.DuplicateKeyFoundException;
import org.zenframework.z8.server.exceptions.db.ObjectAlreadyExistException;
import org.zenframework.z8.server.exceptions.db.ObjectNotFoundException;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.utils.ErrorUtils;

public class SqlExceptionConverter {
    static public void rethrowIfKnown(DatabaseVendor type, SQLException e) throws SQLException {
        rethrow(type, e, false);
    }
    
    static public void rethrow(DatabaseVendor type, SQLException e) throws SQLException {
        rethrow(type, e, true);
    }
    
    static public void rethrow(DatabaseVendor type, SQLException e, boolean rethrowUnknown) throws SQLException {
        switch(type) {
        case Oracle:
            oracle(e);
            break;
        case SqlServer:
            sqlServer(e);
            break;
        case Postgres:
            postgres(e);
            break;
        default:
            throw new UnknownDatabaseException();
        }
        
        if(rethrowUnknown)
            throw e;
    }

    static public boolean isKnownException(DatabaseVendor type, SQLException e) throws SQLException {
        switch(type) {
        case Oracle:
            oracle(e);
            break;
        case SqlServer:
            sqlServer(e);
            break;
        case Postgres:
            return isKnownPostgres(e);
        default:
            throw new UnknownDatabaseException();
        }
        return false;
    }

    static private boolean isKnownPostgres(SQLException e) {
        String state = e.getSQLState();
        return "42P01".equals(state) || "42704".equals(state) || "42P16".equals(state);
    }
    
    static private void postgres(SQLException e) throws RuntimeException {
        String state = e.getSQLState();

        if("42P01".equals(state) || "42704".equals(state))
            throw new ObjectNotFoundException(ErrorUtils.getMessage(e), e.getSQLState(), e.getErrorCode());
        else if("42P16".equals(state))
            throw new ObjectAlreadyExistException(ErrorUtils.getMessage(e), e.getSQLState(), e.getErrorCode());
    }

    static private void oracle(SQLException e) throws RuntimeException {
        switch(e.getErrorCode()) {
        case 1:
        case 2437:
            throw new DuplicateKeyFoundException(ErrorUtils.getMessage(e), e.getSQLState(), e.getErrorCode());

        case 2443:
        case 4080:
        case 1418:
        case 904:
        case 942:
            throw new ObjectNotFoundException(ErrorUtils.getMessage(e), e.getSQLState(), e.getErrorCode());

        case 2275:
        case 955:
        case 2260:
            throw new ObjectAlreadyExistException(ErrorUtils.getMessage(e), e.getSQLState(), e.getErrorCode());
        }
    }

    static private void sqlServer(SQLException e) {
        switch(e.getErrorCode()) {
        case 1913:
        case 1779:
        case 2714:
            throw new ObjectAlreadyExistException(ErrorUtils.getMessage(e), e.getSQLState(), e.getErrorCode());

        case 1088:
        case 3701:
        case 3728:
            throw new ObjectNotFoundException(ErrorUtils.getMessage(e), e.getSQLState(), e.getErrorCode());
        }
    }
}
