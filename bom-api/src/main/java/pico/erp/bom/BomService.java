package pico.erp.bom;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.bom.BomRequests.DeleteRequest;
import pico.erp.bom.BomRequests.DetermineRequest;
import pico.erp.bom.BomRequests.DraftRequest;
import pico.erp.bom.BomRequests.UpdateRequest;
import pico.erp.bom.BomRequests.VerifyByItemRequest;
import pico.erp.bom.BomRequests.VerifyByItemSpecRequest;
import pico.erp.bom.BomRequests.VerifyByMaterialRequest;
import pico.erp.bom.BomRequests.VerifyByProcessRequest;
import pico.erp.bom.BomRequests.VerifyRequest;
import pico.erp.item.ItemId;

public interface BomService {

  void delete(@NotNull @Valid DeleteRequest request);

  void determine(@NotNull @Valid DetermineRequest request);

  BomData draft(@NotNull @Valid DraftRequest request);

  boolean exists(@NotNull BomId id);

  boolean exists(@NotNull ItemId id);

  BomData get(@NotNull BomId id);

  BomData get(@NotNull ItemId id);

  BomData get(@NotNull ItemId id, @NotNull int revision);

  BomHierarchyData getHierarchy(@NotNull BomId id);

  BomHierarchyData getHierarchy(@NotNull ItemId id);

  void update(@Valid UpdateRequest request);

  void verify(@NotNull @Valid VerifyRequest request);

  void verify(@NotNull @Valid VerifyByProcessRequest request);

  void verify(@NotNull @Valid VerifyByItemSpecRequest request);

  void verify(@NotNull @Valid VerifyByItemRequest request);

  void verify(@NotNull @Valid VerifyByMaterialRequest request);
}
