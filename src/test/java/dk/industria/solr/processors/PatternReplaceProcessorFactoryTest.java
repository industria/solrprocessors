/**
 * Copyright 2011 James Lindstorff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.industria.solr.processors;


import org.apache.solr.common.util.NamedList;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
     * <str name="clean">prefix</str>
     * <str name="clean">punctuation</str>
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
        fields.add("clean", "prefix");
        fields.add("clean", "punctuation");

        NamedList<NamedList<String>> args = new NamedList<NamedList<String>>();
        args.add("rule", punctuation);
        args.add("rule", prefix);
        args.add("fields", fields);
        return args;
    }

    @Test
    public void notInitializedReturnsFieldRulesP() {
        PatternReplaceProcessorFactory factory = new PatternReplaceProcessorFactory();
        Collection<FieldPatternReplaceRules> fields = factory.getFieldRules();
        assertNotNull(fields);
        assertEquals(0, fields.size());
    }

    @Test
    public void configTest() {
        PatternReplaceProcessorFactory factory = new PatternReplaceProcessorFactory();
        factory.init(createLegalConfig());

        List<String> expectedFields = new ArrayList<String>();
        expectedFields.add("title");
        expectedFields.add("name");
        expectedFields.add("comment");
        expectedFields.add("card");
        expectedFields.add("clean");

        Collection<FieldPatternReplaceRules> fr = factory.getFieldRules();

        assertEquals(expectedFields.size(), fr.size());

        for(FieldPatternReplaceRules r : fr) {
            String fn = r.getFieldName();
            if(fn.equals("card")) {
                List<PatternReplaceRule> cardList = r.getRules();
                assertEquals(1, cardList.size());
                assertEquals("prefix", cardList.get(0).getId());
            } else if(fn.equals("title")) {
                List<PatternReplaceRule> titleList = r.getRules();
                assertEquals(1, titleList.size());
                assertEquals("punctuation", titleList.get(0).getId());
            } else if(fn.equals("clean")) {
                List<PatternReplaceRule> cleanList = r.getRules();
                assertEquals(2, cleanList.size());
                assertEquals("prefix", cleanList.get(0).getId());
                assertEquals("punctuation", cleanList.get(1).getId());
            }
        }
    }

}
