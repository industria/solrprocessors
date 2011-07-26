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
    @Test public void getInstanceLegelTest() {
	String field = "content_type";
	String pattern = "default";
	FieldMatchRule rule = FieldMatchRule.getInstance(field, pattern);
	assertNotNull(rule);
    }

    /**
     * Test an illegal getInstance where field is null.
     */
    @Test(expected=IllegalArgumentException.class) public void getInstanceIllegalNullFieldTest() {
	String field = null;
	String pattern = "default";
	FieldMatchRule rule = FieldMatchRule.getInstance(field, pattern);
    }

    /**
     * Test an illegal getInstance where field is null.
     */
    @Test(expected=IllegalArgumentException.class) public void getInstanceIllegalNullPatternTest() {
	String field = "content_type";
	String pattern = null;
	FieldMatchRule rule = FieldMatchRule.getInstance(field, pattern);
    }

    /**
     * Test an illegal pattern which should throw an exception.
     */
    @Test(expected=IllegalArgumentException.class) public void getInstanceIllegalPatternTest() {
	String field = "content_type";
	String pattern = "(fi+";
	FieldMatchRule rule = FieldMatchRule.getInstance(field, pattern);
    }

    /**
     * Testing toString format.
     */
    @Test public void toStringTest() {
        FieldMatchRule fmr = FieldMatchRule.getInstance("field", "matchPattern");
        String result = fmr.toString();

        assertEquals("Field: field Pattern: matchPattern", result);
    }



}
