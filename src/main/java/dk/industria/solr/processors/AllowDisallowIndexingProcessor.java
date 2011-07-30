package dk.industria.solr.processors;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

/**
 * <p>Implements an UpdateRequestProcessor for filtering documents based on allow/disallow rules
 * matching regular expressions in field values.
 * <p/>
 * For more information @see AllowDisallowIndexingProcessorFactory
 */
class AllowDisallowIndexingProcessor extends UpdateRequestProcessor {
    /**
     * Logger
     * UpdateRequestProcessor has it's own log variable tied to the UpdateRequestProcessor class,
     * which makes controlling log output from this project difficult unless a different
     * logger is used as in this case.
     */
    private static final Logger logger = LoggerFactory.getLogger(AllowDisallowIndexingProcessor.class);
    /**
     * Indicates configured mode of operation.
     */
    private final AllowDisallowMode mode;
    /**
     * List of field match rules.
     */
    private final List<FieldMatchRule> rules;

    /**
     * Name of the schema unique key, null if one is not defined for the schema.
     */
    private final String uniqueKey;

    /**
     * Construct a AllowDisallowIndexingProcessor.
     *
     * @param mode      AllowDisallowMode indicating the mode of operation.
     * @param rules     List of field match rule.
     * @param uniqueKey Name of the document unique key. Null is no unique key is defined in the schema.
     * @param next      Next UpdateRequestProcessor in the processor chain.
     */
    public AllowDisallowIndexingProcessor(final AllowDisallowMode mode, final List<FieldMatchRule> rules, final String uniqueKey, final UpdateRequestProcessor next) {
        super(next);
        this.mode = mode;
        this.rules = rules;
        this.uniqueKey = uniqueKey;
    }

    /**
     * Indicates if running the rules results on a match in the document.
     *
     * @param rules    List of field match rules to run against the document.
     * @param document SolrInputDocument to run rules against.
     * @return True if one of the rules matched the document.
     */
    private static boolean rulesMatch(final List<FieldMatchRule> rules, final SolrInputDocument document) {
        for (FieldMatchRule rule : rules) {
            logger.debug("Testing rule: {}", String.valueOf(rule));

            String ruleField = rule.getField();
            Collection<Object> fieldValues = document.getFieldValues(ruleField);
            for (Object objectValue : fieldValues) {
                if (objectValue instanceof String) {
                    String value = (String) objectValue;
                    if (rule.match(value)) {
                        logger.debug("Matched rule [{}] on value [{}]", String.valueOf(rule), value);
                        return true;
                    }
                }
            }
        }
        // No rules matched
        return false;
    }

    /**
     * Get the value of the documents unique key.
     *
     * @param document SolrInputDocument to get the value from.
     * @return String representation of the documents unique value key.
     */
    private String uniqueKeyValue(final SolrInputDocument document) {
        if (null == this.uniqueKey) return "";

        Object value = document.getFieldValue(this.uniqueKey);
        return String.valueOf(value);
    }

    /**
     * Called by the processor chain on document add/update operations.
     * This is where we check the allow / disallow rules.
     *
     * @param cmd AddUpdateCommand
     * @throws IOException
     */
    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        if (this.mode == AllowDisallowMode.UNKNOWN) {
            logger.warn("Mode UNKNOWN, indexing, check configuration!");
            super.processAdd(cmd);
        } else {
            SolrInputDocument document = cmd.getSolrInputDocument();
            boolean match = rulesMatch(this.rules, document);

            if ((this.mode == AllowDisallowMode.ALLOW) && (!match)) {
                logger.info("DocId [{}] discarded - allow mode without rule match", uniqueKeyValue(document));
                return;
            }

            if ((this.mode == AllowDisallowMode.DISALLOW) && (match)) {
                logger.info("DocId [{}] discarded - disallow mode with rule match", uniqueKeyValue(document));
                return;
            }

            logger.info("DocId [{}] indexing", uniqueKeyValue(document));

            super.processAdd(cmd);
        }
    }

}