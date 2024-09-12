package today.bonfire.jutils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class BitSetHelpersTest {

  @Test
  void encodeBitSetToBase64_EncodesBitSetCorrectly() {
    // given
    var bitSet = BitSetHelpers.newBitSet();
    bitSet.set(126);
    bitSet.set(62);
    bitSet.set(15);
    bitSet.set(7);
    bitSet.set(3);
    bitSet.set(1);
    var result = BitSetHelpers.encodeBitSetToBase64(bitSet);
    assertEquals("ioAAAAAAAEAAAAAAAAAAQA", result);
  }

  @Test
  public void testEncodeAndParseBitSet() {
    BitSet originalBitSet = new BitSet();
    originalBitSet.set(0, true);
    originalBitSet.set(1, false);
    originalBitSet.set(62, true);
    String base64String = BitSetHelpers.encodeBitSetToBase64(originalBitSet);
    BitSet parsedBitSet = BitSetHelpers.parseBitSetEncodedString(base64String);
    assertEquals(originalBitSet, parsedBitSet);
  }
}
