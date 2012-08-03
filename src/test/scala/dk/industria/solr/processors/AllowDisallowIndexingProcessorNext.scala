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

import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor

import org.slf4j.{Logger, LoggerFactory}

import java.io.IOException

/**
 * Implements an UpdateRequestProcessor used to record if a previous processor
 * passed the command up the chain.
 * Used in AllowDisallowIndexingProcessorTest.
 * @param next UpdateRequestProcessor to pass command on to.
 */
class AllowDisallowIndexingProcessorNext(next: UpdateRequestProcessor) extends UpdateRequestProcessor(next) {
  /**
   * Logger
   */
  private val logger = LoggerFactory.getLogger(getClass())

  /**
   * Indicated if processAdd was activated.
   */
  var called = false

  /**
   *  Implement processAdd setting the instance variable called to true.
   * @param cmd AddUpdateCommand to process.
   * @throws IOException
   */
  @throws(classOf[IOException])
  override def processAdd(cmd: AddUpdateCommand) = {
    logger.debug("Record processAdd called")
    called = true
    super.processAdd(cmd)
  }
}
