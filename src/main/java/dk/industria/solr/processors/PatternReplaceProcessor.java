package dk.industria.solr.processors;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;


import org.apache.solr.update.AddUpdateCommand;

import org.apache.solr.update.processor.UpdateRequestProcessor;


class PatternReplaceProcessor extends UpdateRequestProcessor {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PatternReplaceProcessor.class);
    /**
     * Collection of field names to process.
     */
    private final Collection<String> fields;
    /**
     * Mapping from field name to pattern replace rule.
     */
    private final Map<String, PatternReplaceRule> rules;

    /**
     * Construct a PatternReplaceProcessor.
     *
     * @param fields Collection of field names to process.
     * @param rules Mapping of field names to pattern replace rules..
     * @param next Next UpdateRequestProcessor in the processor chain.
     */
    public PatternReplaceProcessor(final Collection<String> fields, final Map<String,PatternReplaceRule> rules, UpdateRequestProcessor next) {
        super(next);
        this.fields = fields;
        this.rules = rules;
    }

    /**
     * Called by the processor chain on document add/update operations.
     *
     * @param cmd AddUpdateCommand
     * @throws IOException
     */
    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        super.processAdd(cmd);
    }

}
