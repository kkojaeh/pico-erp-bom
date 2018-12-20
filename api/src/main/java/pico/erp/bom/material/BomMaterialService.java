package pico.erp.bom.material;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.bom.BomData;
import pico.erp.bom.BomId;

public interface BomMaterialService {

  void changeOrder(@Valid @NotNull BomMaterialRequests.ChangeOrderRequest request);

  BomData create(@Valid @NotNull BomMaterialRequests.CreateRequest request);

  BomData get(@NotNull BomId bomId, @NotNull BomId materialId);

  List<BomData> getAll(@NotNull BomId bomId);

  void delete(@Valid @NotNull BomMaterialRequests.DeleteRequest request);

  void nextRevision(@Valid @NotNull BomMaterialRequests.NextRevisionRequest request);

  void update(@Valid @NotNull BomMaterialRequests.UpdateRequest request);

}
