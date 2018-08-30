package pico.erp.bom;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.bom.data.BomData;
import pico.erp.bom.data.BomId;

public interface BomMaterialService {

  BomData create(@Valid BomMaterialRequests.CreateRequest request);

  void delete(@Valid BomMaterialRequests.DeleteRequest request);

  BomData get(@NotNull BomId bomId, @NotNull BomId materialId);

  List<BomData> getAll(@NotNull BomId bomId);

  void update(@Valid BomMaterialRequests.UpdateRequest request);

}
