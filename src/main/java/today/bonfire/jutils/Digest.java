package today.bonfire.jutils;

import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;
import today.bonfire.jutils.constants.DigestHash;

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
   * @return sha 3 digest value, null in case of null, empty or whitespace
   */
  public static String Base64SHA3_256(String value) {
    if (value == null) return null;
    return Encoder.toBase64(
      getDigest(DigestHash.SHA_3_256).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static String Base32SHA3_256(String value) {
    if (value == null) return null;
    return Encoder.toBase32(
      getDigest(DigestHash.SHA_3_256).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  static MessageDigest getDigest(DigestHash hashAlgo) {
    try {
      return MessageDigest.getInstance(hashAlgo.toString());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * return a base64 url encoded sha 3 string from the input. StandardCharsets.UTF_8 is used
   *
   * @param value the input string
   * @return sha 3 digest value, null in case of null, empty or whitespace
   */
  public static String Base64SHA3_384(String value) {
    if (value == null) return null;
    return Encoder.toBase64(
      getDigest(DigestHash.SHA_3_384).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * return a base64 url encoded sha 3 string from the input. StandardCharsets.UTF_8 is used
   *
   * @param value the input string
   * @return sha 3 digest value, null in case of null, empty or whitespace
   */
  public static String Base64SHA3_512(String value) {
    if (value == null) return null;
    return Encoder.toBase64(
      getDigest(DigestHash.SHA_3_512).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static String Base64SHA_256(String value) {
    if (value == null) return null;
    return Encoder.toBase64(
      getDigest(DigestHash.SHA_256).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static String Base64SHA_384(String value) {
    if (value == null) return null;
    return Encoder.toBase64(
      getDigest(DigestHash.SHA_384).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static String Base64SHA_512(String value) {
    if (value == null) return null;
    return Encoder.toBase64(
      getDigest(DigestHash.SHA_512).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static String sha256(String value) {
    if (value == null) return null;
    return Encoder.toBase16(
      getDigest(DigestHash.SHA_256).digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  public static String HmacSHA256Hex(String data, String key) {
    return BaseEncoding.base16().encode(hmacSHA(data, key, DigestHash.HMAC_SHA_256));
  }

  private static byte[] hmacSHA(String data, String key, DigestHash shaOption) {
    try {
      Mac           hmac       = Mac.getInstance(shaOption.toString());
      SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), shaOption.toString());
      hmac.init(secret_key);
      return hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      log.error("Error in {} ", shaOption, e);
      throw new RuntimeException(e);
    }
  }

  public static String HmacSHA256(String data, String key) {
    return Encoder.toBase64(hmacSHA(data, key, DigestHash.HMAC_SHA_256));
  }

  public static String HmacSHA384(String data, String key) {
    return Encoder.toBase64(hmacSHA(data, key, DigestHash.HMAC_SHA_384));
  }

  public static String HmacSHA512(String data, String key) {
    return Encoder.toBase64(hmacSHA(data, key, DigestHash.HMAC_SHA_512));
  }

}
