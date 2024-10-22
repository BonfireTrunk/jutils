package today.bonfire.jutils;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class Please {

  private static final Runnable emptyRunnable = () -> {};

  /**
   * Determines if the provided Runnable throws an exception when executed.
   *
   * @param runnable the Runnable to be executed
   * @return a boolean indicating whether an exception was thrown
   */
  public static boolean throwsException(Runnable runnable) {
    try {
      runnable.run();
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Executes a given Callable and returns its result. If an exception occurs, it will be handled by
   * the provided Function and its result will be returned.
   *
   * @param callable      the Callable to be executed
   * @param exceptionFunc the Function that handles any exceptions thrown by the Callable
   * @return the result of the Callable, or the result of the exception handling Function
   */
  public static <T> T tryThis(Callable<T> callable, Function<Exception, T> exceptionFunc) {
    return tryThis(callable, exceptionFunc, emptyRunnable);
  }

  /**
   * Calls a given callable function and returns its value or applies an exception function if the
   * callable function throws an exception. Also runs a final function regardless of whether the
   * callable function threw an exception or not.
   *
   * @param callable      the callable function to be called
   * @param exceptionFunc the function to apply if callable throws an exception
   * @param finallyFunc   the function to be run regardless of whether an exception was thrown
   * @return the return value of the callable function or the result of calling exceptionFunc if an
   * exception was thrown
   */
  public static <T> T tryThis(Callable<T> callable,
                              Function<Exception, T> exceptionFunc,
                              Runnable finallyFunc) {
    try {
      return callable.call();
    } catch (Exception e) {
      return exceptionFunc.apply(e);
    } finally {
      finallyFunc.run();
    }
  }
}
