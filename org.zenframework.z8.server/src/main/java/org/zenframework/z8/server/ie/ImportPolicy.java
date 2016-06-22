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
     * Если импортируемая запись существует, агрегировать текущее и новое значения поля
     */
    AGGREGATE;

    public static final ImportPolicy DEFAULT = KEEP;

    public static ImportPolicy getPolicy(String policy, ImportPolicy defaultPolicy) {
        return policy == null || policy.isEmpty() ? defaultPolicy : ImportPolicy.valueOf(policy);
    }

    public static ImportPolicy getPolicy(String policy) {
        return getPolicy(policy, ImportPolicy.DEFAULT);
    }

}
