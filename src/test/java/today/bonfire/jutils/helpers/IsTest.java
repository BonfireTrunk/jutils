package today.bonfire.jutils.helpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class IsTest {

  @Nested
  @DisplayName("True method tests")
  class TrueTests {
    @Test
    @DisplayName("Should return true only for Boolean.TRUE")
    void shouldReturnTrueOnlyForBooleanTrue() {
      assertThat(Is.True(Boolean.TRUE)).isTrue();
      assertThat(Is.True(Boolean.FALSE)).isFalse();
      assertThat(Is.True(null)).isFalse();
      assertThat(Is.True(true)).isTrue(); // primitive boolean wrapped to new Boolean
    }
  }

  @Nested
  @DisplayName("False method tests")
  class FalseTests {
    @Test
    @DisplayName("Should return true only for Boolean.FALSE")
    void shouldReturnTrueOnlyForBooleanFalse() {
      assertThat(Is.False(Boolean.FALSE)).isTrue();
      assertThat(Is.False(Boolean.TRUE)).isFalse();
      assertThat(Is.False(null)).isFalse();
      assertThat(Is.False(false)).isTrue(); // primitive boolean wrapped to new Boolean
    }
  }

  @Nested
  @DisplayName("Falsy method tests")
  class FalsyTests {
    @Test
    @DisplayName("Should return true for null and Boolean.FALSE")
    void shouldReturnTrueForNullAndFalse() {
      assertThat(Is.Falsy(null)).isTrue();
      assertThat(Is.Falsy(Boolean.FALSE)).isTrue();
      assertThat(Is.Falsy(false)).isTrue();
      assertThat(Is.Falsy(Boolean.TRUE)).isFalse();
      assertThat(Is.Falsy(true)).isFalse();
    }
  }

  @Nested
  @DisplayName("jsonBlank method tests")
  class JsonBlankTests {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", "null", "[]", "{}"})
    @DisplayName("Should return true for blank or empty JSON values")
    void shouldReturnTrueForBlankOrEmptyJson(String input) {
      assertThat(Is.jsonBlank(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "{\"key\":\"value\"}",
      "[1,2,3]",
      "\"string\"",
      "123",
      "false"
    })
    @DisplayName("Should return false for non-blank JSON values")
    void shouldReturnFalseForNonBlankJson(String input) {
      assertThat(Is.jsonBlank(input)).isFalse();
    }
  }

  @Nested
  @DisplayName("colour method tests")
  class ColourTests {
    @ParameterizedTest
    @ValueSource(strings = {
      "#000000",    // Black
      "#FFFFFF",    // White
      "#ff0000",    // Red
      "#00ff00",    // Green
      "#0000ff",    // Blue
      "#123456",    // Random valid color
      "#abc",       // 3-digit format
      "#12345678"   // With alpha channel
    })
    @DisplayName("Should return true for valid hex colors")
    void shouldReturnTrueForValidHexColors(String input) {
      assertThat(Is.colour(input)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
      "000000",     // Missing #
      "#12",        // Too short
      "#1234",      // Invalid length
      "#12345",     // Invalid length
      "#1234567",   // Invalid length
      "#123456789", // Too long
      "#GGGGGG",    // Invalid characters
      "red",        // Named color
      "#fff#",      // Invalid format
      "##ffffff"    // Double hash
    })
    @DisplayName("Should return false for invalid hex colors")
    void shouldReturnFalseForInvalidHexColors(String input) {
      assertThat(Is.colour(input)).isFalse();
    }
  }

  @Nested
  @DisplayName("email method tests")
  class EmailTests {
    @ParameterizedTest
    @ValueSource(strings = {
      "test@example.com",
      "user.name@domain.com",
      "user+tag@domain.com",
      "user@sub.domain.com",
      "a@b.com",
      "123@domain.com",
      "user@domain-name.com",
      "user_name@domain.com",
      "user.name+tag@domain.com",
      "user@domain.co.uk"
    })
    @DisplayName("Should return true for valid email addresses")
    void shouldReturnTrueForValidEmails(String input) {
      assertThat(Is.email(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "user@xn--80ak6aa92e.com",              // Punycode domain (мой.com)
      "test@xn--d1acj3b.com",                 // Punycode domain (почта.com)
      "user@xn--80aqecdr1a.xn--p1ai",         // Multiple punycode parts (Russian domain)
      "test@xn--domain.com",                  // Simple punycode domain
      "user.name@xn--80ak6aa92e.com",         // Punycode with complex local part
      "user+tag@xn--d1acj3b.co.uk",          // Punycode with multi-part TLD
      "test@sub.xn--80ak6aa92e.com",         // Punycode with subdomain
      "test@xn--90a3ac.xn--90a3ac"           // Multiple punycode parts (Serbian domain)
    })
    @DisplayName("Should return true for valid punycode domain email addresses")
    void shouldReturnTrueForPunycodeEmails(String input) {
      assertThat(Is.email(input)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
      "test",                     // No @ symbol
      "@domain.com",              // No local part
      "test@",                    // No domain
      "test@domain",              // No TLD
      "test@.com",                // Domain starts with dot
      "test@domain..com",         // Consecutive dots
      "test@domain.c",            // TLD too short
      "test space@domain.com",    // Contains space
      "test@dom ain.com",         // Domain contains space
      ".test@domain.com",         // Starts with dot
      "test.@domain.com",         // Ends with dot
      "test..test@domain.com",    // Consecutive dots
      "test@domain.com.",         // Ends with dot
      "test@@domain.com",         // Multiple @ symbols
      "test@domain@com",          // Multiple @ symbols
      "тест@domain.com",          // Non-ASCII characters in local part
      "test@xn--",               // Incomplete punycode
      "test@xn--.com",           // Invalid punycode (missing punycode part)
      "test@xn--invalid%.com",   // Invalid punycode characters
      "test@xn--a.xn--",         // Incomplete punycode in subdomain
      "test@xn--90a3ac..xn--90a3ac", // Consecutive dots in punycode
      "as..as@example.com"
    })
    @DisplayName("Should return false for invalid email addresses")
    void shouldReturnFalseForInvalidEmails(String input) {
      assertThat(Is.email(input)).isFalse();
    }
  }


  @Nested
  @DisplayName("subDomain method tests")
  class SubDomainTests {
    @ParameterizedTest
    @ValueSource(strings = {
      "domain",
      "localhost",
      "example",
      "my-site",
      "123abc",
      "xn--80ak6aa92e"
    })
    @DisplayName("Should return true for valid single word subdomains")
    void shouldReturnTrueForValidSubdomains(String input) {
      assertThat(Is.subDomain(input)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
      "example.com",
      "123.456",
      "-invalid",
      "invalid-",
      "..invalid",
      "invalid..",
      "asdasd.",
      ".asdasda",
      "",
      "asd_sadasd",
      "example@domain"
    })
    @DisplayName("Should return false for invalid single word subdomains")
    void shouldReturnFalseForInvalidSubdomains(String input) {
      assertThat(Is.subDomain(input)).isFalse();
    }
  }
}
