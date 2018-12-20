package pico.erp.bom.process;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.bom.BomId;
import pico.erp.process.ProcessId;

public interface BomProcessService {

  void changeOrder(@Valid BomProcessRequests.ChangeOrderRequest request);

  BomProcessData create(@Valid BomProcessRequests.CreateRequest request);

  void delete(@Valid BomProcessRequests.DeleteRequest request);

  BomProcessData get(@NotNull BomProcessId id);

  List<BomProcessData> getAll(@NotNull BomId bomId);

  List<BomProcessData> getAll(@NotNull ProcessId processId);

  void update(@Valid BomProcessRequests.UpdateRequest request);

  void nextRevision(@Valid @NotNull BomProcessRequests.NextRevisionRequest request);

}
