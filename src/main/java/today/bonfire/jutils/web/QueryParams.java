package today.bonfire.jutils.web;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryParams {
  private static final QueryParams               EMPTY = new QueryParams();
  private final        Map<String, List<String>> params;

  private QueryParams() {
    params = new HashMap<>();
  }

  /**
   * Parse a query string into a QueryParams object.
   *
   * @param query the query string to parse
   * @return a QueryParams object representing the query string
   */
  public static QueryParams parseQueryString(String query) {
    if (query == null || query.isBlank()) {
      return EMPTY;
    }

    QueryParams queryParams = new QueryParams();
    var         qs          = StringUtils.trim(query);
    qs = StringUtils.stripStart(qs, "?");
    String[] pairs = qs.split("&");

    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      String   key      = keyValue[0].trim();
      String   value    = keyValue.length > 1 ? keyValue[1].trim() : null;

      queryParams.params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    return queryParams;
  }

  /**
   * Get the first value associated with the given key.
   *
   * @param key the key to retrieve the first value for
   * @return the first value associated with the key, or null if the key is not present or the value is empty
   */
  public String getFirst(String key) {
    List<String> values = get(key);
    return values != null && !values.isEmpty() ? values.get(0) : null;
  }

  /**
   * Get the values associated with the given key.
   *
   * @param key the key to retrieve values for
   * @return the values associated with the key, or null if the key is not present
   */
  public List<String> get(String key) {
    return params.get(key);
  }

  /**
   * Get the query parameters as a flat map.
   *
   * @return a flat map of query parameters
   */
  public Map<String, String> getParamsAsFlatMap() {
    Map<String, String> flatMap = new HashMap<>();
    params.forEach((key, values) -> flatMap.put(key, String.join("|", values)));
    return flatMap;
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
}
