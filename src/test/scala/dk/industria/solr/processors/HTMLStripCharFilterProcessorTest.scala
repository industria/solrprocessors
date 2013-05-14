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


import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.update.processor.UpdateRequestProcessor;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.update.AddUpdateCommand;

import org.scalatest.FunSuite

class HTMLStripCharFilterProcessorTest extends FunSuite {
  /** Configure a UpdateRequestProcessor to read the fields header and content.
   *
   * @param normalize Indicate if spaces should be normalized as part of the filter processor.
   * @return UpdateRequestProcessor (HTMLStripCharFilterProcessor)
   */
  private def headerContentProcessor(normalize: Boolean): UpdateRequestProcessor = {
    val input = new NamedList[Any]
    input.add("field", "header")
    input.add("field", "content")
    if(!normalize) {
      input.add("normalize", false);
    }

    val factory = new HTMLStripCharFilterProcessorFactory()
    factory.init(input)

    factory.getInstance(null, null, null)
  }

  /**Create a SolrInputDocument containing two fields header and content
   * Field header : Doesn't contain any markup
   * Field content : Contains markup
   *
   * @return SolrInputDocument with fields header and content.
   */
  private def createDocumentWithMarkup(): SolrInputDocument = {
    val document = new SolrInputDocument()
    document.addField("header", "Header without markup", 1f)
    document.addField("content", "<P>Content</P> <em>with</em> markup", 1f)
    document
  }

  /** Create a SolrInputDocument containing two fields header and content
   * Field header : Doesn't contain any markup
   * Field content : Contains markup in multiple values
   *
   * @return SolrInputDocument with fields header and content.
   */
  private def createDocumentWithMarkupMultipleValue(): SolrInputDocument = {
    val document = new SolrInputDocument()
    document.addField("header", "Header without markup", 1f)
    val c = new ArrayList[Any]
    c.add("<P>Content</P> <em>with</em> markup")
    c.add("<script>function t() { return f;}</script>second value")
    document.addField("content", c, 1f)
    document
  }

  /** Create a SolrInputDocument containing two fields header and content
   * Field header : Doesn't contain any markup
   * Field content : Contains markup in multiple values
   *
   * @return SolrInputDocument with fields header and content.
   */
  private def createDocumentWithMarkupMultipleValueNoBreakSpaces(): SolrInputDocument = {
    val document = new SolrInputDocument()
    document.addField("header", "Header without markup", 1f);
    val c = new ArrayList[Any]
    c.add("<P>Content</P> <em>with</em> markup")
    c.add("<script>function t() { return f;}</script>second value")
    c.add("this\u00A0has\u00A0no-break\u00A0spaces")
    document.addField("content", c, 1f)
    document
  }

  test("Checks the getInstance creates an UpdateRequestProcessor") {
    val processor = headerContentProcessor(true)
    assert(null != processor)
  }

  /** Process a document where header doesn't contain any markup but the content does
   * see createDocumentWithMarkup for document content.
   */
  test("markupNoneHeaderInContent") {
    val processor = headerContentProcessor(true)

    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocumentWithMarkup()

    processor.processAdd(cmd)

    expect("Header without markup") { cmd.solrDoc.getFieldValue("header").asInstanceOf[String] }

    expect("Content with markup") { cmd.solrDoc.getFieldValue("content").asInstanceOf[String] }
  }

  /** Process a document where header doesn't contain any markup but the content does
   * see createDocumentWithMarkupMultipleValue for document content.
   */
  test("markupNoneHeaderInContentMultivalued") {
    val processor = headerContentProcessor(true)
    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocumentWithMarkupMultipleValue()

    processor.processAdd(cmd)

    val actualHeader = cmd.solrDoc.getFieldValue("header").asInstanceOf[String]
    assert(actualHeader.equals("Header without markup"))
    val actualContent = cmd.solrDoc.getFieldValues("content")
    val rgActual = actualContent.toArray()
    val rgExpected = Array("Content with markup", "second value")
    for(i <- 0 to rgActual.length - 1) {
      assert(rgActual(i) == rgExpected(i))
    }
  }

  /** Process a document where header doesn't contain any markup the content does
   * and it also contains no-break spaces
   * see createDocumentWithMarkupMultipleValueNoBreakSpaces for document content.
   */
  test("markupNoneHeaderInContentMultivaluedNoBreak") {
    val processor = headerContentProcessor(true)

    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocumentWithMarkupMultipleValueNoBreakSpaces()

    processor.processAdd(cmd);

    val actualHeader = cmd.solrDoc.getFieldValue("header").asInstanceOf[String]
    assert(actualHeader.equals("Header without markup"))
    val actualContent = cmd.solrDoc.getFieldValues("content")
    val rgActual = actualContent.toArray()
    val rgExpected = Array("Content with markup", "second value", "this has no-break spaces")
    
    for (i <- 0 to rgActual.length - 1) {
      assert(rgActual(i) == rgExpected(i))
    }
  }


  /** Process a document where header doesn't contain any markup but the content does
   * but the space normalization is turned off.
   * see createDocumentWithMarkup for document content.
   */
  test("markupNoneHeaderInContentNoNormalization") {
    val processor = headerContentProcessor(false)

    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocumentWithMarkup()

    processor.processAdd(cmd);

    expect("Header without markup") { cmd.solrDoc.getFieldValue("header").asInstanceOf[String] }

    expect("\nContent\n with markup") { cmd.solrDoc.getFieldValue("content").asInstanceOf[String] }
  }

}
