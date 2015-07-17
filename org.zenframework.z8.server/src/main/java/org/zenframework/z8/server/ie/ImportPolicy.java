package org.zenframework.z8.server.ie;

public enum ImportPolicy {

    /**
     * Если импортируемая запись существует, не менять
     */
    KEEP(null, null, false),

    /**
     * Если импортируемая запись существует, обновить
     */
    OVERRIDE(null, null, true),

    /**
     * Если импортируемая запись существует, обновить.
     * По умолчанию для записей, на которые ссылается импортируемая запись,
     * применять политику KEEP
     */
    OVERRIDE_AND_KEEP_RELATIONS(OVERRIDE, KEEP, true);

    public static final ImportPolicy DEFAULT = OVERRIDE;

    private final ImportPolicy selfPolicy;
    private final ImportPolicy relationsPolicy;
    private final boolean override;

    private ImportPolicy(ImportPolicy selfPolicy, ImportPolicy relationsPolicy, boolean override) {
        this.selfPolicy = selfPolicy;
        this.relationsPolicy = relationsPolicy;
        this.override = override;
    }

    public ImportPolicy getSelfPolicy() {
        return selfPolicy == null ? this : selfPolicy;
    }

    public ImportPolicy getRelationsPolicy() {
        return relationsPolicy == null ? this : relationsPolicy;
    }
    
    public boolean isOverride() {
        return override;
    }
    
    public static ImportPolicy getPolicy(String policy) {
        return policy == null ? ImportPolicy.DEFAULT : ImportPolicy.valueOf(policy);
    }

}
