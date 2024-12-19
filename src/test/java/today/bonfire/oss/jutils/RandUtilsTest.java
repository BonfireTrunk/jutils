package today.bonfire.oss.jutils;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


@Slf4j
public class RandUtilsTest {

  @BeforeAll
  public static void setUp() {
    MDC.put("uid", RandUtils.tuid());
  }

  @Test
  public void testTuidNotNull() {
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> RandUtils.tuid(0));
  }

  @Test
  public void testTuidLength() {
    String tuid = RandUtils.tuid(12);
    assertThat(tuid.length()).isEqualTo(29);
  }

  @Test
  public void testTuidUnique() {
    var set = new HashSet<String>();
    for (int i = 0; i < 100000; i++) {
      var t = RandUtils.tuid();
      assertThat(set.contains(t)).isFalse();
      set.add(t);
    }
    assertThat(set.size()).isEqualTo(100000);
  }
}
