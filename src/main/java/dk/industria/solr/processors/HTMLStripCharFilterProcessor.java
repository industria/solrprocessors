package dk.industria.solr.processors;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

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
     * Size of the buffer used to read the input through the HTMLStripCharFilter.
     */
    private static final int BUFFER_SIZE = 4096;

    private List<String> fieldsToProcess;

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
	    log.error("StringReader used directly");
	    r = sr;
	} else {
	    log.error("BufferedReader because mark support is not supported.");
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
	    log.error("IOException thrown in HTML Stripper: " + ioe.toString());
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
  
    public void processAdd(AddUpdateCommand cmd) throws IOException {
	SolrInputDocument doc = cmd.getSolrInputDocument();


	SolrInputField field = doc.getField("underrubrik_text");
	if (null != field) {

	    // Right now we just assume string so we blindly cast it
	    String fieldValue = (String)field.getValue();
	    log.error("Field value:" + fieldValue);

	    String strippedFieldValue = htmlStripString(fieldValue);
	    log.error("Stripped field value:" + strippedFieldValue);


	    // Update the field
	    float boost = field.getBoost();
	    field.setValue(strippedFieldValue, boost);



	}


	// pass it up the chain
	super.processAdd(cmd);
    }



}