package org.zenframework.z8.server.exceptions;

import org.zenframework.z8.server.types.exception;

public class RedirectException extends exception {
	private static final long serialVersionUID = -5066765332568537633L;

	public String url;

	public RedirectException(String url) {
		super();
		this.url = url;
	}
}
