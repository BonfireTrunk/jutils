package today.bonfire.jutils.parallel;

import lombok.Getter;

public class ParallelException extends RuntimeException {
  @Getter
  private final Context<?> context;

  public ParallelException(String message, Context<?> context) {
    super(message);
    this.context = context;
  }

  public ParallelException(String message, Throwable cause, Context<?> context) {
    super(message, cause);
    this.context = context;
  }

  public ParallelException(Throwable cause, Context<?> context) {
    super(cause);
    this.context = context;
  }
}
