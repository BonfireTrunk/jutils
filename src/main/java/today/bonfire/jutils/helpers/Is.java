package today.bonfire.jutils.helpers;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * A utility class providing various checks and validations for different data types.
 */
public class Is {

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
   * - Alphanumeric characters, underscore, plus, ampersand, hyphen in local part
   * - Multiple dots allowed in local part
   * - Domain part supports both regular domains and punycode (xn--) format
   * - Domain part must be valid with 2-25 character TLD or punycode TLD
   */
  private static final Pattern EMAIL_PATTERN =
    Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:(?:xn--[a-z0-9]+(?<!-))|(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?))\\.(?:(?:(?:xn--[a-z0-9]+(?<!-))|(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?))\\.)*(?:xn--[a-z0-9]+(?<!-)|[a-zA-Z]{2,25})$");

  /**
   * Checks if a JSON string is blank or represents an empty value
   * Empty values include: null, blank string, "null", "[]", "{}"
   *
   * @param json the JSON string to check
   * @return true if the JSON string is blank or represents an empty value, false otherwise
   */
  public static boolean jsonBlank(String json) {
    return json == null || json.isBlank() || json.equals("null") || json.equals("[]") || json.equals("{}");
  }

  /**
   * Checks if a Boolean value is strictly true using reference equality
   *
   * @param b the Boolean value to check
   * @return true only if the Boolean value is equal to Boolean.TRUE, false otherwise
   */
  public static boolean True(Boolean b) {
    return Boolean.TRUE == b;
  }

  /**
   * Checks if a Boolean value is falsy (null or false)
   * This is different from {@link #False(Boolean)} as it also returns true for null values
   *
   * @param b the Boolean value to check
   * @return true if the Boolean value is null or false, false if it's true
   */
  public static boolean Falsy(Boolean b) {
    return Boolean.TRUE != b;
  }

  /**
   * Checks if a Boolean value is strictly false using reference equality
   *
   * @param b the Boolean value to check
   * @return true only if the Boolean value is equal to Boolean.FALSE, false otherwise
   */
  public static boolean False(Boolean b) {
    return Boolean.FALSE == b;
  }

  /**
   * Validates if a string represents a valid hexadecimal color code
   * Supports the following formats:
   * - #RGB (3 digits, shorthand)
   * - #RRGGBB (6 digits)
   * - #RRGGBBAA (8 digits with alpha channel)
   *
   * @param colour the string to validate as a color code
   * @return true if the string is a valid hex color code, false otherwise
   */
  public static boolean colour(String colour) {
    return !StringUtils.isBlank(colour) && HEX_COLOUR_PATTERN.matcher(colour).matches();
  }

  /**
   * Validates if a string represents a valid email address
   * Uses a simplified regex pattern based on OWASP validation rules
   * Note: This is a basic validation and may not catch all edge cases
   * defined in email RFCs
   *
   * @param email the string to validate as an email address
   * @return true if the string is a valid email address, false otherwise
   */
  public static boolean email(String email) {
    if (StringUtils.isBlank(email) || email.trim().length() < 5) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

}
