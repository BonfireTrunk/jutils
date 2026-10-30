package today.bonfire.oss.jutils.helpers;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import today.bonfire.oss.jutils.RandUtils;
import today.bonfire.oss.jutils.constants.CC;

/**
 * Sanitizing and validating helpers for user supplied strings such as file names, slugs, usernames and URLs.
 */
@Slf4j
public final class GetValid {

    private GetValid() {}

    private static final String SPECIAL_CHARS = "[`~!@#$%^&*()_|+=?;:'\",.<>\\{\\}\\[\\]\\\\\\/]";
    private static final String NON_WORD_CHARS = "\\W";
    private static final int DEFAULT_MAX_LENGTH = 255;
    private static final int MAX_TAG_LENGTH = 100;
    private static final int MAX_SLUG_LENGTH = 100;
    private static final String SLUG_SEPARATOR = "-";
    private static final Pattern MULTIPLE_SEPARATORS = Pattern.compile("-{2,}");
    private static final Pattern LEADING_TRAILING_SEPARATOR = Pattern.compile("^-+|-+$");
    private static final int MAX_HASHTAG_LENGTH = 140; // Common social media limit

    private static final String DEFAULT_LOCALE = "en";
    private static final String DEFAULT_TIMEZONE = "UTC";

    private static final String DEFAULT_SCHEME = "https";

    // "scheme:" not followed by a port number. Separates "javascript:alert(1)" or "mailto:a@b.com" (rejected)
    // from "example.com:8080/x" (a host with a port, accepted).
    private static final Pattern SCHEME_WITHOUT_AUTHORITY =
            Pattern.compile("^[A-Za-z][A-Za-z0-9+.\\-]*:(?!\\d+(?:[/?#]|$))");

    /**
     * Returns the ASCII letters and digits of the input, in order, truncated to 255 characters.
     * Everything else is removed: whitespace, punctuation, underscores and all non-ASCII characters
     * (including accented letters).
     * Returns an empty string (never a random ID) when the input is null or empty or nothing remains.
     *
     * @param text input string to process, may be null
     * @return the ASCII alphanumeric characters of the input, or "" if there are none
     */
    public static String alphaNumeric(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return alphaNumeric(text, DEFAULT_MAX_LENGTH);
    }

    /**
     * Returns the ASCII letters and digits of the input, in order, truncated to {@code maxLength} characters.
     * Everything else is removed: whitespace, punctuation, underscores and all non-ASCII characters
     * (including accented letters).
     * Returns an empty string (never a random ID) when the input is null or empty or nothing remains.
     *
     * @param text      input string to process, may be null
     * @param maxLength maximum length of the result
     * @return the ASCII alphanumeric characters of the input, or "" if there are none
     * @throws StringIndexOutOfBoundsException if {@code maxLength} is negative and the result is longer than it
     */
    public static String alphaNumeric(@Nullable String text, int maxLength) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }

        var r = text.replaceAll(NON_WORD_CHARS, "").replaceAll("_", "");

        if (StringUtils.isBlank(r)) {
            return "";
        }

        return r.length() > maxLength ? r.substring(0, maxLength) : r;
    }

    /**
     * Sanitizes a filename by removing invalid characters and ensuring it's URL safe.
     * Whitespace and the characters {@code ` ~ ! # $ % ^ & * ( ) | + = ? ; : ' " , < > { } [ ] \ /} are removed,
     * then the rest is URL-encoded (UTF-8). The result is at most 255 characters and is cut only between
     * encoded characters, never inside a {@code %XX} escape.
     * The result is one file-name component, not a path, and it does not make names unique.
     * A random 8-character ID is returned when the input is null or blank, when nothing remains after cleaning,
     * or when only dots remain (".", "..", ...), so the result is never a directory reference.
     *
     * @param name the filename to sanitize, may be null
     * @return sanitized filename, or a random ID as described above
     */
    public static String fileName(@Nullable String name) {
        if (StringUtils.isBlank(name)) {
            return RandUtils.nanoIdWordSafe(8);
        }

        String cleaned = RegExUtils.removeAll(name, "\\s");
        cleaned = RegExUtils.removeAll(cleaned, "[`~!#$%^&*()&%|+=?;:'\",<>\\{\\}\\[\\]\\\\\\/]");

        if (cleaned.isEmpty() || cleaned.chars().allMatch(c -> c == '.')) {
            return RandUtils.nanoIdWordSafe(8);
        }

        var result = new StringBuilder();
        for (int i = 0; i < cleaned.length(); ) {
            int cp = cleaned.codePointAt(i);
            i += Character.charCount(cp);
            String encoded = URLEncoder.encode(new String(Character.toChars(cp)), StandardCharsets.UTF_8);
            if (result.length() + encoded.length() > DEFAULT_MAX_LENGTH) {
                break;
            }
            result.append(encoded);
        }
        return result.toString();
    }

    /**
     * Sanitizes a tag name by removing special characters and normalizing spaces.
     * Returns empty string for invalid input.
     *
     * @param name the tag name to sanitize, may be null
     * @return sanitized tag name or empty string
     */
    public static String tagName(@Nullable String name) {
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
     * Equivalent to {@code urlSlug(name, true)}.
     *
     * @param name the string to convert to a slug, may be null
     * @return the slug, or a random 6-character ID if the input is null, blank or yields an empty slug
     * @see #urlSlug(String, boolean)
     */
    public static String urlSlug(@Nullable String name) {
        return urlSlug(name, true);
    }

    /**
     * Creates a URL-friendly slug from the given string.
     * The input is lowercased with {@link Locale#ROOT} (independent of the default locale), cut to its first
     * 100 code points, and every code point that is not a letter or digit (including whitespace) becomes a hyphen.
     * Runs of hyphens collapse to one and leading and trailing hyphens are removed. Unicode letters are kept.
     *
     * @param name             the string to convert to a slug, may be null
     * @param setRandomIfEmpty if true, a random 6-character ID is returned when the input is null, blank or
     *                         yields an empty slug
     * @return the slug, the random ID, or "" if empty and {@code setRandomIfEmpty} is false
     */
    public static String urlSlug(@Nullable String name, boolean setRandomIfEmpty) {
        if (StringUtils.isBlank(name)) {
            return setRandomIfEmpty ? RandUtils.nanoIdWordSafe(6) : CC.EMPTY_STRING;
        }

        String slug = name.toLowerCase(Locale.ROOT)
                .trim()
                .codePoints()
                .limit(MAX_SLUG_LENGTH)
                .map(cp -> {
                    if (Character.isLetterOrDigit(cp)) {
                        return cp;
                    } else {
                        return 45; // Code point for '-'
                    }
                })
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

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
     * Removes one leading '#', then keeps only Unicode letters, digits and '_' (at most 140 code points).
     * It does not transliterate to ASCII and does not change case.
     *
     * @param s the hashtag to sanitize (with or without leading #), may be null
     * @return sanitized hashtag without # prefix, or null if the input is null or blank or nothing remains
     */
    @Nullable
    public static String hashtag(@Nullable String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        // Remove leading # if present
        String tag = s.startsWith("#") ? s.substring(1) : s;

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
     * @param s the locale string to validate, may be null
     * @return valid locale tag
     */
    public static String locale(@Nullable String s) {
        try {
            var localeTag = StringUtils.defaultIfBlank(s, DEFAULT_LOCALE);
            var locale = Locale.forLanguageTag(localeTag);
            return LocaleUtils.isAvailableLocale(locale)
                            && !locale.toLanguageTag().equals("und")
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
     * @param s the timezone string to validate, may be null
     * @return valid timezone ID
     */
    public static String timeZone(@Nullable String s) {
        try {
            return ZoneId.of(StringUtils.defaultIfBlank(s, DEFAULT_TIMEZONE)).getId();
        } catch (Exception e) {
            log.warn("Invalid timezone: {}", s, e);
            return DEFAULT_TIMEZONE;
        }
    }

    /**
     * Validates and normalizes an HTTP(S) URL string.
     * The input is trimmed. A protocol-relative URL ("//host/x") gets an "https:" prefix and a value without
     * "://" gets an "https://" prefix. The result is accepted only if it parses as a URI with scheme http or https
     * (case-insensitive) and a non-null host. A value that starts with another scheme but has no "//"
     * (for example "javascript:alert(1)" or "mailto:a@b.com") is rejected instead of being read as user info.
     * Never throws; a parse failure is logged at debug level and gives null.
     *
     * @param url the URL string to validate, may be null
     * @return the normalized URL ({@code URI.toString()}), or null if the input is null, blank or not a valid
     *         http(s) URL
     */
    @Nullable
    public static String httpUrlString(@Nullable String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        String trimmed = url.trim();
        try {
            String normalizedUrl = trimmed;
            if (trimmed.startsWith("//")) {
                normalizedUrl = DEFAULT_SCHEME + ":" + trimmed;
            } else if (!trimmed.contains("://")) {
                if (SCHEME_WITHOUT_AUTHORITY.matcher(trimmed).find()) {
                    return null;
                }
                normalizedUrl = DEFAULT_SCHEME + "://" + trimmed;
            }

            URI uri = URI.create(normalizedUrl);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                return null;
            }
            if (uri.getHost() == null) {
                return null;
            }

            return uri.toString();
        } catch (Exception e) {
            log.debug("Invalid URL: {}", url, e);
            return null;
        }
    }

    /**
     * Derives a username from an email address.
     * The local part is the text before the first '@' (the whole input if there is no '@'), and it is passed to
     * {@link #username(String, boolean)}. So "", "@", "@x.com" and null all give the "buddy" fallback.
     *
     * @param email            the email address, may be null
     * @param needRandomSuffix if true, "_" plus a random 5-character ID is appended
     * @return a username as described in {@link #username(String, boolean)}
     */
    public static String userNameFromEmail(@Nullable String email, boolean needRandomSuffix) {
        if (email == null) {
            return username(null, needRandomSuffix);
        }
        int at = email.indexOf('@');
        return username(at < 0 ? email : email.substring(0, at), needRandomSuffix);
    }

    /**
     * Builds a username containing only Unicode letters, digits and underscore.
     * The input is trimmed, whitespace and '-' become '_', and every other character that is not a letter,
     * digit or '_' is removed ("José" stays "José", emoji and symbols are dropped). If nothing remains, a random
     * 8-character ID is used. If the input is null or blank, "buddy" plus a random 8-character ID is returned
     * instead.
     *
     * @param username         the desired username, may be null
     * @param needRandomSuffix if true, "_" plus a random 5-character ID is appended to the name (not to the
     *                         blank-input fallback)
     * @return the sanitized username, never empty
     */
    public static String username(@Nullable String username, boolean needRandomSuffix) {
        if (StringUtils.isNotBlank(username)) {
            String name = username.strip()
                    .codePoints()
                    .map(cp -> Character.isWhitespace(cp) || cp == '-' ? '_' : cp)
                    .filter(cp -> Character.isLetterOrDigit(cp) || cp == '_')
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            if (name.isEmpty()) name = RandUtils.nanoIdWordSafe(8);
            return name + (needRandomSuffix ? (CC.Separator.US + RandUtils.nanoIdWordSafe(5)) : "");
        } else {
            return "buddy" + RandUtils.nanoIdWordSafe(8);
        }
    }

    /**
     * Equivalent to {@code username(username, false)}.
     *
     * @param username the desired username, may be null
     * @return the sanitized username, never empty
     * @see #username(String, boolean)
     */
    public static String username(@Nullable String username) {
        return username(username, false);
    }
}
