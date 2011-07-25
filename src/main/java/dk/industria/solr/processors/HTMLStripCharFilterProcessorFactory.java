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
     * Generate a string containing the fields configured, the string is
     * on the form {field1} {field2} ... {fieldn}
     * @param fields The fields for the field string.
     * @return String on the form {field1} {field2} ... {fieldn}
     */
    private static String configuredFieldsString(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for(String field : fields) {
            sb.append(" {");
            sb.append(field);
            sb.append("}");
        }
        return sb.toString();
    }


    /**
     * Extract field names from init arguments.
     * That is fields with a key of field and a type of String.
     * @param initArguments NamedList containing the init arguments.
     * @return List of field names.
     */
    private static List<String> extractFields(final NamedList initArguments) {
	List<String> fieldNames = new ArrayList<String>();
	List valuesWithField = initArguments.getAll("field");
	for(Object value : valuesWithField) {
	    if(value instanceof String) {
		String valueToAdd = ((String)value).trim();
		if(0 < valueToAdd.length()) {
		    if(logger.isDebugEnabled()) {
			logger.debug("Adding field: [" + valueToAdd + "]");
		    }
		    fieldNames.add(valueToAdd);
		} else {
		    logger.warn("Field of length 0 not added value was [" + value.toString() +"]");
		}
	    } else {
		if(null == value) {
		    logger.warn("Misconfigured field, value was [null]");
		} else {
		    logger.warn("Misconfigured field of type [" + value.getClass().getName() + "] with value [" + value.toString() +"]");
		}
	    }
	}
	return fieldNames;
    }


    /**
     * Get the list of field names configured for processing.
     * @return Unmodifiable list of field names configured.
     */
    public List<String> getFields() {
	if (null == fieldsToProcess) {
	    return Collections.EMPTY_LIST;
	}
	return Collections.unmodifiableList(fieldsToProcess);
    }



    /**
     * Init called by SOLR processor chain
     * The values configured for keys field is extracted to fieldsToProcess.
     * @param args NamedList of parameters set in the processor definition (solrconfig.xml)
     */
    public void init(final NamedList args) {
        this.fieldsToProcess = extractFields(args);

	if(logger.isDebugEnabled()) {
            String fls = configuredFieldsString(this.fieldsToProcess);
            logger.debug("Configured with fields [" + fls + "]");
        }

        if(this.fieldsToProcess.isEmpty()) {
            logger.warn("No fields configured");
        }
    }


    /**
     * Factory method for the HTMLStripCharFilterProcessor called by SOLR processor chain.
     * @param req SolrQueryRequest
     * @param rsp SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of HTMLStripCharFilterProcessor initialized with the fields to process.
     */
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        if(logger.isDebugEnabled()) {
            String fls = configuredFieldsString(this.fieldsToProcess);
            logger.debug("Create HTMLStripCharFilterProcessor with fields:" + fls);
        }
        return new HTMLStripCharFilterProcessor(this.fieldsToProcess, next);
    }



}