package dk.industria.solr.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


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
    private static final Logger logger = LoggerFactory.getLogger(AllowDisallowIndexingProcessorFactory.class);


    /**
     * Mode configured
     */
    private AllowDisallowMode mode = AllowDisallowMode.UNKNOWN;

    /**
     * List of field match rules configured.
     */
    private List<FieldMatchRule> rules;


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
     * Converts the raw NamedList field match configuration to a list of FieldMatchRule.
     * @param configuration The NamedList of allow/disallow lst element (solrconfig.xml).
     * @return List of FieldMatchRule items.
     */
    private List<FieldMatchRule> getFieldMatchRules(NamedList configuration) {
        List<FieldMatchRule> rules = new ArrayList<FieldMatchRule>();

        @SuppressWarnings("unchecked")
        Iterator<Map.Entry<String, ?>> itr = (Iterator<Map.Entry<String, ?>>)configuration.iterator();
        while(itr.hasNext()) {
            Map.Entry<String, ?> kv = itr.next();
            String key = kv.getKey();
            if(null == key) {
                logger.warn("Item missing name attribute: " + kv.toString());
                continue;
            }

            Object oValue = kv.getValue();
            if(!(oValue instanceof String)) {
                logger.warn("Item not a str element: " + kv.toString());
                continue;
            }

            String value = ((String)oValue).trim();
            if(0 == value.length()) {
                logger.warn("Item trimmed value is empty: " + kv.toString());
                continue;
            }

            logger.debug("Creating FieldMatchRule with: [" + key + "] [" + value + "]");
            rules.add(new FieldMatchRule(key, value));
        }
        if(logger.isDebugEnabled()) {
            logger.debug(rules.toString());
        }
        return rules;
    }


    /**
     * Get the configured mode of operation.
     * @return Mode of operation as a AllowDisallowMode enum.
     */
    public AllowDisallowMode getMode() {
        return this.mode;
    }

    /**
     * Get the list of field match rules configured.
     * @return Unmodifiable list of rules.
     */
    public List<FieldMatchRule> getRules() {
        if(null == rules) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(rules);
    }


    /**
     * Init called by Solr processor chain
     * 
     * @param args NamedList of parameters set in the processor definition (solrconfig.xml)
     */
    @Override public void init(final NamedList args) {
        if(logger.isDebugEnabled()) {
            logger.debug("ARGS: " + args.toString());
        }

        NamedList allow = getConfiguredList(args, "allow");
        if(null != allow) {
            logger.debug("Running with allow semantics: " + allow.toString());
            this.mode = AllowDisallowMode.ALLOW;
            this.rules = getFieldMatchRules(allow);
            return;
        }

        NamedList disallow = getConfiguredList(args, "disallow");
        if(null != disallow) {
            logger.debug("Running with disallow semantics: " + disallow.toString());
            this.mode = AllowDisallowMode.DISALLOW;
            this.rules = getFieldMatchRules(disallow);
            return;
        }

        logger.warn("Neither allow or disallow rules configured for the processor.");
        this.mode = AllowDisallowMode.UNKNOWN;
    }


    /**
     * Factory method for the AllowDisallowIndexingProcessor called by Solr processor chain.
     * @param req SolrQueryRequest
     * @param rsp SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of AllowDisallowIndexingProcessor initialized with the fields to process.
     */
    @Override public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        // TODO: Pass in allow mode, this.mode 
        // TODO: Pass in rules, this.rules
        return new AllowDisallowIndexingProcessor(next);
    }


}
