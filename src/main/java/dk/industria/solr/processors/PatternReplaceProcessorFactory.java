package dk.industria.solr.processors;

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
      * Init called by Solr processor chain.
      *
      * @param args NamedList of parameters set in the processor definition in solrconfig.xml
      */
     @Override
     public void init(final NamedList args) {
     }

    /**
     * Factory method for the PatternReplaceProcessor called by Solr processor chain.
     *
     * @param req  SolrQueryRequest
     * @param rsp  SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of HTMLStripCharFilterProcessor initialized with the fields to process.
     */
    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new PatternReplaceProcessor(next);
    }

}
