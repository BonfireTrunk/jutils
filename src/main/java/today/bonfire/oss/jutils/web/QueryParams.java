package today.bonfire.oss.jutils.web;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import today.bonfire.oss.jutils.constants.CC;

/**
 * An immutable, insertion-ordered view of a parsed URL query string. A key can have several values.
 */
public final class QueryParams {
    private static final QueryParams EMPTY = new QueryParams(Map.of());
    private final Map<String, List<String>> params;

    private QueryParams(Map<String, List<String>> params) {
        this.params = params;
    }

    /**
     * Parse a query string into a QueryParams object.
     * The input is trimmed and leading '?' characters are removed. It is split on '&amp;' (empty segments are
     * skipped) and each segment is split at its first '='. Keys and values are trimmed and then URL-decoded
     * (UTF-8, '+' becomes a space). A segment without '=' has the value "". Segments whose decoded key is empty
     * are skipped. Values are never null.
     *
     * @param query the query string to parse, may be null
     * @return a QueryParams object representing the query string; empty if the input is null or blank
     * @throws IllegalArgumentException if a key or value contains malformed percent-encoding such as "%zz"
     */
    public static QueryParams parseQueryString(@Nullable String query) {
        if (query == null || query.isBlank()) {
            return EMPTY;
        }

        var qs = StringUtils.stripStart(query.trim(), "?");
        Map<String, List<String>> parsed = new LinkedHashMap<>();

        for (String segment : qs.split("&")) {
            if (segment.isEmpty()) {
                continue;
            }
            int eq = segment.indexOf('=');
            String rawKey = eq < 0 ? segment : segment.substring(0, eq);
            String rawValue = eq < 0 ? "" : segment.substring(eq + 1);

            String key = URLDecoder.decode(rawKey.trim(), StandardCharsets.UTF_8);
            if (key.isEmpty()) {
                continue;
            }
            String value = URLDecoder.decode(rawValue.trim(), StandardCharsets.UTF_8);
            parsed.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        parsed.replaceAll((k, v) -> List.copyOf(v));
        return new QueryParams(Collections.unmodifiableMap(parsed));
    }

    /**
     * Get the first value associated with the given key.
     *
     * @param key the key to retrieve the first value for
     * @return the first value associated with the key (possibly ""), or null if the key is not present
     */
    public @Nullable String getFirst(String key) {
        List<String> values = params.get(key);
        return values == null ? null : values.get(0);
    }

    /**
     * Get the values associated with the given key.
     *
     * @param key the key to retrieve values for
     * @return an unmodifiable list of the values in order of appearance, or an empty list if the key is not present
     */
    public List<String> get(String key) {
        return params.getOrDefault(key, List.of());
    }

    /**
     * Get all parameters.
     *
     * @return an unmodifiable map in insertion order whose values are unmodifiable lists
     */
    public Map<String, List<String>> asMap() {
        return params;
    }

    /**
     * Get the query parameters as a flat map. This is lossy: multiple values of a key are joined with ",", so
     * "a=1,2" and "a=1&amp;a=2" give the same entry. Use {@link #asMap()} to keep them apart.
     *
     * @return an unmodifiable flat map in insertion order
     */
    public Map<String, String> getParamsAsFlatMap() {
        Map<String, String> flatMap = new LinkedHashMap<>();
        params.forEach((key, values) -> flatMap.put(key, String.join(CC.Separator.COMMA, values)));
        return Collections.unmodifiableMap(flatMap);
    }

    /**
     * Check if the query parameters contain the given key.
     *
     * @param key the key to check for
     * @return true if the key is present, false otherwise
     */
    public boolean hasQueryParam(String key) {
        return params.containsKey(key);
    }

    /**
     * Check whether there are no parameters.
     *
     * @return true if no parameter was parsed, false otherwise
     */
    public boolean isEmpty() {
        return params.isEmpty();
    }

    /**
     * Render the parameters as a query string without a leading '?'. Keys and values are URL-encoded (UTF-8,
     * a space becomes '+'), pairs are written as "k=v" and joined with '&amp;', repeated keys are repeated, in
     * insertion order.
     *
     * @return the encoded query string, or "" if there are no parameters
     */
    public String toQueryString() {
        return params.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(v -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                                + CC.Separator.EQUALS
                                + URLEncoder.encode(v, StandardCharsets.UTF_8)))
                .collect(Collectors.joining(CC.Separator.AMPERSAND));
    }
}
