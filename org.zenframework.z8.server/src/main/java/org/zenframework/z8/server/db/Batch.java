package org.zenframework.z8.server.db;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.zenframework.z8.server.logs.Trace;

public class Batch {
	private class Data {
		public Statement statement;
		public int count = 0;

		Data(Statement statement) {
			this.statement = statement;
		}

		@Override
		public int hashCode() {
			return statement.hashCode();
		}
	}

	private int totalCount = 0;
	private ArrayList<Data> data = new ArrayList<Data>(100);

	public Statement statement(String sql) {
		Data data = getData(sql);
		return data != null ? data.statement : null;
	}

	public void add(Statement statement) throws SQLException {
		Data data = register(statement);

		statement.addBatch();

		totalCount++;

		if(totalCount % Connection.MaxBatchSize == 0)
			flush();
		else
			data.count++;
	}

	public void commit() throws SQLException {
		try {
			flush();
		} finally {
			close();
		}
	}

	public void rollback() throws SQLException {
		close();
	}

	public void flush() throws SQLException {
		for(Data data : this.data) {
			if(data.count == 0)
				continue;

			try {
				data.count = 0;
				data.statement.executeBatch();
			} catch(BatchUpdateException e) {
				Trace.logError(e);
				throw e.getNextException();
			}
		}
	}

	private Data getData(String sql) {
		int hash = sql.hashCode();

		for(Data data: this.data) {
			if(data.hashCode() == hash)
				return data;
		}

		return null;
	}

	private Data register(Statement statement) {
		Data newData = getData(statement.getSql());

		if(newData != null)
			return newData;

		newData = new Data(statement);
		int priority = statement.getPriority();

		for(int i = 0, size = data.size(); i < size; i++) {
			Data d = data.get(i);
			if(d.statement.getPriority() > priority) {
				data.add(i, newData);
				return newData;
			}
		}

		data.add(newData);
		return newData;
	}

	private void close() {
		for(Data data : this.data)
			data.statement.close();
	}
}