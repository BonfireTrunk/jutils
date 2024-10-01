package today.bonfire.jutils.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Run {

  private static final ExecutorService executorService = Executors.getNewExecutor("juvt-");

  public static ArrayList<Future<?>> parallel(List<Callable<?>> callables) {
    var futures = new ArrayList<Future<?>>();
    for (Callable<?> callable : callables) {
      Future<?> future = executorService.submit(callable);
      futures.add(future);
    }
    return futures;
  }
}
