package dk.industria.solr.processors;


import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;


import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;


public class AllowDisallowIndexingProcessorFactory extends UpdateRequestProcessorFactory {
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(AllowDisallowIndexingProcessorFactory.class);


    /**
     * Get the NamedList associated with a key.
     * @param args The NamedList to look for the key.
     * @param key The key to look for in the list.
     * @return NamedList associated with the key or null if the keys isn't in the args or isn't a NamedList.
     */
    private NamedList getConfiguredList(NamedList args, String key) {
	Object o = args.get(key);
	if(o instanceof NamedList) {
	    return (NamedList)o;
	}
	logger.debug("Key [" + key + "] not in configuration arguments");
	return null;
    }

    /**
     * Init called by Solr processor chain
     * 
     * @param args NamedList of parameters set in the processor definition (solrconfig.xml)
     */
    public void init(NamedList args) {
	if(logger.isDebugEnabled()) {
	    logger.debug("ARGS: " + args.toString());
	}


	NamedList allow = getConfiguredList(args, "allow");
	if(null != allow) {
	    logger.debug("Running with allow semantics: " + allow.toString());
	    return;
	}

	NamedList disallow = getConfiguredList(args, "disallow");
	if(null != disallow) {
	    logger.debug("Running with disallow semantics: " + disallow.toString());
	    return;
	}

	logger.warn("Neither allow or disallow rules configured for the processor.");
    }


    /**
     * Factory method for the AllowDisallowIndexingProcessor called by Solr processor chain.
     * @param req SolrQueryRequest
     * @param rsp SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of AllowDisallowIndexingProcessor initialized with the fields to process.
     */
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
	return new AllowDisallowIndexingProcessor(next);
    }


}