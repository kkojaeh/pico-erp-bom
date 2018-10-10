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

  Stream<BomMaterial> findAllBy(@NotNull BomId bomId);

  /**
   * material 을 사용하고 있는 bom 을 검색하여 리턴 한다.
   */
  Stream<Bom> findAllReferencedBy(@NotNull BomId materialId);

  Optional<BomMaterial> findBy(@NotNull BomId bomId, @NotNull BomId materialId);

  Optional<BomMaterial> findBy(@NotNull ItemSpecId itemSpecId);

  void update(@NotNull BomMaterial material);


}
