package today.bonfire.jutils.parallel;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.*;

class ExecutorsTest {

  @Test
  void testGetNewExecutorWithPrefix() {
    String          prefix   = "test-prefix-";
    ExecutorService executor = Executors.getNewExecutor(prefix);

    assertThat(executor).isNotNull();

    // Submit a task to verify the executor works
    var future = executor.submit(() -> "test");
    assertThat(future).isNotNull();

    executor.shutdown();
  }

  @Test
  void testGetNewExecutorWithNullPrefix() {
    ExecutorService executor = Executors.getNewExecutor(null);

    assertThat(executor).isNotNull();

    // Submit a task to verify the executor works
    var future = executor.submit(() -> "test");
    assertThat(future).isNotNull();

    executor.shutdown();
  }

  @Test
  void testGetNewExecutorWithCustomThreadBuilder() {
    String          prefix        = "custom-prefix-";
    Thread.Builder  threadBuilder = Thread.ofVirtual().name(prefix);
    ExecutorService executor      = Executors.getNewExecutor(prefix, threadBuilder);

    assertThat(executor).isNotNull();

    // Submit a task to verify the executor works
    var future = executor.submit(() -> "test");
    assertThat(future).isNotNull();

    executor.shutdown();
  }

  @Test
  void testExecutorUsesContextAwareThreadFactory() throws InterruptedException, ExecutionException {
    ExecutorService executor    = Executors.getNewExecutor("test-");
    Context<String> testContext = Context.of("test");
    ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.set(testContext);

    try {
      var future = executor.submit(() -> {
        return ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.get();
      });

      Context<?> threadContext = (Context<?>) future.get();
      assertThat(threadContext).isEqualTo(testContext);
    } finally {
      ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.remove();
      executor.shutdown();
    }
  }
}
