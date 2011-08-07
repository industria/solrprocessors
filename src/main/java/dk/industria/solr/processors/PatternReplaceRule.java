package dk.industria.solr.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a pattern replace rule for the PatternReplaceProcessor .
 */
class PatternReplaceRule {
    /**
     * Rule id.
     */
    private final String id;
    /**
     * Rule pattern as a compiled regular expression.
     */
    private final Pattern pattern;
    /**
     * Value to replace the pattern with.
     */
    private final String replacement;

    /**
     * Construct a PatternReplaceRule
     * @param id Id used to identify the rule.
     * @param pattern Regular expression defining the pattern.
     * @param replacement Value to replace the pattern match with.
     */
    private PatternReplaceRule(final String id, final Pattern pattern, final String replacement) {
        this.id = id;
        this.pattern = pattern;
        this.replacement = replacement;
    }

    /**
     * Get id of the rule.
     *
     * @return String containing the id of the rule.
     */
    public String getId() {
        return id;
    }

    /**
     * Create a new PatternReplaceRule.
     *
     * @param id Id used to identify the rule in field mappings.
     * @param pattern String containing the regular expression defining the pattern to replace.
     * @param replacement String containing the value to replace pattern matches with. Null equals an empty string.
     * @return PatternReplaceRule
     * @throws IllegalArgumentException If iid is null or empty. If the pattern isn't a regular expression.
     */
    public static PatternReplaceRule getInstance(final String id, final String pattern, final String replacement) throws IllegalArgumentException {
        if(null == id) throw new IllegalArgumentException("id is null");
        if(0 == id.length()) throw new IllegalArgumentException("id is an empty string");
        if(null == pattern) throw new IllegalArgumentException("pattern is null");

        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            String replacementValue = (null != replacement) ? replacement : "";
            return new PatternReplaceRule(id, compiledPattern, replacementValue);
        } catch(PatternSyntaxException e) {
            String msg = "Failed to compile pattern [" + pattern + "] for rule id [" + id + "] : " + e.getMessage();
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * Apply the pattern replace rule to a value.
     * @param value Value to apply the pattern replace rule to.
     * @return Value after the rule has been applied to the value.
     */
    public String replace(String value) {
        if(null == value) throw new IllegalArgumentException("value is null");

        Matcher matcher = pattern.matcher(value);
        return matcher.replaceAll(replacement);
    }


    /**
     * Get a String representation of the pattern replace rule.
     *
     * @return String representation of the pattern replace rule.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Id: [");
        s.append(this.id);
        s.append("] Pattern: [");
        s.append(this.pattern);
        s.append("] Replace: [");
        s.append(this.replacement);
        s.append("]");
        return s.toString();
    }
}
