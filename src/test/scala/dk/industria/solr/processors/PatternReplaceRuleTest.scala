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

/**
 * Implements tests for PatternReplaceRule.
 */
class PatternReplaceRuleTest extends FunSuite {
  /**
   * Test creation of a valid PatterReplaceRule.
   */
  test("Create legal instance") {
    val rule = PatternReplaceRule.getInstance("prefix_rule", "\\p{P}", " ")
    assert(null != rule)
  }

  /**
   * Testing null as id.
   */
  test("Create Illegal null id instance") {
    intercept[IllegalArgumentException] {
      PatternReplaceRule.getInstance(null, "legal", "")
    }
  }

  /**
   * Testing empty id
   */
  test("Create illegal empty id instance") {
    intercept[IllegalArgumentException] {
      PatternReplaceRule.getInstance("", "legal", "")
    }
  }

  /**
   * Testing null pattern.
   */
  test("Create illegal null pattern instance") {
    intercept[IllegalArgumentException] {
      PatternReplaceRule.getInstance("id", null, "")
    }
  }

  /**
   * Testing illegal pattern (missing closing } so it will not compile.
   */
  test("Create illegal non legal regular expression instance") {
    intercept[IllegalArgumentException] {
      PatternReplaceRule.getInstance("id", "(\\d+", "")
    }
  }

  /**
   * Test the string representation
   */
  test("To string test") {
    assertResult("Id: [id] Pattern: [(\\d+)] Replace: [?]") {
      val r = PatternReplaceRule.getInstance("id", "(\\d+)", "?")
      r.toString()
    }
  }

  /**
   * Testing replace with a match
   */
  test("Replace test") {
    assertResult("1111-2222-3333-xxxx") {
      val r = PatternReplaceRule.getInstance("id", "\\d{4}$", "xxxx")
      r.replace("1111-2222-3333-4444")
    }
  }

  /**
   * Testing replace with a non matching pattern
   */
  test("Replace with non matching pattern") {
    assertResult("1111-2222-3333-4444") {
      val r = PatternReplaceRule.getInstance("id", "\\d{6}$", "xxxx")
      r.replace("1111-2222-3333-4444")
    }
  }

  /**
   * Testing replace with a null value
   */
  test("Replace with null argument") {
    intercept[IllegalArgumentException] {
      val r = PatternReplaceRule.getInstance("id", "\\d{6}$", "xxxx")
      r.replace(null)
    }
  }
}
