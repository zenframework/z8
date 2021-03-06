package org.zenframework.z8.webserver2;

import java.util.Objects;

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.servlet.Source;
import org.eclipse.jetty.util.ArrayUtil;

public class Z8ServletHandler extends ServletHandler {

	/**
	 * Convenience method to add a servlet.
	 *
	 * @param className the class name
	 * @param pathSpec the path spec
	 * @return The servlet holder.
	 */
	public ServletHolder addServletWithMapping(String className, String... pathSpecs)
	{
		ServletHolder holder = newServletHolder(Source.EMBEDDED);
		holder.setClassName(className);
		addServletWithMapping(holder, pathSpecs);
		return holder;
	}

	/**
	 * Convenience method to add a servlet.
	 *
	 * @param servlet the servlet class
	 * @param pathSpec the path spec
	 * @return The servlet holder.
	 */
	public ServletHolder addServletWithMapping(Class<? extends Servlet> servlet, String... pathSpecs)
	{
		ServletHolder holder = newServletHolder(Source.EMBEDDED);
		holder.setHeldClass(servlet);
		addServletWithMapping(holder, pathSpecs);

		return holder;
	}

	/**
	 * Convenience method to add a servlet.
	 *
	 * @param servlet servlet holder to add
	 * @param pathSpec servlet mappings for the servletHolder
	 */
	public void addServletWithMapping(ServletHolder servlet, String... pathSpecs) {
		Objects.requireNonNull(servlet);
		ServletHolder[] holders = getServlets();
		try {
			synchronized (this) {
				if (!containsServletHolder(servlet))
					setServlets(ArrayUtil.addToArray(holders, servlet, ServletHolder.class));
			}

			ServletMapping mapping = new ServletMapping();
			mapping.setServletName(servlet.getName());
			mapping.setPathSpecs(pathSpecs);
			setServletMappings(ArrayUtil.addToArray(getServletMappings(), mapping, ServletMapping.class));
		} catch (RuntimeException e) {
			setServlets(holders);
			throw e;
		}
	}

}
