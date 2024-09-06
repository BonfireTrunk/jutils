package today.bonfire.jutils;

import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.BitSet;


public class BitSetHelpers {

  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  /**
   * Encodes a BitSet to a Base64 encoded string.
   *
   * @param bitSet the BitSet to encode
   * @return the Base64 encoded string
   */
  public static String encodeBitSetToBase64(BitSet bitSet) {
    return ENCODER.encodeToString(bitSet.toByteArray());
  }

  /**
   * Generates a new empty BitSet.
   *
   * @return a new empty BitSet
   */
  public static BitSet newBitSet() {
    return new BitSet();
  }

  public static BitSet parseBitSetEncodedString(String encodedString) {
    if (StringUtils.isBlank(encodedString)) {
      return new BitSet();
    }
    return BitSet.valueOf(DECODER.decode(encodedString));
  }

}
