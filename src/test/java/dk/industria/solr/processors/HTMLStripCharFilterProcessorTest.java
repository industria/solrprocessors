package dk.industria.solr.processors;


import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.update.processor.UpdateRequestProcessor;

import org.junit.Test;

import static org.junit.Assert.*;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.update.AddUpdateCommand;


public class HTMLStripCharFilterProcessorTest {

    /**
     * Configure a UpdateRequestProcessor to read the fields header and content.
     *
     * @return UpdateRequestProcessor (HTMLStripCharFilterProcessor)
     */
    private static UpdateRequestProcessor headerContentProcessor() {
        NamedList input = new NamedList();
        input.add("field", "header");
        input.add("field", "content");

        HTMLStripCharFilterProcessorFactory factory = new HTMLStripCharFilterProcessorFactory();
        factory.init(input);

        return factory.getInstance(null, null, null);
    }


    /**
     * Create a SolrInputDocument containing two fields header and content
     * Field header : Doesn't contain any markup
     * Field conent : Contains markup
     *
     * @return SolrInputDocument with fields header and content.
     */
    private static SolrInputDocument createDocumentWithMarkup() {
        SolrInputDocument document = new SolrInputDocument();

        document.addField("header", "Header without markup", 1f);
        document.addField("content", "<P>Content</P> <em>with</em> markup", 1f);

        return document;
    }


    /**
     * Create a SolrInputDocument containing two fields header and content
     * Field header : Doesn't contain any markup
     * Field conent : Contains markup in multivalues
     *
     * @return SolrInputDocument with fields header and content.
     */
    private static SolrInputDocument createDocumentWithMarkupMultiValue() {
        SolrInputDocument document = new SolrInputDocument();

        document.addField("header", "Header without markup", 1f);
        Collection<Object> c = new ArrayList<Object>();
        c.add("<P>Content</P> <em>with</em> markup");
        c.add("<script>function t() { return f;}</script>second value");
        document.addField("content", c, 1f);

        return document;
    }


    /**
     * Create a SolrInputDocument containing two fields header and content
     * Field header : Doesn't contain any markup
     * Field conent : Contains markup in multivalues
     *
     * @return SolrInputDocument with fields header and content.
     */
    private static SolrInputDocument createDocumentWithMarkupMultiValueNoBreakSpaces() {
        SolrInputDocument document = new SolrInputDocument();

        document.addField("header", "Header without markup", 1f);
        Collection<Object> c = new ArrayList<Object>();
        c.add("<P>Content</P> <em>with</em> markup");
        c.add("<script>function t() { return f;}</script>second value");
        c.add("this\u00A0has\u00A0no-break\u00A0spaces");
        document.addField("content", c, 1f);

        return document;
    }


    /**
     * Checks the getInstance creates an UpdateRequestProcessor.
     */
    @Test
    public void instanceReturn() {
        UpdateRequestProcessor processor = headerContentProcessor();
        assertNotNull(processor);
    }


    /**
     * Process a document where header doesn't contain any markup but the content does
     * see createDocumentWithMarkup for document content.
     */
    @Test
    public void markupNoneHeaderInContent() {
        UpdateRequestProcessor processor = headerContentProcessor();

        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocumentWithMarkup();

        try {
            processor.processAdd(cmd);

            String actualHeader = (String) cmd.solrDoc.getFieldValue("header");
            assertEquals(actualHeader, "Header without markup");
            String actualContent = (String) cmd.solrDoc.getFieldValue("content");
            assertEquals(actualContent, "Content with markup");
        } catch (IOException e) {
            fail(e.toString());
        }
    }


    /**
     * Process a document where header doesn't contain any markup but the content does
     * see createDocumentWithMarkupMultiValue for document content.
     */
    @Test
    public void markupNoneHeaderInContentMultivalued() {
        UpdateRequestProcessor processor = headerContentProcessor();

        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocumentWithMarkupMultiValue();

        try {
            processor.processAdd(cmd);

            String actualHeader = (String) cmd.solrDoc.getFieldValue("header");
            assertEquals(actualHeader, "Header without markup");
            Collection<Object> actualContent = cmd.solrDoc.getFieldValues("content");
            Object[] rgActual = actualContent.toArray();
            Object[] rgExpected = {"Content with markup", "second value"};
            for (int i = 0; i < rgActual.length; i++) {
                assertEquals(rgActual[i], rgExpected[i]);
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /**
     * Process a document where header doesn't contain any markup the content does
     * and it also contains no-break spaces
     * see createDocumentWithMarkupMultiValueNoBreakSpaces for document content.
     */
    @Test
    public void markupNoneHeaderInContentMultivaluedNoBreak() {
        UpdateRequestProcessor processor = headerContentProcessor();

        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocumentWithMarkupMultiValueNoBreakSpaces();

        try {
            processor.processAdd(cmd);

            String actualHeader = (String) cmd.solrDoc.getFieldValue("header");
            assertEquals(actualHeader, "Header without markup");
            Collection<Object> actualContent = cmd.solrDoc.getFieldValues("content");
            Object[] rgActual = actualContent.toArray();
            Object[] rgExpected = {"Content with markup", "second value", "this has no-break spaces"};

            for (int i = 0; i < rgActual.length; i++) {
                assertEquals(rgActual[i], rgExpected[i]);
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }


}