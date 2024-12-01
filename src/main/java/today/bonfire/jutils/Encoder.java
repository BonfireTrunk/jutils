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

  public static String urlEncode(String value) {
    return StringUtils.isBlank(value) ? value : URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public static String urlDecode(String value) {
    return StringUtils.isBlank(value) ? value : URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  public static String HtmlEscape(String value) {
    if (value == null) return null;
    var cleaner = new Cleaner(Safelist.none());
    return cleaner.clean(Jsoup.parse(value)).text();
  }

  public static String toBase64String(String s) {
    return toBase64(toByteArray(s));
  }

  public static String toBase64(byte[] bytes) {
    return new String(base64UrlEncoder.encode(bytes), StandardCharsets.UTF_8);
  }

  public static String toBase64Basic(String s) {
    return new String(base64Encoder.encode(toByteArray(s)), StandardCharsets.UTF_8);
  }

  public static String toBase32(byte[] bytes) {
    return base32Encoder.encode(bytes);
  }

  public static String toBase32(String s) {
    return toBase32(toByteArray(s));
  }

  public static String toBase16(byte[] bytes) {
    return base16Encoding.encode(bytes);
  }

  public static byte[] toByteArray(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  public static String decodeBase64(String data) {
    if (StringUtils.isBlank(data)) return null;
    return new String(base64Decoder.decode(data.trim()), StandardCharsets.UTF_8);
  }

  public static String decodeBase64Mime(String data) {
    if (StringUtils.isBlank(data)) return null;
    return new String(base64MimeDecoder.decode(data.trim()), StandardCharsets.UTF_8);
  }


  public static String encodeBase64Mime(String data) {
    return encodeBase64Mime(toByteArray(data));
  }

  public static String encodeBase64Mime(byte[] data) {
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
