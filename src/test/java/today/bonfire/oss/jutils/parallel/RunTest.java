package today.bonfire.oss.jutils.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.MDC;

@Timeout(60)
class RunTest {

    @AfterEach
    void cleanUp() {
        MDC.clear();
    }

    @Test
    void testAllReturnsResultsInOrderForSubtypedList() throws Exception {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int n = i;
            tasks.add(() -> {
                Thread.sleep((5 - n) * 10L);
                return n;
            });
        }

        List<Integer> results = Run.all(tasks);

        assertThat(results).containsExactly(0, 1, 2, 3, 4);
        assertThatThrownBy(() -> results.add(9)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testAllWithTimeoutOverload() throws Exception {
        List<Callable<String>> tasks = List.of(() -> "a", () -> "b");
        assertThat(Run.all(tasks, Duration.ofSeconds(10))).containsExactly("a", "b");
    }

    @Test
    void testAllAllowsNullResult() throws Exception {
        List<Callable<String>> tasks = List.of(() -> null, () -> "x");
        List<String> results = Run.all(tasks);
        assertThat(results).containsExactly(null, "x");
    }

    @Test
    void testAllEmptyReturnsEmptyList() throws Exception {
        assertThat(Run.all(List.<Callable<String>>of())).isEmpty();
    }

    @Test
    void testAllRejectsNonPositiveTimeout() {
        List<Callable<String>> tasks = List.of(() -> "a");
        assertThatThrownBy(() -> Run.all(tasks, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Run.all(tasks, Duration.ofSeconds(-1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testTimeoutCancelsAndInterruptsBlockedTaskAndHonoursTimeout() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        List<Callable<String>> tasks = List.of(() -> {
            started.countDown();
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                interrupted.countDown();
                throw e;
            }
            return "late";
        });

        Context<String> ctx = Context.of("submitter");
        try (var scope = Contexts.open(ctx)) {
            assertThatThrownBy(() -> Run.all(tasks, Duration.ofMillis(200)))
                    .isInstanceOfSatisfying(ParallelException.class, e -> {
                        assertThat(e).hasMessageStartingWith("Timed out after ");
                        assertThat(e).hasCauseInstanceOf(TimeoutException.class);
                        assertThat(e.getContext()).isSameAs(ctx);
                    });
        }
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(interrupted.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testFailFastCancelsBlockedTaskAndKeepsOriginalCause() throws Exception {
        CountDownLatch blockedStarted = new CountDownLatch(1);
        CountDownLatch blockedInterrupted = new CountDownLatch(1);
        var original = new IllegalStateException("original");
        List<Callable<String>> tasks = List.of(
                () -> {
                    blockedStarted.countDown();
                    try {
                        Thread.sleep(30_000);
                    } catch (InterruptedException e) {
                        blockedInterrupted.countDown();
                        throw e;
                    }
                    return "late";
                },
                () -> {
                    assertThat(blockedStarted.await(5, TimeUnit.SECONDS)).isTrue();
                    throw original;
                });

        assertThatThrownBy(() -> Run.all(tasks, Duration.ofSeconds(20)))
                .isInstanceOfSatisfying(
                        ParallelException.class, e -> assertThat(e.getCause()).isSameAs(original));
        assertThat(blockedInterrupted.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testFailureCarriesSubmittersContext() {
        Context<String> ctx = Context.of("submitter");
        List<Callable<String>> tasks = List.of(() -> {
            throw new RuntimeException("fail");
        });

        try (var scope = Contexts.open(ctx)) {
            assertThatThrownBy(() -> Run.all(tasks)).isInstanceOfSatisfying(ParallelException.class, e -> {
                assertThat(e.getContext()).isSameAs(ctx);
                assertThat(e.getCause()).hasMessage("fail");
            });
        }
    }

    @Test
    void testFailureWithoutContextCarriesEmptyContext() {
        List<Callable<String>> tasks = List.of(() -> {
            throw new RuntimeException("fail");
        });
        assertThatThrownBy(() -> Run.all(tasks)).isInstanceOfSatisfying(ParallelException.class, e -> {
            assertThat(e.getContext().hasData()).isFalse();
            assertThat(e.getContext().name()).isNull();
        });
    }

    @Test
    void testSubmitAllFailureFutureCarriesSubmittersContext() throws Exception {
        Context<String> ctx = Context.of("submitter");
        List<Callable<String>> tasks = List.of(() -> {
            throw new RuntimeException("fail");
        });
        List<Future<String>> futures;
        try (var scope = Contexts.open(ctx)) {
            futures = Run.submitAll(tasks);
        }
        assertThatThrownBy(() -> futures.getFirst().get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ParallelException.class)
                .satisfies(e -> assertThat(((ParallelException) e.getCause()).getContext())
                        .isSameAs(ctx));
    }

    @Test
    void testContextAndMdcVisibleInsideTasks() throws Exception {
        Context<String> ctx = Context.of("submitter");
        List<Callable<Object[]>> tasks = List.of(() -> new Object[] {Contexts.current(), MDC.get("req")});

        MDC.put("req", "42");
        try (var scope = Contexts.open(ctx)) {
            List<Object[]> results = Run.all(tasks);
            assertThat(results.getFirst()[0]).isSameAs(ctx);
            assertThat(results.getFirst()[1]).isEqualTo("42");
        }
    }

    @Test
    void testSubmitAllReturnsImmediately() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        List<Callable<String>> tasks = List.of(
                () -> {
                    assertThat(release.await(20, TimeUnit.SECONDS)).isTrue();
                    return "a";
                },
                () -> "b");

        List<Future<String>> futures = Run.submitAll(tasks);

        assertThat(futures).hasSize(2);
        assertThat(futures.get(0).isDone()).isFalse();
        assertThatThrownBy(() -> futures.add(null)).isInstanceOf(UnsupportedOperationException.class);
        release.countDown();
        assertThat(futures.get(0).get(5, TimeUnit.SECONDS)).isEqualTo("a");
        assertThat(futures.get(1).get(5, TimeUnit.SECONDS)).isEqualTo("b");
    }

    @Test
    void testSubmitAllAcceptsSubtypedList() throws Exception {
        List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2);
        List<Future<Integer>> futures = Run.submitAll(tasks);
        assertThat(futures.get(0).get(5, TimeUnit.SECONDS)).isEqualTo(1);
        assertThat(futures.get(1).get(5, TimeUnit.SECONDS)).isEqualTo(2);
    }

    @Test
    void testSubmitAllCancelInterruptsTask() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        List<Callable<String>> tasks = List.of(() -> {
            started.countDown();
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                interrupted.countDown();
                throw e;
            }
            return "late";
        });
        List<Future<String>> futures = Run.submitAll(tasks);
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
        futures.getFirst().cancel(true);
        assertThat(interrupted.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testCustomExecutorIsUsedForAllAndSubmitAll() throws Exception {
        AtomicInteger created = new AtomicInteger();
        ExecutorService executor = Executors.newCachedThreadPool(r -> {
            created.incrementAndGet();
            Thread t = new Thread(r, "custom-run-thread");
            t.setDaemon(true);
            return t;
        });
        try {
            List<Callable<String>> tasks = List.of(() -> Thread.currentThread().getName());

            assertThat(Run.all(executor, tasks, Duration.ofSeconds(10))).containsExactly("custom-run-thread");
            assertThat(Run.submitAll(executor, tasks).getFirst().get(5, TimeUnit.SECONDS))
                    .isEqualTo("custom-run-thread");
            assertThat(created.get()).isPositive();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testPooledExecutorGivesEachSubmissionItsOwnContext() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        try {
            Context<String> a = Context.of("A");
            Context<String> b = Context.of("B");
            List<Callable<Context<?>>> tasks = List.of(Contexts::current);
            List<Context<?>> first;
            List<Context<?>> second;
            try (var scope = Contexts.open(a)) {
                first = Run.all(pool, tasks, Duration.ofSeconds(10));
            }
            try (var scope = Contexts.open(b)) {
                second = Run.all(pool, tasks, Duration.ofSeconds(10));
            }
            assertThat(first.getFirst()).isSameAs(a);
            assertThat(second.getFirst()).isSameAs(b);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void testDefaultExecutorUsableRepeatedly() throws Exception {
        List<Callable<String>> tasks = List.of(() -> "x");
        assertThat(Run.all(tasks)).containsExactly("x");
        assertThat(Run.all(tasks)).containsExactly("x");
    }

    @Test
    void testInterruptedWaiterCancelsTasksAndRethrows() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch taskInterrupted = new CountDownLatch(1);
        List<Callable<String>> tasks = List.of(() -> {
            started.countDown();
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                taskInterrupted.countDown();
                throw e;
            }
            return "late";
        });
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        CountDownLatch waiterDone = new CountDownLatch(1);
        Thread waiter = new Thread(() -> {
            try {
                Run.all(tasks, Duration.ofSeconds(25));
            } catch (Throwable t) {
                thrown.set(t);
            } finally {
                waiterDone.countDown();
            }
        });
        waiter.start();
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
        waiter.interrupt();

        assertThat(waiterDone.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(thrown.get()).isInstanceOf(InterruptedException.class);
        assertThat(taskInterrupted.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
