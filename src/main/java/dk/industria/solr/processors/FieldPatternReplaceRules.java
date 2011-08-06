package dk.industria.solr.processors;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements a field with a list of pattern replace rules attached..
 */
class FieldPatternReplaceRules {
    /**
     * Field name the rules are attached to.
     */
    private String fieldName;
    /**
     * List of pattern replace rules attached to the field.
     * @see PatternReplaceRule
     */
    private List<PatternReplaceRule> rules;

    /**
     * Get the field name .
     * @return Field name.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Get pattern replace rules attached to the field.
     *
     * @return List of pattern replace rules for the field.
     */
    public List<PatternReplaceRule> getRules() {
        return this.rules;
    }

    /**
     * Construct a fieldPatternReplaceRules
     *
     * @param fieldName Name of the fields the rules should be attached to.
     * @throws IllegalArgumentException if the field name if null.
     */
    public FieldPatternReplaceRules(final String fieldName) {
        if(null == fieldName) throw new IllegalArgumentException("null");

        this.fieldName = fieldName;
        this.rules = new LinkedList<PatternReplaceRule>();
    }

    /**
     * Add a pattern replace rule to the field.
     *
     * @param rule PatternReplaceRule to add to the field.
     * @throws IllegalArgumentException if the rule is null.
     */
    public void add(PatternReplaceRule rule) {
        if(null == rule) throw new IllegalArgumentException("rule is null");

        this.rules.add(rule);
    }


}
