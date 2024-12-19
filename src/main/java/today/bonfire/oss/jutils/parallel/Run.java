package today.bonfire.oss.jutils.parallel;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Run {
  private static final ExecutorService executorService = Executors.getNewExecutor("jutils-parallel-");
  private static final Duration        DEFAULT_TIMEOUT = Duration.ofMinutes(1);

  public static List<Future<?>> parallel(List<Callable<?>> callables) {
    return parallel(callables, DEFAULT_TIMEOUT);
  }

  public static List<Future<?>> parallel(List<Callable<?>> callables, Duration timeout) {
    if (callables == null || callables.isEmpty()) {
      return Collections.emptyList();
    }

    var futures = new ArrayList<Future<?>>();
    for (Callable<?> callable : callables) {
      Future<?> future = executorService.submit(wrap(callable));
      futures.add(future);
    }
    return futures;
  }

  private static <T> Callable<T> wrap(Callable<T> callable) {
    return () -> {
      try {
        return callable.call();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new ParallelException("Task interrupted", e, Context.empty());
      } catch (Exception e) {
        throw new ParallelException("Task execution failed", e, Context.empty());
      }
    };
  }

  public static void shutdown() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      log.warn("Failed to shutdown executor service", e);
      Thread.currentThread().interrupt();
    }
  }
}
