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
 * Represents a rule for matching field values
 * <p>Used by the @see AllowDisallowIndexingProcessor.</p>.
 */
class FieldMatchRule(field: String, pattern: String) {
  require(null != field)
  require(null != pattern)
  
  /**
   * Pattern to match against the field.
   */
  private val _pattern = pattern.r

  /**
   * Get the field name of the match rule.
   * @return Field name of the match field.
   */ 
  def field(): String = field

  /**
   * Matches the field value against the pattern.
   * @param fieldValue Value to test the pattern against.
   * @return True if the pattern matches the field value.
   */ 
  def matches(fieldValue: String): Boolean = {
    if(null == fieldValue) return false;

    val m = _pattern.findFirstMatchIn(fieldValue)
    m.isDefined
  }

  /**
   * Returns a String representation of the field match rule.
   * @return String representing the rule.
   */
  override def toString = {
    val s = new StringBuilder(128)
    s.append(field)
    s.append(" =~ m/")
    s.append(_pattern)
    s.append("/")
    s.toString()
  }
}
