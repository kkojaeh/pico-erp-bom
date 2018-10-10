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

  @Query("SELECT bm.bom FROM BomMaterial bm JOIN bm.material m WHERE m.id = :materialId")
  Stream<BomEntity> findAllBomBy(@Param("materialId") BomId materialId);

  @Query("SELECT bm FROM BomMaterial bm JOIN bm.bom b WHERE b.id = :bomId")
  Stream<BomMaterialEntity> findAllMaterialBy(@Param("bomId") BomId bomId);
/*

  @Query("SELECT bm.bom FROM BomMaterial bm JOIN bm.material m WHERE m.id = :materialId")
  Stream<BomEntity> findAllBy(@Param("materialId") BomId materialId);
*/

  @Query("SELECT bm FROM BomMaterial bm WHERE bm.itemSpecId = :itemSpecId")
  BomMaterialEntity findBy(@Param("itemSpecId") ItemSpecId itemSpecId);

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
    val entity = mapper.entity(material);
    val created = repository.save(entity);
    return mapper.domain(created);
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
    val references = repository.findAllBomBy(materialId)
      .map(bomMapper::domain)
      .collect(Collectors.toList());
    references.addAll(
      references.stream().flatMap(bom -> this.findAllReferencedBy(bom.getId()))
        .collect(Collectors.toList())
    );
    return references.stream();
  }

  @Override
  public Stream<BomMaterial> findAllBy(BomId id) {
    return repository.findAllMaterialBy(id)
      .map(mapper::domain);
  }

  @Override
  public Stream<Bom> findAllReferencedBy(BomId materialId) {
    return repository.findAllBomBy(materialId)
      .map(bomMapper::domain);
  }

  @Override
  public Optional<BomMaterial> findBy(BomId bomId, BomId materialId) {
    return Optional.ofNullable(repository.findOne(BomMaterialKey.from(bomId, materialId)))
      .map(mapper::domain);
  }

  @Override
  public Optional<BomMaterial> findBy(ItemSpecId itemSpecId) {
    return Optional.ofNullable(repository.findBy(itemSpecId))
      .map(mapper::domain);
  }

  @Override
  public void update(BomMaterial material) {
    val entity = repository.findOne(BomMaterialKey.from(material));
    mapper.pass(mapper.entity(material), entity);
    repository.save(entity);
  }
}