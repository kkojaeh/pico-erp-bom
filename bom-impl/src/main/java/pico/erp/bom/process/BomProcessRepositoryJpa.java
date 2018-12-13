package pico.erp.bom.process;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pico.erp.bom.BomId;
import pico.erp.process.ProcessId;

@Repository
interface BomProcessEntityRepository extends CrudRepository<BomProcessEntity, BomProcessId> {

  @Query("SELECT COUNT(bp) FROM BomProcess bp WHERE bp.bomId = :bomId")
  long countBy(@Param("bomId") BomId bomId);

  @Query("SELECT CASE WHEN COUNT(bp) > 0 THEN true ELSE false END FROM BomProcess bp WHERE bp.bomId = :bomId AND bp.processId = :processId")
  boolean exists(@Param("bomId") BomId bomId, @Param("processId") ProcessId processId);

  @Query("SELECT bp FROM BomProcess bp WHERE bp.bomId = :bomId ORDER BY bp.order")
  Stream<BomProcessEntity> findAllBy(@Param("bomId") BomId bomId);

  @Query("SELECT bp FROM BomProcess bp WHERE bp.processId = :processId ORDER BY bp.order")
  Stream<BomProcessEntity> findAllBy(@Param("processId") ProcessId processId);


}

@Repository
@Transactional
public class BomProcessRepositoryJpa implements BomProcessRepository {

  @Autowired
  private BomProcessEntityRepository repository;

  @Autowired
  private BomProcessMapper mapper;

  @Override
  public long countBy(BomId bomId) {
    return repository.countBy(bomId);
  }

  @Override
  public BomProcess create(BomProcess material) {
    val entity = mapper.jpa(material);
    val created = repository.save(entity);
    return mapper.jpa(created);
  }

  @Override
  public void deleteBy(BomProcessId id) {
    repository.delete(id);
  }

  @Override
  public boolean exists(BomProcessId id) {
    return repository.exists(id);
  }

  @Override
  public boolean exists(BomId bomId, ProcessId processId) {
    return repository.exists(bomId, processId);
  }

  @Override
  public Stream<BomProcess> findAllBy(ProcessId processId) {
    return repository.findAllBy(processId)
      .map(mapper::jpa);
  }

  @Override
  public Stream<BomProcess> findAllBy(BomId bomId) {
    return repository.findAllBy(bomId)
      .map(mapper::jpa);
  }

  @Override
  public Optional<BomProcess> findBy(BomProcessId id) {
    return Optional.ofNullable(repository.findOne(id))
      .map(mapper::jpa);
  }

  @Override
  public void update(BomProcess bomProcess) {
    val entity = repository.findOne(bomProcess.getId());
    mapper.pass(mapper.jpa(bomProcess), entity);
    repository.save(entity);
  }
}
