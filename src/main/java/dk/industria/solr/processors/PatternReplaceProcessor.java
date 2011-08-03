package dk.industria.solr.processors;

import java.io.IOException;


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
     * Construct a PatternReplaceProcessor.
     *
     * @param next   Next UpdateRequestProcessor in the processor chain.
     */
    public PatternReplaceProcessor(UpdateRequestProcessor next) {
        super(next);
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
