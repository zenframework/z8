package org.zenframework.z8.server.db;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.zenframework.z8.server.logs.Trace;

public class Batch {
	private class Data {
		public int hash;
		public PreparedStatement statement;
		public int count = 0;
		public int priority = 0;

		Data(String sql, PreparedStatement statement, int priority) {
			hash = sql.hashCode();
			this.statement = statement;
			this.priority = priority;
		}

		@Override
		public String toString() {
			return statement.toString() + ", priority: " + priority;
		}
	}

	private int totalCount = 0;
	private ArrayList<Data> data = new ArrayList<Data>(100);

	public void register(String sql, PreparedStatement statement, int priority) {
		Data newData = new Data(sql, statement, priority);

		for(int i = 0, size = data.size(); i < size; i++) {
			Data d = data.get(i);
			if(d.priority > priority) {
				data.add(i, newData);
				return;
			}
		}

		data.add(newData);
	}

	public PreparedStatement statement(String sql) {
		int hash = sql.hashCode();

		for(Data data: this.data) {
			if(data.hash == hash)
				return data.statement;
		}

		return null;
	}

	public Data getStatementData(PreparedStatement statement) {
		for(Data data: this.data) {
			if(data.statement == statement)
				return data;
		}

		throw new RuntimeException("Batch.count() - unregistered statement;");
	}

	public void add(PreparedStatement statement) throws SQLException {
		statement.addBatch();

		totalCount++;

		if(totalCount % Connection.MaxBatchSize == 0)
			flush();
		else
			getStatementData(statement).count++;
	}

	public void flush() throws SQLException {
		flush(false);
	}

	public void commit() throws SQLException {
		flush(true);
	}

	public void rollback() throws SQLException {
		for(Data data : this.data)
			data.statement.close();
	}

	private void flush(boolean close) throws SQLException {
		for(Data data : this.data) {
			if(data.count != 0) {
				try {
					data.count = 0;
					data.statement.executeBatch();
				} catch(BatchUpdateException e) {
					Trace.logError(e);
					throw e.getNextException();
				}
			}
			if(close)
				data.statement.close();
		}
	}
}
