package dk.industria.solr.processors;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.CharStream;

import org.apache.solr.analysis.HTMLStripCharFilter;
import org.apache.solr.analysis.HTMLStripCharFilterFactory;


import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;




import org.apache.solr.update.AddUpdateCommand;

import org.apache.solr.update.processor.UpdateRequestProcessor;


public class HTMLStripCharFilterProcessor extends UpdateRequestProcessor {
    /**
     * Logger 
     * UpdateRequestProcessor has it's own log variable tied to the UpdateRequestProcessor class,
     * which makes controlling log output from this project difficult unless a different 
     * logger is used as in this case.
     */
    private static Logger logger = LoggerFactory.getLogger(HTMLStripCharFilterProcessor.class);
    /**
     * Size of the buffer used to read the input through the HTMLStripCharFilter.
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * List of fields to process with the HTMLStripCharFilter.
     */
    private List<String> fieldsToProcess;

    /**
     * Removes duplicate spaces from a string.
     */
    private String removeDuplicateSpaces(String text) {
	if(null == text) return ""; 
	String trimmed = text.trim();
	return trimmed.replaceAll("\\p{Blank}{2,}", " ");
    }



    private String htmlStripString(String text) {

	StringBuilder stripped = new StringBuilder();

	StringReader sr = new StringReader(text);
	Reader r = null;
	if(sr.markSupported()) {
	    logger.error("StringReader used directly");
	    r = sr;
	} else {
	    logger.error("BufferedReader because mark support is not supported.");
	    r = new BufferedReader(sr);
	}
	CharStream cs = CharReader.get(r);

	try {
	    char[] buffer = new char[BUFFER_SIZE];
	    HTMLStripCharFilter filter = new HTMLStripCharFilter(cs);
	    while(true) {
		int nCharsRead = filter.read(buffer);
		if(-1 == nCharsRead) {
		    break;
		}
		if(0 < nCharsRead) {
		    stripped.append(buffer, 0, nCharsRead);
		}
	    }
	    filter.close();
	} catch(IOException ioe) {
	    logger.error("IOException thrown in HTML Stripper: " + ioe.toString());
	}
	
	// The HTML strip filter replaces tags with spaces. Therefore the string
	// should be processed to remove duplicate spaces in the string.
	return removeDuplicateSpaces(stripped.toString());
    }


    /**
     * Construct a HTMLStripCharFilterProcessor.
     * @param fields List of fields to process.
     * @param next Next UpdateRequestProcessor in the processor chain.
     */
    public HTMLStripCharFilterProcessor(List<String> fields, UpdateRequestProcessor next) {
	super(next);
	this.fieldsToProcess = fields; 
    }

    /**
     * Called by the processor chain on document add/update opeations.
     * This is where we process the fields configured before they are indexed.
     * @param cmd AddUpdateCommand
     */
    public void processAdd(AddUpdateCommand cmd) throws IOException {


	// For all fields configured
	for(String field : this.fieldsToProcess) {
	    if(logger.isDebugEnabled()) {
		logger.debug("Processing field: " + field);
	    }



	}




	SolrInputDocument doc = cmd.getSolrInputDocument();


	SolrInputField field = doc.getField("underrubrik_text");
	if (null != field) {

	    // Right now we just assume string so we blindly cast it
	    String fieldValue = (String)field.getValue();
	    logger.error("Field value:" + fieldValue);

	    String strippedFieldValue = htmlStripString(fieldValue);
	    logger.error("Stripped field value:" + strippedFieldValue);


	    // Update the field
	    float boost = field.getBoost();
	    field.setValue(strippedFieldValue, boost);



	}


	// pass it up the chain
	super.processAdd(cmd);
    }



}