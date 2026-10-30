package today.bonfire.oss.jutils.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
@Timeout(30)
class ContextAwareThreadFactoryTest {

    @Mock
    private ThreadFactory delegateFactory;

    @Test
    void testThreadCreationWithContextAndMDC() throws InterruptedException {
        when(delegateFactory.newThread(any(Runnable.class))).thenAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            return new Thread(r);
        });

        ContextAwareThreadFactory factory = new ContextAwareThreadFactory(delegateFactory);
        Context<String> testContext = Context.of("testData");
        Map<String, String> mdcData = new HashMap<>();
        mdcData.put("key", "value");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Context<?>> threadContext = new AtomicReference<>();
        AtomicReference<Map<String, String>> threadMDC = new AtomicReference<>();

        MDC.setContextMap(mdcData);
        try (var scope = Contexts.open(testContext)) {
            Thread thread = factory.newThread(() -> {
                threadContext.set(Contexts.current());
                threadMDC.set(MDC.getCopyOfContextMap());
                latch.countDown();
            });
            thread.start();
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

            verify(delegateFactory).newThread(any(Runnable.class));
            assertThat(threadContext.get()).isEqualTo(testContext);
            assertThat(threadMDC.get()).containsAllEntriesOf(mdcData);
        } finally {
            MDC.clear();
        }
    }

    @Test
    void testThreadCreationWithoutContext() throws InterruptedException {
        when(delegateFactory.newThread(any(Runnable.class))).thenAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            return new Thread(r);
        });

        ContextAwareThreadFactory factory = new ContextAwareThreadFactory(delegateFactory);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Context<?>> threadContext = new AtomicReference<>();
        AtomicReference<Map<String, String>> threadMDC = new AtomicReference<>();

        Thread thread = factory.newThread(() -> {
            threadContext.set(Contexts.current());
            threadMDC.set(MDC.getCopyOfContextMap());
            latch.countDown();
        });
        thread.start();
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

        verify(delegateFactory).newThread(any(Runnable.class));
        assertThat(threadContext.get()).isNull();
        assertThat(threadMDC.get()).isNull();
    }

    @Test
    void testNullDelegateRejected() {
        assertThatThrownBy(() -> new ContextAwareThreadFactory(null)).isInstanceOf(NullPointerException.class);
    }
}
