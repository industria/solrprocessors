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

/**
 * Indicate the mode of operation.
 * @see AllowDisallowIndexingProcessor
 */
enum AllowDisallowMode {
    /**
     * Indicates the mode of operation is unknown.
     *
     * No rules will be run and all documents will be indexed.
     */
    UNKNOWN,
    /**
     * Indicates the mode of operation is allow.
     *
     * In allow mode, only documents that matches a rule
     * will be indexed. Everything else will be filtered out.
     */
    ALLOW,
    /**
     * Indicates the mode of operation is disallow. 
     *
     * In disallow mode, all documents will be indexed, 
     * except for those that matches a rule.
     */
    DISALLOW
}

