package dk.industria.solr.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;


import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * Implements a factory for the HTMLStripCharFilterProcessor
 * <p/>
 * The main purpose of this is to process the init arguments into a list
 * of fields that should be processed with the HTMLStripCharFilterProcessor.
 * <p/>
 * Configuration is done by placing str elements with a name attribute set to field
 * and a element value set to the name of the field that should be processed.
 * <p/>
 * The space normalization done by the processor can be turned off by placing
 * a bool element with name attribute of normalize set to false.
 * <p/>
 * Example processor configuration for processing fields header and content:
 * <p/>
 * <pre>
 * {@code
 * <processor class="dk.industria.solr.processors.HTMLStripCharFilterProcessorFactory">
 *   <str name="field">header</str>
 *   <str name="field">content</str>
 *   <bool name="normalize">true</bool>
 * </processor>
 * }
 * </pre>
 */
public class HTMLStripCharFilterProcessorFactory extends UpdateRequestProcessorFactory {
    /**
     * Logger
     */
    private final static Logger logger = LoggerFactory.getLogger(HTMLStripCharFilterProcessorFactory.class);
    /**
     * List of fields configured for HTML character stripping.
     */
    private List<String> fieldsToProcess;
    /**
     * Indicates if spaces should be normalized after running HTMLStripCharFilter.
     */
    private boolean spaceNormalize = true;

    /**
     * Generate a string containing the fields configured, the string is
     * on the form {field1} {field2} ... {fieldN}
     *
     * @param fields The fields for the field string.
     * @return String on the form {field1} {field2} ... {fieldN}
     */
    private static String configuredFieldsString(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(" {").append(field).append("}");
        }
        return sb.toString();
    }

    /**
     * Extract field names from init arguments.
     * That is fields with a key of field and a type of String.
     *
     * @param initArguments NamedList containing the init arguments.
     * @return List of field names.
     */
    private static List<String> extractFields(final NamedList initArguments) {
        List<String> fieldNames = new ArrayList<String>();
        List valuesWithField = initArguments.getAll("field");
        for (Object value : valuesWithField) {
            if (value instanceof String) {
                String valueToAdd = ((String) value).trim();
                if (0 < valueToAdd.length()) {
                    logger.debug("Adding field, with value [{}]", valueToAdd);
                    fieldNames.add(valueToAdd);
                }
            }
        }
        return fieldNames;
    }

    /**
     * Extract space normalization setting from boolean with key normalize.
     * <p/>
     * If a bool element with normalize name attribute does not exists in
     * the arguments it will default to true.
     *
     * @param initArguments NamedList containing the init arguments.
     * @return True if space normalization should be turned on.
     */
    private static boolean extractSpaceNormalization(final NamedList initArguments) {
        Object oValue = initArguments.get("normalize");
        return (oValue instanceof Boolean) ? (Boolean)oValue : true;
    }

    /**
     * Get the list of field names configured for processing.
     *
     * @return Unmodifiable list of field names configured.
     */
    public List<String> getFields() {
        if (null == fieldsToProcess) {
            return Collections.unmodifiableList(new ArrayList<String>());
        }
        return Collections.unmodifiableList(fieldsToProcess);
    }

    /**
     * Get space normalization setting.
     *
     * @return True is space normalization should be performed in the processor.
     */
    public boolean getNormalize() {
        return this.spaceNormalize;
    }

    /**
     * Init called by Solr processor chain
     * The values configured for name attribute field are extracted to fieldsToProcess.
     *
     * @param args NamedList of parameters set in the processor definition in solrconfig.xml
     */
    @Override
    public void init(final NamedList args) {
        this.spaceNormalize = extractSpaceNormalization(args);

        logger.debug("Configured with space normalization set to: {}", String.valueOf(this.spaceNormalize));

        this.fieldsToProcess = extractFields(args);

        logger.debug("Configured with fields [{}]", configuredFieldsString(this.fieldsToProcess));

        if (this.fieldsToProcess.isEmpty()) {
            logger.warn("No fields configured. Consider removing the processor.");
        }
    }

    /**
     * Factory method for the HTMLStripCharFilterProcessor called by SOLR processor chain.
     *
     * @param req  SolrQueryRequest
     * @param rsp  SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of HTMLStripCharFilterProcessor initialized with the fields to process.
     */
    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new HTMLStripCharFilterProcessor(this.getFields(), this.getNormalize(), next);
    }
}