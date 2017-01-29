package org.zenframework.z8.pde.build;

import java.io.PrintWriter;

public class RepositoryLocalChangesException extends Exception {
	private static final long serialVersionUID = -1209934995472605559L;

	private String m_errorMsg;

	public RepositoryLocalChangesException() {
		m_errorMsg = "� �������� ������� ������� � ���������� �����������!";
	}

	@Override
	public void printStackTrace(PrintWriter printWriter) {
		printWriter.println("RepositoryLocalChangesException");
	}

	@Override
	public String getMessage() {
		return m_errorMsg;
	}

}
