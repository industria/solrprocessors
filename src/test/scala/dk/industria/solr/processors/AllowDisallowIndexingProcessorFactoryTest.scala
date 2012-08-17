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

import org.apache.solr.common.util.NamedList;

import org.scalatest.FunSuite

/** Implements tests for AllowDisallowIndexingProcessorFactory. */
class AllowDisallowIndexingProcessorFactoryTest extends FunSuite {
  /** Create a configuration for allow mode.
   *
   * @return NamedList configured for allow mode.
   */
  private def createAllowConfig():NamedList[NamedList[String]] =  {
    val rules = new NamedList[String]
    rules.add("content_type", "default")
    
    val args = new NamedList[NamedList[String]]
    args.add("allow", rules)
    args
  }

  /** Create a configuration for allow mode with illegal rules
   * that should be filtered by the factory.
   *
   * @return NamedList configured for allow mode with illegal rules..
   */
  private def createAllowWithIllegalConfig(): NamedList[_] = {
    val rules = new NamedList[Any]
    rules.add("content_type", "default")
    rules.add("content_type", "news")
    rules.add("illegalInteger", 100)
    rules.add(null, null)
    rules.add("list_type", new NamedList())
    rules.add(null, "unnamed")
    rules.add("empty", "")
    rules.add("value_null", null)
    rules.add("", "EmptyStringAttribute")
    rules.add("content_type", "(fi+")

    val args = new NamedList[NamedList[_]]
    args.add("allow", rules)
    args
  }

  /** Create a configuration for disallow mode.
   *
   * @return NamedList configured for disallow mode.
   */
  private def createDisallowConfig(): NamedList[NamedList[Any]] = {
    val rules = new NamedList[Any]

    val args = new NamedList[NamedList[Any]]
    args.add("disallow", rules)
    args
  }

  /** Create a configuration for with name other that allow or disallow (no_mode)  mode.
   *
   * @return NamedList configured for no mode.
   */
  private def createUnknownValueConfig(): NamedList[_] = {
    val rules = new NamedList[Any]
    val args = new NamedList[NamedList[Any]]
    args.add("no_mode", rules)
    args
  }

  /** Get an initialized factory
   *
   * @param args The arguments to initialize the factory with.
   * @return AllowDisallowIndexingProcessorFactory initialized with args.
   */
  private def initializedFactory(args: NamedList[_]): AllowDisallowIndexingProcessorFactory = {
    val factory = new AllowDisallowIndexingProcessorFactory()
    factory.init(args)
    factory
  }

  test("Test calling properties on factory that is not configured") {
    val factory = new AllowDisallowIndexingProcessorFactory()
    assert(AllowDisallowMode.Unknown == factory.mode)
    val rules = factory.rules
    assert(null != rules)
    assert(0 == rules.size)
  }

  test("Configured with allow mode arguments") {
    val factory = initializedFactory(createAllowConfig())
    assert(AllowDisallowMode.Allow == factory.mode)
  }

  test("Configured with disallow mode arguments") {
    val factory = initializedFactory(createDisallowConfig())
    assert(AllowDisallowMode.Disallow == factory.mode)
  }

  test("Configured with unknown mode arguments") {
    val factory = initializedFactory(createUnknownValueConfig())
    assert(AllowDisallowMode.Unknown == factory.mode)
  }

  test("Args containing illegal entries should be filtered by the factory") {
    val illegalRulesArg = createAllowWithIllegalConfig()
    val factory = initializedFactory(illegalRulesArg)

    val expectedRules = List(new FieldMatchRule("content_type", "default"), new FieldMatchRule("content_type", "news"))
    val actualRules = factory.rules

    assert(expectedRules.size == actualRules.size)
  }

}

