package today.bonfire.oss.jutils.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class GetValidTest {

    @ParameterizedTest
    @CsvSource({
        "valid_file.txt, valid_file.txt",
        "another.valid_file.doc, another.valid_file.doc",
        "file-name.pdf, file-name.pdf",
        "invalid/file:name.txt, invalidfilename.txt",
        "<invalid>.txt, invalid.txt",
        "*invalid*.txt, invalid.txt"
    })
    @DisplayName("Should handle various filename inputs")
    void shouldHandleVariousFilenameInputs(String input, String expectedOutput) {
        String result = GetValid.fileName(input);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource({
        "valid_username, valid_username",
        "user_name123, user_name123",
        "user.name, username",
        "'', buddy", // empty string case
        "invalid username, invalid_username",
        "user@name, username",
        "user#name, username",
        "user.name@domain.com, usernamedomaincom",
    })
    @DisplayName("Should handle various username inputs")
    void shouldHandleVariousUsernameInputs(String input, String expectedOutput) {
        String result = GetValid.username(input, false);
        if (input.isEmpty()) {
            assertThat(result).startsWith(expectedOutput);
        } else {
            assertThat(result).isEqualTo(expectedOutput);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "valid-url, valid-url",
        "another-valid-url-123, another-valid-url-123",
        "url/with/slashes, url-with-slashes",
        "'', ''", // empty string case
        "invalid url!, invalid-url",
        "url#with#special&chars, url-with-special-chars",
        "http://example.com/path, http-example-com-path",
        "http://example.com/invalid space, http-example-com-invalid-space",
    })
    @DisplayName("Should handle various URL slug inputs")
    void shouldHandleVariousUrlSlugInputs(String input, String expectedOutput) {
        String result = GetValid.urlSlug(input, false);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource({
        "valid-tag, valid-tag",
        "'another valid tag', 'another valid tag'",
        "tag_with_special_chars!, tagwithspecialchars",
        "'', ''", // empty string case
        "invalid#tag!, invalidtag",
        "'tag with spaces', 'tag with spaces'"
    })
    @DisplayName("Should handle various tag name inputs")
    void shouldHandleVariousTagNameInputs(String input, String expectedOutput) {
        String result = GetValid.tagName(input);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource({
        "America/New_York, America/New_York",
        "UTC, UTC",
        "Europe/London, Europe/London",
        "'', UTC", // empty string case
        "Invalid/Timezone, UTC",
        "NotAZone, UTC"
    })
    @DisplayName("Should handle various timezone inputs")
    void shouldHandleVariousTimezoneInputs(String input, String expectedOutput) {
        String result = GetValid.timeZone(input);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource({
        "en-US, en-US",
        "fr-FR, fr-FR",
        "de-DE, de-DE",
        "en_US, en-US",
        "fr_FR, fr-FR",
        "zh_Hans_CN, zh-Hans-CN",
        "zh-CN-Hans, zh-CN",
        "'', en", // empty string case
        "Invalid/Locale, en",
        "xx, en",
        ", en"
    })
    @DisplayName("Should handle various locale inputs")
    void shouldHandleVariousLocaleInputs(String input, String expectedOutput) {
        String result = GetValid.locale(input);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource({
        "test123, test123",
        "special@#$chars, specialchars",
        "with space, withspace",
        "'', ''", // empty string case
        "very_long_string_that_exceeds_limit, verylongstringthatexceeds",
        "123, 123"
    })
    @DisplayName("Should handle various alphanumeric inputs")
    void shouldHandleVariousAlphanumericInputs(String input, String expectedOutput) {
        String result = GetValid.alphaNumeric(input, 25);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource({
        "http://example.com, http://example.com",
        "https://test.com, https://test.com",
        "//example.com, https://example.com",
        "'',", // empty string case
        "javascript:alert(1),", // blocked scheme
        "invalid-url, https://invalid-url"
    })
    @DisplayName("Should handle various HTTP URL inputs")
    void shouldHandleVariousHttpUrlInputs(String input, String expectedOutput) {
        String result = GetValid.httpUrlString(input);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @CsvSource({
        "#validHashtag, validHashtag",
        "#another_valid, another_valid",
        "#invalid#hashtag, invalidhashtag",
        "#hashtag_with_numbers123, hashtag_with_numbers123",
        "#tag_with_special@chars, tag_with_specialchars",
        "#, ",
        ", ",
        "#hashtag_with_specials!@#, hashtag_with_specials",
        "#hashtag123, hashtag123",
        "#  leadingSpace, leadingSpace",
        "#trailingSpace , trailingSpace",
        "#a, a",
        "#hashtag_with_specials!@#$%^&*(), hashtag_with_specials",
        "#hashtag_with_numbers1234567890, hashtag_with_numbers1234567890",
        "#hashtag_with_underscores_and_numbers_123, hashtag_with_underscores_and_numbers_123"
    })
    @DisplayName("Should handle various hashtag inputs")
    void shouldHandleVariousHashtagInputs(String input, String expectedOutput) {
        String result = GetValid.hashtag(input);
        assertThat(result).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @ValueSource(strings = {".", "..", "...", " .. ", "./", "../"})
    @DisplayName("fileName should not return dot-only names")
    void fileNameShouldNotReturnDotOnlyNames(String input) {
        String result = GetValid.fileName(input);
        assertThat(result).hasSize(8).doesNotContain(".");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "/", "<>"})
    @DisplayName("fileName should return a random ID when nothing remains")
    void fileNameShouldReturnRandomIdWhenNothingRemains(String input) {
        assertThat(GetValid.fileName(input)).hasSize(8);
    }

    @Test
    @DisplayName("fileName should not cut an encoded character in half")
    void fileNameShouldNotCutEscapes() {
        assertThat(GetValid.fileName("x".repeat(254) + "é")).isEqualTo("x".repeat(254));
        assertThat(GetValid.fileName("x".repeat(249) + "é")).isEqualTo("x".repeat(249) + "%C3%A9");
    }

    @Test
    @DisplayName("fileName should cap at 255 characters and keep a supplementary code point whole")
    void fileNameShouldCapLength() {
        assertThat(GetValid.fileName("a".repeat(400))).hasSize(255);
        assertThat(GetValid.fileName("a".repeat(243) + "😀")).isEqualTo("a".repeat(243) + "%F0%9F%98%80");
        assertThat(GetValid.fileName("a".repeat(250) + "😀")).isEqualTo("a".repeat(250));
    }

    @Test
    @DisplayName("urlSlug should lowercase independent of the default locale")
    void urlSlugShouldIgnoreDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(GetValid.urlSlug("TITLE", false)).isEqualTo("title");
            assertThat(GetValid.urlSlug("TITLE")).isEqualTo("title");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    @DisplayName("urlSlug should return a random ID for blank input when requested")
    void urlSlugShouldReturnRandomId() {
        assertThat(GetValid.urlSlug(null)).hasSize(6);
        assertThat(GetValid.urlSlug("!!!")).hasSize(6);
        assertThat(GetValid.urlSlug(null, false)).isEmpty();
        assertThat(GetValid.urlSlug("a   b\tc")).isEqualTo("a-b-c");
        assertThat(GetValid.urlSlug("Café Crème")).isEqualTo("café-crème");
    }

    @ParameterizedTest
    @CsvSource({"नमस्ते दुनिया, नमस्ते-दुनिया", "வணக்கம் உலகம், வணக்கம்-உலகம்"})
    @DisplayName("urlSlug should preserve combining marks in words")
    void urlSlugShouldPreserveCombiningMarks(String input, String expected) {
        assertThat(GetValid.urlSlug(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"செல்வம்", "नमस्ते", "বাংলা", "का", "q\u0307", "a\u20dd", "\uD801\uDC28\uD834\uDD65"})
    @DisplayName("slugs, usernames and hashtags should keep letters and all combining mark categories")
    void shouldKeepCombiningMarks(String input) {
        assertThat(GetValid.urlSlug(input, false)).isEqualTo(input);
        assertThat(GetValid.username(input)).isEqualTo(input);
        assertThat(GetValid.hashtag("#" + input)).isEqualTo(input);
    }

    @Test
    @DisplayName("slugs, usernames and hashtags should normalize decomposed accents to NFC")
    void shouldNormalizeDecomposedAccents() {
        String input = "re\u0301sume\u0301 cafe\u0301";
        assertThat(GetValid.urlSlug(input)).isEqualTo("résumé-café");
        assertThat(GetValid.username(input)).isEqualTo("résumé_café");
        assertThat(GetValid.hashtag("#" + input)).isEqualTo("résumécafé");
        assertThat(GetValid.username("Jose\u0301", true)).matches("José_.{5}");
        assertThat(GetValid.hashtag("#a😀b☃c©")).isEqualTo("abc");
    }

    @Test
    @DisplayName("slug and hashtag limits should count code points after normalization")
    void shouldCountNormalizedCodePoints() {
        assertThat(GetValid.urlSlug("e\u0301".repeat(101))).isEqualTo("é".repeat(100));
        assertThat(GetValid.hashtag("#" + "e\u0301".repeat(141))).isEqualTo("é".repeat(140));
        assertThat(GetValid.urlSlug("\uD801\uDC28".repeat(101))).isEqualTo("\uD801\uDC28".repeat(100));
        assertThat(GetValid.hashtag("#" + "\uD801\uDC28".repeat(141))).isEqualTo("\uD801\uDC28".repeat(140));
        assertThat(GetValid.urlSlug("का".repeat(51))).isEqualTo("का".repeat(50));
        assertThat(GetValid.hashtag("#" + "का".repeat(71))).isEqualTo("का".repeat(70));
    }

    @Test
    @DisplayName("alphaNumeric should keep only ASCII letters and digits and return empty for null")
    void alphaNumericShouldKeepAscii() {
        assertThat(GetValid.alphaNumeric(null)).isEmpty();
        assertThat(GetValid.alphaNumeric("")).isEmpty();
        assertThat(GetValid.alphaNumeric("!!!")).isEmpty();
        assertThat(GetValid.alphaNumeric("a_b-cé 1")).isEqualTo("abc1");
        assertThat(GetValid.alphaNumeric("abcdef")).isEqualTo("abcdef");
        assertThat(GetValid.alphaNumeric(null, 5)).isEmpty();
    }

    @Test
    @DisplayName("tagName should return empty for null")
    void tagNameShouldHandleNull() {
        assertThat(GetValid.tagName(null)).isEmpty();
        assertThat(GetValid.tagName("a".repeat(150))).hasSize(100);
        assertThat(GetValid.tagName("a    b")).isEqualTo("a b");
    }

    @ParameterizedTest
    @CsvSource({"foo & bar, foo bar", "c++ / java-script, c java-script"})
    @DisplayName("tagName should collapse spaces after removing special characters")
    void tagNameShouldCollapseSpacesAfterRemoval(String input, String expected) {
        assertThat(GetValid.tagName(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("tagName should collapse whitespace and trim the result")
    void tagNameShouldNormalizeWhitespace() {
        assertThat(GetValid.tagName("foo\tbar\nbaz")).isEqualTo("foo bar baz");
        assertThat(GetValid.tagName(" \tfoo\r\n\f\u000b & bar \t ")).isEqualTo("foo bar");
        assertThat(GetValid.tagName(" \t\r\n ")).isEmpty();
        assertThat(GetValid.tagName("`~!@#$%^&*()_|+=?;:'\",.<> {}[]\\/")).isEmpty();
        assertThat(GetValid.tagName("a-b: c.d_")).isEqualTo("a-b cd");
    }

    @Test
    @DisplayName("tagName should truncate by code point and trim again")
    void tagNameShouldTruncateByCodePoint() {
        assertThat(GetValid.tagName("a".repeat(99) + "😀z")).isEqualTo("a".repeat(99) + "😀");
        assertThat(GetValid.tagName("😀".repeat(101))).isEqualTo("😀".repeat(100));
        assertThat(GetValid.tagName("a".repeat(99) + " & z")).isEqualTo("a".repeat(99));
    }

    @Test
    @DisplayName("locale and timeZone should fall back for null")
    void localeAndTimeZoneShouldHandleNull() {
        assertThat(GetValid.locale(null)).isEqualTo("en");
        assertThat(GetValid.timeZone(null)).isEqualTo("UTC");
    }

    @Test
    @DisplayName("hashtag should keep Unicode letters and case")
    void hashtagShouldKeepUnicodeAndCase() {
        assertThat(GetValid.hashtag("#CaféTime")).isEqualTo("CaféTime");
        assertThat(GetValid.hashtag("#" + "b".repeat(200))).hasSize(140);
        assertThat(GetValid.hashtag("   ")).isNull();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "ftp://example.com",
                "javascript://example.com/path",
                "javascript:alert(1)",
                "data:text/html,hi",
                "file:///etc/passwd",
                "mailto:user@example.com",
                "jar:file:///x.jar!/a",
                "http://",
                "https://",
                "   ",
                "//"
            })
    @DisplayName("httpUrlString should reject non-http(s) and hostless URLs")
    void httpUrlStringShouldReject(String input) {
        assertThat(GetValid.httpUrlString(input)).isNull();
    }

    @Test
    @DisplayName("httpUrlString should return null for null")
    void httpUrlStringShouldHandleNull() {
        assertThat(GetValid.httpUrlString(null)).isNull();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://evil.com@good.com/",
                "https://user:pass@x.com/",
                "https://@x.com/",
                "//user@例え.jp/path",
                "user@good.com/",
                "https://evil.com＠good.com/",
                "https://例え.jp／other.com/",
                "https://例え.jp：8443/",
                "https://\u200D.jp/",
                "https://a..例え.jp/",
                "https://例え.jp:invalid/",
                "https://exa_mple.com/",
                "https://good.com%40evil.com/"
            })
    @DisplayName("httpUrlString should reject user info and invalid internationalized hosts")
    void httpUrlStringShouldRejectUserInfoAndInvalidHosts(String input) {
        assertThat(GetValid.httpUrlString(input)).isNull();
    }

    @Test
    @DisplayName("httpUrlString should reject host labels longer than the IDN limit")
    void httpUrlStringShouldRejectLongIdnLabel() {
        assertThat(GetValid.httpUrlString("https://" + "例".repeat(64) + ".jp/")).isNull();
        assertThat(GetValid.httpUrlString("https://" + "a".repeat(64) + ".com/"))
                .isNull();
    }

    @ParameterizedTest
    @CsvSource({
        "HTTP://Example.com/x, HTTP://Example.com/x",
        "//example.com, https://example.com",
        "example.com/path, https://example.com/path",
        "'  https://example.com  ', https://example.com",
        "example.com:8080/x, https://example.com:8080/x",
        "localhost:3000, https://localhost:3000",
        "https://例え.jp/path, https://xn--r8jz45g.jp/path",
        "//例え.jp/path, https://xn--r8jz45g.jp/path",
        "例え.jp:8443/path, https://xn--r8jz45g.jp:8443/path",
        "https://xn--r8jz45g.jp/path, https://xn--r8jz45g.jp/path",
        "https://例え.jp:8443/a%2Fb/é?q=%26&x=値#f%20g, https://xn--r8jz45g.jp:8443/a%2Fb/é?q=%26&x=値#f%20g",
        "https://例え.jp?x=1#part, https://xn--r8jz45g.jp?x=1#part",
        "http://127.0.0.1:8080/x?q=1#part, http://127.0.0.1:8080/x?q=1#part",
        "https://[::1]:8443/x, https://[::1]:8443/x",
        "//[::1]/x?q=%2F#part, https://[::1]/x?q=%2F#part"
    })
    @DisplayName("httpUrlString should accept and normalize http(s) URLs")
    void httpUrlStringShouldAccept(String input, String expected) {
        assertThat(GetValid.httpUrlString(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"a@x.com, a", "john.doe@x.com, johndoe", "no-at-sign, no_at_sign", "a@b@c.com, a"})
    @DisplayName("userNameFromEmail should use the text before the first @")
    void userNameFromEmailShouldUseLocalPart(String input, String expected) {
        assertThat(GetValid.userNameFromEmail(input, false)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"@", "@x.com", "  @x.com"})
    @DisplayName("userNameFromEmail should fall back to buddy without throwing")
    void userNameFromEmailShouldFallBack(String input) {
        assertThat(GetValid.userNameFromEmail(input, false)).startsWith("buddy").hasSize(13);
    }

    @Test
    @DisplayName("userNameFromEmail should append a random suffix when requested")
    void userNameFromEmailShouldAppendSuffix() {
        assertThat(GetValid.userNameFromEmail("a@x.com", true)).matches("a_.{5}");
    }

    @Test
    @DisplayName("username should drop emoji and symbols and keep accented letters")
    void usernameShouldKeepOnlyLettersDigitsUnderscore() {
        assertThat(GetValid.username("José")).isEqualTo("José");
        assertThat(GetValid.username("a😀b☃c©")).isEqualTo("abc");
        assertThat(GetValid.username("a-b c")).isEqualTo("a_b_c");
        assertThat(GetValid.username("😀😀")).hasSize(8);
        assertThat(GetValid.username("!!!")).hasSize(8);
    }

    @Test
    @DisplayName("username should handle suffix and blank input")
    void usernameShouldHandleSuffixAndBlank() {
        assertThat(GetValid.username("abc", true)).matches("abc_.{5}");
        assertThat(GetValid.username(null)).startsWith("buddy").hasSize(13);
        assertThat(GetValid.username("   ", true)).startsWith("buddy").hasSize(13);
    }
}
