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

import org.scalatest.FunSuite

/** Implements tests for the PatternReplaceProcessorFactory. */
class PatternReplaceProcessorFactoryTest extends FunSuite {
  /** Create configuration argument for the below XML:
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
   * <str name="clean">prefix</str>
   * <str name="clean">punctuation</str>
   * </lst>
   *
   * @return NamedList containing the above configuration.
   */
  private def createLegalConfig(): NamedList[NamedList[String]] = {
    val punctuation = new NamedList[String]
    punctuation.add("id", "punctuation")
    punctuation.add("pattern", "\\p{P}")
    punctuation.add("replace", "")

    val prefix = new NamedList[String]
    prefix.add("id", "prefix")
    prefix.add("pattern", "^\\d{4}")
    prefix.add("replace", "****")

    val fields = new NamedList[String]
    fields.add("title", "punctuation")
    fields.add("name", "punctuation")
    fields.add("comment", "punctuation")
    fields.add("card", "prefix")
    fields.add("clean", "prefix")
    fields.add("clean", "punctuation")

    val args = new NamedList[NamedList[String]]
    args.add("rule", punctuation)
    args.add("rule", prefix)
    args.add("fields", fields)
    args
  }

  test("notInitializedReturnsFieldRulesP") {
    val factory = new PatternReplaceProcessorFactory()
    val fields = factory.fieldRules
    assert(0 == fields.size)
  }

  test("configTest") {
    val factory = new PatternReplaceProcessorFactory()
    factory.init(createLegalConfig())

    val expectedFields = List("title", "name", "comment", "card", "clean")

    val fr = factory.fieldRules

    assert(expectedFields.size == fr.size)

    for(r: FieldPatternReplaceRules <- fr) {
      val fn = r.fieldName
      if(fn.equals("card")) {
        val cardList = r.rules
        assert(1 == cardList.size)
        assert("prefix".equals(cardList(0).getId()))
      } else if(fn.equals("title")) {
        val titleList = r.rules
        assert(1 == titleList.size)
        assert("punctuation".equals(titleList(0).getId()))
      } else if(fn.equals("clean")) {
        val cleanList = r.rules
        assert(2 == cleanList.size)
        assert("prefix".equals(cleanList(0).getId()))
        assert("punctuation".equals(cleanList(1).getId()))
      }
    }
  }
}
