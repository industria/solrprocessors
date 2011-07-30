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
     * Create a configuration with content_type default and news
     * @param mode String to act as lst name attribute value
     * @return NamedList configured for mode.
     */
    private static NamedList createDefaultNewsConfig(String mode) {
        NamedList<String> rules = new NamedList<String>();
        rules.add("content_type", "default");
        rules.add("content_type", "news");

        NamedList<NamedList> args = new NamedList<NamedList>();
        args.add(mode, rules);
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
     * Create a Solr input document with fields header, content and content_type
     * where content_type is set by the content_type argument.
     * @param contentType The content type field value.
     * @return SolrInputDocument with a content_type of the content_type argument.
     */
    private static SolrInputDocument createDocument(String contentType) {
        SolrInputDocument document = new SolrInputDocument();

        document.addField("header", "Header without markup", 1f);
        document.addField("content", "<P>Content</P> <em>with</em> markup", 1f);
        document.addField("content_type", contentType);
        return document;
    }

    /**
     * Checks the getInstance creates an UpdateRequestProcessor.
     */
    @Test
    public void instanceReturn() {
        UpdateRequestProcessor processor = getProcessor(createDefaultNewsConfig("allow"), null);
        assertNotNull(processor);
    }

    /**
     * Allow mode field with a match and thereby catching a pass in the recorder
     */
    @Test
    public void documentAllowMatch() {
        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocument("default");
        AllowDisallowIndexingProcessorNext passRecorder = new AllowDisallowIndexingProcessorNext(null);
        UpdateRequestProcessor processor = getProcessor(createDefaultNewsConfig("allow"), passRecorder);

        try {
            processor.processAdd(cmd);
            assertTrue(passRecorder.called);
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /**
     * Allow mode field without a match and thereby not catching a pass in the recorder
     */
    @Test
    public void documentAllowNoMatch() {
        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocument("person");
        AllowDisallowIndexingProcessorNext passRecorder = new AllowDisallowIndexingProcessorNext(null);
        UpdateRequestProcessor processor = getProcessor(createDefaultNewsConfig("allow"), passRecorder);

        try {
            processor.processAdd(cmd);
            assertFalse(passRecorder.called);
        } catch (IOException e) {
            fail(e.toString());
        }
    }


    /**
     * Disallow mode field with a match and thereby not catching a pass in the recorder
     */
    @Test
    public void documentDisallowMatch() {
        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocument("default");
        AllowDisallowIndexingProcessorNext passRecorder = new AllowDisallowIndexingProcessorNext(null);
        UpdateRequestProcessor processor = getProcessor(createDefaultNewsConfig("disallow"), passRecorder);

        try {
            processor.processAdd(cmd);
            assertFalse(passRecorder.called);
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /**
     * Disallow mode field without a match and thereby catching a pass in the recorder
     */
    @Test
    public void documentDisallowNoMatch() {
        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocument("person");
        AllowDisallowIndexingProcessorNext passRecorder = new AllowDisallowIndexingProcessorNext(null);
        UpdateRequestProcessor processor = getProcessor(createDefaultNewsConfig("disallow"), passRecorder);

        try {
            processor.processAdd(cmd);
            assertTrue(passRecorder.called);
        } catch (IOException e) {
            fail(e.toString());
        }
    }
}
