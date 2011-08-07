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
 * Implements tests for PatternReplaceRule.
 */
public class PatternReplaceRuleTest {

    /**
     * Test creation of a valid PatterReplaceRule.
     */
    @Test
    public void createLegalInstance() {
        PatternReplaceRule rule = PatternReplaceRule.getInstance("prefix_rule", "\\p{P}", " ");
        assertNotNull(rule);
    }

    /**
     * Testing null as id.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createIllegalNullId() {
        PatternReplaceRule.getInstance(null, "legal", "");
    }

    /**
     * Testing empty id
     */
    @Test(expected = IllegalArgumentException.class)
    public void createIllegalEmptyId() {
        PatternReplaceRule.getInstance("", "legal", "");
    }

    /**
     * Testing null pattern.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createIllegalNullPattern() {
        PatternReplaceRule.getInstance("id", null, "");
    }

    /**
     * Testing illegal pattern (missing closing } so it will not compile.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createIllegalNonLegalPattern() {
        PatternReplaceRule.getInstance("id", "(\\d+", "");
    }

    /**
     * Test the string representation
     */
    @Test()
    public void toStringTest() {
        PatternReplaceRule r = PatternReplaceRule.getInstance("id", "(\\d+)", "?");
        assertEquals("Id: [id] Pattern: [(\\d+)] Replace: [?]", r.toString());
    }

    /**
     * Testing replace with a match
     */
    @Test
    public void replaceTest() {
        PatternReplaceRule r = PatternReplaceRule.getInstance("id", "\\d{4}$", "xxxx");
        String actual = r.replace("1111-2222-3333-4444");
        assertEquals("1111-2222-3333-xxxx", actual);
    }

    /**
     * Testing replace with a non matching pattern
     */
    @Test
    public void replaceNoMatchTest() {
        PatternReplaceRule r = PatternReplaceRule.getInstance("id", "\\d{6}$", "xxxx");
        String actual = r.replace("1111-2222-3333-4444");
        assertEquals("1111-2222-3333-4444", actual);
    }

    /**
     * Testing replace with a null value
     */
    @Test(expected = IllegalArgumentException.class)
    public void replaceNullArgTest() {
        PatternReplaceRule r = PatternReplaceRule.getInstance("id", "\\d{6}$", "xxxx");
        r.replace(null);
    }
}
