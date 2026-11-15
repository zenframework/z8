package org.zenframework.z8.server.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.IDatabase;
import org.zenframework.z8.server.request.IRequest;

public class SelectStatement extends Statement {
	private Cursor cursor = null;

	public SelectStatement(Connection connection, String sql) {
		super(connection, sqlWithTrace(sql), 0);
	}

	private static String sqlWithTrace(String sql) {
		if(!ServerConfig.traceSqlInComment())
			return sql;

		StringBuilder comment = new StringBuilder("/*\n");
		IRequest request = ApplicationServer.getRequest();
		if(request != null) {
			appendCommentValue(comment, request.toString());
			comment.append('\n');
		}

		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for(int i = 2; i < stack.length; i++)
			comment.append("  at ").append(stack[i].toString().replace("/*", "/ *").replace("*/", "* /")).append('\n');
		return comment.append("*/\n").append(sql).toString();
	}

	private static void appendCommentValue(StringBuilder comment, String value) {
		if(value == null)
			return;

		StringBuilder safeValue = new StringBuilder(value.length());
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			safeValue.append(Character.isISOControl(c) || c == '\u2028' || c == '\u2029' ? ' ' : c);
		}
		comment.append(safeValue.toString().replace("/*", "/ *").replace("*/", "* /"));
	}

	@Override
	protected PreparedStatement createPreparedStatement(Connection connection, String sql) throws SQLException {
		return  connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	protected void prepare() throws SQLException {
	}

	public Cursor execute() throws SQLException {
		if(cursor != null)
			cursor.close();
		return cursor = new Cursor(this);
	}

	@Override
	public void close() {
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}
		super.close();
	}

	public static Cursor cursor(String sql) throws SQLException {
		return cursor(ConnectionManager.get(), sql);
	}

	public static Cursor cursor(IDatabase database, String sql) throws SQLException {
		return cursor(ConnectionManager.get(database), sql);
	}

	public static Cursor cursor(Connection connection, String sql) throws SQLException {
		SelectStatement select = new SelectStatement(connection, sql);
		return select.execute();
	}
}
