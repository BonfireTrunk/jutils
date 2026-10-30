package today.bonfire.oss.jutils.helpers;

import java.net.IDN;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
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

    private static final Pattern SPECIAL_CHARS = Pattern.compile("[`~!@#$%^&*()_|+=?;:'\",.<>\\{\\}\\[\\]\\\\\\/]");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern INVALID_FILENAME_CHARS =
            Pattern.compile("[`~!#$%^&*()&%|+=?;:'\",<>\\{\\}\\[\\]\\\\\\/]");
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

        var r = NON_ALPHANUMERIC.matcher(text).replaceAll("");

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

        String cleaned = WHITESPACE.matcher(name).replaceAll("");
        cleaned = INVALID_FILENAME_CHARS.matcher(cleaned).replaceAll("");

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
     * Sanitizes a tag name by removing the characters
     * {@code ` ~ ! @ # $ % ^ & * ( ) _ | + = ? ; : ' " , . < > { } [ ] \ /}.
     * After removal, each run of spaces, tabs, newlines, carriage returns, form feeds or vertical tabs becomes
     * one space. The result is trimmed, cut to at most 100 code points, then trimmed again.
     * Returns an empty string for null or blank input or when nothing remains.
     *
     * @param name the tag name to sanitize, may be null
     * @return sanitized tag name or empty string
     */
    public static String tagName(@Nullable String name) {
        if (StringUtils.isBlank(name)) {
            return CC.EMPTY_STRING;
        }

        String result = SPECIAL_CHARS.matcher(name).replaceAll("");
        result = MULTIPLE_WHITESPACE.matcher(result).replaceAll(" ").trim();

        if (result.codePointCount(0, result.length()) > MAX_TAG_LENGTH) {
            result = result.substring(0, result.offsetByCodePoints(0, MAX_TAG_LENGTH));
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
     * The input is normalized to NFC, lowercased with {@link Locale#ROOT} (independent of the default locale),
     * trimmed and cut to its first 100 code points after normalization. Every code point that is not a Unicode
     * letter, digit or combining mark (including whitespace) becomes a hyphen. Runs of hyphens collapse to one
     * and leading and trailing hyphens are removed.
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

        String slug = Normalizer.normalize(name, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT)
                .trim()
                .codePoints()
                .limit(MAX_SLUG_LENGTH)
                .map(cp -> {
                    if (isLetterDigitOrMark(cp)) {
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
     * Normalizes the input to NFC and removes one leading '#', then keeps only Unicode letters, digits,
     * combining marks and '_' (at most 140 code points, counted after normalization).
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

        String tag = Normalizer.normalize(s, Normalizer.Form.NFC);
        tag = tag.startsWith("#") ? tag.substring(1) : tag;

        tag = tag.trim()
                .codePoints()
                .filter(cp -> isLetterDigitOrMark(cp) || cp == '_')
                .limit(MAX_HASHTAG_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return StringUtils.trimToNull(tag);
    }

    /**
     * Validates and returns a proper locale tag.
     * Underscores are treated as hyphens, so "en_US" becomes "en-US".
     * Falls back to "en" if the input is null, blank or does not resolve to an available locale.
     *
     * @param s the locale string to validate, may be null
     * @return valid locale tag
     */
    public static String locale(@Nullable String s) {
        var localeTag = StringUtils.defaultIfBlank(s, DEFAULT_LOCALE).replace('_', '-');
        var locale = Locale.forLanguageTag(localeTag);
        return LocaleUtils.isAvailableLocale(locale) && !locale.toLanguageTag().equals("und")
                ? locale.toLanguageTag()
                : DEFAULT_LOCALE;
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
     * (case-insensitive), a non-null host and no user info (including an empty user-info component).
     * Domain names are converted to ASCII with {@link IDN#toASCII(String)}; a rejected host
     * gives null. Ports, paths, queries, fragments, IPv4 and bracketed IPv6 hosts are preserved.
     * A value that starts with another scheme but has no "//"
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
            String authority = uri.getRawAuthority();
            if (authority == null || authority.indexOf('@') >= 0) {
                return null;
            }
            if (!authority.startsWith("[")) {
                int portStart = authority.lastIndexOf(':');
                String host = portStart < 0 ? authority : authority.substring(0, portStart);
                String asciiHost = IDN.toASCII(host);
                String asciiAuthority = asciiHost + (portStart < 0 ? "" : authority.substring(portStart));
                uri = URI.create(scheme + "://" + asciiAuthority + uri.getRawPath()
                        + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery())
                        + (uri.getRawFragment() == null ? "" : "#" + uri.getRawFragment()));
                if (!asciiHost.equals(uri.getHost())) {
                    return null;
                }
            }
            if (uri.getHost() == null || uri.getRawUserInfo() != null) {
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
     * Builds a username containing only Unicode letters, digits, combining marks and underscore.
     * The input is normalized to NFC and trimmed, whitespace and '-' become '_', and every other character
     * that is not a letter, digit, combining mark or '_' is removed ("José" stays "José", emoji and symbols
     * are dropped). If nothing remains, a random 8-character ID is used. If the input is null or blank, "buddy"
     * plus a random 8-character ID is returned instead.
     *
     * @param username         the desired username, may be null
     * @param needRandomSuffix if true, "_" plus a random 5-character ID is appended to the name (not to the
     *                         blank-input fallback)
     * @return the sanitized username, never empty
     */
    public static String username(@Nullable String username, boolean needRandomSuffix) {
        if (StringUtils.isNotBlank(username)) {
            String name = Normalizer.normalize(username, Normalizer.Form.NFC)
                    .strip()
                    .codePoints()
                    .map(cp -> Character.isWhitespace(cp) || cp == '-' ? '_' : cp)
                    .filter(cp -> isLetterDigitOrMark(cp) || cp == '_')
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

    private static boolean isLetterDigitOrMark(int cp) {
        int type = Character.getType(cp);
        return Character.isLetterOrDigit(cp)
                || type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK
                || type == Character.ENCLOSING_MARK;
    }
}
