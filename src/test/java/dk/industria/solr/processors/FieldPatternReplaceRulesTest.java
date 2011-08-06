package dk.industria.solr.processors;

import org.junit.Test;

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


}
