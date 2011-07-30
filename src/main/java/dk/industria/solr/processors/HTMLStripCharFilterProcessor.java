package dk.industria.solr.processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.CharStream;

import org.apache.solr.analysis.HTMLStripCharFilter;


import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;


import org.apache.solr.update.AddUpdateCommand;

import org.apache.solr.update.processor.UpdateRequestProcessor;

/**
 * Implements an UpdateRequestProcessor for running Solr HTMLStripCharFilter on
 * select document fields before they are stored.
 * <p/>
 * For more information @see HTMLStripCharFilterProcessorFactory
 */
class HTMLStripCharFilterProcessor extends UpdateRequestProcessor {
    /**
     * Logger
     * UpdateRequestProcessor has it's own log variable tied to the UpdateRequestProcessor class,
     * which makes controlling log output from this project difficult unless a different
     * logger is used as in this case.
     */
    private static final Logger logger = LoggerFactory.getLogger(HTMLStripCharFilterProcessor.class);
    /**
     * Size of the buffer used to read the input through the HTMLStripCharFilter.
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * List of fields to process with the HTMLStripCharFilter.
     */
    private final List<String> fieldsToProcess;

    /**
     * Removes duplicate spaces from a string.
     * TODO: Also removes no break space which should be moved out
     *
     * @param text String possibly containing duplicate spaces.
     * @return String with duplicate spaces removed.
     */
    private String removeDuplicateSpaces(String text) {
        if (null == text) return "";
        String trimmed = text.trim();
        // Strip out No-break space
        String charStripped = trimmed.replaceAll("\u00A0", " ");
        return charStripped.replaceAll("\\p{Space}{2,}", " ");
    }


    /**
     * Strip HTML/XML from string by reading it through the
     * the Solr HTMLStripCharFilter.The string is also normalized
     * with regards to spacing.
     *
     * @param text String containing HTML/XML to be stripped.
     * @return String with HTML/XML removed.
     * @throws IOException  if reading the string through the HTMLStripCharFilter.
     */
    private String htmlStripString(String text) throws IOException {
        Reader r = new StringReader(text);
        if (!r.markSupported()) {
            logger.debug("Reader returned false for mark support, wrapped in BufferedReader.");
            r = new BufferedReader(r);
        }
        CharStream cs = CharReader.get(r);

        StringBuilder stripped = new StringBuilder();
        try {
            char[] buffer = new char[BUFFER_SIZE];
            HTMLStripCharFilter filter = new HTMLStripCharFilter(cs);
            while (true) {
                int nCharsRead = filter.read(buffer);
                if (-1 == nCharsRead) {
                    break;
                }
                if (0 < nCharsRead) {
                    stripped.append(buffer, 0, nCharsRead);
                }
            }
            filter.close();
        } catch (IOException e) {
            logger.error("IOException thrown in HTML Stripper: {}", e.toString());
            throw e;
        }
        // The HTML strip filter replaces tags with spaces. Therefore the string
        // should be processed to remove duplicate spaces in the string.
        return removeDuplicateSpaces(stripped.toString());
    }

    /**
     * Construct a HTMLStripCharFilterProcessor.
     *
     * @param fields List of field names to process.
     * @param next   Next UpdateRequestProcessor in the processor chain.
     */
    public HTMLStripCharFilterProcessor(final List<String> fields, final UpdateRequestProcessor next) {
        super(next);
        this.fieldsToProcess = fields;
    }

    /**
     * Called by the processor chain on document add/update operations.
     * This is where we process the fields configured before they are indexed.
     *
     * @param cmd AddUpdateCommand
     * @throws IOException
     */
    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        SolrInputDocument doc = cmd.getSolrInputDocument();
        for (String fieldName : this.fieldsToProcess) {
            logger.debug("Processing field: {}", fieldName);

            SolrInputField field = doc.getField(fieldName);
            if (null == field) continue;

            Collection<Object> values = field.getValues();
            if (null == values) continue;

            Collection<Object> newValues = new ArrayList<Object>();
            for (Object value : values) {
                if (value instanceof String) {
                    String strippedValue = htmlStripString((String) value);
                    newValues.add(strippedValue);
                } else {
                    newValues.add(value);
                }
            }
            float boost = field.getBoost();
            field.setValue(newValues, boost);
        }
        super.processAdd(cmd);
    }
}
