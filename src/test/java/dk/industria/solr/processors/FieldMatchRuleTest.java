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


    @Test public void getFieldPassthrough() {
	String fieldname = "content_type";
	String pattern = "all_*";
	FieldMatchRule rule = FieldMatchRule.getInstance(fieldname, pattern);
	assertEquals(fieldname, rule.getField());
    }


    @Test public void matchValue() {
	FieldMatchRule rule = FieldMatchRule.getInstance("content_type", "^\\d{2}$");
	assertFalse(rule.match(null));
	assertFalse(rule.match(""));
	assertFalse(rule.match("a1b2"));
	assertFalse(rule.match("a42x"));
	assertTrue(rule.match("42"));
    }



    /**
     * Testing toString format.
     */
    @Test public void toStringTest() {
        FieldMatchRule fmr = FieldMatchRule.getInstance("field", "matchPattern");
        String result = fmr.toString();

        assertEquals("field =~ m/matchPattern/", result);
    }



}
