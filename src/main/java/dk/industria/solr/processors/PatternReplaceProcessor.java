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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.solr.client.solrj.request.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;


import org.apache.solr.update.AddUpdateCommand;

import org.apache.solr.update.processor.UpdateRequestProcessor;


class PatternReplaceProcessor extends UpdateRequestProcessor {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PatternReplaceProcessor.class);
    /**
     * Collection of field pattern replace rules to process.
     */
    private final Collection<FieldPatternReplaceRules> fieldPatternRules;

    /**
     * Construct a PatternReplaceProcessor.
     *
     * @param fieldPatternRules Collection of field pattern rules to process.
     * @param next Next UpdateRequestProcessor in the processor chain.
     */
    public PatternReplaceProcessor(final Collection<FieldPatternReplaceRules> fieldPatternRules, UpdateRequestProcessor next) {
        super(next);
        this.fieldPatternRules = fieldPatternRules;
    }

    /**
     * Called by the processor chain on document add/update operations.
     *
     * @param cmd AddUpdateCommand
     * @throws IOException
     */
    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        SolrInputDocument document = cmd.getSolrInputDocument();

        for(FieldPatternReplaceRules fieldRules : this.fieldPatternRules) {
            String fieldName = fieldRules.getFieldName();
            logger.debug("Processing field: {}", fieldName);

            SolrInputField field = document.getField(fieldName);
            if (null == field) continue;

            Collection<Object> values = field.getValues();
            if (null == values) continue;

            Collection<Object> newValues = new ArrayList<Object>();
            for (Object value : values) {
                if (value instanceof String) {
                    String newValue = fieldRules.replace((String)value);
                    newValues.add(newValue);
                } else {
                    newValues.add(value);
                }
            }
            float boost = field.getBoost();
            field.setValue(newValues, boost);

        }

        super.processAdd(cmd);
    }

}
