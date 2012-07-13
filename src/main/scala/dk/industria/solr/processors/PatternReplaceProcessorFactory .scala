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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.solr.common.util.NamedList

import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse

import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.update.processor.UpdateRequestProcessorFactory

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
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
class PatternReplaceProcessorFactory extends UpdateRequestProcessorFactory {
  /**
   * Logger
   */
  private val logger = LoggerFactory.getLogger(getClass())
  /**
   * Field pattern rules configured.
   */
  private var fieldPatternRules: List[FieldPatternReplaceRules] = Nil
  /**
   * Get a String element from a NamedList.
   *
   * @param args NamedList to get the String from.
   * @param name String containing the name of the name attribute to retrieve.
   * @return Value of the name attribute. Null if the value isn't a String or doesn't exists.
   */
  private def getStringElement(args: NamedList[_], name: String): String = {
    val o = args.get(name);
    if(o.isInstanceOf[String]) {
      o.asInstanceOf[String]
    } else {
      null
    }
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
  private def extractRules(args: NamedList[_]): Map[String, PatternReplaceRule] = {
    var rules: Map[String, PatternReplaceRule] = new HashMap
    
    val ruleElements = args.getAll("rule").asScala
    for(ruleElement <- ruleElements) {
      
      if(!(ruleElement.isInstanceOf[NamedList[_]])) {
        logger.warn("Element with name attribute set to rule but it is not a <lst> element. Check your configuration.")
      } else { 
	try {
          val id = getStringElement(ruleElement.asInstanceOf[NamedList[_]], "id")
          if(null == id) {
            logger.warn("id not found for rule")
          } else {
            val pattern = getStringElement(ruleElement.asInstanceOf[NamedList[_]], "pattern")
            if(null == pattern) {
              logger.warn("pattern not found for rule")
            } else {
              val replace = getStringElement(ruleElement.asInstanceOf[NamedList[_]], "replace")
              if(null == replace) {
		logger.warn("replace not found for rule")
              } else {
		val rule = PatternReplaceRule.getInstance(id, pattern, replace)
		rules = rules.updated(id, rule)
		logger.info("Added rule: {}", rule.toString())
	      }
	    }
	  }
	} catch { 
	  case e: IllegalArgumentException => logger.warn("Unable to create rule for {}, error was {}", ruleElement.toString(), e.getMessage())
	}
      }
    }
    rules
  }

  /**
   * Extract field pattern replace rules..
   *
   * @param args NamedList with arguments as passed by the processor chain.
   * @return List of field pattern replace rules.
   */
  private def extractFieldRuleMappings(args: NamedList[_]): List[FieldPatternReplaceRules] = {
    val idRules: Map[String, PatternReplaceRule] = extractRules(args)
    
    var fieldPatternRules: Map[String, FieldPatternReplaceRules] = new HashMap
    
    val fieldsElement = args.get("fields")
    if(fieldsElement.isInstanceOf[NamedList[_]]) {
      //Iterator<Map.Entry<String, ?>> itr = (Iterator<Map.Entry<String, ?>>)((NamedList)fieldsElement).iterator();
      val itr = fieldsElement.asInstanceOf[NamedList[_]].iterator()
      while (itr.hasNext()) {
        val kv = itr.next()
        if(kv.getValue().isInstanceOf[String]) {
          val fieldName = kv.getKey()
	  val ruleId = kv.getValue().asInstanceOf[String]
	  
          // TODO: might need to check fieldName for null
	  
          // Make sure there is a FieldPatternReplaceRules attached to the
          // field in the fieldPatternRules
          var fr = fieldPatternRules.get(fieldName) match {
	    case None => { 
	      val fprr = new FieldPatternReplaceRules(fieldName)
	      fieldPatternRules = fieldPatternRules.updated(fieldName, fprr)
	      fprr
	    }
	    case Some(x) => x
	  }

          if(idRules.contains(ruleId)) {
            fr.add(idRules.get(ruleId).get)
          } else {
            logger.warn("Unknown rule id {}", String.valueOf(ruleId))
          }
        } else {
          logger.warn("Element in fields list not a <str> element [{}]", String.valueOf(kv))
        }
      }
    } else {
      logger.warn("Element with fields name attribute not a <lst> element. Check the configuration.")
    }
    fieldPatternRules.values.toList
  }

  /**
   * Get collection of field pattern replace rules.
   *
   * @return Unmodifiable collection of pattern replace rules.
   */
  def getFieldRules(): java.util.Collection[FieldPatternReplaceRules] = java.util.Collections.unmodifiableCollection(this.fieldPatternRules.asJava)

  /**
   * Init called by Solr processor chain.
   *
   * @param args NamedList of parameters set in the processor definition in solrconfig.xml
   */
  override def init(args: NamedList[_]) = {
    this.fieldPatternRules = extractFieldRuleMappings(args)
    if(logger.isInfoEnabled()) {
      for(fieldRules <- this.fieldPatternRules) {
        logger.info("Field [{}] configured with rule {}", fieldRules.getFieldName(), String.valueOf(fieldRules))
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
  override def getInstance(solrQueryRequest: SolrQueryRequest, solrQueryResponse: SolrQueryResponse, updateRequestProcessor: UpdateRequestProcessor): UpdateRequestProcessor = {
    new PatternReplaceProcessor(this.fieldPatternRules, updateRequestProcessor);
  }
}
