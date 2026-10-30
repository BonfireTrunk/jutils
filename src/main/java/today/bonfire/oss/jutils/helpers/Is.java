package today.bonfire.oss.jutils.helpers;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * A utility class providing various checks and validations for different data types.
 */
public final class Is {

    private Is() {}

    /**
     * A pattern to validate hexadecimal color codes in various formats:
     * - #RGB (3 digits)
     * - #RRGGBB (6 digits)
     * - #RRGGBBAA (8 digits with alpha)
     */
    private static final Pattern HEX_COLOUR_PATTERN =
            Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");

    /**
     * Pattern for validating email addresses using a simplified regex based on OWASP validation rules
     * Supports standard email format with:
     * - Letters, digits and the characters {@code . _ % + -} in the local part, up to 64 characters,
     *   starting and ending with a letter or digit
     * - Single dots allowed in the local part; consecutive dots are rejected
     * - Domain part supports both regular domains and punycode (xn--) format
     * - Domain part must be valid with 2-25 character TLD or punycode TLD
     */
    private static final Pattern EMAIL_LOCAL_PART_PATTERN =
            Pattern.compile("^[A-Za-z0-9](?:[a-zA-Z0-9._%+\\-]{0,62}[A-Za-z0-9])?$");

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?!.{256})(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+(?:[a-z]{2,63}|xn--[a-z0-9]{2,59})$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SUBDOMAIN_PATTERN =
            Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9])?$");

    /**
     * Checks if a JSON string is blank or represents an empty value
     * Empty values include: null, blank string, "null", "[]", "{}"
     *
     * @param json the JSON string to check, may be null
     * @return true if the JSON string is blank or represents an empty value, false otherwise
     */
    public static boolean jsonBlank(@Nullable String json) {
        return json == null || json.isBlank() || json.equals("null") || json.equals("[]") || json.equals("{}");
    }

    /**
     * Validates if a string represents a valid hexadecimal color code
     * Supports the following formats:
     * - #RGB (3 digits, shorthand)
     * - #RRGGBB (6 digits)
     * - #RRGGBBAA (8 digits with alpha channel)
     *
     * @param colour the string to validate as a color code, may be null
     * @return true if the string is a valid hex color code; false otherwise, including for null or blank input
     */
    public static boolean colour(@Nullable String colour) {
        return !StringUtils.isBlank(colour)
                && HEX_COLOUR_PATTERN.matcher(colour).matches();
    }

    /**
     * Validates if a string represents a valid email address
     * Uses a simplified regex pattern based on OWASP validation rules.
     * The local part allows letters, digits and {@code . _ % + -} (not {@code &}), up to 64 characters.
     * The domain part is matched case-insensitively. The whole address must be shorter than 256 characters.
     * Note: This is a basic validation and may not catch all edge cases
     * defined in email RFCs. Never throws.
     *
     * @param email the string to validate as an email address, may be null
     * @return true if the string is a valid email address; false otherwise, including for null or blank input
     */
    public static boolean email(@Nullable String email) {
        if (StringUtils.isBlank(email) || email.trim().length() < 5) {
            return false;
        }

        // Split the email into local part and domain part
        var parts = email.split("@", 2);
        if (parts.length != 2) {
            return false;
        }
        var localPart = parts[0];
        var domainPart = parts[1];

        // Validate the local part
        if (!EMAIL_LOCAL_PART_PATTERN.matcher(localPart).matches()) return false;

        if (localPart.contains("..")) return false;

        // Validate the domain part
        if (!DOMAIN_PATTERN.matcher(domainPart).matches()) return false;

        return email.length() < 256;
    }

    /**
     * Validates a single DNS label such as a custom sub-domain.
     * The label must:
     * - Not be null or blank
     * - Be 1 to 63 characters long
     * - Contain only ASCII letters, digits and hyphens, and neither start nor end with a hyphen
     * Dots are not allowed, so "example.com" is rejected. Store the label in lowercase for better results.
     *
     * @param hostname the label to validate, may be null
     * @return true if the label is a valid DNS label; false otherwise, including for null or blank input
     */
    public static boolean subDomain(@Nullable String hostname) {
        if (StringUtils.isBlank(hostname)) {
            return false;
        }

        return SUBDOMAIN_PATTERN.matcher(hostname).matches();
    }
}
