package org.zenframework.z8.server.db;

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

	public Data count(PreparedStatement statement) {
		for(Data data: this.data) {
			if(data.statement == statement)
				return data;
		}

		throw new RuntimeException("Batch.count() - unregistered statement;");
	}

	public void add(PreparedStatement statement) throws SQLException {
		statement.addBatch();

		totalCount++;

		if(totalCount % Connection.MaxBatchSize == 0) {
			for(Data data : this.data) {
				data.count = 0;
				data.statement.executeBatch();
			}
		} else
			count(statement).count++;
	}

	public void flush() throws SQLException {
		for(Data data : this.data) {
			if(data.count != 0)
				data.statement.executeBatch();
			data.statement.close();
		}
		Trace.logEvent("Batch statements: " + data.size());
	}

	public void clear() throws SQLException {
		for(Data data : this.data)
			data.statement.close();
	}
}
