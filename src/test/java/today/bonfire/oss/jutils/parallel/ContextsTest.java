package today.bonfire.oss.jutils.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.MDC;

@Timeout(30)
class ContextsTest {

    @AfterEach
    void cleanUp() {
        MDC.clear();
        try (var ignored = Contexts.open(null)) {
            // clears whatever a failing test left behind
        }
    }

    @Test
    void testCurrentIsNullByDefault() {
        assertThat(Contexts.current()).isNull();
    }

    @Test
    void testOpenInstallsAndCloseRestores() {
        Context<String> outer = Context.of("outer");
        Context<String> inner = Context.of("inner");
        try (var s1 = Contexts.open(outer)) {
            assertThat(Contexts.current()).isSameAs(outer);
            try (var s2 = Contexts.open(inner)) {
                assertThat(Contexts.current()).isSameAs(inner);
            }
            assertThat(Contexts.current()).isSameAs(outer);
        }
        assertThat(Contexts.current()).isNull();
    }

    @Test
    void testOpenNullRemovesContextForScope() {
        Context<String> outer = Context.of("outer");
        try (var s1 = Contexts.open(outer)) {
            try (var s2 = Contexts.open(null)) {
                assertThat(Contexts.current()).isNull();
            }
            assertThat(Contexts.current()).isSameAs(outer);
        }
    }

    @Test
    void testSecondCloseDoesNothing() {
        Context<String> outer = Context.of("outer");
        Context<String> inner = Context.of("inner");
        try (var s1 = Contexts.open(outer)) {
            var s2 = Contexts.open(inner);
            s2.close();
            assertThat(Contexts.current()).isSameAs(outer);
            var s3 = Contexts.open(inner);
            s2.close();
            assertThat(Contexts.current()).isSameAs(inner);
            s3.close();
            assertThat(Contexts.current()).isSameAs(outer);
        }
    }

    @Test
    void testWrapCallableCarriesContextAndMdcToOtherThread() throws Exception {
        Context<String> ctx = Context.of("data");
        Callable<Object[]> wrapped;
        MDC.put("k", "v");
        try (var scope = Contexts.open(ctx)) {
            wrapped = Contexts.wrap(() -> new Object[] {Contexts.current(), MDC.get("k")});
        }
        MDC.clear();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Object[] seen = executor.submit(wrapped).get(5, TimeUnit.SECONDS);
            assertThat(seen[0]).isSameAs(ctx);
            assertThat(seen[1]).isEqualTo("v");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testWrapRunnableCarriesContext() throws Exception {
        Context<String> ctx = Context.of("data");
        Runnable wrapped;
        var seen = new java.util.concurrent.atomic.AtomicReference<Context<?>>();
        try (var scope = Contexts.open(ctx)) {
            wrapped = Contexts.wrap(() -> seen.set(Contexts.current()));
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(wrapped).get(5, TimeUnit.SECONDS);
            assertThat(seen.get()).isSameAs(ctx);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testPooledThreadSeesEachTasksOwnContext() throws Exception {
        Context<String> a = Context.of("A");
        Context<String> b = Context.of("B");
        ExecutorService pool = Executors.newFixedThreadPool(1);
        try {
            Context<?> seenA;
            Context<?> seenB;
            try (var scope = Contexts.open(a)) {
                seenA = pool.submit(Contexts.wrap(Contexts::current)).get(5, TimeUnit.SECONDS);
            }
            try (var scope = Contexts.open(b)) {
                seenB = pool.submit(Contexts.wrap(Contexts::current)).get(5, TimeUnit.SECONDS);
            }
            assertThat(seenA).isSameAs(a);
            assertThat(seenB).isSameAs(b);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void testWrapRestoresRunningThreadsContextAndMdc() throws Exception {
        Context<String> captured = Context.of("captured");
        Context<String> own = Context.of("own");
        Callable<String> wrapped;
        MDC.put("k", "captured");
        try (var scope = Contexts.open(captured)) {
            wrapped = Contexts.wrap(() -> "x");
        }
        MDC.clear();

        MDC.put("k", "own");
        try (var scope = Contexts.open(own)) {
            assertThat(wrapped.call()).isEqualTo("x");
            assertThat(Contexts.current()).isSameAs(own);
            assertThat(MDC.get("k")).isEqualTo("own");
        }
    }

    @Test
    void testWrapWithoutMdcClearsRunningThreadsMdcDuringTask() throws Exception {
        Callable<Map<String, String>> wrapped = Contexts.wrap(MDC::getCopyOfContextMap);
        MDC.put("k", "own");
        assertThat(wrapped.call()).isNull();
        assertThat(MDC.get("k")).isEqualTo("own");
    }

    @Test
    void testWrapRestoresEvenWhenTaskThrowsAndPropagatesException() {
        Context<String> own = Context.of("own");
        var boom = new IllegalStateException("boom");
        Callable<String> wrapped = Contexts.wrap((Callable<String>) () -> {
            throw boom;
        });
        MDC.put("k", "own");
        try (var scope = Contexts.open(own)) {
            assertThatThrownBy(wrapped::call).isSameAs(boom);
            assertThat(Contexts.current()).isSameAs(own);
            assertThat(MDC.get("k")).isEqualTo("own");
        }
    }

    @Test
    void testWrapRunnableRestoresAndPropagatesException() {
        Context<String> own = Context.of("own");
        var boom = new IllegalStateException("boom");
        Runnable wrapped = Contexts.wrap((Runnable) () -> {
            throw boom;
        });
        try (var scope = Contexts.open(own)) {
            assertThatThrownBy(wrapped::run).isSameAs(boom);
            assertThat(Contexts.current()).isSameAs(own);
        }
    }

    @Test
    void testWrapAllowsNullResult() throws Exception {
        Callable<String> wrapped = Contexts.wrap((Callable<String>) () -> null);
        assertThat(wrapped.call()).isNull();
        assertThat(List.of(wrapped)).hasSize(1);
    }
}
