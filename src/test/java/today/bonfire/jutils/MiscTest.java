package today.bonfire.jutils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.security.Security;

@Slf4j
public class MiscTest {

  @Test
  public void test1() {
    var providers = Security.getProviders();
    for (var provider : providers) {
      log.trace("Provider: {} - {}", provider.getName(), provider.getVersionStr());
      log.trace("Info: {}", provider.getInfo());
      log.trace("Services:");
      for (var service : provider.getServices()) {
        log.trace("  - {}: {}", service.getType(), service.getAlgorithm());
      }
      log.trace("");
    }
  }


  @Test
  public void test2() {
    for (int i = 0; i < 65; i++) {
      var t = RandUtils.generateNewToken(i);
      log.debug("iteration: {}, bits: {}, length: {}, token: {}", i, i * 8, t.length(), t);
    }
  }


}
