package dk.industria.solr.processors;

import org.junit.Test;
import static org.junit.Assert.*;

public class FieldMatchRuleTest {


    @Test public void toStringTest() {

        FieldMatchRule fmr = new FieldMatchRule("field", "matchPattern");
        String result = fmr.toString();

        assertEquals("Field: field Pattern: matchPattern", result);




    }



}
