package pico.erp.bom.process;

import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import pico.erp.bom.BomId;
import pico.erp.process.ProcessId;

public interface BomProcessRepository {

  long countBy(@NotNull BomId bomId);

  BomProcess create(@NotNull BomProcess bomProcess);

  void deleteBy(@NotNull BomProcessId id);

  boolean exists(@NotNull BomProcessId id);

  boolean exists(@NotNull BomId bomId, @NotNull ProcessId processId);

  Stream<BomProcess> findAllBy(@NotNull ProcessId processId);

  Stream<BomProcess> findAllBy(@NotNull BomId bomId);

  Optional<BomProcess> findBy(@NotNull BomProcessId id);

  void update(@NotNull BomProcess bomProcess);


}
