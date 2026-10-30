package today.bonfire.oss.jutils.parallel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

/**
 * Runs groups of tasks in parallel, propagating the submitter's {@link Context} and MDC into every task.
 *
 * <p>Tasks that fail surface as {@link ParallelException} carrying the submitter's context (or an empty
 * one if none was installed). Methods without an executor argument use a shared virtual-thread
 * executor that is created lazily and never shut down: it holds no idle threads and virtual threads do
 * not keep the JVM alive. Callers who want lifecycle control pass their own executor.
 */
public final class Run {

    /** Timeout applied by the {@code all} overloads that do not take one. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);

    private Run() {}

    private static final class DefaultExecutor {
        static final ExecutorService INSTANCE = ContextExecutors.newVirtualThreadExecutor("jutils-run-");
    }

    /**
     * Runs all tasks on the shared executor and waits for them using {@link #DEFAULT_TIMEOUT}.
     *
     * @param tasks the tasks, not null; elements must not be null
     * @param <T>   the result type, which may be null
     * @return the results in task order (may contain null), unmodifiable
     * @throws InterruptedException if the calling thread is interrupted while waiting; unfinished tasks
     *                              are cancelled
     * @throws ParallelException    on the first task failure or on timeout; see
     *                              {@link #all(ExecutorService, Collection, Duration)}
     */
    public static <T extends @Nullable Object> List<T> all(Collection<? extends Callable<? extends T>> tasks)
            throws InterruptedException {
        return all(tasks, DEFAULT_TIMEOUT);
    }

    /**
     * Runs all tasks on the shared executor and waits for them.
     *
     * @param tasks   the tasks, not null; elements must not be null
     * @param timeout the maximum total wait, must be positive
     * @param <T>     the result type, which may be null
     * @return the results in task order (may contain null), unmodifiable
     * @throws IllegalArgumentException if {@code timeout} is not positive
     * @throws InterruptedException     if the calling thread is interrupted while waiting
     * @throws ParallelException        on the first task failure or on timeout
     */
    public static <T extends @Nullable Object> List<T> all(
            Collection<? extends Callable<? extends T>> tasks, Duration timeout) throws InterruptedException {
        return all(DefaultExecutor.INSTANCE, tasks, timeout);
    }

    /**
     * Runs all tasks on the given executor and blocks until all finish, the timeout passes, or the first
     * task fails.
     *
     * <p>On the first failure the remaining tasks are cancelled with interruption and that task's
     * {@link ParallelException} (cause: the original exception) is thrown. On timeout, unfinished tasks
     * are cancelled with interruption and a {@link ParallelException} caused by a
     * {@link TimeoutException} is thrown. If the waiting thread is interrupted, all tasks are cancelled
     * and the {@link InterruptedException} is rethrown. Each task runs with the context and MDC current
     * at the time of this call. An empty collection returns an empty list immediately.
     *
     * @param executor the executor running the tasks, not null; it is not shut down
     * @param tasks    the tasks, not null; elements must not be null
     * @param timeout  the maximum total wait, must be positive
     * @param <T>      the result type, which may be null
     * @return the results in task order (may contain null), unmodifiable
     * @throws IllegalArgumentException if {@code timeout} is not positive
     * @throws InterruptedException     if the calling thread is interrupted while waiting
     * @throws ParallelException        on the first task failure or on timeout, carrying the
     *                                  submitter's context
     * @throws java.util.concurrent.RejectedExecutionException if the executor refuses a task; tasks
     *                                  already submitted are cancelled
     */
    public static <T extends @Nullable Object> List<T> all(
            ExecutorService executor, Collection<? extends Callable<? extends T>> tasks, Duration timeout)
            throws InterruptedException {
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive: " + timeout);
        }
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        var submitterContext = submitterContext();
        long timeoutNanos = toNanosSaturated(timeout);
        long start = System.nanoTime();

        var completion = new ExecutorCompletionService<T>(executor);
        var futures = new ArrayList<Future<T>>(tasks.size());
        var results = new ArrayList<T>(tasks.size());
        try {
            for (var task : tasks) {
                futures.add(completion.submit(wrapTask(task, submitterContext)));
            }

            for (int done = 0; done < futures.size(); done++) {
                long remaining = timeoutNanos - (System.nanoTime() - start);
                var finished = remaining <= 0 ? completion.poll() : completion.poll(remaining, TimeUnit.NANOSECONDS);
                if (finished == null) {
                    throw new ParallelException("Timed out after " + timeout, new TimeoutException(), submitterContext);
                }
                try {
                    finished.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof ParallelException pe) throw pe;
                    throw new ParallelException("Task execution failed", e.getCause(), submitterContext);
                } catch (CancellationException e) {
                    throw new ParallelException("Task cancelled", e, submitterContext);
                }
            }

            for (var future : futures) {
                results.add(future.resultNow());
            }
        } catch (InterruptedException | RuntimeException e) {
            cancelAll(futures);
            throw e;
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Submits all tasks to the shared executor without waiting.
     *
     * @param tasks the tasks, not null; elements must not be null
     * @param <T>   the result type, which may be null
     * @return the futures in task order, unmodifiable. A failed task's future completes exceptionally
     *         with a {@link ParallelException} carrying the submitter's context.
     */
    public static <T extends @Nullable Object> List<Future<T>> submitAll(
            Collection<? extends Callable<? extends T>> tasks) {
        return submitAll(DefaultExecutor.INSTANCE, tasks);
    }

    /**
     * Submits all tasks to the given executor in order and returns immediately. Each task runs with the
     * context and MDC current at the time of this call.
     *
     * @param executor the executor running the tasks, not null; it is not shut down
     * @param tasks    the tasks, not null; elements must not be null
     * @param <T>      the result type, which may be null
     * @return the futures in task order, unmodifiable
     * @throws java.util.concurrent.RejectedExecutionException if the executor refuses a task; tasks
     *                                  already submitted are cancelled
     */
    public static <T extends @Nullable Object> List<Future<T>> submitAll(
            ExecutorService executor, Collection<? extends Callable<? extends T>> tasks) {
        var submitterContext = submitterContext();
        var futures = new ArrayList<Future<T>>(tasks.size());
        try {
            for (var task : tasks) {
                futures.add(executor.submit(wrapTask(task, submitterContext)));
            }
        } catch (RuntimeException e) {
            cancelAll(futures);
            throw e;
        }
        return Collections.unmodifiableList(futures);
    }

    private static Context<?> submitterContext() {
        var current = Contexts.current();
        return current != null ? current : Context.empty();
    }

    private static <T extends @Nullable Object> Callable<T> wrapTask(
            Callable<? extends T> task, Context<?> submitterContext) {
        Callable<T> propagating = Contexts.wrap((Callable<T>) task::call);
        return () -> {
            try {
                return propagating.call();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ParallelException("Task interrupted", e, submitterContext);
            } catch (Exception e) {
                throw new ParallelException("Task execution failed", e, submitterContext);
            }
        };
    }

    private static void cancelAll(List<? extends Future<?>> futures) {
        for (var future : futures) {
            future.cancel(true);
        }
    }

    private static long toNanosSaturated(Duration timeout) {
        try {
            return timeout.toNanos();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }
}
