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

import org.apache.solr.common.util.NamedList;

import org.scalatest.FunSuite

class HTMLStripCharFilterProcessorFactoryTest extends FunSuite {
  /** Get list with header and content strings.
   *
   * @return List of strings containing header and content.
   */
  private def expectedHeaderContent(): List[String] = List("header", "content")

  test("Test simple legal configuration of the factory with fields") {
    val input = new NamedList[Any]
    input.add("field", "header")
    input.add("field", "content")
    input.add("ignored", 100)

    val factory = new HTMLStripCharFilterProcessorFactory()
    factory.init(input)
    assert(expectedHeaderContent() == factory.fields)
  }

  test("Test with illegal types in the arguments list similar to Solr configuration with an element other than str list") {
    val input = new NamedList[Any]
    input.add("field", "header")
    input.add("field", 100)
    input.add("field", "content")

    val factory = new HTMLStripCharFilterProcessorFactory()
    factory.init(input)
    assert(expectedHeaderContent() == factory.fields)
  }

  test("Test with illegal types in the arguments list similar to Solr configuration with an element other than str") {
    val input = new NamedList[Any]
    input.add("field", 1L)
    input.add("field", 100)
    input.add("field", new NamedList[Any])
    input.add("field", "")
    input.add("field", "  ")
    input.add("field", null)

    val factory = new HTMLStripCharFilterProcessorFactory()
    factory.init(input)
    assert(0 == factory.fields.size)
  }

  test("Make sure calling getFields before init does break things") {
    val factory = new HTMLStripCharFilterProcessorFactory()
    assert(0 == factory.fields.size)
  }

  test("Test the the space normalization is the default") {
    val input = new NamedList[Any]
    input.add("field", "header")
    input.add("field", "content")

    val factory = new HTMLStripCharFilterProcessorFactory()
    factory.init(input)
    assert(factory.normalize)
  }

  test("Test the the space normalization is turned on") {
    val input = new NamedList[Any]
    input.add("field", "header")
    input.add("field", "content")
    input.add("normalize", true)

    val factory = new HTMLStripCharFilterProcessorFactory()
    factory.init(input)
    assert(factory.normalize)
  }

  test("Test the space normalization is turned off") {
    val input = new NamedList[Any]
    input.add("field", "header")
    input.add("field", "content")
    input.add("normalize", false)

    val factory = new HTMLStripCharFilterProcessorFactory()
    factory.init(input)
    assert(!factory.normalize)
  }

}
