package today.bonfire.jutils.pool;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class SimpleObjectPool<T extends PoolEntity> implements AutoCloseable {
  private final int                                  maxPoolSize;
  private final int minPoolSize;
  private final long                                 idleEvictionTimeoutMillis;
  private final long                                 abandonedObjectTimeoutMillis;
  private final LinkedBlockingDeque<PooledEntity<T>> idleObjects     = new LinkedBlockingDeque<>();
  private final Map<Long, PooledEntity<T>>           borrowedObjects = new ConcurrentHashMap<>();
  private final ReentrantLock                        lock            = new ReentrantLock();
  private final Condition                            notEmpty        = lock.newCondition();
  private final PooledObjectFactory<T>               objectFactory;
  private final AtomicLong                           idCounter       = new AtomicLong(0);
  ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public SimpleObjectPool(int maxPoolSize, int minPoolSize, long idleEvictionTimeout, long abandonedObjectTimeout, PooledObjectFactory<T> objectFactory) {
    this.maxPoolSize                  = maxPoolSize;
    this.minPoolSize = minPoolSize;
    this.idleEvictionTimeoutMillis    = idleEvictionTimeout;
    this.abandonedObjectTimeoutMillis = abandonedObjectTimeout;
    this.objectFactory                = objectFactory;
    if (minPoolSize > maxPoolSize || minPoolSize < 1) {
      throw new IllegalArgumentException("minIdleObjects cannot be greater than maxPoolSize");
    }

    if (maxPoolSize != minPoolSize && idleEvictionTimeoutMillis > 0) {
      scheduler.scheduleAtFixedRate(this::evictIdleObjects, idleEvictionTimeoutMillis, idleEvictionTimeoutMillis, TimeUnit.MILLISECONDS);
    }
    scheduler.scheduleAtFixedRate(this::removeAbandonedObjects, abandonedObjectTimeoutMillis, abandonedObjectTimeoutMillis, TimeUnit.MILLISECONDS);
  }

  private void evictIdleObjects() {
    try {
      long                  now              = System.currentTimeMillis();
      List<PooledEntity<T>> objectsToDestroy = new ArrayList<>();

      // Remove expired idle objects and collect them for destruction
      idleObjects.removeIf(pooledEntity -> {
        if (idleObjects.size() > minPoolSize &&
            (now - pooledEntity.getLastUsageTime()) > idleEvictionTimeoutMillis) {

          // Collect the object for destruction
          objectsToDestroy.add(pooledEntity);
          return true; // Indicate that this object should be removed
        }
        return false;
      });

      // Destroy collected objects
      for (PooledEntity<T> pooledEntity : objectsToDestroy) {
        destroyObject(pooledEntity);
      }
    } catch (Exception e) {
      log.warn("Error evicting idle objects", e);
    }

  }

  private void destroyObject(PooledEntity<T> pooledEntity) {
    borrowedObjects.remove(pooledEntity.getEntityId());
    objectFactory.destroyObject(pooledEntity.getObject());
  }

  private void removeAbandonedObjects() {
    try {
      long                  now             = System.currentTimeMillis();
      List<PooledEntity<T>> objectsToRemove = new ArrayList<>();

      borrowedObjects.forEach((id, pooledEntity) -> {
        if ((pooledEntity.isAbandoned(now, abandonedObjectTimeoutMillis))) {
          objectsToRemove.add(pooledEntity);
        }
      });

      for (PooledEntity<T> pooledEntity : objectsToRemove) {
        destroyObject(pooledEntity);
      }
    } catch (Exception e) {
      log.warn("Error removing abandoned objects", e);
    }
  }

  public T borrowObject(Duration timeout) throws InterruptedException {
    lock.lock();
    PooledEntity<T> pooledEntity = null;
    try {
      while (idleObjects.isEmpty() && borrowedObjects.size() >= maxPoolSize) {
        notEmpty.awaitNanos(timeout.toNanos());
      }
      pooledEntity = idleObjects.pollFirst();
      if (pooledEntity == null) {
        if (borrowedObjects.size() < maxPoolSize) { // No idle objects available
          pooledEntity = new PooledEntity<>(objectFactory.createObject(), idCounter.incrementAndGet());
          log.debug("Created new resource in pool with id {}", pooledEntity.getEntityId());
        } else {
          log.warn("Timeout waiting to borrow object from pool and max pool size reached");
        }
      }
    } finally {
      lock.unlock();
    }
    if (pooledEntity != null) {
      pooledEntity.borrow();
      borrowedObjects.put(pooledEntity.getEntityId(), pooledEntity);
      return pooledEntity.getObject();
    } else {
      throw new InterruptedException("Failed to borrow object from pool");
    }
  }

  public void returnObject(T obj) {
    returnObject(obj, false);
  }

  public void returnObject(T obj, boolean broken) {
    var pooledEntity = borrowedObjects.get(obj.getEntityId());
    if (pooledEntity != null) {
      if (broken) pooledEntity.setBroken(true);
      if (pooledEntity.isBroken()) {
        destroyObject(pooledEntity);
        log.info("Returned broken entity with id {} and destroyed it.", pooledEntity.getEntityId());
      } else {
        pooledEntity.markIdle();
        borrowedObjects.remove(pooledEntity.getEntityId());
        idleObjects.addFirst(pooledEntity);
      }
      lock.lock();
      try {
        notEmpty.signal();
      } finally {
        lock.unlock();
      }
    } else {
      log.error("Attempted returning object that is not in borrowed objects list. EntityId: {}", obj.getEntityId());
    }
  }

  @Override
  public void close() {
    scheduler.shutdown();
    borrowedObjects.values().forEach(this::destroyObject);
    idleObjects.forEach(this::destroyObject);
  }
}