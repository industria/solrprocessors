package dk.industria.solr.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;


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
     * Construct a AllowDisallowIndexingProcessor.
     * @param next Next UpdateRequestProcessor in the processor chain.
     */
    public AllowDisallowIndexingProcessor(UpdateRequestProcessor next) {
	super(next);
    }

    /**
     * Called by the processor chain on document add/update opeations.
     * This is where we check the allow / disallow rules.
     * @param cmd AddUpdateCommand
     */
    public void processAdd(AddUpdateCommand cmd) throws IOException {

	//SolrInputDocument doc = cmd.getSolrInputDocument();
	//SolrInputField field = doc.getField(fieldName);

	logger.debug("processAdd");


	// We should choose whether to pass the document up the chain or just stop here

	// pass it up the chain
	super.processAdd(cmd);
    }



}