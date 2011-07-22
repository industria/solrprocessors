package dk.industria.solr.processors;


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
    private static Logger logger = LoggerFactory.getLogger(HTMLStripCharFilterProcessorFactory.class);
    /**
     * List of fields configured for HTML character stripping.
     */
    private List<String> fieldsToProcess;


    /**
     * Init called by SOLR processor chain
     * The values configured for keys field is extracted to fieldsToProcess.
     * @param args NamedList of parameters set in the processor definition (solrconfig.xml)
     */
    public void init(NamedList args) {
	this.fieldsToProcess = args.getAll("field");

	if(this.fieldsToProcess.isEmpty()) {
	    logger.warn("No fields defined for HTMLStripCharFilterProcessor");
	} else if(logger.isDebugEnabled()) {
	    String fls = configuredFieldsString(this.fieldsToProcess);
	    logger.debug("HTMLStripCharFilterProcessor fields:" + fls);
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

}