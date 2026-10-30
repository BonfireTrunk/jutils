package today.bonfire.oss.jutils.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(30)
class ContextExecutorsTest {

    @Test
    void testNewVirtualThreadExecutorWithPrefix() throws Exception {
        ExecutorService executor = ContextExecutors.newVirtualThreadExecutor("test-prefix-");
        try {
            var name = executor.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS);
            assertThat(name).startsWith("test-prefix-");
            assertThat(executor.submit(() -> Thread.currentThread().isVirtual()).get(5, TimeUnit.SECONDS))
                    .isTrue();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testNewVirtualThreadExecutorWithNullPrefixUsesDefault() throws Exception {
        ExecutorService executor = ContextExecutors.newVirtualThreadExecutor(null);
        try {
            var name = executor.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS);
            assertThat(name).startsWith("jutils-vt-");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testNewVirtualThreadExecutorWithBlankPrefixUsesDefault() throws Exception {
        ExecutorService executor = ContextExecutors.newVirtualThreadExecutor("  ");
        try {
            var name = executor.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS);
            assertThat(name).startsWith("jutils-vt-");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testNewThreadPerTaskExecutorUsesGivenFactoryAndDoesNotMutateBuilder() throws Exception {
        Thread.Builder builder = Thread.ofPlatform();
        ThreadFactory factory = builder.name("custom-", 0).factory();
        ExecutorService executor = ContextExecutors.newThreadPerTaskExecutor(factory);
        try {
            var name = executor.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS);
            assertThat(name).startsWith("custom-");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testNewVirtualThreadExecutorDoesNotAffectLaterExecutors() throws Exception {
        ExecutorService first = ContextExecutors.newVirtualThreadExecutor("first-");
        ExecutorService second = ContextExecutors.newVirtualThreadExecutor("second-");
        try {
            assertThat(first.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS))
                    .startsWith("first-");
            assertThat(second.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS))
                    .startsWith("second-");
        } finally {
            first.shutdown();
            second.shutdown();
        }
    }

    @Test
    void testNewThreadPerTaskExecutorRejectsNullFactory() {
        assertThatThrownBy(() -> ContextExecutors.newThreadPerTaskExecutor(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testExecutorPropagatesContext() throws Exception {
        ExecutorService executor = ContextExecutors.newVirtualThreadExecutor("test-");
        Context<String> testContext = Context.of("test");
        try (var scope = Contexts.open(testContext)) {
            var future = executor.submit(Contexts::current);
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo(testContext);
        } finally {
            executor.shutdown();
        }
    }
}
