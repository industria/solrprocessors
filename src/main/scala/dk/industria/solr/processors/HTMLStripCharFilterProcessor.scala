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

import java.io.{BufferedReader, IOException, Reader, StringReader}
import java.util.{ArrayList, Collection}

import org.slf4j.LoggerFactory;

import org.apache.lucene.analysis.{CharReader, CharStream}

import org.apache.solr.analysis.HTMLStripCharFilter

import org.apache.solr.common.{SolrInputDocument, SolrInputField}

import org.apache.solr.update.AddUpdateCommand

import org.apache.solr.update.processor.UpdateRequestProcessor

import scala.collection.JavaConverters._

/**
 * Implements an UpdateRequestProcessor for running Solr HTMLStripCharFilter on
 * select document fields before they are stored.
 * <p/>
 *  In addition to running the HTMLStringCharFilter it also space normalizes the fields
 *  by removing no-break spaces, trimming leading and trailing spaces and finally
 *  replaces multiple recurring spaces with a single space.
 * <p/>
 * For more information on configuration @see HTMLStripCharFilterProcessorFactory
 * 
 * @param fieldsToProcess List of field names to process.
 * @param spaceNormalize Set to true if field values should be space normalized.
 * @param next   Next UpdateRequestProcessor in the processor chain.
 */
class HTMLStripCharFilterProcessor(fieldsToProcess: List[String], spaceNormalize: Boolean, next: UpdateRequestProcessor) extends UpdateRequestProcessor(next) {
  /**
   * Logger
   * UpdateRequestProcessor has it's own log variable tied to the UpdateRequestProcessor class,
   * which makes controlling log output from this project difficult unless a different
   * logger is used as in this case.
   */
  private val logger = LoggerFactory.getLogger(getClass())
  /**
   * Size of the buffer used to read the input through the HTMLStripCharFilter.
   */
  private val BUFFER_SIZE = 4096;
  /**
   * Space normalizes the string by changing no-break space into normal spaces,
   * trimming the string for leading and trailing spaces and finally removing
   * duplicate spaces from the string.
   *
   * @param text String to space normalize.
   * @return String with normalized spaces.
   */
  private def normalizeSpace(text: String): String = {
    // Replace no-break space
    val noBreakRemoved = text.replaceAll("\u00A0", " ")
    val trimmed = noBreakRemoved.trim()
    // Replace multiple recurring spaces with a single space
    trimmed.replaceAll("\\p{Space}{2,}", " ")
  }

  /**
   * Strip HTML/XML from string by reading it through the Solr HTMLStripCharFilter.
   *
   * @param text String containing HTML/XML to be stripped.
   * @return String with HTML/XML removed.
   * @throws IOException  if reading the string through the HTMLStripCharFilter.
   */
  @throws(classOf[IOException])
  private def runHtmlStripCharFilter(text: String): String = {
    val stripped = new StringBuilder(BUFFER_SIZE)
    try {
      val buffer: Array[Char] = new Array(BUFFER_SIZE)
      
      var r: Reader = new StringReader(text)
      if (!r.markSupported()) {
        logger.debug("Reader returned false for mark support, wrapped in BufferedReader.")
        r = new BufferedReader(r)
      }
      
      val cs: CharStream = CharReader.get(r)
      val filter = new HTMLStripCharFilter(cs)

      var nCharsRead = filter.read(buffer)
      while (-1 != nCharsRead) {
        if (0 < nCharsRead) {
          stripped.appendAll(buffer, 0, nCharsRead)
        }
        nCharsRead = filter.read(buffer)
      }
      filter.close()
    } catch {
      case e: IOException => {
	logger.error("IOException thrown in HTMLStripCharFilter: {}", e.toString())
	throw e
      }
    }
    stripped.toString()
  }

  /**
   * Called by the processor chain on document add/update operations.
   * This is where we process the fields configured before they are indexed.
   *
   * @param cmd AddUpdateCommand
   * @throws IOException
   */
  @throws(classOf[IOException])
  override def processAdd(cmd: AddUpdateCommand) = {
    val doc = cmd.getSolrInputDocument()
    for (fieldName <- this.fieldsToProcess) {
      logger.debug("Processing field: {}", fieldName)
      val field = doc.getField(fieldName)
      if (null != field) {
	val values: Collection[Object] = field.getValues()
	if (null != values) {
	  val newValues: Collection[Object] = new ArrayList[Object]()
	  for (value <- values.asScala) {
            if (value.isInstanceOf[String]) {
              var newValue = runHtmlStripCharFilter(value.asInstanceOf[String])
              if(this.spaceNormalize) {
		newValue = normalizeSpace(newValue)
              }
              newValues.add(newValue)
            } else {
              newValues.add(value)
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
