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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;


/**
 * Implements a factory for the PatternReplaceProcessor used by the Solr update request processor chain.
 * <p/>
 * The primary purpose, in addition to acting as a factory, is parsing the configuration placed
 * within the processor element in solrconfig.xml.
 * <p/>
 * Configuration is a two stage process where pattern replacement rules are defined and
 * later the rules are mapped to fields. This makes it possible to reuse common replacement
 * rules on multiple fields.
 * <p/>
 * Replacement rules are defined by lst elements with a name attribute set to rule and three str elements
 * with their name attributes set to id, pattern and replace, where id is the rule identifier used in the second
 * part of the configuration where rules are mapped to a fields. Pattern is the regular expression the should
 * be matched for replacement and replace is the value to replace the matched pattern with.
 * <p/>
 * Mapping pattern replace rules to fields is done in a lst element with the name attribute set to fields.
 * Within this element, str elements are used to map fields to rule ids by setting the name attribute to the
 * field name and the value to the id of the pattern replace rule.
 * <p/>
 * The rules will only be applied to field of type string, in the input document not the schema, but will be
 * applied to all values if they contain multiple values.
 * <p/>
 * An example configuration where two rules with id punctuation and prefix are mapped to the
 * four fields title, name, comment and card, where punctuation is applied to title, name and comment
 * and prefix is applied to card is shown below:
 * <pre>
 * {@code
 * <processor class="dk.industria.solr.processors.PatternReplaceProcessorFactory">
 *   <lst name="rule">
 *      <str name="id">punctuation</str>
 *      <str name="pattern">\p{P}</str>
 *      <str name="replace"/>
 *   </lst>
 *   <lst name="rule">
 *      <str name="id">prefix</str>
 *      <str name="pattern">^\d{4}</str>
 *      <str name="replace">****</str>
 *   </lst>
 *   <lst name="fields">
 *       <str name="title">punctuation</str>
 *       <str name="name">punctuation</str>
 *       <str name="comment">punctuation</str>
  *       <str name="card">prefix</str>
  *   </lst>
 * </processor>
 * }
 * </pre>
 */
public class PatternReplaceProcessorFactory extends UpdateRequestProcessorFactory {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PatternReplaceProcessorFactory.class);
    /**
     * Field pattern rules configured.
     */
    private Collection<FieldPatternReplaceRules> fieldPatternRules;
    /**
     * Get a String element from a NamedList.
     *
     * @param args NamedList to get the String from.
     * @param name String containing the name of the name attribute to retrieve.
     * @return Value of the name attribute. Null if the value isn't a String or doesn't exists.
     */
    private static String getStringElement(final NamedList args, final String name) {
        Object o = args.get(name);
        if(o instanceof String) {
            return (String)o;
        }
        return null;
    }

    /**
     * Extract pattern replace rules from the processor chain arguments.
     *
     * A rule needs to be a NamedList type entry with name attribute set to rule
     * and containing three String type entries with name attribute set to
     * id, pattern and replace.
     *
     * @param args NamedList as supplied by the processor chain.
     * @return Map of PatternReplaceRule extracted from the processor arguments keyed by id..
     */
    private static Map<String, PatternReplaceRule> extractRules(final NamedList args) {
        Map<String, PatternReplaceRule> rules = new HashMap<String, PatternReplaceRule>();

        List ruleElements = args.getAll("rule");
        for(Object ruleElement : ruleElements) {
            if(!(ruleElement instanceof NamedList)) {
                logger.warn("Element with name attribute set to rule but it is not a <lst> element. Check your configuration.");
                continue;
            }
            try {
                String id = getStringElement((NamedList)ruleElement, "id");
                if(null == id) {
                    logger.warn("id not found for rule");
                    continue;
                }
                String pattern = getStringElement((NamedList)ruleElement, "pattern");
                if(null == pattern) {
                    logger.warn("pattern not found for rule");
                    continue;

                }
                String replace = getStringElement((NamedList)ruleElement, "replace");
                if(null == replace) {
                    logger.warn("replace not found for rule");
                    continue;
                }
                PatternReplaceRule rule = PatternReplaceRule.getInstance(id, pattern, replace);
                rules.put(id, rule);
                logger.info("Added rule: {}", rule.toString());
            } catch(IllegalArgumentException e) {
                logger.warn("Unable to create rule for {}, error was {}", ruleElement.toString(), e.getMessage());
            }
        }
        return rules;
    }

    /**
     * Extract field pattern replace rules..
     *
     * @param args NamedList with arguments as passed by the processor chain.
     * @return Collection of field pattern replace rules.
     */
    private static Collection<FieldPatternReplaceRules> extractFieldRuleMappings(final NamedList args) {
        Map<String, PatternReplaceRule> idRules = extractRules(args);

        Map<String, FieldPatternReplaceRules> fieldPatternRules = new HashMap<String, FieldPatternReplaceRules>();

        Object fieldsElement = args.get("fields");
        if(fieldsElement instanceof NamedList) {
            @SuppressWarnings("unchecked")
            Iterator<Map.Entry<String, ?>> itr = (Iterator<Map.Entry<String, ?>>)((NamedList)fieldsElement).iterator();
            while (itr.hasNext()) {
                Map.Entry<String, ?> kv = itr.next();
                if(kv.getValue() instanceof String) {
                    String fieldName = kv.getKey();
                    String ruleId = (String)kv.getValue();

                    // TODO: might need to check fieldName for null

                    // Make sure there is a FieldPatternReplaceRiles attached to the
                    // field in the fieldPatternRules
                    FieldPatternReplaceRules fr = fieldPatternRules.get(fieldName);
                    if(null == fr) {
                        fr = new FieldPatternReplaceRules(fieldName);
                        fieldPatternRules.put(fieldName, fr);
                    }


                    if(idRules.containsKey(ruleId)) {
                        fr.add(idRules.get(ruleId));
                    } else {
                        logger.warn("Unknown rule id {}", String.valueOf(ruleId));
                    }
                } else {
                    logger.warn("Element in fields list not a <str> element [{}]", String.valueOf(kv));
                }
            }
        } else {
            logger.warn("Element with fields name attribute not a <lst> element. Check the configuration.");
        }
        return fieldPatternRules.values();
    }

    /**
     * Get collection of field pattern replace rules.
     *
     * @return Unmodifiable collection of pattern replace rules.
     */
    public Collection<FieldPatternReplaceRules> getFieldRules() {
        if(null == this.fieldPatternRules) {
            return Collections.unmodifiableCollection(new ArrayList<FieldPatternReplaceRules>());
        }
        return Collections.unmodifiableCollection(this.fieldPatternRules);
    }

    /**
      * Init called by Solr processor chain.
      *
      * @param args NamedList of parameters set in the processor definition in solrconfig.xml
      */
     @Override
     public void init(final NamedList args) {
         this.fieldPatternRules = extractFieldRuleMappings(args);
         if(logger.isInfoEnabled()) {
             for(FieldPatternReplaceRules fieldRules : this.fieldPatternRules) {
                 logger.info("Field [{}] configured with rule {}", fieldRules.getFieldName(), String.valueOf(fieldRules));
             }
         }
     }

    /**
     * Factory method for the PatternReplaceProcessor called by Solr processor chain.
     *
     * @param solrQueryRequest SolrQueryRequest
     * @param solrQueryResponse SolrQueryResponse
     * @param updateRequestProcessor UpdateRequestProcessor
     * @return Instance of PatternReplaceProcessor configured with field list and rule mapping.
     */
    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest solrQueryRequest, org.apache.solr.response.SolrQueryResponse solrQueryResponse, UpdateRequestProcessor updateRequestProcessor) {
        return new PatternReplaceProcessor(this.fieldPatternRules, updateRequestProcessor);
    }
}
