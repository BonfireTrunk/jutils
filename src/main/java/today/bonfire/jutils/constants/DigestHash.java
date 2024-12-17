package today.bonfire.jutils.constants;

/**
 * Hashing and password related constants
 * along with configuration constants for http client/ cookies
 */
public enum DigestHash {
  HMAC_SHA_256("HmacSHA256"),
  HMAC_SHA_384("HmacSHA384"),
  HMAC_SHA_512("HmacSHA512"),
  SHA_3_256("SHA3-256"),
  SHA_3_384("SHA3-384"),
  SHA_3_512("SHA3-512"), // consult before using
  SHA_512("SHA-512"),
  SHA_384("SHA-384"),
  SHA_256("SHA-256");

  private final String value;

  DigestHash(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
