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
    private static Logger log = LoggerFactory.getLogger(HTMLStripCharFilterProcessorFactory.class);

    private NamedList configArgs;
    

    public void init(NamedList args) {
	configArgs = args;

	List<String> fields = args.getAll("field");


    }


    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
	log.error("Factory created!!!!" + configArgs.toString());
	return new HTMLStripCharFilterProcessor(next);
    }
}