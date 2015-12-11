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

import scala.collection.JavaConverters._

import scala.language.existentials
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
class AllowDisallowIndexingProcessorFactory extends UpdateRequestProcessorFactory {
  /** Logger */
  private val logger = LoggerFactory.getLogger(getClass)

  /** Mode configured */
  private var _mode = AllowDisallowMode.Unknown

  /** List of field match rules configured. */
  private var _rules: List[FieldMatchRule] = Nil

  /** Get the NamedList associated with a key.
    *
    * @param args The NamedList to look for the key.
    * @param key  The key to look for in the list.
    * @return Option NamedList associated with the key.
    */
  private def getConfiguredList(args: NamedList[_], key: String): Option[NamedList[_]] = {
    Option(args.get(key)).filter(_.isInstanceOf[NamedList[_]]).map(_.asInstanceOf[NamedList[_]])
  }

  /** Converts the raw NamedList field match configuration to a list of FieldMatchRule.
    *
    * @param configuration The NamedList of allow/disallow lst element (solrconfig.xml).
    * @return List of FieldMatchRule items.
    */
  private def getFieldMatchRules(configuration: NamedList[_]): List[FieldMatchRule] = {
    var rules: List[FieldMatchRule] = Nil
    val itr = configuration.iterator().asScala
    while (itr.hasNext) {
      val kv = itr.next
      val key = kv.getKey()
      if ((null == key) || (0 == key.trim().length())) {
        logger.warn("Item missing name attribute: {}", kv)
      } else {
        val oValue = kv.getValue()
        if (!oValue.isInstanceOf[String]) {
          logger.warn("Item not a <str> element: {}", kv)
        } else {
          val value = oValue.asInstanceOf[String].trim()
          if (0 == value.length()) {
            logger.warn("Item trimmed value is empty: {}", kv)
          } else {
            try {
              val rule = new FieldMatchRule(key, value)
              rules = rule :: rules
              logger.debug("Added FieldMatchRule : {}", rule)
            } catch {
              case e: IllegalArgumentException => logger.warn("Couldn't create FieldMatchRule: {}", e.getMessage())
            }
          }
        }
      }
    }
    logger.info("Rules configured: {}", rules)
    rules.reverse
  }

  /** Get the name of the unique key defined for the schema.
    *
    * @param request SolrQueryRequest
    * @return Option String containing the name of the schema unique key.
    */
  private def uniqueKey(request: SolrQueryRequest): Option[String] = {
    Option(request) match {
      case Some(r) => {
        val schema = r.getCore().getLatestSchema()
        Option(schema.getUniqueKeyField()).map(_.getName())
      }
      case _ => None
    }
  }

  /** Get the configured mode of operation.
    *
    * @return Mode of operation as a AllowDisallowMode enum.
    */
  def mode: AllowDisallowMode.Value = _mode

  /** Get the list of field match rules configured.
    *
    * @return List of rules.
    */
  def rules: List[FieldMatchRule] = _rules

  /** Init called by Solr processor chain
    *
    * @param args NamedList of parameters set in the processor definition in solrconfig.xml
    */
  override def init(args: NamedList[_]) {
    val allow = getConfiguredList(args, "allow")
    if (allow.isDefined) {
      logger.debug("Running with allow semantics: {}", allow.get)
      _mode = AllowDisallowMode.Allow
      _rules = getFieldMatchRules(allow.get)
    } else {
      val disallow = getConfiguredList(args, "disallow")
      if (disallow.isDefined) {
        logger.debug("Running with disallow semantics: {}", disallow.get)
        _mode = AllowDisallowMode.Disallow
        _rules = getFieldMatchRules(disallow.get)
      } else {
        logger.warn("No rules configured for the processor. Consider removing it from chain.")
        _mode = AllowDisallowMode.Unknown;
      }
    }
  }

  /** Factory method for the AllowDisallowIndexingProcessor called by Solr processor chain.
    *
    * @param solrQueryRequest SolrQueryRequest
    * @param solrQueryResponse SolrQueryResponse
    * @param updateRequestProcessor UpdateRequestProcessor
    * @return Instance of AllowDisallowIndexingProcessor initialized with the fields to process.
    */
  override def getInstance(solrQueryRequest: SolrQueryRequest,
                           solrQueryResponse: SolrQueryResponse,
                           updateRequestProcessor: UpdateRequestProcessor): UpdateRequestProcessor = {
    val uniqueKeyField = uniqueKey(solrQueryRequest)
    new AllowDisallowIndexingProcessor(_mode, _rules, uniqueKeyField, updateRequestProcessor)
  }
}
