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

import org.apache.solr.common.SolrInputDocument 
import org.apache.solr.common.util.NamedList

import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor

/**
 * Implements tests for the PatternReplaceProcessor.
 */
class PatternReplaceProcessorTest extends FunSuite {
  /**
   * Create configuration argument for the below XML:
   *
   * <lst name="rule">
   * <str name="id">punctuation</str>
   * <str name="pattern">\p{P}</str>
   * <str name="replace"></str>
   * </lst>
   * <lst name="rule">
   * <str name="id">prefix</str>
   * <str name="pattern">^\d{4}</str>
   * <str name="replace">****</str>
   * </lst>
   * <lst name="fields">
   * <str name="title">punctuation</str>
   * <str name="name">punctuation</str>
   * <str name="comment">punctuation</str>
   * <str name="card">prefix</str>
   * <str name="clean">punctuation</str>
   * <str name="clean">prefix</str>
   * </lst>
   *
   * @return NamedList containing the above configuration.
   */
  private def createConfig(): NamedList[NamedList[String]] =  {
    val punctuation = new NamedList[String]()
    punctuation.add("id", "punctuation")
    punctuation.add("pattern", "\\p{P}")
    punctuation.add("replace", "")
    
    val prefix = new NamedList[String]()
    prefix.add("id", "prefix")
    prefix.add("pattern", "^\\d{4}")
    prefix.add("replace", "****")
    
    val fields = new NamedList[String]()
    fields.add("title", "punctuation")
    fields.add("name", "punctuation")
    fields.add("comment", "punctuation")
    fields.add("card", "prefix")
    fields.add("clean", "punctuation")
    fields.add("clean", "prefix")
    
    val args = new NamedList[NamedList[String]]();
    args.add("rule", punctuation)
    args.add("rule", prefix)
    args.add("fields", fields)
    return args
  }

  /**
   * Create a SolrInputDocument.
   *
   * @return SolrInputDocument with fields header and content.
   */
  private def createDocumentWithCardField(): SolrInputDocument = {
    val document = new SolrInputDocument()
    
    document.addField("header", "Header without markup", 1f)
    document.addField("content", "Content with markup", 1f)
    document.addField("card", "3333-1111-2222-3333", 1f)
    document.addField("comment", "There, is. punctuation!!", 1f)
    document.addField("clean", "3333-1111-2222-3333", 1f)
    
    return document
  }

  test("Pattern replace processor run") {
    val factory = new PatternReplaceProcessorFactory()
    factory.init(createConfig())
    val processor = factory.getInstance(null, null, null)
    
    val cmd = new AddUpdateCommand(null)
    cmd.solrDoc = createDocumentWithCardField()
    
    processor.processAdd(cmd);
    assertResult("Header without markup") { cmd.solrDoc.getFieldValue("header") }
    assertResult("Content with markup") { cmd.solrDoc.getFieldValue("content") }
    assertResult("****-1111-2222-3333") { cmd.solrDoc.getFieldValue("card") }
    assertResult("There is punctuation") { cmd.solrDoc.getFieldValue("comment") }
    assertResult("****111122223333") { cmd.solrDoc.getFieldValue("clean") }
  }
}
