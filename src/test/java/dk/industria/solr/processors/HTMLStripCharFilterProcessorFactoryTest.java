package dk.industria.solr.processors;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.update.processor.UpdateRequestProcessor;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.solr.common.util.NamedList;

public class HTMLStripCharFilterProcessorFactoryTest {


    /**
     * Compare to lists of string 
     * @param expected List of fields expected.
     * @param actual List of field actually in the list.
     * @return True if both are not null, the same size and all elements are equal.
     */
    private static boolean compareFieldLists(List<String> expected, List<String> actual) {
	if((null == expected) || (null == actual)) {
	    return false;
	}

	if(expected.size() != actual.size()) {
	    return false;
	}

	for(int i = 0; i < expected.size(); i++) {
	    String expectedItem = expected.get(i);
	    String actualItem = actual.get(i);
	    if(!expectedItem.equals(actualItem)) {
		return false;
	    }
	}
	return true;
    }

    /** 
     * Get list with header and content strings.
     * @return List of strings containing header and content.
     */
    private static List<String> expectedHeaderContent() {
	List<String> expectedFields = new ArrayList<String>();
	expectedFields.add("header");
	expectedFields.add("content");
	return expectedFields;
    }

    /**
     * Test simple legal configuration of the factory with fields
     */
    @Test public void initFactoryWithLegal() {
	NamedList input = new NamedList();
        input.add("field", "header");
        input.add("field", "content");
        input.add("ignored", 100);

        HTMLStripCharFilterProcessorFactory factory = new HTMLStripCharFilterProcessorFactory();
        factory.init(input);

	List<String> actualFields = factory.getFields();
	
	List<String> expectedFields = expectedHeaderContent();

	assertTrue(compareFieldLists(actualFields, expectedFields));

	//        UpdateRequestProcessor processor = factory.getInstance(null, null, null);
    }

    /**
     * Test with illegal types in the arguments list similar to 
     * Solr configuration with an element other than str
     */
    @Test public void initFactoryWithIllegalTypeFields() {
	NamedList input = new NamedList();
        input.add("field", "header");
        input.add("field", Integer.valueOf(100));
        input.add("field", "content");

        HTMLStripCharFilterProcessorFactory factory = new HTMLStripCharFilterProcessorFactory();
        factory.init(input);

	List<String> actualFields = factory.getFields();

	List<String> expectedFields = expectedHeaderContent();

	assertTrue(compareFieldLists(actualFields, expectedFields));

    }

    /**
     * Test with illegal types in the arguments list similar to 
     * Solr configuration with an element other than str
     */
    @Test public void initFactoryWithAllIllegalTypeFields() {
	NamedList input = new NamedList();
        input.add("field", Long.valueOf(1));
        input.add("field", Integer.valueOf(100));
        input.add("field", new NamedList());
	input.add("field", "");
	input.add("field", "  ");
	input.add("field", null);

        HTMLStripCharFilterProcessorFactory factory = new HTMLStripCharFilterProcessorFactory();
        factory.init(input);

	List<String> actualFields = factory.getFields();

	assertNotNull(actualFields);
	assertEquals(0, actualFields.size());
    }

    /**
     * Make sure calling getFields before init does break things
     */
    @Test public void fieldsBeforeInit() {
        HTMLStripCharFilterProcessorFactory factory = new HTMLStripCharFilterProcessorFactory();
	List<String> actualFields = factory.getFields();
	assertNotNull(actualFields);
	assertEquals(0, actualFields.size());
	
    }



}
