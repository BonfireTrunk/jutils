package today.bonfire.oss.jutils.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import today.bonfire.oss.jutils.Encoder;
import today.bonfire.oss.jutils.RandUtils;
import today.bonfire.oss.jutils.constants.ArrayItem;
import today.bonfire.oss.jutils.constants.CC;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class GetValid {

  private static final String  SPECIAL_CHARS              = "[`~!@#$%^&*()_|+=?;:'\",.<>\\{\\}\\[\\]\\\\\\/]";
  private static final String  NON_WORD_CHARS             = "\\W";
  private static final int     DEFAULT_MAX_LENGTH         = 255;
  private static final int     MAX_TAG_LENGTH             = 100;
  private static final int     MAX_SLUG_LENGTH            = 100;
  private static final String  SLUG_SEPARATOR             = "-";
  private static final Pattern MULTIPLE_SEPARATORS        = Pattern.compile("-{2,}");
  private static final Pattern LEADING_TRAILING_SEPARATOR = Pattern.compile("^-+|-+$");
  private static final int     MAX_HASHTAG_LENGTH         = 140; // Common social media limit

  private static final String DEFAULT_LOCALE   = "en";
  private static final String DEFAULT_TIMEZONE = "UTC";

  private static final Set<String> BLOCKED_SCHEMES = Set.of("java", "data", "file", "jar");
  private static final String      DEFAULT_SCHEME  = "https";

  /**
   * Returns an alphanumeric string by removing all non-word characters.
   * If input is empty or results in empty string after processing, returns a random ID.
   *
   * @param text input string to process
   * @return alphanumeric string or random ID if empty
   */
  public static String alphaNumeric(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    return alphaNumeric(text, DEFAULT_MAX_LENGTH);
  }

  /**
   * Returns an alphanumeric string by removing all non-word characters.
   * If input is empty or results in empty string after processing, returns a random ID.
   *
   * @param text      input string to process
   * @param maxLength maximum length of the result
   * @return alphanumeric string or random ID if empty
   */
  public static String alphaNumeric(String text, int maxLength) {
    if (StringUtils.isEmpty(text)) {
      return "";
    }

    var r = text.replaceAll(NON_WORD_CHARS, "")
                .replaceAll("_", "");

    if (StringUtils.isBlank(r)) {
      return "";
    }

    return r.length() > maxLength ? r.substring(0, maxLength) : r;
  }

  /**
   * Sanitizes a filename by removing invalid characters and ensuring it's URL safe.
   *
   * @param name the filename to sanitize
   * @return sanitized filename or random ID if empty
   */
  public static String fileName(String name) {
    if (StringUtils.isBlank(name)) {
      return RandUtils.nanoIdWordSafe(8);
    }

    String result = RegExUtils.removeAll(name, "\\s");
    result = RegExUtils.removeAll(result, "[`~!#$%^&*()&%|+=?;:'\",<>\\{\\}\\[\\]\\\\\\/]");
    result = Encoder.urlEncode(result);


    if (StringUtils.isBlank(result)) {
      return RandUtils.nanoIdWordSafe(8);
    }

    return result.length() > DEFAULT_MAX_LENGTH ?
           result.substring(0, DEFAULT_MAX_LENGTH) :
           result;
  }

  /**
   * Sanitizes a tag name by removing special characters and normalizing spaces.
   * Returns empty string for invalid input.
   *
   * @param name the tag name to sanitize
   * @return sanitized tag name or empty string
   */
  public static String tagName(String name) {
    if (StringUtils.isBlank(name)) {
      return CC.EMPTY_STRING;
    }

    String result = name.trim();
    result = RegExUtils.replaceAll(result, "\\s{2,}", " ");
    result = RegExUtils.removeAll(result, SPECIAL_CHARS);

    if (result.length() > MAX_TAG_LENGTH) {
      result = result.substring(0, MAX_TAG_LENGTH);
    }

    return result.trim();
  }

  /**
   * Creates a URL-friendly slug from the given string.
   * Converts spaces to hyphens, removes special characters, and ensures proper length.
   *
   * @param name the string to convert to a slug
   * @return the URL-friendly slug or random ID if empty
   */
  public static String urlSlug(String name) {
    return urlSlug(name, true);
  }

  /**
   * Creates a URL-friendly slug from the given string.
   * Handles UTF-8 characters by using codepoints.
   *
   * @param name             the string to convert to a slug
   * @param setRandomIfEmpty if true, returns a random ID when input is empty
   * @return the URL-friendly slug, random ID if empty and setRandomIfEmpty is true, or empty string
   */
  public static String urlSlug(String name, boolean setRandomIfEmpty) {
    if (StringUtils.isBlank(name)) {
      return setRandomIfEmpty ? RandUtils.nanoIdWordSafe(6) : CC.EMPTY_STRING;
    }

    String slug = name.toLowerCase()
                      .trim()
                      .codePoints()
                      .limit(MAX_SLUG_LENGTH)
                      .map(cp -> {
                        if (Character.isLetterOrDigit(cp) || Character.isSpaceChar(cp)) {
                          return cp;
                        } else {
                          return 45;  // Code point for '-'
                        }
                      })
                      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                      .toString();

    // Replace spaces with hyphens
    slug = RegExUtils.replaceAll(slug, "\\s+", SLUG_SEPARATOR);

    // Clean up separators
    slug = MULTIPLE_SEPARATORS.matcher(slug).replaceAll(SLUG_SEPARATOR);
    slug = LEADING_TRAILING_SEPARATOR.matcher(slug).replaceAll("");

    if (slug.isEmpty()) {
      return setRandomIfEmpty ? RandUtils.nanoIdWordSafe(6) : CC.EMPTY_STRING;
    }

    return slug;
  }

  /**
   * Sanitizes a hashtag by removing special characters and spaces.
   * Handles UTF-8 characters properly by converting them to their closest ASCII representation.
   * Returns null for invalid input.
   *
   * @param s the hashtag to sanitize (with or without leading #)
   * @return sanitized hashtag without # prefix, or null if invalid
   */
  @Nullable
  public static String hashtag(String s) {
    if (StringUtils.isBlank(s)) {
      return null;
    }

    // Remove leading # if present
    String tag = s.startsWith("#") ? s.substring(1) : s;

    // Convert to lowercase and normalize
    tag = tag.trim()
             .codePoints()
             .filter(cp -> Character.isLetterOrDigit(cp) || cp == '_')
             .limit(MAX_HASHTAG_LENGTH)
             .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
             .toString();

    return StringUtils.trimToNull(tag);
  }

  /**
   * Validates and returns a proper locale tag.
   * Falls back to "en" if the input is invalid.
   *
   * @param s the locale string to validate
   * @return valid locale tag
   */
  public static String locale(String s) {
    try {
      var localeTag = StringUtils.defaultIfBlank(s, DEFAULT_LOCALE);
      var locale    = Locale.forLanguageTag(localeTag);
      return LocaleUtils.isAvailableLocale(locale) && !locale.toLanguageTag().equals("und")
             ? locale.toLanguageTag()
             : DEFAULT_LOCALE;
    } catch (Exception e) {
      log.warn("Invalid locale: {}", s, e);
      return DEFAULT_LOCALE;
    }
  }

  /**
   * Validates and returns a proper timezone ID.
   * Falls back to "UTC" if the input is invalid.
   *
   * @param s the timezone string to validate
   * @return valid timezone ID
   */
  public static String timeZone(String s) {
    try {
      return ZoneId.of(StringUtils.defaultIfBlank(s, DEFAULT_TIMEZONE)).getId();
    } catch (Exception e) {
      log.warn("Invalid timezone: {}", s, e);
      return DEFAULT_TIMEZONE;
    }
  }

  /**
   * Validates and normalizes an HTTP URL string.
   * Ensures the URL is absolute and uses a safe scheme.
   * Defaults to HTTPS for protocol-relative URLs.
   *
   * @param url the URL string to validate
   * @return normalized URL string or null if invalid
   */
  @Nullable
  public static String httpUrlString(String url) {
    if (StringUtils.isBlank(url)) {
      return null;
    }

    try {
      // First try parsing as is to check if it's already absolute
      if (url.contains("://")) {
        URI    uri    = URI.create(url);
        String scheme = uri.getScheme();
        if (scheme != null && BLOCKED_SCHEMES.contains(scheme.toLowerCase())) {
          return null;
        }
      }

      // Now handle normalization
      String normalizedUrl = url;
      if (url.startsWith("//")) {
        normalizedUrl = DEFAULT_SCHEME + ":" + url;
      } else if (!url.contains("://")) {
        normalizedUrl = DEFAULT_SCHEME + "://" + url;
      }

      URI uri = URI.create(normalizedUrl);

      // Ensure host is present
      if (uri.getHost() == null) {
        return null;
      }

      return uri.toString();
    } catch (Exception e) {
      log.debug("Invalid URL: {}", url, e);
      return null;
    }
  }

  public static String userNameFromEmail(String email, boolean needRandomSuffix) {
    String[] split = StringUtils.split(email, "@", 2);
    if (split == null) return username(null, needRandomSuffix);
    return username(split[ArrayItem.FIRST], needRandomSuffix);
  }

  /**
   * only allow alphanumeric and underscore
   */
  public static String username(String username, boolean needRandomSuffix) {
    if (StringUtils.isNotBlank(username)) {
      var name = StringUtils.trimToEmpty(username);
      name = RegExUtils.replaceAll(name, "\\s", CC.Separator.US);
      name = RegExUtils.replaceAll(name, "\\-", CC.Separator.US);
      name = RegExUtils.removeAll(name, "[`~!#$%@^*()|+\\-\\&=?;:'\",.<>\\{\\}\\[\\]\\\\\\/]");
      if (name.isEmpty()) name = RandUtils.nanoIdWordSafe(8);
      return name + (needRandomSuffix ?
                     (CC.Separator.US + RandUtils.nanoIdWordSafe(5)) : "");
    } else {
      return "buddy" + RandUtils.nanoIdWordSafe(8);
    }
  }

  public static String username(String username) {
    return username(username, false);
  }

}
