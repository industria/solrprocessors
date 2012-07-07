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

import static org.junit.Assert.*;


/**
 * Implements tests for field matcher rule.
 */
public class FieldMatchRuleTest {


    /**
     * Test a legal FieldMatchRule getInstance call.
     */
    @Test
    public void getInstanceLegalTest() {
        String field = "content_type";
        String pattern = "default";
        FieldMatchRule rule = new FieldMatchRule(field, pattern);
        assertNotNull(rule);
    }

    /**
     * Test an illegal getInstance where field is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getInstanceIllegalNullFieldTest() {
        String field = null;
        String pattern = "default";
        new FieldMatchRule(field, pattern);
    }

    /**
     * Test an illegal getInstance where field is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getInstanceIllegalNullPatternTest() {
        String field = "content_type";
        String pattern = null;
        new FieldMatchRule(field, pattern);
    }

    /**
     * Test an illegal pattern which should throw an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getInstanceIllegalPatternTest() {
        String field = "content_type";
        String pattern = "(fi+";
        new FieldMatchRule(field, pattern);
    }


    @Test
    public void getFieldPassThrough() {
        String fieldName = "content_type";
        String pattern = "all_*";
        FieldMatchRule rule = new FieldMatchRule(fieldName, pattern);
        assertEquals(fieldName, rule.getField());
    }


    @Test
    public void matchValue() {
        FieldMatchRule rule = new FieldMatchRule("content_type", "^\\d{2}$");
        assertFalse(rule.matches(null));
        assertFalse(rule.matches(""));
        assertFalse(rule.matches("a1b2"));
        assertFalse(rule.matches("a42x"));
        assertTrue(rule.matches("42"));
    }

    /**
     * Testing toString format.
     */
    @Test
    public void toStringTest() {
        FieldMatchRule fmr = new FieldMatchRule("field", "matchPattern");
        assertEquals("field =~ m/matchPattern/", fmr.toString());
    }
}
