package dk.industria.solr.processors;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.solr.update.processor.UpdateRequestProcessor;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.update.AddUpdateCommand;


/**
 * Implements tests for AllowDisallowIndexingProcessor.
 */
public class AllowDisallowIndexingProcessorTest {
    /**
     * Create a configuration for allow mode with contenttype default rule..
     * @return NamedList configured for allow mode.
     */
    private static NamedList createAllowConfig() {
        NamedList rules = new NamedList();
        rules.add("contenttype", "default");

        NamedList args = new NamedList();
        args.add("allow", rules);
        return args;
    }

    /**
     *  Create a update request processor with init arguments.
     * @param args NamedList containing the init arguments for the update request processor.
     * @param next UpdateRequestProcessor called if the processor passed the request on.
     * @return UpdateRequestProcessor based on the init arguments in args.
     */
    private static UpdateRequestProcessor getProcessor(NamedList args, UpdateRequestProcessor next) {
        AllowDisallowIndexingProcessorFactory factory = new AllowDisallowIndexingProcessorFactory();
        factory.init(args);
        return factory.getInstance(null, null, next);
    }

    /**
     * Create a Solr input document with fields header, content and contenttype
     * where contenttype is defailt.
     * @return SolrInputDocument with a contenttype of default.
     */
    private static SolrInputDocument createMatchDocument() {
        SolrInputDocument document = new SolrInputDocument();

        document.addField("header", "Header without markup", 1f);
        document.addField("content", "<P>Content</P> <em>with</em> markup", 1f);
        document.addField("contenttype", "default");
        return document;
    }

    /**
     * Checks the getInstance creates an UpdateRequestProcessor.
     */
    @Test
    public void instanceReturn() {
        UpdateRequestProcessor processor = getProcessor(createAllowConfig(), null);
        assertNotNull(processor);
    }

    @Test
    public void documentAllowMatch() {
        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createMatchDocument();
        AllowDisallowIndexingProcessorNext passRecorder = new AllowDisallowIndexingProcessorNext(null);
        UpdateRequestProcessor processor = getProcessor(createAllowConfig(), passRecorder);

        try {
            processor.processAdd(cmd);
            assertTrue(passRecorder.called);
        } catch (IOException e) {
            fail(e.toString());
        }

    }


}
