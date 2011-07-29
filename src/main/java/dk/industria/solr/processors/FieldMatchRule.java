package dk.industria.solr.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a rule for matching field values
 * <p>Used by the @see AllowDisallowIndexingProcessor.</p>.
 */
class FieldMatchRule {
    /**
     * Field to match the pattern against.
     */
    private final String field;
    /**
     * Pattern to match against the field.
     */
    private final Pattern pattern;

   /**
     * Get the field name of the match rule.
     * @return Field name of the match field.
     */ 
    public String getField() {
        return this.field;
    }

    /**
     * Construct a FieldMatchRule.
     * @param field Field to match against.
     * @param pattern Compiled version of inputPattern.
     */
    private FieldMatchRule(String field, Pattern pattern) {
        this.field = field;
        this.pattern = pattern;
    }

    /**
     * Create a new FieldMatchRule.
     * @param field String with the field name to match the pattern against.
     * @param pattern String containing a regular expression to match against the field.
     * @return FieldMatchRule
     * @throws IllegalArgumentException if the arguments does not compile into a legal pattern.
     */
    public static FieldMatchRule getInstance(final String field, final String pattern) throws IllegalArgumentException {
        if(null == field) throw new IllegalArgumentException("field is null");
        if(null == pattern) throw new IllegalArgumentException("pattern is null");

        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            return new FieldMatchRule(field, compiledPattern);
        } catch(PatternSyntaxException e) {
            String msg = "Failed to compile pattern [" + pattern + "] for field [" + field + "] : " + e.getMessage();
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * Matches the field value against the pattern.
     * @param fieldValue Value to test the pattern against.
     * @return True if the pattern matches the field value.
     */
    public boolean match(String fieldValue) {
        if(null == fieldValue) return false;

        Matcher m = this.pattern.matcher(fieldValue);
        return m.find();
    }

  /**
     * Returns a String representation of the field match rule.
     * @return String representing the rule.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(field);
        s.append(" =~ m/");
        s.append(pattern);
        s.append("/");
        return s.toString();
    }
}
