/**
 * Copyright 2011 James Lindstorff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.industria.solr.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;


import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;


/**
 * Implements a factory for the AllowDisallowIndexingProcessor
 * <p/>
 * <p>The main purpose of this is to process the init arguments into a list
 * of field match rules and a mode of operation.</p>
 * <p/>
 * <p>Configuration is done by placing a lst element within the processor element
 * with a name attribute set to either allow or disallow which indicates the mode
 * of operation. Within that lst element str elements with the name attribute set
 * to the field to match and the value to the matching rule.</p>
 * <p/>
 * <p>Example processor configuration with mode of operation set to allow and matching
 * field rules content_type = default and content_type = news, which would index
 * all document with the field content_type set to either default or news:</p>
 * <p/>
 * <pre>
 * {@code
 * <processor class="dk.industria.solr.processors.AllowDisallowIndexingProcessorFactory">
 *   <lst name="allow">
 *     <str name="content_type">default</str>
 *     <str name="content_type">news</str>
 *     </lst>
 * </processor>
 * }
 * </pre>
 */
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
     *
     * @param args The NamedList to look for the key.
     * @param key  The key to look for in the list.
     * @return NamedList associated with the key or null if the keys isn't in the args or isn't a NamedList.
     */
    private NamedList getConfiguredList(NamedList args, String key) {
        Object o = args.get(key);
        if (o instanceof NamedList) {
            return (NamedList) o;
        }
        logger.debug("Key [{}] not in configuration arguments", key);
        return null;
    }

    /**
     * Converts the raw NamedList field match configuration to a list of FieldMatchRule.
     *
     * @param configuration The NamedList of allow/disallow lst element (solrconfig.xml).
     * @return List of FieldMatchRule items.
     */
    private List<FieldMatchRule> getFieldMatchRules(NamedList configuration) {
        List<FieldMatchRule> rules = new ArrayList<FieldMatchRule>();

        @SuppressWarnings("unchecked")
        Iterator<Map.Entry<String, ?>> itr = (Iterator<Map.Entry<String, ?>>) configuration.iterator();
        while (itr.hasNext()) {
            Map.Entry<String, ?> kv = itr.next();
            String key = kv.getKey();
            if ((null == key) || (0 == key.trim().length())) {
                logger.warn("Item missing name attribute: {}", kv.toString());
                continue;
            }

            Object oValue = kv.getValue();
            if (!(oValue instanceof String)) {
                logger.warn("Item not a <str> element: {}", kv.toString());
                continue;
            }

            String value = ((String) oValue).trim();
            if (0 == value.length()) {
                logger.warn("Item trimmed value is empty: {}", kv.toString());
                continue;
            }

            try {
                FieldMatchRule rule = FieldMatchRule.getInstance(key, value);
                rules.add(rule);
                logger.debug("Added FieldMatchRule : {}", rule.toString());
            } catch (IllegalArgumentException e) {
                logger.warn("Couldn't create FieldMatchRule: {}", e.getMessage());
            }
        }
        logger.info("Rules configured: {}", rules.toString());
        return rules;
    }

    /**
     * Get the name of the unique key defined for the schema.
     *
     * @param request SolrQueryRequest
     * @return String containing the name of the schema unique key or null it one is not defined.
     */
    private String uniqueKey(SolrQueryRequest request) {
        if (null == request) return null;

        SolrCore core = request.getCore();
        IndexSchema schema = core.getSchema();
        SchemaField field = schema.getUniqueKeyField();
        return (null != field) ? field.getName() : null;
    }

    /**
     * Get the configured mode of operation.
     *
     * @return Mode of operation as a AllowDisallowMode enum.
     */
    public AllowDisallowMode getMode() {
        return this.mode;
    }

    /**
     * Get the list of field match rules configured.
     *
     * @return Unmodifiable list of rules.
     */
    public List<FieldMatchRule> getRules() {
        if (null == rules) {
            return Collections.unmodifiableList(new ArrayList<FieldMatchRule>());
        }
        return Collections.unmodifiableList(rules);
    }


    /**
     * Init called by Solr processor chain
     *
     * @param args NamedList of parameters set in the processor definition in solrconfig.xml
     */
    @Override
    public void init(final NamedList args) {
        NamedList allow = getConfiguredList(args, "allow");
        if (null != allow) {
            logger.debug("Running with allow semantics: {}", allow.toString());
            this.mode = AllowDisallowMode.ALLOW;
            this.rules = getFieldMatchRules(allow);
            return;
        }

        NamedList disallow = getConfiguredList(args, "disallow");
        if (null != disallow) {
            logger.debug("Running with disallow semantics: {}", disallow.toString());
            this.mode = AllowDisallowMode.DISALLOW;
            this.rules = getFieldMatchRules(disallow);
            return;
        }

        logger.warn("No rules configured for the processor. Consider removing it from chain.");
        this.mode = AllowDisallowMode.UNKNOWN;
    }

    /**
     * Factory method for the AllowDisallowIndexingProcessor called by Solr processor chain.
     *
     * @param req  SolrQueryRequest
     * @param rsp  SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of AllowDisallowIndexingProcessor initialized with the fields to process.
     */
    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        String uniqueFieldName = uniqueKey(req);
        return new AllowDisallowIndexingProcessor(this.mode, this.rules, uniqueFieldName, next);
    }

}
