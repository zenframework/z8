package org.zenframework.z8.server.router;

import java.util.Map;

public interface IRoute {
	
	/**
	 * 
	 * @return
	 */
	public String getPath();
	
	/**
	 * 
	 * @param path
	 */
	public void setPath(String path);
	
	/**
	 * 
	 * @return
	 */
	public String[] getMethods();
	
	/**
	 * 
	 * @param methods
	 */
	public void setMethods(String[] methods);
	
	/**
	 * 
	 * @param method
	 * @return
	 */
	public boolean hasMethod(String method);
	
	/**
	 * 
	 * @return
	 */
	public Map<String, String> getOptions();
	
	/**
	 * 
	 * @param options
	 */
	public void setOptions(Map<String, String> options);
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void addOption(String key, String value);
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasOption(String key);
}
