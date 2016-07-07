package org.zenframework.z8.server.base.simple;

import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.exception;

public class Transaction extends OBJECT {
    public static class CLASS<T extends Transaction> extends OBJECT.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Transaction.class);
        }

        @Override
        public Object newObject(IObject container) {
            return new Transaction(container);
        }
    }

    public Transaction(IObject container) {
        super(container);
    }

    public void z8_run() {
    	Connection connection = ConnectionManager.get();
    	
    	connection.beginTransaction();
    	
    	try {
    		z8_transaction();
    		connection.commit();
    	} catch(Throwable e) {
    		connection.rollback();
    		throw new exception(e);
    	}
    }

    protected void z8_transaction() {}
}
