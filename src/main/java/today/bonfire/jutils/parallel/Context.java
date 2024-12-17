package today.bonfire.jutils.parallel;

import lombok.Builder;
import lombok.With;

@Builder
@With
public record Context<T>(
  T data,
  String name,
  long startTime
) {
  public static <T> Context<T> of(T data) {
    return new Context<>(data, null, System.currentTimeMillis());
  }

  public static <T> Context<T> empty() {
    return new Context<>(null, null, System.currentTimeMillis());
  }

  public boolean hasData() {
    return data != null;
  }

  public Context<T> merge(Context<T> other) {
    if (other == null) return this;
    return new Context<>(
      other.data != null ? other.data : this.data,
      other.name != null ? other.name : this.name,
      Math.min(this.startTime, other.startTime)
    );
  }
}
