package today.bonfire.oss.jutils;

import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.BitSet;

/**
 * Utility class for BitSet operations including Base64 encoding and decoding.
 */
public class BitSetHelpers {

  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  /**
   * Encodes a BitSet to a Base64 string. Returns empty string for null or empty BitSet.
   *
   * @param bitSet the BitSet to encode
   * @return empty string if BitSet is null or empty, Base64 encoded string otherwise
   */
  public static String encode(BitSet bitSet) {
    if (bitSet == null || bitSet.isEmpty()) {
      return "";
    }
    return ENCODER.encodeToString(bitSet.toByteArray());
  }

  /**
   * Creates a new empty BitSet.
   *
   * @return a new empty BitSet instance
   */
  public static BitSet empty() {
    return new BitSet();
  }

  /**
   * Decodes a Base64 string back to a BitSet.
   *
   * @param encoded the Base64 encoded string
   * @return empty BitSet if input is blank, decoded BitSet otherwise
   * @throws IllegalArgumentException if input is not a valid Base64 string
   */
  public static BitSet decode(String encoded) {
    if (StringUtils.isBlank(encoded)) {
      return new BitSet();
    }
    return BitSet.valueOf(DECODER.decode(encoded));
  }

}
