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

/**
 * Implements a field with a list of pattern replace rules attached..
 * @param fieldName Name of the fields the rules should be attached to.
 */
class FieldPatternReplaceRules(val fieldName: String) {
  require(null != fieldName)
  /**
   * List of pattern replace rules attached to the field.
   * @see PatternReplaceRule
   */
  private var _rules: List[PatternReplaceRule] = Nil

  /**
   * Get pattern replace rules attached to the field.
   *
   * @return List of pattern replace rules for the field.
   */
  def rules: List[PatternReplaceRule] = _rules

  /**
   * Add a pattern replace rule to the field.
   *
   * @param rule PatternReplaceRule to add to the field.
   * @throws IllegalArgumentException if the rule is null.
   */
  def add(rule: PatternReplaceRule): Unit = {
    require(null != rule)
    val ruleList = rule :: _rules
    _rules = ruleList.reverse
  }

  /**
   * Replace patterns in the value according to the rules.
   *
   * @param value Value to apply the pattern replace rules to.
   * @return New value after pattern replace rules have been applied.
   * @throws IllegalArgumentException if called with a null value.
   */
  def replace(value: String): String = {
    require(null != value)
    (value /: _rules)((v, rule) => rule.replace(v))
  }
}
