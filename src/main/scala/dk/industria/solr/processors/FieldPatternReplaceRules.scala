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

import scala.collection.JavaConverters._

/**
 * Implements a field with a list of pattern replace rules attached..
 * @param fieldName Name of the fields the rules should be attached to.
 */
class FieldPatternReplaceRules(fieldName: String) {
  require(null != fieldName)
  /**
   * List of pattern replace rules attached to the field.
   * @see PatternReplaceRule
   */
  var rules: List[PatternReplaceRule] = Nil

    /**
     * Get the field name .
     * @return Field name.
     */
  def getFieldName(): String = fieldName

  /**
   * Get pattern replace rules attached to the field.
   *
   * @return List of pattern replace rules for the field.
   */
  def getRules(): java.util.List[PatternReplaceRule] = rules.asJava 

  /**
   * Add a pattern replace rule to the field.
   *
   * @param rule PatternReplaceRule to add to the field.
   * @throws IllegalArgumentException if the rule is null.
   */
  def add(rule: PatternReplaceRule) = {
    require(null != rule)
    val ruleList = rule :: rules
    rules = ruleList.reverse
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
    var newValue = value
    for(rule <- rules) {
      newValue = rule.replace(newValue)
    }
    newValue;
  }
}
