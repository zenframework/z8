package org.zenframework.z8.server.ie;

public enum ImportPolicy {

	/**
	 * Если импортируемое запись существует, не менять
	 */
	Keep,

	/**
	 * Если импортируемая запись существует, обновить
	 */
	Override,
	
	/**
	 * Если импортируемая запись существует, использовать метод <code>DataMessage.onMerge()</code>
	 */
	Merge;

	public static final ImportPolicy Default = Keep;
}
