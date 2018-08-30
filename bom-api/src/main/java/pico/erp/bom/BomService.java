package pico.erp.bom;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.bom.BomRequests.DeleteRequest;
import pico.erp.bom.BomRequests.DetermineRequest;
import pico.erp.bom.BomRequests.DraftRequest;
import pico.erp.bom.BomRequests.UpdateRequest;
import pico.erp.bom.data.BomData;
import pico.erp.bom.data.BomHierarchyData;
import pico.erp.bom.data.BomId;
import pico.erp.item.data.ItemId;

public interface BomService {

  void delete(@Valid DeleteRequest request);

  void determine(@Valid DetermineRequest request);

  BomData draft(@Valid DraftRequest request);

  boolean exists(@NotNull BomId id);

  boolean exists(@NotNull ItemId id);

  BomData get(@NotNull BomId id);

  BomData get(@NotNull ItemId id);

  BomData get(@NotNull ItemId id, @NotNull int revision);

  BomHierarchyData getHierarchy(@NotNull BomId id);

  void update(@Valid UpdateRequest request);

}
