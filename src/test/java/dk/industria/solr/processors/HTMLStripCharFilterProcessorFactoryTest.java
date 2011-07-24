package dk.industria.solr.processors;

import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.solr.common.util.NamedList;

public class HTMLStripCharFilterProcessorFactoryTest {




    @Test public void initWithLegal() {
        // TODO: Implement test
        NamedList l = new NamedList();
        l.add("field", "header");
        l.add("field", "content");
        l.add("ignored", 100);

        HTMLStripCharFilterProcessorFactory f = new HTMLStripCharFilterProcessorFactory();
        f.init(l);
        UpdateRequestProcessor p = f.getInstance(null, null, null);
        assertTrue(true);
    }

    @Test public void initWithIllegal() {
        //TODO: implement test
        assertTrue(true);
    }






}
