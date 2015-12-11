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

import org.scalatest.FunSuite

import org.apache.solr.update.processor.UpdateRequestProcessor

import org.apache.solr.common.{SolrInputDocument, SolrInputField}

import org.apache.solr.common.util.NamedList

import org.apache.solr.update.AddUpdateCommand

/** Implements tests for AllowDisallowIndexingProcessor. */
class AllowDisallowIndexingProcessorTest extends FunSuite {
  /** Create a configuration with content_type default and news
    *
    * @param mode String to act as lst name attribute value
    * @return NamedList configured for mode.
    */
  private def createDefaultNewsConfig(mode: String): NamedList[NamedList[String]] = {
    val rules = new NamedList[String]()
    rules.add("content_type", "default")
    rules.add("content_type", "news")
    
    val args = new NamedList[NamedList[String]]()
    args.add(mode, rules)
    return args
  }

  /** Create a update request processor with init arguments.
    *
    * @param args NamedList containing the init arguments for the update request processor.
    * @param next UpdateRequestProcessor called if the processor passed the request on.
    * @return UpdateRequestProcessor based on the init arguments in args.
    */
  private def getProcessor(args: NamedList[NamedList[String]], next: UpdateRequestProcessor): UpdateRequestProcessor = {
    val factory = new AllowDisallowIndexingProcessorFactory()
    factory.init(args)
    factory.getInstance(null, null, next)
  }

  /** Create a Solr input document with fields header, content and content_type
    * where content_type is set by the content_type argument.
    *
    * @param contentType The content type field value.
    * @return SolrInputDocument with a content_type of the content_type argument.
    */
  private def createDocument(contentType: String): SolrInputDocument = {
    val document = new SolrInputDocument()
    
    document.addField("header", "Header without markup", 1f)
    document.addField("content", "<P>Content</P> <em>with</em> markup", 1f)
    document.addField("content_type", contentType)
  
    return document
  }

  test("NullPointerException when rule fields are not in the fields collection") {
    val document = new SolrInputDocument()
    document.addField("content_typex", "fisk")

    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = document

    val passRecorder = new AllowDisallowIndexingProcessorNext(null)
    val processor = getProcessor(createDefaultNewsConfig("allow"), passRecorder)
    processor.processAdd(cmd)
    assert(!passRecorder.called)
  }

  /** Checks the getInstance creates an UpdateRequestProcessor. */
  test("Instance creation") {
    val processor = getProcessor(createDefaultNewsConfig("allow"), null)
    assert(null != processor)
  }

  /** Allow mode field with a match and thereby catching a pass in the recorder */
  test("Document allow match") {
    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocument("default")
    val passRecorder = new AllowDisallowIndexingProcessorNext(null)
    val processor = getProcessor(createDefaultNewsConfig("allow"), passRecorder)
    processor.processAdd(cmd)
    assert(passRecorder.called)
  }

  /** Allow mode field without a match and thereby not catching a pass in the recorder */
  test("Document allow with no match") {
    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocument("person")
    val passRecorder = new AllowDisallowIndexingProcessorNext(null)
    val processor = getProcessor(createDefaultNewsConfig("allow"), passRecorder)
    processor.processAdd(cmd)
    assert(!passRecorder.called)
  }

  /** Disallow mode field with a match and thereby not catching a pass in the recorder */
  test("Document disallow match") {
    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocument("default")
    val passRecorder = new AllowDisallowIndexingProcessorNext(null)
    val processor = getProcessor(createDefaultNewsConfig("disallow"), passRecorder)
    processor.processAdd(cmd)
    assert(!passRecorder.called)
  }

  /** Disallow mode field without a match and thereby catching a pass in the recorder */
  test("Document disallow with no match") {
    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocument("person")
    val passRecorder = new AllowDisallowIndexingProcessorNext(null)
    val processor = getProcessor(createDefaultNewsConfig("disallow"), passRecorder)
    processor.processAdd(cmd)
    assert(passRecorder.called)
  }

}
