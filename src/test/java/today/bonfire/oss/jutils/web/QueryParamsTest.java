package today.bonfire.oss.jutils.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class QueryParamsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "?", "&", "&&", "=", "?=", "=x"})
    @DisplayName("Should give an empty instance for blank or keyless input")
    void shouldBeEmpty(String input) {
        var params = QueryParams.parseQueryString(input);
        assertThat(params.isEmpty()).isTrue();
        assertThat(params.asMap()).isEmpty();
        assertThat(params.toQueryString()).isEmpty();
    }

    @Test
    @DisplayName("Should split at the first equals sign only")
    void shouldSplitAtFirstEquals() {
        var params = QueryParams.parseQueryString("a=b=c");
        assertThat(params.getFirst("a")).isEqualTo("b=c");
    }

    @Test
    @DisplayName("Should skip empty segments and strip the leading question mark")
    void shouldSkipEmptySegments() {
        var params = QueryParams.parseQueryString(" ?a&&b=2 ");
        assertThat(params.asMap()).containsOnlyKeys("a", "b");
        assertThat(params.getFirst("a")).isEmpty();
        assertThat(params.getFirst("b")).isEqualTo("2");
        assertThat(params.isEmpty()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "a=", "a=&"})
    @DisplayName("Should use an empty string for missing values")
    void shouldUseEmptyStringForMissingValue(String input) {
        var params = QueryParams.parseQueryString(input);
        assertThat(params.get("a")).containsExactly("");
        assertThat(params.hasQueryParam("a")).isTrue();
    }

    @Test
    @DisplayName("Should keep repeated keys in order")
    void shouldKeepRepeatedKeys() {
        var params = QueryParams.parseQueryString("a=1&b=x&a=2&a=3");
        assertThat(params.get("a")).containsExactly("1", "2", "3");
        assertThat(params.getFirst("a")).isEqualTo("1");
        assertThat(params.asMap().keySet()).containsExactly("a", "b");
    }

    @Test
    @DisplayName("Should decode plus and percent escapes in keys and values")
    void shouldDecode() {
        var params = QueryParams.parseQueryString("q=a+b%20c&k%20ey=%C3%A9");
        assertThat(params.getFirst("q")).isEqualTo("a b c");
        assertThat(params.getFirst("k ey")).isEqualTo("é");
    }

    @Test
    @DisplayName("Should trim keys and values before decoding")
    void shouldTrim() {
        var params = QueryParams.parseQueryString(" a = b ");
        assertThat(params.getFirst("a")).isEqualTo("b");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a=%zz", "%zz=1", "a=%"})
    @DisplayName("Should throw for malformed percent-encoding")
    void shouldThrowForMalformedEncoding(String input) {
        assertThatThrownBy(() -> QueryParams.parseQueryString(input)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should return an empty list and null for an absent key")
    void shouldHandleAbsentKey() {
        var params = QueryParams.parseQueryString("a=1");
        assertThat(params.get("zzz")).isEmpty();
        assertThat(params.getFirst("zzz")).isNull();
        assertThat(params.hasQueryParam("zzz")).isFalse();
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        var params = QueryParams.parseQueryString("a=1&a=2");
        List<String> values = params.get("a");
        assertThatThrownBy(() -> values.add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> params.get("zzz").add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> params.asMap().put("b", List.of())).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> params.asMap().get("a").add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> params.getParamsAsFlatMap().put("b", "1"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(params.get("a")).containsExactly("1", "2");
    }

    @Test
    @DisplayName("Flat map should join values with a comma and be lossy")
    void shouldFlatten() {
        var repeated = QueryParams.parseQueryString("a=1&a=2&b=x");
        var joined = QueryParams.parseQueryString("a=1,2&b=x");
        assertThat(repeated.getParamsAsFlatMap()).containsExactly(Map.entry("a", "1,2"), Map.entry("b", "x"));
        assertThat(repeated.getParamsAsFlatMap()).isEqualTo(joined.getParamsAsFlatMap());
    }

    @Test
    @DisplayName("toQueryString should encode and round trip")
    void shouldRoundTrip() {
        var params = QueryParams.parseQueryString("z=1&a=x+y&a=%26%3D&k%20ey=%C3%A9&e=");
        String rendered = params.toQueryString();
        assertThat(rendered).isEqualTo("z=1&a=x+y&a=%26%3D&k+ey=%C3%A9&e=");
        assertThat(QueryParams.parseQueryString(rendered).asMap()).isEqualTo(params.asMap());
    }
}
