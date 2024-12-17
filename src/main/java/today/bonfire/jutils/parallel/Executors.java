package today.bonfire.jutils.parallel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Executors {
  private static final AtomicInteger poolCounter = new AtomicInteger(1);

  public static ExecutorService getNewExecutor(String threadPrefix) {
    return getNewExecutor(threadPrefix, Thread.ofVirtual());
  }

  public static ExecutorService getNewExecutor(String threadPrefix, Thread.Builder threadBuilder) {
    if (threadPrefix == null || threadPrefix.isBlank()) {
      threadPrefix = "jutils-vt-" + poolCounter.getAndIncrement() + "-";
    }

    ThreadFactory virtualFactory = threadBuilder
      .name(threadPrefix, 0)
      .factory();

    log.debug("Creating new executor with thread prefix: {}", threadPrefix);
    return java.util.concurrent.Executors.newThreadPerTaskExecutor(
      new ContextAwareThreadFactory(virtualFactory)
    );
  }
}
