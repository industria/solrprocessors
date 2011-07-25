package dk.industria.solr.processors;

import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Implements an UpdateRequestProcessor used to record if a previous processor
 * passed the command up the chain.
 * Used in AllowDisallowIndexingProcessorTest.
 */
public class AllowDisallowIndexingProcessorNext extends UpdateRequestProcessor {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(AllowDisallowIndexingProcessor.class);

    /**
     * Indicated if processAdd was activated.
     */
    public boolean called = false;

    /**
     * Construct an AllowDisallowIndexingProcessorNext request processor..
     * @param next UpdateRequestProcessor to pass command on to.
     */
    public AllowDisallowIndexingProcessorNext(UpdateRequestProcessor next) {
        super(next);
    }

    /**
     *  Implement processAdd setting the instance variable called to true.
     * @param cmd AddUpdateCommand to process.
     * @throws IOException
     */
    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        logger.debug("Record processAdd called");
        this.called = true;
        super.processAdd(cmd);
    }
}
