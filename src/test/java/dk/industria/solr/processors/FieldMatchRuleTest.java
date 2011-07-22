package dk.industria.solr.processors;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: james
 * Date: 7/22/11
 * Time: 8:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class FieldMatchRuleTest {


    @Test public void toStringTest() {

        FieldMatchRule fmr = new FieldMatchRule("field", "matchPattern");
        String result = fmr.toString();

        assertEquals("Field: field Pattern: matchPattern", result);




    }



}
