package dk.industria.solr.processors;

/**
 * Indicate the mode of operation.
 * @see AllowDisallowIndexingProcessor
 */
enum AllowDisallowMode {
    /**
     * Indicates the mode of operation is unknown.
     *
     * No rules will be run and all documents will be indexed.
     */
    UNKNOWN,
    /**
     * Indicates the mode of operation is allow.
     *
     * In allow mode, only documents that matches a rule
     * will be indexed. Everything else will be filtered out.
     */
    ALLOW,
    /**
     * Indicates the mode of operation is disallow. 
     *
     * In disallow mode, all documents will be indexed, 
     * except for those that matches a rule.
     */
    DISALLOW
}

