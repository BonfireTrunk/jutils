package today.bonfire.oss.jutils.parallel;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

/**
 * Immutable carrier for per-request data that travels with tasks across threads (see {@link Contexts}).
 *
 * <p>{@code data} is shared by reference, not copied, so it should be immutable (or at least safe for
 * concurrent use) because every task that sees this context sees the same instance. The record itself is
 * thread-safe if {@code data} is.
 *
 * @param data      the payload, may be null
 * @param name      an optional label, may be null
 * @param startTime creation time in epoch milliseconds
 * @param <T>       the payload type
 */
@Builder
@With
public record Context<T>(@Nullable T data, @Nullable String name, long startTime) {

    /**
     * Creates a context holding the given data, with no name and the current time as start time.
     *
     * @param data the payload, may be null
     * @param <T>  the payload type
     * @return a new context, never null
     */
    public static <T> Context<T> of(@Nullable T data) {
        return new Context<>(data, null, System.currentTimeMillis());
    }

    /**
     * Creates a context with no data and no name, started now.
     *
     * @param <T> the payload type
     * @return a new empty context, never null
     */
    public static <T> Context<T> empty() {
        return new Context<>(null, null, System.currentTimeMillis());
    }

    /**
     * Tells whether this context carries data.
     *
     * @return true if {@code data} is not null
     */
    public boolean hasData() {
        return data != null;
    }

    /**
     * Combines this context with another: the other's non-null data and name win, and the earlier start
     * time is kept. Neither context is modified.
     *
     * @param other the context to merge in; if null, this context is returned
     * @return the merged context, never null
     */
    public Context<T> merge(@Nullable Context<T> other) {
        if (other == null) return this;
        return new Context<>(
                other.data != null ? other.data : this.data,
                other.name != null ? other.name : this.name,
                Math.min(this.startTime, other.startTime));
    }
}
