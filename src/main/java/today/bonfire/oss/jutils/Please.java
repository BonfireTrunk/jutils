package today.bonfire.oss.jutils;

import java.util.concurrent.Callable;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * Helpers for running code that may throw without writing try/catch blocks.
 *
 * <p>All methods run the supplied code on the calling thread and block for as long as it runs. Each
 * {@code Callable} overload restores the thread's interrupt flag when the caught exception is an
 * {@link InterruptedException}.
 */
@Slf4j
public final class Please {

    private static final Runnable emptyRunnable = () -> {};

    private Please() {}

    /**
     * Determines if the provided Runnable throws an exception when executed.
     *
     * @param runnable the Runnable to be executed, not null
     * @return a boolean indicating whether an exception was thrown
     */
    public static boolean checkIfThrows(Runnable runnable) {
        try {
            runnable.run();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Executes a given Callable and returns its result. If an exception occurs, it will be handled by
     * the provided Function and its result will be returned. If the exception is an
     * {@link InterruptedException}, the interrupt flag is restored before the Function is invoked.
     *
     * @param callable      the Callable to be executed, not null
     * @param exceptionFunc the Function that handles any exceptions thrown by the Callable, not null
     * @param <T>           the result type, which may be null
     * @return the result of the Callable, or the result of the exception handling Function
     */
    public static <T extends @Nullable Object> T tryThis(Callable<T> callable, Function<Exception, T> exceptionFunc) {
        return tryThis(callable, exceptionFunc, emptyRunnable);
    }

    /**
     * Calls a given callable function and returns its value or logs the exception and returns null if
     * the callable function throws an exception. If the exception is an {@link InterruptedException},
     * the interrupt flag is restored before logging.
     *
     * @param callable the Callable to be executed, not null
     * @param <T>      the result type
     * @return the result of the Callable, or null if an exception occurred
     */
    public static <T extends @Nullable Object> @Nullable T tryThis(Callable<T> callable) {
        return tryThis(
                callable,
                (e) -> {
                    log.error("Exception occurred", e);
                    return null;
                },
                emptyRunnable);
    }

    /**
     * Calls a given callable function and returns its value or applies an exception function if the
     * callable function throws an exception. Also runs a final function regardless of whether the
     * callable function threw an exception or not. If the exception is an {@link InterruptedException},
     * the interrupt flag is restored before the exception function is invoked.
     *
     * @param callable      the callable function to be called, not null
     * @param exceptionFunc the function to apply if callable throws an exception, not null
     * @param finallyFunc   the function to be run regardless of whether an exception was thrown, not null
     * @param <T>           the result type, which may be null
     * @return the return value of the callable function or the result of calling exceptionFunc if an
     * exception was thrown
     */
    public static <T extends @Nullable Object> T tryThis(
            Callable<T> callable, Function<Exception, T> exceptionFunc, Runnable finallyFunc) {
        try {
            return callable.call();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return exceptionFunc.apply(e);
        } finally {
            finallyFunc.run();
        }
    }

    /**
     * Executes a given Runnable and logs any exceptions that occur.
     * useful if you want to try a task and not care if it fails.
     *
     * @param runnable the Runnable to be executed, not null
     */
    public static void tryThis(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error("Exception occurred", e);
        }
    }
}
