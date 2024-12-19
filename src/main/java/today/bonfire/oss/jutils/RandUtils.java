package today.bonfire.oss.jutils;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

/**
 * Utility class for generating random numbers, tokens, and IDs.
 */
public class RandUtils {
  /**
   * Default byte length for token generation. 36 bytes provides:
   * - 288 bits (36 * 8) of entropy
   * - Results in 48 characters when base64 encoded (36 * 4/3)
   * - Provides sufficient collision resistance for most use cases
   * - Balances security with string length
   */
  private static final int DEF_RANDOM_BYTE_LEN = 36;

  /**
   * Base64 URL-safe alphabet with '-' and '_' for padding-free encoding
   * Used in general token generation where case sensitivity is required
   */
  private static final char[] DEFAULT_ALPHABET_64 = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray();

  /**
   * Standard alphanumeric alphabet (0-9, A-Z, a-z)
   * Used when only alphanumeric characters are needed
   */
  private static final char[] DEFAULT_ALPHABET_62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

  /**
   * Word-safe alphabet avoiding similar looking characters
   * Useful for human readable IDs where visual confusion should be minimized
   * Excludes characters like: 0/O, 1/l/I, etc.
   */
  private static final char[] WORDSAFE_ALPHABET_32 = "23456789CFGHJMPQRVWXcfghjmpqrvwx".toCharArray();

  /**
   * Case-insensitive alphabet for generating IDs
   * Useful when case sensitivity doesn't matter and consistency is needed
   */
  private static final char[] CI_ALPHABET_32 = "23456789abcdefghijklmnpqrstvwxyz".toCharArray();

  private static final SecureRandom   secureRandom         = new SecureRandom();
  private static final Base64.Encoder BASE64ENCODER_NOPADD = Base64.getUrlEncoder().withoutPadding();

  /**
   * Returns a random {@code int} value between the specified origin (inclusive) and bound (exclusive).
   *
   * @param start the least value returned
   * @param bound the upper bound (excluded)
   * @return a random {@code int} value between the origin (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException if bound is not greater than start
   */
  public static int newInt(int start, int bound) {
    if (bound <= start) {
      throw new IllegalArgumentException("bound must be greater than start");
    }
    return RandomUtils.secureStrong().randomInt(start, bound);
  }

  /**
   * Returns a random {@code long} value between the specified origin (inclusive) and bound (exclusive).
   *
   * @param start the least value returned
   * @param bound the upper bound (excluded)
   * @return a random {@code long} value between the origin (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException if bound is not greater than start
   */
  public static long newLong(long start, long bound) {
    if (bound <= start) {
      throw new IllegalArgumentException("bound must be greater than start");
    }
    return RandomUtils.secureStrong().randomLong(start, bound);
  }

  /**
   * Generates a base64 URL encoded string of length 48.
   */
  public static String generateNewToken() {
    return generateNewToken(DEF_RANDOM_BYTE_LEN);
  }

  /**
   * Generates a base64 URL encoded string of random bytes. The length of the string is (byteLength * 4) / 3 e.g.
   * byteLength is 36 returns string of 48 chars byteLength is 12 returns string of 16 chars byteLength is 18 returns
   * string of 24 chars
   *
   * @param byteLength length of byte array
   * @return base64 URL encoded random string
   * @throws IllegalArgumentException if byteLength is not positive
   */
  public static String generateNewToken(int byteLength) {
    if (byteLength <= 0) {
      throw new IllegalArgumentException("byteLength must be positive");
    }
    return BASE64ENCODER_NOPADD.encodeToString(generateByteArray(byteLength));
  }

  /**
   * Generates a byte array of the specified length.
   *
   * @param byteLength length of byte array
   * @return byte array of the specified length
   * @throws IllegalArgumentException if byteLength is not positive
   */
  public static byte[] generateByteArray(int byteLength) {
    if (byteLength <= 0) {
      throw new IllegalArgumentException("byteLength must be positive");
    }
    byte[] randomBytes = new byte[byteLength];
    secureRandom.nextBytes(randomBytes);
    return randomBytes;
  }

  /**
   * Generates a nano ID using the default 62-character alphabet.
   *
   * @param size the length of the nano ID to be generated
   * @return a nano ID of the specified length
   */
  public static String nanoId62(int size) {
    return nanoId(DEFAULT_ALPHABET_62, size);
  }

  /**
   * Generates a nano ID using the specified alphabet.
   *
   * @param alphabet the alphabet to use for the nano ID
   * @param size     the length of the nano ID to be generated
   * @return a nano ID of the specified length
   * @throws IllegalArgumentException if alphabet is null or empty, or if size is not positive
   */
  public static String nanoId(final char[] alphabet, final int size) {
    if (alphabet == null || alphabet.length == 0) {
      throw new IllegalArgumentException("alphabet must not be null or empty");
    }
    if (size <= 0) {
      throw new IllegalArgumentException("size must be positive");
    }

    int    mask = alphabet.length;
    char[] res  = new char[size];
    byte[] bytes = new byte[size + 4];
    secureRandom.nextBytes(bytes);

    for (int i = 0; i < size; i++) {
      res[i] = alphabet[Math.floorMod(bytes[i], mask)];
    }

    return new String(res);
  }

  /**
   * Generates a word-safe nano ID.
   *
   * @param size the length of the nano ID to be generated
   * @return a word-safe nano ID of the specified length
   */
  public static String nanoIdWordSafe(int size) {
    return nanoId(WORDSAFE_ALPHABET_32, size);
  }

  /**
   * Generates a case-insensitive nano ID.
   *
   * @param size the length of the nano ID to be generated
   * @return a case-insensitive nano ID of the specified length
   */
  public static String nanoIdCaseInsensitive(int size) {
    return nanoId(CI_ALPHABET_32, size);
  }

  /**
   * Generates a base32 encoded string in lower case with a length of 26 chars.
   * Can be used as a bucket name or folder name without any issue.
   * Collision resistant to a great extent.
   */
  public static String genBucketName() {
    byte[] bytes = new byte[20];
    secureRandom.nextBytes(bytes);
    return BaseEncoding.base32().omitPadding().lowerCase().encode(bytes);
  }

  /**
   * Generates a URL path with good collision resistance.
   * 8*33 = 264 bits length of string is 44 chars.
   * For all practical cases, it is a good random to use in URL path for items such as images, files in S3 storages.
   */
  public static String getUrlPath() {
    return StringUtils.replaceChars(generateNewToken(33), "_-", "vt");
  }

  /**
   * Generates a nano ID using the default 64-character alphabet.
   *
   * @param size the length of the nano ID to be generated
   * @return a nano ID of the specified length
   */
  public static String nanoId(final int size) {
    return nanoId(DEFAULT_ALPHABET_64, size);
  }

  /**
   * Generates a timestamp-based unique ID (TUID).
   * Instant + uniqueValue, 9 chars + 16 chars.
   * Time + 96bits of random stuff should be better than UUID for all practical use cases.
   * The timestamp should be in a sortable manner, but not within the same millisecond.
   *
   * @return 29chars of random data
   */
  public static String tuid() {
    return tuid(12);
  }

  /**
   * Generates a timestamp-based unique ID (TUID) with the specified size.
   *
   * @param size the size of the TUID to be generated
   * @return a TUID of the specified size
   * @throws IllegalArgumentException if size is not positive
   */
  public static String tuid(int size) {
    var time = time();
    var rand = BaseEncoding.base32().omitPadding().encode(generateByteArray(size));
    return time.toUpperCase(Locale.ROOT) + rand;
  }

  /**
   * Returns the current time in a sortable format.
   */
  public static String time() {
    return Long.toString(Instant.now().toEpochMilli(), 32);
  }
}
