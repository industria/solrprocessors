package dk.industria.solr.processors;


import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.solr.update.processor.UpdateRequestProcessor;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.update.AddUpdateCommand;


/**
 * Implements tests for AllowDisallowIndexingProcessorFactory.
 */
public class AllowDisallowIndexingProcessorFactoryTest {

    /**
     * Create a configuration for allow mode.
     * @return NamedList configured for allow mode.
     */
    private static NamedList createAllowConfig() {
        NamedList rules = new NamedList();
        rules.add("contenttype", "default");

        NamedList args = new NamedList();
        args.add("allow", rules);
        return args;
    }

    /**
     * Create a configuration for allow mode with illegal rules
     * that should be filtered by the factory.
     * @return NamedList configured for allow mode with illegal rules..
     */
    private static NamedList createAllowWithIllegalConfig() {
        NamedList rules = new NamedList();
        rules.add("contenttype", "default");
        rules.add("contenttype", "news");
        rules.add("illegalInteger", Integer.valueOf(100));
        rules.add(null, null);
        rules.add("listtype", new NamedList());
        rules.add(null, "unnamed");
        rules.add("empty", "");
        rules.add("valuenull", null); 
        rules.add("", "EmptyStringAttribute"); 
	rules.add("content_type", "(fi+");

        NamedList args = new NamedList();
        args.add("allow", rules);
        return args;
    }

    /**
     * Create a configuration for disallow mode.
     * @return NamedList configured for disallow mode.
     */
    private static NamedList createDisallowConfig() {
        NamedList rules = new NamedList();

        NamedList args = new NamedList();
        args.add("disallow", rules);
        return args;
    }

    /**
     * Create a configuration for with name other that allow or disallow (nomode)  mode.
     * @return NamedList configured for no mode.
     */
    private static NamedList createUnknownValueConfig() {
        NamedList rules = new NamedList();

        NamedList args = new NamedList();
        args.add("nomode", rules);
        return args;
    }


    /**
     * Get an initialized factory
     * @param args The arguments to initialize the factory with.
     * @return AllowDisallowIndexingProcessorFactory initialized with args.
     */
    private AllowDisallowIndexingProcessorFactory initializedFactory(NamedList args) {
        AllowDisallowIndexingProcessorFactory factory = new AllowDisallowIndexingProcessorFactory();
        factory.init(args);
        return factory;
    }

    /**
     * Test calling properties on unconfigured factory.
     */
    @Test public void modeAndRulesUnconfigured() {
        AllowDisallowIndexingProcessorFactory factory = new AllowDisallowIndexingProcessorFactory();
        assertEquals(AllowDisallowMode.UNKNOWN, factory.getMode());
        List<FieldMatchRule> rules = factory.getRules();
        assertNotNull(rules);
        assertEquals(0, rules.size());
    }

    /**
     * Configured with allow mode arguments 
     */
    @Test public void modeAfterInitAllow() {
        AllowDisallowIndexingProcessorFactory factory = initializedFactory(createAllowConfig());
        assertEquals(AllowDisallowMode.ALLOW, factory.getMode());
    }

    /**
     * Configured with disallow mode arguments
     */
    @Test public void modeAfterInitDisallow() {
        AllowDisallowIndexingProcessorFactory factory = initializedFactory(createDisallowConfig());
        assertEquals(AllowDisallowMode.DISALLOW, factory.getMode());
    }

    /**
     * Configured with disallow mode arguments
     */
    @Test public void modeAfterInitNomode() {
        AllowDisallowIndexingProcessorFactory factory = initializedFactory(createUnknownValueConfig());
        assertEquals(AllowDisallowMode.UNKNOWN, factory.getMode());
    }


    /**
     * Args containing illegal entries should be filtered by the factory.
     */
    @Test public void illegalRulesFiltered() {
        NamedList illegalRulesArg = createAllowWithIllegalConfig();
        AllowDisallowIndexingProcessorFactory factory = initializedFactory(illegalRulesArg);

        List<FieldMatchRule> expectedRules = new ArrayList<FieldMatchRule>();
        expectedRules.add(FieldMatchRule.getInstance("contenttype", "default"));
        expectedRules.add(FieldMatchRule.getInstance("contenttype", "news"));

        List<FieldMatchRule> actualRules = factory.getRules();

        assertEquals(expectedRules.size(), actualRules.size());

    }

}

