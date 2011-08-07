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
        String actual = r.replace(null);
    }
}
