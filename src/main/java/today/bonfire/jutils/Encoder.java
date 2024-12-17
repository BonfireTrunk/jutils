package today.bonfire.jutils;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class Encoder {

  private static final BaseEncoding   base16Encoding   = BaseEncoding.base16()
                                                                     .lowerCase();
  private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder base64Decoder     = Base64.getUrlDecoder();
  private static final Base64.Encoder base64Encoder    = Base64.getEncoder();
  private static final Base64.Encoder base64MimeEncoder = Base64.getMimeEncoder();
  private static final Base64.Decoder base64MimeDecoder = Base64.getMimeDecoder();
  private static final BaseEncoding   base32Encoder     = BaseEncoding.base32()
                                                                      .omitPadding()
                                                                      .lowerCase();

  /**
   * Encodes a URL string using UTF-8 encoding.
   *
   * @param value the string to URL encode, may be null or blank
   * @return the URL encoded string, or the original string if input is null or blank
   */
  public static String urlEncode(String value) {
    return StringUtils.isBlank(value) ? value : URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  /**
   * Decodes a URL encoded string using UTF-8 encoding.
   *
   * @param value the string to URL decode, may be null or blank
   * @return the URL decoded string, or the original string if input is null or blank
   */
  public static String urlDecode(String value) {
    return StringUtils.isBlank(value) ? value : URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  /**
   * Escapes HTML content by removing all HTML tags and entities.
   * Uses JSoup's {@link Cleaner} with {@link Safelist#none()} for strict cleaning.
   *
   * @param value the string containing HTML to escape, may be null
   * @return the cleaned string with all HTML removed, or null if input is null
   */
  public static String htmlSafe(String value) {
    if (value == null) return null;
    var cleaner = new Cleaner(Safelist.none());
    return cleaner.clean(Jsoup.parse(value)).text();
  }

  /**
   * Encodes a string to Base64 URL-safe format without padding. Uses '-' and '_' instead of '+' and '/'
   * characters, making it safe for use in URLs and filenames.
   *
   * @param s the string to encode, may be null or blank
   * @return the Base64 URL-safe encoded string, or null if input is null
   */
  public static String toBase64String(String s) {
    if (s == null) return null;
    return toBase64(toByteArray(s));
  }

  /**
   * Encodes a byte array to Base64 URL-safe format without padding. Uses '-' and '_' instead of '+' and '/'
   * characters, making it safe for use in URLs and filenames.
   *
   * @param bytes the byte array to encode, may be null
   * @return the Base64 URL-safe encoded string, or null if input is null
   */
  public static String toBase64(byte[] bytes) {
    if (bytes == null) return null;
    return new String(base64UrlEncoder.encode(bytes), StandardCharsets.UTF_8);
  }

  /**
   * Encodes a string to standard Base64 format.
   *
   * @param s the string to encode, may be null or blank
   * @return the Base64 encoded string, or null if input is null
   */
  public static String toBase64Basic(String s) {
    if (s == null) return null;
    return new String(base64Encoder.encode(toByteArray(s)), StandardCharsets.UTF_8);
  }

  /**
   * Encodes a byte array to Base32 format without padding.
   *
   * @param bytes the byte array to encode, may be null
   * @return the Base32 encoded string, or null if input is null
   */
  public static String toBase32(byte[] bytes) {
    if (bytes == null) return null;
    return base32Encoder.encode(bytes);
  }

  /**
   * Encodes a string to Base32 format without padding.
   *
   * @param s the string to encode, may be null
   * @return the Base32 encoded string, or null if input is null
   */
  public static String toBase32(String s) {
    if (s == null) return null;
    return toBase32(toByteArray(s));
  }

  /**
   * Encodes a byte array to Base16 (hexadecimal) format.
   *
   * @param bytes the byte array to encode, may be null
   * @return the Base16 encoded string, or null if input is null
   */
  public static String toBase16(byte[] bytes) {
    if (bytes == null) return null;
    return base16Encoding.encode(bytes);
  }

  /**
   * Converts a string to a byte array using UTF-8 encoding.
   *
   * @param value the string to convert, must not be null
   * @return the byte array representation of the string
   * @throws NullPointerException if value is null
   */
  public static byte[] toByteArray(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Decodes a Base64 URL-safe string.
   *
   * @param data the Base64 URL-safe string to decode, may be null or blank
   * @return the decoded string, or null if input is null or blank
   */
  public static String decodeBase64(String data) {
    if (StringUtils.isBlank(data)) return null;
    return new String(base64Decoder.decode(data.trim()), StandardCharsets.UTF_8);
  }

  /**
   * Decodes a Base64 MIME formatted string. MIME format uses standard Base64 alphabet (with '+' and '/' characters)
   * and includes line breaks after every 76 characters for better compatibility with email systems.
   *
   * @param data the Base64 MIME string to decode, may be null or blank
   * @return the decoded string, or null if input is null or blank
   */
  public static String decodeBase64Mime(String data) {
    if (StringUtils.isBlank(data)) return null;
    return new String(base64MimeDecoder.decode(data.trim()), StandardCharsets.UTF_8);
  }

  /**
   * Encodes a string to Base64 MIME format. Uses standard Base64 alphabet (with '+' and '/' characters)
   * and adds line breaks after every 76 characters, making it suitable for email attachments and
   * systems that have line length limitations.
   *
   * @param data the string to encode, may be null
   * @return the Base64 MIME encoded string with line breaks, or null if input is null
   */
  public static String encodeBase64Mime(String data) {
    if (data == null) return null;
    return encodeBase64Mime(toByteArray(data));
  }

  /**
   * Encodes a byte array to Base64 MIME format. Uses standard Base64 alphabet (with '+' and '/' characters)
   * and adds line breaks after every 76 characters, making it suitable for email attachments and
   * systems that have line length limitations.
   *
   * @param data the byte array to encode, may be null
   * @return the Base64 MIME encoded string with line breaks, or null if input is null
   */
  public static String encodeBase64Mime(byte[] data) {
    if (data == null) return null;
    return new String(base64MimeEncoder.encode(data), StandardCharsets.UTF_8);
  }

  /**
   * Returns a string representation of the input byte array.
   *
   * @param bytes the input byte array
   * @return a string representation of the input byte array, or null if the input array is empty
   */
  public static String toStr(byte[] bytes) {
    if (ObjectUtils.isEmpty(bytes)) return null;
    return new String(bytes, StandardCharsets.UTF_8);
  }

}
