package today.bonfire.jutils;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@SuppressWarnings("unused")
public class RandUtils {
  private static final char[] DEFAULT_ALPHABET_64  = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final char[] DEFAULT_ALPHABET_62  = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final char[] WORDSAFE_ALPHABET_32 = "23456789CFGHJMPQRVWXcfghjmpqrvwx".toCharArray();
  private static final char[] CI_ALPHABET_32       = "23456789abcdefghijklmnpqrstvwxyz".toCharArray();

  private static final SecureRandom   secureRandom         = new SecureRandom();
  private static final Base64.Encoder BASE64ENCODER_NOPADD = Base64.getUrlEncoder()
                                                                   .withoutPadding();
  private static final int            DEF_RANDOM_BYTE_LEN  = 36; // ~ 36 * 8 = 288 bits of randomness


  public static int newInt(int start, int bound) {
    return secureRandom.nextInt(start, bound);
  }

  public static long newLong(long start, long bound) {
    return secureRandom.nextLong(start, bound);
  }

  /**
   * generates a base64 url encoded string of length 48.
   */
  public static String generateNewToken() {
    return generateNewToken(DEF_RANDOM_BYTE_LEN);
  }

  /**
   * generates a base64 url encoded string of random bytes. The length of the string is (byteLength * 4) / 3 e.g.
   * byteLength is 36 returns string of 48 chars byteLength is 12 returns string of 16 chars byteLength is 18 returns
   * string of 24 chars
   *
   * @param byteLength length of byte array
   * @return base64 url encoded random string
   */
  public static String generateNewToken(int byteLength) {
    return BASE64ENCODER_NOPADD.encodeToString(generateByteArray(byteLength));
  }

  public static byte[] generateByteArray(int byteLength) {
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
   * The string is generated using the given random number generator.
   * <p>
   * approx on local system Benchmark        Mode    Cnt     Score         Units randomNanoId thrpt 2      226465.531
   * ops/s generateNewToken thrpt    2      303675.167    ops/s custom implementation
   *
   * @param alphabet see {@link RandUtils} fields
   * @param size     Make sure it is greater than 10
   */
  private static String nanoId(final char[] alphabet, final int size) {

    int    mask  = alphabet.length;
    char[] res   = new char[size];
    byte[] bytes = new byte[size + 4];
    secureRandom.nextBytes(bytes); // random bytes

    for (int i = 0; i < size; i++) {
      res[i] = alphabet[Math.floorMod(bytes[i], mask)];
    }

    return new String(res);
  }

  public static String nanoIdWordSafe(int size) {
    return nanoId(WORDSAFE_ALPHABET_32, size);
  }

  public static String nanoIdCaseInsensitive(int size) {
    return nanoId(CI_ALPHABET_32, size);
  }

  /**
   * base32 encoded string in lower case with a length of 26 chars can be used as bucket name or folder name without
   * any issue collision resistant to a great extant
   */
  public static String genBucketName() {
    byte[] bytes = new byte[20];
    secureRandom.nextBytes(bytes);
    return BaseEncoding.base32().omitPadding().lowerCase().encode(bytes);
  }

  /**
   * good collision resistance, 8*33 = 264 bits length of string is 44 chars and for all practical cases it is a good
   * random to use in url path for items such as images, file in s3 storages
   */
  public static String getUrlPath() {
    return StringUtils.replaceChars(generateNewToken(33), "_-", "vt");
  }

  public static String nanoId(final int size) {
    return nanoId(DEFAULT_ALPHABET_64, size);
  }

  /**
   * Timestamp UID Instant + uniqueValue, 9 chars + 16 chars, time + 96bits of random stuff should be better than uuid
   * for all practical use cases the timestamp should be in a sortable manner, but not withing the same millisecond
   *
   * @return 29chars of random data
   */
  public static String tuid() {
    return tuid(12);
  }

  public static String tuid(int size) {
    var time = time();
    var rand = BaseEncoding.base32().omitPadding().encode(generateByteArray(size));
    return time.toUpperCase(Locale.ROOT) + rand;
  }

  public static String time() {
    return Long.toString(Instant.now().toEpochMilli(), 32);
  }

}
