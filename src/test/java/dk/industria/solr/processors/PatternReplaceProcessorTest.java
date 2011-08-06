package dk.industria.solr.processors;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Implements tests for the PatternReplaceProcessor.
 */
public class PatternReplaceProcessorTest {
    /**
     * Create configuration argument for the below XML:
     *
     * <lst name="rule">
     * <str name="id">punctuation</str>
     * <str name="pattern">\p{P}</str>
     * <str name="replace"></str>
     * </lst>
     * <lst name="rule">
     * <str name="id">prefix</str>
     * <str name="pattern">^\d{4}</str>
     * <str name="replace">****</str>
     * </lst>
     * <lst name="fields">
     * <str name="title">punctuation</str>
     * <str name="name">punctuation</str>
     * <str name="comment">punctuation</str>
     * <str name="card">prefix</str>
     * </lst>
     *
     * @return NamedList containing the above configuration.
     */
    private static NamedList<NamedList<String>> createConfig() {
        NamedList<String> punctuation = new NamedList<String>();
        punctuation.add("id", "punctuation");
        punctuation.add("pattern", "\\p{P}");
        punctuation.add("replace", "");

        NamedList<String> prefix = new NamedList<String>();
        prefix.add("id", "prefix");
        prefix.add("pattern", "^\\d{4}");
        prefix.add("replace", "****");

        NamedList<String> fields = new NamedList<String>();
        fields.add("title", "punctuation");
        fields.add("name", "punctuation");
        fields.add("comment", "punctuation");
        fields.add("card", "prefix");

        NamedList<NamedList<String>> args = new NamedList<NamedList<String>>();
        args.add("rule", punctuation);
        args.add("rule", prefix);
        args.add("fields", fields);
        return args;
    }

    /**
     * Create a SolrInputDocument containing two fields header, content and card
     * Field header : Text nothing special
     * Field content : Text nothing special
     * Field comment: Text with punctuation.
     * Field card : xxxx-xxxx-xxxx-xxxx formatted where x are numeric.
     *
     * @return SolrInputDocument with fields header and content.
     */
    private static SolrInputDocument createDocumentWithCardField() {
        SolrInputDocument document = new SolrInputDocument();

        document.addField("header", "Header without markup", 1f);
        document.addField("content", "Content with markup", 1f);
        document.addField("card", "3333-1111-2222-3333", 1f);
        document.addField("comment", "There, is. punctuation!!", 1f);

        return document;
    }

    @Test
    public void createTest() {
        PatternReplaceProcessorFactory factory = new PatternReplaceProcessorFactory();
        factory.init(createConfig());
        UpdateRequestProcessor processor = factory.getInstance(null, null, null);

        AddUpdateCommand cmd = new AddUpdateCommand();
        cmd.solrDoc = createDocumentWithCardField();

        try {
            processor.processAdd(cmd);

            String actualHeader = (String)cmd.solrDoc.getFieldValue("header");
            assertEquals("Header without markup", actualHeader);
            String actualContent = (String)cmd.solrDoc.getFieldValue("content");
            assertEquals("Content with markup", actualContent);
            String expectedCard = "****-1111-2222-3333";
            assertEquals(expectedCard, cmd.solrDoc.getFieldValue("card"));
            String expectedComment = "There is punctuation";
            assertEquals(expectedComment, cmd.solrDoc.getFieldValue("comment"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

}
