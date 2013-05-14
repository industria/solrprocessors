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
 * Implements tests for field matcher rule.
 */
class FieldMatchRuleTest extends FunSuite {
  /**
   * Test a legal FieldMatchRule getInstance call.
   */
  test("Get instance with legal parameters") {
    val rule = new FieldMatchRule("content_type", "default")
    assert(null != rule)
  }

  /**
   * Test an illegal getInstance where field is null.
   */
  test("Get instance with null field") {
    intercept[IllegalArgumentException] { new FieldMatchRule(null, "default") }
  }

  /**
   * Test an illegal getInstance where field is null.
   */
  test("Get instance with null pattern") {
    intercept[IllegalArgumentException] { new FieldMatchRule("content_type", null) }
  }

  /**
   * Test an illegal pattern which should throw an exception.
   */
  test("Get instance with illegal regular expression") {
    intercept[IllegalArgumentException] { new FieldMatchRule("content_type", "(fi+") }
  }

  /**
   * Test different match rules for two digits.
   */
  test("Match value") {
    val rule = new FieldMatchRule("content_type", "^\\d{2}$")
    assert(!rule.matches(null))
    assert(!rule.matches(""))
    assert(!rule.matches("a1b2"))
    assert(!rule.matches("a42x"))
    assert(rule.matches("42"))
  }

  /**
   * Testing toString format.
   */
  test("To string format") {
    expectResult("field =~ m/matchPattern/") {
      val fmr = new FieldMatchRule("field", "matchPattern")
      fmr.toString()
    }
  }
}
