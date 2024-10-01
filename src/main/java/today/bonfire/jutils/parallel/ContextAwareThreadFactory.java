package today.bonfire.jutils.parallel;

import org.slf4j.MDC;

import java.util.concurrent.ThreadFactory;

public class ContextAwareThreadFactory implements ThreadFactory {

  public static ThreadLocal<Context<?>> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();
  private final ThreadFactory           delegate;

  public ContextAwareThreadFactory(ThreadFactory delegate) {
    this.delegate = delegate;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    var parentMDC = MDC.getCopyOfContextMap();
    var context   = CONTEXT_THREAD_LOCAL.get();
    final Runnable wr = () -> {
      // Set ThreadLocal and MDC for the new thread
      if (parentMDC != null) MDC.setContextMap(parentMDC);
      CONTEXT_THREAD_LOCAL.set(context);
      try {
        runnable.run();
      } finally {
        MDC.clear();
        CONTEXT_THREAD_LOCAL.remove();
      }
    };
    return delegate.newThread(wr);
  }
}
