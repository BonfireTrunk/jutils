package today.bonfire.jutils.parallel;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class RunTest {

  @Test
  void testParallelExecution() throws Exception {
    int               taskCount = 5;
    AtomicInteger     counter   = new AtomicInteger(0);
    List<Callable<?>> tasks     = new ArrayList<>();

    for (int i = 0; i < taskCount; i++) {
      tasks.add(() -> counter.incrementAndGet());
    }

    List<Future<?>> futures = Run.parallel(tasks);

    assertThat(futures).hasSize(taskCount);

    // Wait for all tasks to complete
    for (Future<?> future : futures) {
      future.get();
    }

    assertThat(counter.get()).isEqualTo(taskCount);
  }

  @Test
  void testParallelExecutionWithTimeout() throws Exception {
    List<Callable<?>> tasks = List.of(
      () -> {
        Thread.sleep(100);
        return "task1";
      },
      () -> "task2"
    );

    List<Future<?>> futures = Run.parallel(tasks, Duration.ofSeconds(1));

    assertThat(futures).hasSize(2);

    // Wait for all tasks to complete
    for (Future<?> future : futures) {
      assertThat(future.get()).isNotNull();
    }
  }

  @Test
  void testParallelWithNullList() {
    List<Future<?>> futures = Run.parallel(null);
    assertThat(futures).isEmpty();
  }

  @Test
  void testParallelWithEmptyList() {
    List<Future<?>> futures = Run.parallel(List.of());
    assertThat(futures).isEmpty();
  }

  @Test
  void testTaskInterruption() {
    List<Callable<?>> tasks = List.of(() -> {
      Thread.sleep(10000); // Long running task
      return null;
    });

    List<Future<?>> futures = Run.parallel(tasks);
    futures.getFirst().cancel(true);

    assertThatThrownBy(() -> futures.getFirst().get())
      .isInstanceOf(CancellationException.class);
  }

  @Test
  void testTaskException() {
    List<Callable<?>> tasks = List.of(() -> {
      throw new RuntimeException("Test exception");
    });

    List<Future<?>> futures = Run.parallel(tasks);

    assertThatThrownBy(() -> futures.get(0).get())
      .hasRootCauseMessage("Test exception");
  }

  @Test
  void testContextPropagation() throws Exception {
    Context<String> testContext = Context.of("test");
    ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.set(testContext);

    try {
      List<Callable<?>> tasks = List.of(
        () -> ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.get()
      );

      List<Future<?>> futures       = Run.parallel(tasks);
      Context<?>      resultContext = (Context<?>) futures.get(0).get();

      assertThat(resultContext).isEqualTo(testContext);
    } finally {
      ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.remove();
    }
  }
}
