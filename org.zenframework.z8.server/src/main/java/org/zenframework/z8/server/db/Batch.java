package org.zenframework.z8.server.db;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.logs.Trace;

public class Batch {
	class Data {
		public int hash;
		public PreparedStatement statement;
		public int count = 0;

		Data(String sql, PreparedStatement statement) {
			hash = sql.hashCode();
			this.statement = statement;
		}
	}

	int totalCount = 0;
	Collection<Data> data = new ArrayList<Data>(100);

	public void register(String sql, PreparedStatement statement) {
		data.add(new Data(sql, statement));
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
