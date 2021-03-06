package pico.erp.bom.material;

import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import pico.erp.bom.Bom;
import pico.erp.bom.BomId;
import pico.erp.item.spec.ItemSpecId;

public interface BomMaterialRepository {

  BomMaterial create(@NotNull BomMaterial material);

  void deleteBy(@NotNull BomId bomId, @NotNull BomId materialId);

  void deleteBy(@NotNull BomMaterial material);

  boolean exists(@NotNull BomId id, @NotNull BomId materialId);

  boolean exists(@NotNull BomMaterial material);

  /**
   * material 을 사용하고 있는 bom 을 상위로 탐색하여 리턴 한다.
   */
  Stream<Bom> findAllAscendBy(@NotNull BomId materialId);

  /**
   * material 을 사용하고 있는 bom 을 검색하여 리턴
   */
  Stream<Bom> findAllIncludeMaterialBomBy(@NotNull BomId materialId);

  /**
   * bomId 에 포함된 자재를 리턴
   * @param bomId
   * @return
   */
  Stream<BomMaterial> findAllIncludedMaterialBy(@NotNull BomId bomId);

  long countIncludedMaterialBy(@NotNull BomId bomId);

  Optional<BomMaterial> findBy(@NotNull BomId bomId, @NotNull BomId materialId);

  Stream<BomMaterial> findAllBy(@NotNull ItemSpecId itemSpecId);

  void update(@NotNull BomMaterial material);


}
