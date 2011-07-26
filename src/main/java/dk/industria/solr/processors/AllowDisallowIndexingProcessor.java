package dk.industria.solr.processors;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.solr.common.SolrInputDocument;


import org.apache.solr.update.AddUpdateCommand;

import org.apache.solr.update.processor.UpdateRequestProcessor;


public class AllowDisallowIndexingProcessor extends UpdateRequestProcessor {
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
     * Construct a AllowDisallowIndexingProcessor.
     *
     * @param mode  AllowDisallowMode indicating the mode of operation.
     * @param rules List of field match rule.
     * @param next  Next UpdateRequestProcessor in the processor chain.
     */
    public AllowDisallowIndexingProcessor(final AllowDisallowMode mode, final List<FieldMatchRule> rules, final UpdateRequestProcessor next) {
        super(next);
        this.mode = mode;
        this.rules = rules;
    }

    /**
     * Indicates if running the rules results on a match in the document.
     * @param rules List of field match rules to run against the document.
     * @param document SolrInputDocument to run rules against.
     * @return  True if one of the rules matched the document.
     */
    private static boolean rulesMatch(final List<FieldMatchRule> rules, final SolrInputDocument document) {
	for(FieldMatchRule rule : rules) {
	    String ruleField = rule.getField();
	    logger.debug("Testing rule: " + rule.toString());

	    // Get field from document
	    Collection<Object> fieldValues = document.getFieldValues(ruleField);
	    for(Object objectValue : fieldValues) {

		// Only process String values
		if(objectValue instanceof String) {
		    String value = (String)objectValue;
		    boolean match = rule.match(value);
		    if(match) {
			if(logger.isDebugEnabled()) {
			    logger.debug("Matched rule [" + rule.toString() + "] on value [" + value + "]");  
			}
			return true;
		    } 
		} else {
		    logger.debug("Value item for field [" + ruleField + "] is not a String");  
		}
	    }
	}
	// No field rules matched the document
        return false;
    }


    /**
     * Called by the processor chain on document add/update operations.
     * This is where we check the allow / disallow rules.
     *
     * @param cmd AddUpdateCommand
     */
    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        if(this.mode == AllowDisallowMode.UNKNOWN) {
            logger.warn("Mode UNKNOWN, pass command up the chain");
            super.processAdd(cmd);
        } else {
            SolrInputDocument document = cmd.getSolrInputDocument();
            boolean match = rulesMatch(this.rules, document);

            if((this.mode == AllowDisallowMode.ALLOW) && (!match)) {
                logger.debug("Allow mode and no match, don't pass");
                return;
            }

            if((this.mode == AllowDisallowMode.DISALLOW) && (match)) {
                logger.debug(("Disallow mode and match, don't pass"));
                return;
            }

	    logger.debug("Pass command up the processor chain");

            // pass it up the chain
            super.processAdd(cmd);
        }
    }


}