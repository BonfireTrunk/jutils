package today.bonfire.jutils.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public class Executors {

  public static ExecutorService getNewExecutor(String threadPrefix) {
    ThreadFactory virtualFactory = Thread.ofVirtual()
                                         .name(threadPrefix == null ? "vt-" : threadPrefix, 1)
                                         .factory();
    return java.util.concurrent.Executors.newThreadPerTaskExecutor(new ContextAwareThreadFactory(virtualFactory));
  }
}
