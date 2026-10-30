package today.bonfire.oss.jutils.parallel;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} whose threads run with the {@link Context} and MDC of the thread that created
 * them.
 *
 * <p>The context and MDC are captured when the thread is created, which is correct for thread-per-task
 * executors. With pooled executors, where one thread runs many tasks, wrap each task with
 * {@link Contexts#wrap(Runnable)} instead, otherwise every task sees the context of whoever caused the
 * thread to be created.
 *
 * <p>Thread-safe if the delegate is.
 */
public final class ContextAwareThreadFactory implements ThreadFactory {

    private final ThreadFactory delegate;

    /**
     * Creates the factory.
     *
     * @param delegate the factory that actually creates threads, not null
     * @throws NullPointerException if {@code delegate} is null
     */
    public ContextAwareThreadFactory(ThreadFactory delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Creates a thread through the delegate whose runnable is wrapped with the caller's current context
     * and MDC. Does not block.
     *
     * @param runnable the code to run, not null
     * @return the new thread, as returned by the delegate
     */
    @Override
    public Thread newThread(Runnable runnable) {
        return delegate.newThread(Contexts.wrap(runnable));
    }
}
