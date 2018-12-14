package pico.erp.bom;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.item.ItemId;

public interface BomService {

  void delete(@NotNull @Valid BomRequests.DeleteRequest request);

  void determine(@NotNull @Valid BomRequests.DetermineRequest request);

  BomData draft(@NotNull @Valid BomRequests.DraftRequest request);

  boolean exists(@NotNull BomId id);

  boolean exists(@NotNull ItemId id);

  BomData get(@NotNull BomId id);

  BomData get(@NotNull ItemId id);

  BomData get(@NotNull ItemId id, @NotNull int revision);

  BomHierarchyData getHierarchy(@NotNull BomId id);

  BomHierarchyData getHierarchy(@NotNull ItemId id);

  void verify(@NotNull @Valid BomRequests.VerifyRequest request);

  void verify(@NotNull @Valid BomRequests.VerifyByItemSpecRequest request);

  void verify(@NotNull @Valid BomRequests.VerifyByItemRequest request);

  void verify(@NotNull @Valid BomRequests.VerifyByMaterialRequest request);
}
