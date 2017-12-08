package org.zenframework.z8.server.ie;

public enum ImportPolicy {

	/**
	 * Если импортируемое запись существует, не менять
	 */
	KEEP,

	/**
	 * Если импортируемая запись существует, обновить
	 */
	OVERRIDE,
	
	/**
	 * Если импортируемая запись существует, использовать метод <code>DataMessage.merge()</code>
	 */
	MERGE;

	public static final ImportPolicy DEFAULT = KEEP;
}
