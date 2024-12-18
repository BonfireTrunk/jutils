package today.bonfire.jutils.helpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

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
    "'', buddy",  // empty string case
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
    "'', ''",  // empty string case
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
    "'', ''",  // empty string case
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
    "'', UTC",  // empty string case
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
    "en_US, en",
    "zh-CN-Hans, zh-CN",

    "'', en",  // empty string case
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
    "'', ''",  // empty string case
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
    "'',",  // empty string case
    "javascript:alert(1),",  // blocked scheme
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
}
