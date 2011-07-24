package dk.industria.solr.processors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a rule for matching field values.
 * Used by the AllowDisallowIndexingProcessor.
 */
public class FieldMatchRule {
    /**
     * Logger 
     */
    private static final Logger logger = LoggerFactory.getLogger(FieldMatchRule.class);


    private String field;

    private String pattern;


    /**
     * Construct a FieldMatchRule.
     * @param field Name of the file the rule should match.
     * @param pattern Pattern to match on the field.
     */
    public FieldMatchRule(String field, String pattern) {
    this.field = field;
    this.pattern = pattern;
    }


    public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("Field: ");
    if(null != this.field) {
        s.append(this.field);
    } else {
        s.append("null");
    }
    s.append(" Pattern: ");
    if(null != this.pattern) {
        s.append(this.pattern);
    } else {
        s.append("null");
    }
    return s.toString();
    }


}