package dk.industria.solr.processors;


import org.apache.solr.common.util.NamedList;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Implements tests for the PatternReplaceProcessorFactory.
 */
public class PatternReplaceProcessorFactoryTest {
    /**
     * Create configuration argument for the below XML:
     *
     * <lst name="rule">
     * <str name="id">punctuation</str>
     * <str name="pattern">\p{P}</str>
     * <str name="replace"></str>
     * </lst>
     * <lst name="rule">
     * <str name="id">prefix</str>
     * <str name="pattern">^\d{4}</str>
     * <str name="replace">****</str>
     * </lst>
     * <lst name="fields">
     * <str name="title">punctuation</str>
     * <str name="name">punctuation</str>
     * <str name="comment">punctuation</str>
     * <str name="card">prefix</str>
     * </lst>
     *
     * @return NamedList containing the above configuration.
     */
    private static NamedList<NamedList<String>> createLegalConfig() {
        NamedList<String> punctuation = new NamedList<String>();
        punctuation.add("id", "punctuation");
        punctuation.add("pattern", "\\p{P}");
        punctuation.add("replace", "");

        NamedList<String> prefix = new NamedList<String>();
        prefix.add("id", "prefix");
        prefix.add("pattern", "^\\d{4}");
        prefix.add("replace", "****");

        NamedList<String> fields = new NamedList<String>();
        fields.add("title", "punctuation");
        fields.add("name", "punctuation");
        fields.add("comment", "punctuation");
        fields.add("card", "prefix");

        NamedList<NamedList<String>> args = new NamedList<NamedList<String>>();
        args.add("rule", punctuation);
        args.add("rule", prefix);
        args.add("fields", fields);
        return args;
    }


    @Test
    public void configTest() {
        PatternReplaceProcessorFactory factory = new PatternReplaceProcessorFactory();
        factory.init(createLegalConfig());

        assertTrue(true);
    }

}
