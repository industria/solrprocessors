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
        String actual = rules.replace(null);
    }
}
