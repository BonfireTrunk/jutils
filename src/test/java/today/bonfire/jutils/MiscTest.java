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
      System.out.println("Provider: " + provider.getName() + " - " + provider.getVersionStr());
      System.out.println("Info: " + provider.getInfo());
      System.out.println("Services:");
      for (var service : provider.getServices()) {
        System.out.println("  - " + service.getType() + ": " + service.getAlgorithm());
      }
      System.out.println();
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
