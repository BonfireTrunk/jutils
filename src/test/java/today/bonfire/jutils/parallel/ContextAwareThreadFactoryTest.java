package today.bonfire.jutils.parallel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextAwareThreadFactoryTest {

  @Mock
  private ThreadFactory delegateFactory;

  @Test
  void testThreadCreationWithContextAndMDC() throws InterruptedException {
    // Setup
    when(delegateFactory.newThread(any(Runnable.class))).thenAnswer(inv -> {
      Runnable r = inv.getArgument(0);
      return new Thread(r);
    });

    ContextAwareThreadFactory factory     = new ContextAwareThreadFactory(delegateFactory);
    Context<String>           testContext = Context.of("testData");
    Map<String, String>       mdcData     = new HashMap<>();
    mdcData.put("key", "value");

    CountDownLatch                       latch         = new CountDownLatch(1);
    AtomicReference<Context<?>>          threadContext = new AtomicReference<>();
    AtomicReference<Map<String, String>> threadMDC     = new AtomicReference<>();

    // Set parent thread context
    ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.set(testContext);
    MDC.setContextMap(mdcData);

    try {
      // Create and run thread
      Thread thread = factory.newThread(() -> {
        threadContext.set(ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.get());
        threadMDC.set(MDC.getCopyOfContextMap());
        latch.countDown();
      });
      thread.start();
      latch.await();

      // Verify
      verify(delegateFactory).newThread(any(Runnable.class));
      assertThat(threadContext.get()).isEqualTo(testContext);
      assertThat(threadMDC.get()).containsAllEntriesOf(mdcData);
    } finally {
      ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.remove();
      MDC.clear();
    }
  }

  @Test
  void testThreadCreationWithoutContext() throws InterruptedException {
    // Setup
    when(delegateFactory.newThread(any(Runnable.class))).thenAnswer(inv -> {
      Runnable r = inv.getArgument(0);
      return new Thread(r);
    });

    ContextAwareThreadFactory            factory       = new ContextAwareThreadFactory(delegateFactory);
    CountDownLatch                       latch         = new CountDownLatch(1);
    AtomicReference<Context<?>>          threadContext = new AtomicReference<>();
    AtomicReference<Map<String, String>> threadMDC     = new AtomicReference<>();

    // Create and run thread
    Thread thread = factory.newThread(() -> {
      threadContext.set(ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.get());
      threadMDC.set(MDC.getCopyOfContextMap());
      latch.countDown();
    });
    thread.start();
    latch.await();

    // Verify
    verify(delegateFactory).newThread(any(Runnable.class));
    assertThat(threadContext.get()).isNull();
    assertThat(threadMDC.get()).isNull();
  }
}
