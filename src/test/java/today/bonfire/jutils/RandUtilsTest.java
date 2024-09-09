package today.bonfire.jutils;


import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Log4j2
public class RandUtilsTest {

  @BeforeAll
  public static void setUp() {
    MDC.put("uid", RandUtils.tuid());
  }

  @Test
  public void testTuidNotNull() {
    String tuid = RandUtils.tuid(0);
    log.debug("tuid: {}", tuid);
    assertNotNull(tuid);
  }

  @Test
  public void testTuidLength() {
    String tuid = RandUtils.tuid(12);
    log.debug("tuid: {}", tuid);
    assertEquals(29, tuid.length());
  }

  @Test
  public void testTuidUnique() {
    var set = new HashSet<String>();
    for (int i = 0; i < 100000; i++) {
      var t = RandUtils.tuid();
      assertFalse(set.contains(t));
      set.add(t);
    }
    assertEquals(100000, set.size());
  }
}
