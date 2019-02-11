package pico.erp.bom.material;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pico.erp.bom.Bom;
import pico.erp.bom.BomEntity;
import pico.erp.bom.BomId;
import pico.erp.bom.BomMapper;
import pico.erp.bom.material.BomMaterialEntity.BomMaterialKey;
import pico.erp.item.spec.ItemSpecId;

@Repository
interface BomMaterialEntityRepository extends CrudRepository<BomMaterialEntity, BomMaterialKey> {

  @Query("SELECT b FROM BomMaterial bm JOIN Bom b ON bm.key.bomId = b.id WHERE bm.key.materialId = :materialId")
  Stream<BomEntity> findAllIncludeMaterialBomBy(@Param("materialId") BomId materialId);

  @Query("SELECT COUNT(bm) FROM BomMaterial bm WHERE bm.key.bomId = :bomId")
  long countIncludedMaterialBy(@Param("bomId") BomId bomId);

  @Query("SELECT bm FROM BomMaterial bm WHERE bm.key.bomId = :bomId ORDER BY bm.order")
  Stream<BomMaterialEntity> findAllIncludedMaterialBy(@Param("bomId") BomId bomId);

  @Query("SELECT bm FROM BomMaterial bm WHERE bm.itemSpecId = :itemSpecId")
  Stream<BomMaterialEntity> findBy(@Param("itemSpecId") ItemSpecId itemSpecId);

}

@Repository
@Transactional
public class BomMaterialRepositoryJpa implements BomMaterialRepository {

  @Autowired
  private BomMaterialEntityRepository repository;

  @Autowired
  private BomMaterialMapper mapper;

  @Autowired
  private BomMapper bomMapper;

  @Override
  public BomMaterial create(BomMaterial material) {
    val entity = mapper.jpa(material);
    val created = repository.save(entity);
    return mapper.jpa(created);
  }

  @Override
  public void deleteBy(BomId bomId, BomId materialId) {
    repository.delete(BomMaterialKey.from(bomId, materialId));
  }

  @Override
  public void deleteBy(BomMaterial material) {
    repository.delete(BomMaterialKey.from(material));
  }

  @Override
  public boolean exists(BomId bomId, BomId materialId) {
    return repository.exists(BomMaterialKey.from(bomId, materialId));
  }

  @Override
  public boolean exists(BomMaterial material) {
    return repository.exists(BomMaterialKey.from(material));
  }

  @Override
  public Stream<Bom> findAllAscendBy(BomId materialId) {
    val references = repository.findAllIncludeMaterialBomBy(materialId)
      .map(bomMapper::jpa)
      .collect(Collectors.toList());
    references.addAll(
      references.stream().flatMap(bom -> this.findAllIncludeMaterialBomBy(bom.getId()))
        .collect(Collectors.toList())
    );
    return references.stream();
  }

  @Override
  public Stream<Bom> findAllIncludeMaterialBomBy(BomId materialId) {
    return repository.findAllIncludeMaterialBomBy(materialId)
      .map(bomMapper::jpa);
  }

  @Override
  public Stream<BomMaterial> findAllIncludedMaterialBy(BomId id) {
    return repository.findAllIncludedMaterialBy(id)
      .map(mapper::jpa);
  }

  @Override
  public long countIncludedMaterialBy(BomId bomId) {
    return repository.countIncludedMaterialBy(bomId);
  }

  @Override
  public Optional<BomMaterial> findBy(BomId bomId, BomId materialId) {
    return Optional.ofNullable(repository.findOne(BomMaterialKey.from(bomId, materialId)))
      .map(mapper::jpa);
  }

  @Override
  public Stream<BomMaterial> findAllBy(ItemSpecId itemSpecId) {
    return repository.findBy(itemSpecId)
      .map(mapper::jpa);
  }

  @Override
  public void update(BomMaterial material) {
    val entity = repository.findOne(BomMaterialKey.from(material));
    mapper.pass(mapper.jpa(material), entity);
    repository.save(entity);
  }
}
