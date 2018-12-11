package pico.erp.bom.material;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.bom.BomData;
import pico.erp.bom.BomId;

public interface BomMaterialService {

  BomData create(@Valid BomMaterialRequests.CreateRequest request);

  void delete(@Valid BomMaterialRequests.DeleteRequest request);

  BomData get(@NotNull BomId bomId, @NotNull BomId materialId);

  List<BomData> getAll(@NotNull BomId bomId);

  void update(@Valid BomMaterialRequests.UpdateRequest request);

  void changeOrder(@Valid BomMaterialRequests.ChangeOrderRequest request);

}
