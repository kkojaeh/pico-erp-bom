package pico.erp.bom;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pico.erp.item.ItemId;
import pico.erp.process.ProcessId;

@Repository
interface BomEntityRepository extends CrudRepository<BomEntity, BomId> {

  @Query("SELECT b FROM Bom b WHERE b.processId = :processId")
  Stream<BomEntity> findAllBy(@Param("processId") ProcessId processId);

  @Query("SELECT b FROM Bom b WHERE b.itemId = :itemId ORDER BY b.revision")
  Stream<BomEntity> findAllBy(@Param("itemId") ItemId itemId);

  /*@Query("SELECT b FROM Bom b JOIN b.materials m WHERE m.material = :material")
  Stream<BomEntity> findAllIncludedMaterialBy(@Param("material") BomEntity material);*/

  @Query("SELECT b FROM Bom b WHERE b.itemId = :itemId AND b.revision = :revision")
  BomEntity findBy(@Param("itemId") ItemId itemId, @Param("revision") Integer revision);

  @Query("SELECT MAX(b.revision) FROM Bom b WHERE b.itemId = :itemId")
  Integer findLastRevision(@Param("itemId") ItemId itemId);

}

@Repository
@Transactional
public class BomRepositoryJpa implements BomRepository {

  @Autowired
  private BomEntityRepository repository;

  @Autowired
  private BomMapper mapper;

  @Override
  public Bom create(Bom bom) {
    val entity = mapper.entity(bom);
    val created = repository.save(entity);
    return mapper.domain(created);
  }

  @Override
  public void deleteBy(BomId id) {
    repository.delete(id);
  }

  @Override
  public boolean exists(BomId id) {
    return repository.exists(id);
  }

  @Override
  public boolean exists(ItemId id) {
    return repository.findBy(id, 1) != null;
  }

  @Override
  public Optional<BomAggregator> findAggregatorBy(BomId id) {
    return Optional.ofNullable(repository.findOne(id))
      .map(mapper::aggregator);
  }

  @Override
  public Stream<Bom> findAllBy(ProcessId id) {
    return repository.findAllBy(id)
      .map(mapper::domain);
  }

  @Override
  public Stream<Bom> findAllBy(ItemId itemId) {
    return repository.findAllBy(itemId)
      .map(mapper::domain);
  }

  @Override
  public Optional<Bom> findBy(BomId id) {
    return Optional.ofNullable(repository.findOne(id))
      .map(mapper::domain);
  }

  @Override
  public Optional<Bom> findBy(ItemId id, int revision) {
    return Optional.ofNullable(
      repository.findBy(id, revision)
    ).map(mapper::domain);
  }

  @Override
  public Optional<Bom> findWithLastRevision(ItemId itemId) {
    val lastRevision = repository.findLastRevision(itemId);
    if (lastRevision == null || lastRevision < 1) {
      return Optional.ofNullable(null);
    }
    return Optional.ofNullable(
      repository.findBy(itemId, lastRevision)
    ).map(mapper::domain);
  }

  @Override
  public void update(Bom bom) {
    val entity = repository.findOne(bom.getId());
    mapper.pass(mapper.entity(bom), entity);
    repository.save(entity);
  }


}
