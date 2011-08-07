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


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a field with a list of pattern replace rules attached..
 */
class FieldPatternReplaceRules {
    /**
     * Field name the rules are attached to.
     */
    private String fieldName;
    /**
     * List of pattern replace rules attached to the field.
     * @see PatternReplaceRule
     */
    private List<PatternReplaceRule> rules;

    /**
     * Get the field name .
     * @return Field name.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Get pattern replace rules attached to the field.
     *
     * @return List of pattern replace rules for the field.
     */
    public List<PatternReplaceRule> getRules() {
        return this.rules;
    }

    /**
     * Construct a fieldPatternReplaceRules
     *
     * @param fieldName Name of the fields the rules should be attached to.
     * @throws IllegalArgumentException if the field name if null.
     */
    public FieldPatternReplaceRules(final String fieldName) {
        if(null == fieldName) throw new IllegalArgumentException("null");

        this.fieldName = fieldName;
        this.rules = new LinkedList<PatternReplaceRule>();
    }

    /**
     * Add a pattern replace rule to the field.
     *
     * @param rule PatternReplaceRule to add to the field.
     * @throws IllegalArgumentException if the rule is null.
     */
    public void add(PatternReplaceRule rule) {
        if(null == rule) throw new IllegalArgumentException("rule is null");

        this.rules.add(rule);
    }

    /**
     * Replace patterns in the value according to the rules.
     *
     * @param value Value to apply the pattern replace rules to.
     * @return New value after pattern replace rules have been applied.
     * @throws IllegalArgumentException if called with a null value.
     */
    public String replace(String value) {
        if(null == value) throw new IllegalArgumentException("null value");

        String newValue = value;
        for(PatternReplaceRule rule : this.rules) {
            newValue = rule.replace(newValue);
        }
        return newValue;
    }

}
