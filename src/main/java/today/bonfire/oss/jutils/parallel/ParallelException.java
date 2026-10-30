package today.bonfire.oss.jutils.parallel;

import java.util.Objects;
import lombok.Getter;

/**
 * Thrown when a task run through {@link Run} fails, times out or is interrupted. Carries the
 * {@link Context} of the submitting thread.
 */
public class ParallelException extends RuntimeException {
    @Getter
    private final Context<?> context;

    /**
     * Creates an exception without cause.
     *
     * @param message the detail message
     * @param context the submitter's context, not null
     * @throws NullPointerException if {@code context} is null
     */
    public ParallelException(String message, Context<?> context) {
        super(message);
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Creates an exception with message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying failure
     * @param context the submitter's context, not null
     * @throws NullPointerException if {@code context} is null
     */
    public ParallelException(String message, Throwable cause, Context<?> context) {
        super(message, cause);
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Creates an exception with a cause only.
     *
     * @param cause   the underlying failure
     * @param context the submitter's context, not null
     * @throws NullPointerException if {@code context} is null
     */
    public ParallelException(Throwable cause, Context<?> context) {
        super(cause);
        this.context = Objects.requireNonNull(context, "context");
    }
}
