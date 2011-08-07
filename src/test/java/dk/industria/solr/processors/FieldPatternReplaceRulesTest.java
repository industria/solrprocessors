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

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Implements tests for the FieldPatternReplaceRules
 */
public class FieldPatternReplaceRulesTest {
    /**
     * Testing for exception when trying construction with null as field name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createWithNullArgument() {
        new FieldPatternReplaceRules(null);
    }

    /**
     * Testing normal construction.
     */
    @Test
    public void createCard() {
        FieldPatternReplaceRules fieldRules = new FieldPatternReplaceRules("card");
        assertNotNull(fieldRules);
        assertEquals("card", fieldRules.getFieldName());
    }

    /**
     * Testing for exception when trying construction with null as field name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void addNullRule() {
        FieldPatternReplaceRules rules = new FieldPatternReplaceRules("card");
        rules.add(null);
    }


    /**
     * Create a FieldPatternReplaceRules containing two rules.
     *
     * @return FieldPatternReplaceRules
     */
    private static FieldPatternReplaceRules createRules() {
        FieldPatternReplaceRules rules = new FieldPatternReplaceRules("field");
        rules.add(PatternReplaceRule.getInstance("prefix", "^\\d{4}", "xxxx"));
        rules.add(PatternReplaceRule.getInstance("asterisk", "\\*", "-"));
        return rules;
    }

    /**
     * Test the replace method
     */
    @Test
    public void replaceTest() {
        FieldPatternReplaceRules rules = createRules();
        String actual = rules.replace("4444*3333*3333*1111");
        assertEquals("xxxx-3333-3333-1111", actual);
    }

    /**
     * Testing calling replace with a null value.
     */
    @Test(expected = IllegalArgumentException.class)
    public void replaceNull() {
        FieldPatternReplaceRules rules = createRules();
        rules.replace(null);
    }
}
