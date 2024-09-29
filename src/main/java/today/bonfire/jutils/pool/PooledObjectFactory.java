package today.bonfire.jutils.pool;

public interface PooledObjectFactory<T extends PoolEntity> {
  T createObject();

  void destroyObject(T obj);

}
