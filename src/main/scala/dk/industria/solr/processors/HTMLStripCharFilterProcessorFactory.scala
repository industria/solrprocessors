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

/**
 * Implements a factory for the HTMLStripCharFilterProcessor
 * <p/>
 * The main purpose of this is to process the init arguments into a list
 * of fields that should be processed with the HTMLStripCharFilterProcessor.
 * <p/>
 * Configuration is done by placing str elements with a name attribute set to field
 * and a element value set to the name of the field that should be processed.
 * <p/>
 * The space normalization done by the processor can be turned off by placing
 * a bool element with name attribute of normalize set to false.
 * <p/>
 * Example processor configuration for processing fields header and content:
 * <p/>
 * <pre>
 * {@code
 * <processor class="dk.industria.solr.processors.HTMLStripCharFilterProcessorFactory">
 *   <str name="field">header</str>
 *   <str name="field">content</str>
 *   <bool name="normalize">true</bool>
 * </processor>
 * }
 * </pre>
 */
class HTMLStripCharFilterProcessorFactory extends UpdateRequestProcessorFactory {
  /** Logger */
  private val logger = LoggerFactory.getLogger(getClass())

  /** List of fields configured for HTML character stripping. */
  private var fieldsToProcess: List[String] = Nil

  /** Indicates if spaces should be normalized after running HTMLStripCharFilter. */
  private var spaceNormalize = true
  
  /** Generate a string containing the fields configured, the string is
   * on the form {field1} {field2} ... {fieldN}
   *
   * @param fields The fields for the field string.
   * @return String on the form {field1} {field2} ... {fieldN}
   */
  private def configuredFieldsString(fields: List[String]): String = {
    val s = new StringBuilder(256)
    fields foreach { s.append(" {").append(_).append("}") } 
    s.toString()
  }

  /** Extract field names from init arguments.
   * That is fields with a key of field and a type of String.
   *
   * @param initArguments NamedList containing the init arguments.
   * @return List of field names.
   */
  private def extractFields(initArguments: NamedList[_]): List[String] = {
    val fields = initArguments.getAll("field").asScala.toList
    fields filter { _.isInstanceOf[String] } map { _.asInstanceOf[String].trim } filter { 0 < _.length }
  }

  /** Extract space normalization setting from boolean with key normalize.
   * <p/>
   * If a bool element with normalize name attribute does not exists in
   * the arguments it will default to true.
   *
   * @param args NamedList containing the init arguments.
   * @return True if space normalization should be turned on.
   */
  private def extractSpaceNormalization(args: NamedList[_]): Boolean = {
    Option(args.get("normalize")).filter(_.isInstanceOf[Boolean]).map(_.asInstanceOf[Boolean]).getOrElse(true)
  }

  /** Get the list of field names configured for processing.
   * Used for Java test cases.
   *
   * @return List of field names configured.
   */
  def fields: List[String] = fieldsToProcess

  /** Get space normalization setting.
   *
   * @return True is space normalization should be performed in the processor.
   */
  def normalize: Boolean = spaceNormalize
  
  /** Init called by Solr processor chain
   * The values configured for name attribute field are extracted to fieldsToProcess.
   *
   * @param args NamedList of parameters set in the processor definition in solrconfig.xml
   */
  override def init(args: NamedList[_]) = {
    spaceNormalize = extractSpaceNormalization(args)
    logger.debug("Configured with space normalization set to: {}", spaceNormalize)
    
    fieldsToProcess = extractFields(args)
    logger.debug("Configured with fields [{}]", configuredFieldsString(fieldsToProcess))
    
    if (fieldsToProcess.isEmpty) {
      logger.warn("No fields configured. Consider removing the processor.")
    }
  }

  /** Factory method for the HTMLStripCharFilterProcessor called by SOLR processor chain.
   *
   * @param solrQueryRequest SolrQueryRequest
   * @param solrQueryResponse SolrQueryResponse
   * @param updateRequestProcessor UpdateRequestProcessor
   * @return Instance of HTMLStripCharFilterProcessor initialized with the fields to process.
   */
  override def getInstance(solrQueryRequest: SolrQueryRequest, solrQueryResponse: SolrQueryResponse , updateRequestProcessor: UpdateRequestProcessor): UpdateRequestProcessor = new HTMLStripCharFilterProcessor(fieldsToProcess, spaceNormalize, updateRequestProcessor)
}
