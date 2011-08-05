package dk.industria.solr.processors;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;


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
     * Field to pattern replace rule mapping.
     */
    private Map<String, PatternReplaceRule> fieldRules;
    /**
     * Collection of field derived from the above mapping.
     * This so the actual processor doesn't have to derive the collection on every invocation.
     */
    private Collection<String> fields;
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
     * Extract field to pattern replace rule mapping..
     *
     * @param args NamedList with arguments as passed by the processor chain.
     * @return Map with field name to pattern replace rule mapping.
     */
    private static Map<String, PatternReplaceRule> extractFieldRuleMappings(final NamedList args) {
        Map<String, PatternReplaceRule> fieldRules = new HashMap<String, PatternReplaceRule>();

        Map<String, PatternReplaceRule> idRules = extractRules(args);

        Object fieldsElement = args.get("fields");
        if(fieldsElement instanceof NamedList) {
            @SuppressWarnings("unchecked")
            Iterator<Map.Entry<String, ?>> itr = (Iterator<Map.Entry<String, ?>>)((NamedList)fieldsElement).iterator();
            while (itr.hasNext()) {
                Map.Entry<String, ?> kv = itr.next();
                if(kv.getValue() instanceof String) {
                    String fieldName = kv.getKey();
                    String ruleId = (String)kv.getValue();
                    if(idRules.containsKey(ruleId)) {
                        fieldRules.put(fieldName, idRules.get(ruleId));
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
        return fieldRules;
    }


    /**
     *  Get collection of field names with a pattern replace rule attached.
     *
     * @return unmodifiable collection of field names with a pattern rule.
     */
    public Collection<String> getFields() {
        if(null == this.fields) {
            return Collections.unmodifiableCollection(new ArrayList<String>());
        }
        return Collections.unmodifiableCollection(this.fields);
    }

    /**
     * Get a map containing field to pattern replace rule mappings.
     *
     * @return Unmodifiable map containing mapping between fields and pattern replace rules.
     */
    public Map<String, PatternReplaceRule> getRules() {
        if(null == this.fieldRules) {
            return Collections.unmodifiableMap(new HashMap<String, PatternReplaceRule>());
        }
        return Collections.unmodifiableMap(this.fieldRules);
    }

    /**
      * Init called by Solr processor chain.
      *
      * @param args NamedList of parameters set in the processor definition in solrconfig.xml
      */
     @Override
     public void init(final NamedList args) {
         this.fieldRules = extractFieldRuleMappings(args);
         this.fields = this.fieldRules.keySet();
         if(logger.isInfoEnabled()) {
             for(String field : this.fields) {
                 PatternReplaceRule rule = fieldRules.get(field);
                 logger.info("Field [{}] configured with rule {}", field, String.valueOf(rule));
             }
         }
     }

    /**
     * Factory method for the PatternReplaceProcessor called by Solr processor chain.
     *
     * @param req  SolrQueryRequest
     * @param rsp  SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of PatternReplaceProcessor configured with field list and rule mapping.
     */
    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new PatternReplaceProcessor(this.getFields(), this.getRules(), next);
    }

}
