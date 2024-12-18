package today.bonfire.oss.jutils;

import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;
import today.bonfire.oss.jutils.constants.DigestHash;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class Digest {

  /**
   * return a base64 url encoded sha 3 string from the input. StandardCharsets.UTF_8 is used
   *
   * @param value the input string
   * @return sha 3 digest value, null if input is null
   */
  public static String base64Sha3_256(String value) {
    return digestToBase64(value, DigestHash.SHA_3_256);
  }

  public static byte[] base64Sha3_256AsBytes(String value) {
    return digestToBytes(value, DigestHash.SHA_3_256);
  }

  public static String base32Sha3_256(String value) {
    if (value == null) return null;
    return Encoder.toBase32(digestToBytes(value, DigestHash.SHA_3_256));
  }

  /**
   * return a base64 url encoded sha 3 string from the input. StandardCharsets.UTF_8 is used
   *
   * @param value the input string
   * @return sha 3 digest value, null if input is null
   */
  public static String base64Sha3_384(String value) {
    return digestToBase64(value, DigestHash.SHA_3_384);
  }

  /**
   * return a base64 url encoded sha 3 string from the input. StandardCharsets.UTF_8 is used
   *
   * @param value the input string
   * @return sha 3 digest value, null if input is null
   */
  public static String base64Sha3_512(String value) {
    return digestToBase64(value, DigestHash.SHA_3_512);
  }

  public static String base64Sha256(String value) {
    return digestToBase64(value, DigestHash.SHA_256);
  }

  public static String base64Sha384(String value) {
    return digestToBase64(value, DigestHash.SHA_384);
  }

  public static String base64Sha512(String value) {
    return digestToBase64(value, DigestHash.SHA_512);
  }

  public static String sha256(String value) {
    if (value == null) return null;
    return Encoder.toBase16(digestToBytes(value, DigestHash.SHA_256));
  }

  private static String digestToBase64(String value, DigestHash algorithm) {
    if (value == null) return null;
    return Encoder.toBase64(digestToBytes(value, algorithm));
  }

  private static byte[] digestToBytes(String value, DigestHash algorithm) {
    if (value == null) return null;
    return getDigest(algorithm).digest(value.getBytes(StandardCharsets.UTF_8));
  }

  static MessageDigest getDigest(DigestHash hashAlgo) {
    try {
      return MessageDigest.getInstance(hashAlgo.toString());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static byte[] hmacSHA(String data, String key, DigestHash algorithm) {
    if (data == null || key == null) return null;
    return hmacSHA(data.getBytes(StandardCharsets.UTF_8),
                   key.getBytes(StandardCharsets.UTF_8),
                   algorithm);
  }

  private static byte[] hmacSHA(byte[] data, byte[] key, DigestHash algorithm) {
    if (data == null || key == null) return null;
    try {
      Mac           mac           = Mac.getInstance(algorithm.toString());
      SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm.toString());
      mac.init(secretKeySpec);
      return mac.doFinal(data);
    } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
      log.error("HMAC computation failed", e);
      throw new RuntimeException("HMAC computation failed", e);
    }
  }

  /**
   * Compute HMAC-SHA-256 and return the result as a hexadecimal string
   *
   * @param data the input data
   * @param key  the key for HMAC
   * @return hexadecimal encoded HMAC value, null if either input is null
   */
  public static String hmacSha256Hex(String data, String key) {
    byte[] hmac = hmacSHA(data, key, DigestHash.HMAC_SHA_256);
    return hmac != null ? BaseEncoding.base16().encode(hmac) : null;
  }

  /**
   * Compute HMAC-SHA-256 and return the result as a hexadecimal string
   *
   * @param data the input data as bytes
   * @param key  the key for HMAC as bytes
   * @return hexadecimal encoded HMAC value, null if either input is null
   */
  public static String hmacSha256Hex(byte[] data, byte[] key) {
    byte[] hmac = hmacSHA(data, key, DigestHash.HMAC_SHA_256);
    return hmac != null ? BaseEncoding.base16().encode(hmac) : null;
  }

  /**
   * Compute HMAC-SHA-256 and return the raw bytes
   *
   * @param data the input data
   * @param key  the key for HMAC
   * @return raw HMAC value as bytes, null if either input is null
   */
  public static byte[] hmacSha256(String data, String key) {
    return hmacSHA(data, key, DigestHash.HMAC_SHA_256);
  }

  /**
   * Compute HMAC-SHA-256 and return the raw bytes
   *
   * @param data the input data as bytes
   * @param key  the key for HMAC as bytes
   * @return raw HMAC value as bytes, null if either input is null
   */
  public static byte[] hmacSha256(byte[] data, byte[] key) {
    return hmacSHA(data, key, DigestHash.HMAC_SHA_256);
  }

  public static String hmacSha384(String data, String key) {
    byte[] hmac = hmacSHA(data, key, DigestHash.HMAC_SHA_384);
    return hmac != null ? Encoder.toBase64(hmac) : null;
  }

  public static String hmacSha512(String data, String key) {
    byte[] hmac = hmacSHA(data, key, DigestHash.HMAC_SHA_512);
    return hmac != null ? Encoder.toBase64(hmac) : null;
  }
}
