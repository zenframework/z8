package org.zenframework.z8.server.ie;

public enum ImportPolicy {

    /**
     * Если импортируемая запись существует, не менять
     */
    KEEP(false),

    /**
     * Если импортируемая запись существует, обновить
     */
    OVERRIDE(true);

    public static final ImportPolicy DEFAULT = KEEP;

    private final boolean override;

    private ImportPolicy(boolean override) {
        this.override = override;
    }

    public boolean isOverride() {
        return override;
    }
    
    public static ImportPolicy getPolicy(String policy) {
        return policy == null ? ImportPolicy.DEFAULT : ImportPolicy.valueOf(policy);
    }

}
