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

/** Represents a rule for matching field values
  *
  * Used by the @see AllowDisallowIndexingProcessor.
  *
  * @param field Name of the field to match the pattern against.
  * @param patternToMatch Regular expression to match against the field.
  */
class FieldMatchRule(val field: String, patternToMatch: String) {
  require(Option(field).isDefined)
  require(Option(patternToMatch).isDefined)

  /** Pattern to match against the field. */
  private val pattern = patternToMatch.r

  /** Matches the field value against the pattern.
    *
    * @param fieldValue Value to test the pattern against.
    * @return True if the pattern matches the field value.
    */
  def matches(fieldValue: String): Boolean = Option(fieldValue).flatMap(pattern.findFirstMatchIn(_)).isDefined

  /** Returns a String representation of the field match rule.
    *
    * @return String representing the rule.
    */
  override def toString(): String = s"$field =~ m/${pattern}/"
}
