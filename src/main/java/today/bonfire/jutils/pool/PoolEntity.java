package today.bonfire.jutils.pool;

public abstract class PoolEntity {

  private Long entityId;

  Long getEntityId() {
    return entityId;
  }

  final void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

}
