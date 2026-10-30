package today.bonfire.oss.jutils.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * Factory for thread-per-task executors whose threads inherit the creator's {@link Context} and MDC.
 *
 * <p>Context is captured when each thread is created, i.e. at task submission, so these executors are
 * not pooled. All methods are thread-safe and non-blocking.
 */
@Slf4j
public final class ContextExecutors {
    private static final AtomicInteger poolCounter = new AtomicInteger(1);

    private ContextExecutors() {}

    /**
     * Creates a thread-per-task executor running on virtual threads.
     *
     * @param threadPrefix prefix for thread names; if null or blank, "jutils-vt-&lt;N&gt;-" is used with
     *                     N taken from a process-wide counter
     * @return a new executor the caller must shut down, never null
     */
    public static ExecutorService newVirtualThreadExecutor(@Nullable String threadPrefix) {
        String prefix = threadPrefix;
        if (prefix == null || prefix.isBlank()) {
            prefix = "jutils-vt-" + poolCounter.getAndIncrement() + "-";
        }

        ThreadFactory virtualFactory = Thread.ofVirtual().name(prefix, 0).factory();

        log.debug("Creating new executor with thread prefix: {}", prefix);
        return newThreadPerTaskExecutor(virtualFactory);
    }

    /**
     * Creates a thread-per-task executor whose threads are created by the given factory and run with
     * the context and MDC of the submitting thread.
     *
     * @param threadFactory the factory creating the threads, not null
     * @return a new executor the caller must shut down, never null
     * @throws NullPointerException if {@code threadFactory} is null
     */
    public static ExecutorService newThreadPerTaskExecutor(ThreadFactory threadFactory) {
        return Executors.newThreadPerTaskExecutor(new ContextAwareThreadFactory(threadFactory));
    }
}
