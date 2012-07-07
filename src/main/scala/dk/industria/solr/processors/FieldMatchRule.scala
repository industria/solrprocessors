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

class FieldMatchRule(field: String, pattern: String) {
  require(null != field)
  require(null != pattern)
  
  private val regexPattern = pattern.r

  def getField(): String = field

  def matches(fieldValue: String): Boolean = {
    if(null == fieldValue) return false;

    val m = regexPattern.findFirstMatchIn(fieldValue)
    return m.isDefined
  }

  override def toString = {
    val s = new StringBuilder(128)
    s.append(field)
    s.append(" =~ m/")
    s.append(regexPattern)
    s.append("/")
    s.toString()
  }
}
