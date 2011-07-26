package dk.industria.solr.processors;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Represents a rule for matching field values.
 * Used by the AllowDisallowIndexingProcessor.
 */
class FieldMatchRule {
    /**
     * Field to match the pattern against.
     */
    private final String field;
    /**
     * The input pattern as a String
     */
    private final String inputPattern;
    /**
     * Pattern to match against the field.
     */
    private final Pattern pattern;
    


    /**
     * Construct a FieldMatchRule.
     * @param field Field to match against.
     * @param inputPattern String representation of the pattern.
     * @param pattern Compiled version of inputPattern.
     */
    private FieldMatchRule(String field, String inputPattern, Pattern pattern) {
	this.field = field;
	this.inputPattern = inputPattern;
	this.pattern = pattern;
    }

    /**
     * Create a new FieldMatchRule.
     * @param field String with the field name to match the pattern against.
     * @param pattern String containing a regular expression to match against the field.
     * @return FieldMatchRule
     * @throws IllegalArgumentExecption if the arguments does not compile into a legal pattern.
     */
    public static FieldMatchRule getInstance(final String field, final String pattern) throws IllegalArgumentException {
	if(null == field) throw new IllegalArgumentException("field is null");
	if(null == pattern) throw new IllegalArgumentException("pattern is null");
	
	Pattern compiledPattern;
	try {
	    compiledPattern = Pattern.compile(pattern);
	} catch(PatternSyntaxException e) {
	    String msg = "Failed to compile pattern [" + pattern + "] for field [" + field + "] : " + e.getMessage();
	    throw new IllegalArgumentException(msg, e);
	}

	return new FieldMatchRule(field, pattern, compiledPattern);
    }




    /**
     * Returns a String representation of the field match rule.
     * @return String representing the rule.
     */
    @Override
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
            s.append(this.inputPattern);
        } else {
            s.append("null");
        }
        return s.toString();
    }


}