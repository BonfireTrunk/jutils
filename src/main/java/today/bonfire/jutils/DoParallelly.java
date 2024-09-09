package today.bonfire.jutils;

import io.activej.eventloop.Eventloop;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Log4j2
public class DoParallelly {

  /**
   * Runs the given callables in parallel using an event loop.
   *
   * @param callables varargs of callables to be executed in parallel
   * @return a map of results where the key is the index of the callable and the value is the result of the callable
   */
  @SafeVarargs
  public static <T> Map<Integer, T> run(Callable<T>... callables) {
    var eventLoop = Eventloop.create().withCurrentThread();
    var results   = new HashMap<Integer, T>();
    for (int i = 0; i < callables.length; i++) {
      int index = i;
      eventLoop.postLast(() -> {
        try {
          results.put(index, callables[index].call());
        } catch (Exception e) {
          results.put(index, null);
          log.error("Error in parallel run", e);
        }
      });
    }

    eventLoop.run();
    return results;
  }

}
