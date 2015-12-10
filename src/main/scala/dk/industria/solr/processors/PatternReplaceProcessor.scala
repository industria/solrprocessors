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

import java.io.IOException
import java.util.{ArrayList, Collection}

import org.apache.solr.client.solrj.request.UpdateRequest

import org.apache.solr.common.{SolrInputDocument, SolrInputField}

import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor

import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class PatternReplaceProcessor(fieldPatternRules: List[FieldPatternReplaceRules], next: UpdateRequestProcessor) extends UpdateRequestProcessor(next) {
  /**
   * Logger
   */
  private val logger = LoggerFactory.getLogger(getClass())

  /**
   * Called by the processor chain on document add/update operations.
   *
   * @param cmd AddUpdateCommand
   * @throws IOException
   */
  @throws(classOf[IOException])
  override def processAdd(cmd: AddUpdateCommand) =  {
    val document = cmd.getSolrInputDocument()

    for(fieldRules <- this.fieldPatternRules) {
      val fieldName = fieldRules.fieldName
      logger.debug("Processing field: {}", fieldName)

      val field = document.getField(fieldName)
      if (null != field) {
        val values = field.getValues()
        if (null != values) {
          val newValues: Collection[Object] = new ArrayList[Object]()
          for (value <- values.asScala) {
            if (value.isInstanceOf[String]) {
              var newValue = fieldRules.replace(value.asInstanceOf[String])
              newValues.add(newValue);
            } else {
              newValues.add(value);
            }
          }
          val boost = field.getBoost()
          field.setValue(newValues, boost)
        }
      }
    }
    super.processAdd(cmd)
  }
}
