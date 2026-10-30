package today.bonfire.oss.jutils.parallel;

import java.util.Map;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

/**
 * Holds the per-thread {@link Context} and propagates it, together with the SLF4J {@link MDC}, to tasks
 * that run on other threads.
 *
 * <p>All methods are thread-safe; the context itself is thread-local state.
 */
public final class Contexts {

    private static final ThreadLocal<@Nullable Context<?>> CURRENT = new ThreadLocal<>();

    private Contexts() {}

    /**
     * Returns the context installed on the calling thread.
     *
     * @return the current context, or null if none is installed. Never blocks.
     */
    public static @Nullable Context<?> current() {
        return CURRENT.get();
    }

    /**
     * Installs a context on the calling thread until the returned scope is closed, at which point the
     * previous value is restored.
     *
     * <p>Scopes must be closed on the thread that opened them and in reverse order of opening; use
     * try-with-resources.
     *
     * @param context the context to install; null removes the current one for the duration of the scope
     * @return the scope to close, never null. Does not block and throws nothing.
     */
    public static Scope open(@Nullable Context<?> context) {
        var previous = CURRENT.get();
        set(context);
        return new Scope() {
            private boolean closed;

            @Override
            public void close() {
                if (closed) return;
                closed = true;
                set(previous);
            }
        };
    }

    /**
     * Wraps a callable so that it runs with the context and MDC that were current when this method was
     * called. The running thread's own context and MDC are saved first and restored afterwards, even if
     * the task throws, so the wrapper is safe on pooled threads.
     *
     * @param task the task to wrap, not null
     * @param <T>  the result type, which may be null
     * @return the wrapped task. Wrapping does not block; the wrapper blocks as long as the task does and
     *         propagates the task's exceptions unchanged.
     */
    public static <T extends @Nullable Object> Callable<T> wrap(Callable<T> task) {
        var context = current();
        var mdc = MDC.getCopyOfContextMap();
        return () -> {
            var savedContext = current();
            var savedMdc = MDC.getCopyOfContextMap();
            install(context, mdc);
            try {
                return task.call();
            } finally {
                install(savedContext, savedMdc);
            }
        };
    }

    /**
     * Wraps a runnable like {@link #wrap(Callable)}.
     *
     * @param task the task to wrap, not null
     * @return the wrapped task. The task's exceptions propagate unchanged.
     */
    public static Runnable wrap(Runnable task) {
        var context = current();
        var mdc = MDC.getCopyOfContextMap();
        return () -> {
            var savedContext = current();
            var savedMdc = MDC.getCopyOfContextMap();
            install(context, mdc);
            try {
                task.run();
            } finally {
                install(savedContext, savedMdc);
            }
        };
    }

    private static void install(@Nullable Context<?> context, @Nullable Map<String, String> mdc) {
        set(context);
        if (mdc == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(mdc);
        }
    }

    private static void set(@Nullable Context<?> context) {
        if (context == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(context);
        }
    }

    /** Handle returned by {@link Contexts#open(Context)}. */
    public interface Scope extends AutoCloseable {

        /**
         * Restores the context that was installed before the scope was opened. Calling it again does
         * nothing. Must be called on the opening thread.
         */
        @Override
        void close();
    }
}
