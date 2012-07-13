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

import java.util.regex.{Matcher, Pattern, PatternSyntaxException}

/**
 * Represents a pattern replace rule for the PatternReplaceProcessor .
 * @param id Id used to identify the rule.
 * @param pattern Regular expression defining the pattern.
 * @param replacement Value to replace the pattern match with.
 */
class PatternReplaceRule(id: String, pattern: Pattern, replacement: String) {
  require(null != id)
  require(0 < id.length)
  require(null != pattern)
  require(null != replacement)

  /**
   * Get id of the rule.
   *
   * @return String containing the id of the rule.
   */
  def getId(): String = id

  /**
   * Apply the pattern replace rule to a value.
   * @param value Value to apply the pattern replace rule to.
   * @return Value after the rule has been applied to the value.
   */
  def replace(value: String): String = {
    require(null != value)
    
    val matcher = pattern.matcher(value)
    matcher.replaceAll(replacement)
  }


  /**
   * Get a String representation of the pattern replace rule.
   *
   * @return String representation of the pattern replace rule.
   */
  override def toString(): String = {
    val s = new StringBuilder()
    s.append("Id: [")
    s.append(this.id)
    s.append("] Pattern: [")
    s.append(this.pattern)
    s.append("] Replace: [")
    s.append(this.replacement)
    s.append("]")
    s.toString()
  }
}

object PatternReplaceRule {
  /**
   * Create a new PatternReplaceRule.
   *
   * @param id Id used to identify the rule in field mappings.
   * @param pattern String containing the regular expression defining the pattern to replace.
   * @param replacement String containing the value to replace pattern matches with. Null equals an empty string.
   * @return PatternReplaceRule
   * @throws IllegalArgumentException If iid is null or empty. If the pattern isn't a regular expression.
   */
  @throws(classOf[IllegalArgumentException])
  def getInstance(id: String, pattern: String, replacement: String): PatternReplaceRule = {
    require(null != id)
    require(0 < id.length)
    require(null != pattern)
    
    try {
      val compiledPattern = Pattern.compile(pattern)
      val replacementValue = Option(replacement).getOrElse("")
      new PatternReplaceRule(id, compiledPattern, replacementValue)
    } catch{ 
      case e: PatternSyntaxException => {
	val msg = "Failed to compile pattern [" + pattern + "] for rule id [" + id + "] : " + e.getMessage()
	throw new IllegalArgumentException(msg, e)
      }
    }
  }
}
