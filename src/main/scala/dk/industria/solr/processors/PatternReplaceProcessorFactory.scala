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
package dk.industria.solr.processors

import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.processor.{UpdateRequestProcessor, UpdateRequestProcessorFactory}

import org.slf4j.LoggerFactory

import scala.collection.immutable.HashMap

import scala.collection.JavaConverters._

/** Implements a factory for the PatternReplaceProcessor used by the Solr update request processor chain.
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
class PatternReplaceProcessorFactory extends UpdateRequestProcessorFactory {
  /** Logger */
  private val logger = LoggerFactory.getLogger(getClass())

  /** Field pattern rules configured. */
  private var fieldPatternRules: List[FieldPatternReplaceRules] = Nil

  /** Get a String element from a NamedList.
   *
   * @param args NamedList to get the String from.
   * @param name String containing the name of the name attribute to retrieve.
   * @return Option value of the name attribute. 
   */
  private def getStringElement(args: NamedList[_], name: String): Option[String] = {
    Option(args.get(name)).filter(_.isInstanceOf[String]).map(_.asInstanceOf[String])
  }

  /** Extract pattern replace rules from the processor chain arguments.
   *
   * A rule needs to be a NamedList type entry with name attribute set to rule
   * and containing three String type entries with name attribute set to
   * id, pattern and replace.
   *
   * @param args NamedList as supplied by the processor chain.
   * @return Map of PatternReplaceRule extracted from the processor arguments keyed by id.
   */
  private def extractRules(args: NamedList[_]): Map[String, PatternReplaceRule] = {
    var rules: Map[String, PatternReplaceRule] = new HashMap
    
    val ruleElements = args.getAll("rule").asScala.filter(_.isInstanceOf[NamedList[_]]).map(_.asInstanceOf[NamedList[_]])
    for(ruleElement <- ruleElements) {
      try {
        val id = getStringElement(ruleElement, "id")
        if(id.isEmpty) {
          logger.warn("id not found for rule")
        } else {
          val pattern = getStringElement(ruleElement, "pattern")
          if(pattern.isEmpty) {
            logger.warn("pattern not found for rule")
          } else {
            val replace = getStringElement(ruleElement, "replace")
            if(replace.isEmpty) {
	      logger.warn("replace not found for rule")
            } else {
	      val rule = PatternReplaceRule.getInstance(id.get, pattern.get, replace.get)
	      rules = rules.updated(id.get, rule)
	      logger.info("Added rule: {}", rule)
	    }
	  }
	}
      } catch { 
	case e: IllegalArgumentException => logger.warn("Unable to create rule for {}, error was {}", ruleElement, e.getMessage())
      }
    }
    rules
  }

  /** Extract field pattern replace rules..
   *
   * @param args NamedList with arguments as passed by the processor chain.
   * @return List of field pattern replace rules.
   */
  private def extractFieldRuleMappings(args: NamedList[_]): List[FieldPatternReplaceRules] = {
    var fieldPatternRules: Map[String, FieldPatternReplaceRules] = new HashMap
    
    val idRules: Map[String, PatternReplaceRule] = extractRules(args)
    
    val fieldsElement = Option(args.get("fields")).filter(_.isInstanceOf[NamedList[_]]).map(_.asInstanceOf[NamedList[_]])
    if (fieldsElement.isDefined) {
      val itr = fieldsElement.get.iterator()
      while (itr.hasNext()) {
        val kv = itr.next()
        if(kv.getValue().isInstanceOf[String]) {
          val fieldName = kv.getKey()
	  val ruleId = kv.getValue().asInstanceOf[String]
          // Make sure there is a FieldPatternReplaceRules attached to the field in the fieldPatternRules
          var fr = fieldPatternRules.get(fieldName) match {
	    case None => { 
	      val fprr = new FieldPatternReplaceRules(fieldName)
	      fieldPatternRules = fieldPatternRules.updated(fieldName, fprr)
	      fprr
	    }
	    case Some(x) => x
	  }
	  
	  idRules.get(ruleId) match {
	    case Some(x) => fr.add(x)
	    case None => logger.warn("Unknown rule id {}", ruleId)
	  }
        } else {
          logger.warn("Element in fields list not a <str> element [{}]", kv)
        }
      }
    } else {
      logger.warn("Element with fields name attribute not a <lst> element. Check the configuration.")
    }
    fieldPatternRules.values.toList
  }

  /** Get collection of field pattern replace rules.
   * Used by the Java based test cases.
   *
   * @return Unmodifiable collection of pattern replace rules.
   */
  def fieldRules: List[FieldPatternReplaceRules] = this.fieldPatternRules

  /** Init called by Solr processor chain.
   *
   * @param args NamedList of parameters set in the processor definition in solrconfig.xml
   */
  override def init(args: NamedList[_]) = {
    fieldPatternRules = extractFieldRuleMappings(args)
    if(logger.isInfoEnabled()) {
      fieldPatternRules.foreach((rule) => logger.info("Field [{}] configured with rule {}", rule.fieldName, rule)   )
    }
  }
  
  /** Factory method for the PatternReplaceProcessor called by Solr processor chain.
   *
   * @param solrQueryRequest SolrQueryRequest
   * @param solrQueryResponse SolrQueryResponse
   * @param updateRequestProcessor UpdateRequestProcessor
   * @return Instance of PatternReplaceProcessor configured with field list and rule mapping.
   */
  override def getInstance(solrQueryRequest: SolrQueryRequest, solrQueryResponse: SolrQueryResponse, updateRequestProcessor: UpdateRequestProcessor): UpdateRequestProcessor = new PatternReplaceProcessor(fieldPatternRules, updateRequestProcessor)
}
