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

/** Implements tests for the FieldPatternReplaceRules */
class FieldPatternReplaceRulesTest extends FunSuite {
  /** Testing for exception when trying construction with null as field name. */
  test("Create with null argument") {
    intercept[IllegalArgumentException] { new FieldPatternReplaceRules(null) }
  }

  /** Testing normal construction. */
  test("Create card") {
    assertResult("card") { 
      val fieldRules = new FieldPatternReplaceRules("card")
      fieldRules.fieldName 
    }
  }

  /** Testing for exception when trying construction with null as field name. */
  test("Add null rule") {
    intercept[IllegalArgumentException] {
      val rules = new FieldPatternReplaceRules("card")
      rules.add(null)
    }
  }

  /** Create a FieldPatternReplaceRules containing two rules.
   * 
   * @return FieldPatternReplaceRules
   */
  private def createRules(): FieldPatternReplaceRules =  {
    val rules = new FieldPatternReplaceRules("field")
    rules.add(PatternReplaceRule.getInstance("prefix", "^\\d{4}", "xxxx"))
    rules.add(PatternReplaceRule.getInstance("asterisk", "\\*", "-"))
    return rules
  }

  /** Test the replace method */
  test("Replace test") {
    assertResult("xxxx-3333-3333-1111") { 
      val rules = createRules()
      rules.replace("4444*3333*3333*1111")
    }
  }

  /** Testing calling replace with a null value. */
  test("Replace with null argument") {
    intercept[IllegalArgumentException] {
      val rules = createRules()
      rules.replace(null)
    }
  }
}
