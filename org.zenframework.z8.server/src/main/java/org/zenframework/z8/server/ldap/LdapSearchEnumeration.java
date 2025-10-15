package org.zenframework.z8.server.ldap;

import org.zenframework.z8.server.logs.Trace;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.Control;
import java.io.IOException;
import java.util.NoSuchElementException;

public class LdapSearchEnumeration implements NamingEnumeration<SearchResult> {
	private final LdapContext context;
	private final String searchBase;
	private final String searchFilter;
	private final int pageSize;
	private final SearchControls controls;

	private NamingEnumeration<SearchResult> currentPage = null;
	private byte[] cookie = null;
	private boolean notReadYet = true;
	private boolean closed = false;

	public LdapSearchEnumeration(LdapContext context, String searchBase, String searchFilter, int pageSize, SearchControls controls) {
		this.context = context;
		this.searchBase = searchBase;
		this.searchFilter = searchFilter;
		this.pageSize = pageSize;
		this.controls = controls;
	}

	@Override
	public boolean hasMore() throws NamingException {
		if (canReadPage() && (currentPage == null || !currentPage.hasMore()))
			loadNextPage();

		return currentPage != null && currentPage.hasMore();
	}

	private boolean canReadPage() {
		return !closed && (notReadYet || !isCookieEmpty());
	}

	private boolean isCookieEmpty() {
		return cookie == null || cookie.length == 0;
	}

	private void loadNextPage() {
		closeCurrentPage();
		try {
			context.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });
			currentPage = context.search(searchBase, searchFilter, controls);
		} catch (IOException | NamingException e) {
			throw new RuntimeException("LDAP paged search failed for base: " + searchBase + ", filter: " + searchFilter, e);
		} finally {
			notReadYet = false;
		}
	}

	private void closeCurrentPage() {
		try {
			if (currentPage != null)
				currentPage.close();
		} catch (NamingException e) {
			Trace.logEvent("Failed to close LDAP search results: " + e.getMessage());
		} finally {
			currentPage = null;
		}
	}

	private SearchResult findNextElement() {
		try {
			SearchResult result = currentPage.next();

			if(!currentPage.hasMore())
				updateCookieAfterPageConsumed();

			return result;
		} catch (NamingException e) {
			throw new RuntimeException("LDAP search iteration failed", e);
		}
	}

	private void updateCookieAfterPageConsumed() throws NamingException {
		cookie = null;
		Control[] responseControls = context.getResponseControls();
		if (responseControls != null) {
			for (Control control : responseControls) {
				if (control instanceof PagedResultsResponseControl) {
					cookie = ((PagedResultsResponseControl) control).getCookie();
					break;
				}
			}
		}
	}

	@Override
	public SearchResult next() {
		if (!hasMoreElements())
			throw new NoSuchElementException("No more elements available");

		return findNextElement();
	}

	@Override
	public boolean hasMoreElements() {
		try {
			return hasMore();
		} catch (NamingException e) {
			Trace.logEvent("LDAP hasMoreElements failed: " + e.getMessage());
			return false;
		}
	}

	@Override
	public SearchResult nextElement() {
		return next();
	}

	@Override
	public void close() throws NamingException {
		closed = true;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			closeCurrentPage();
		} finally {
			super.finalize();
		}
	}
}
