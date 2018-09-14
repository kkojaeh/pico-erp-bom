package pico.erp.bom;

import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import pico.erp.bom.data.BomId;
import pico.erp.item.data.ItemId;
import pico.erp.process.data.ProcessId;

public interface BomRepository {

  Bom create(@NotNull Bom bom);

  void deleteBy(@NotNull BomId id);

  boolean exists(@NotNull ItemId id);

  boolean exists(@NotNull BomId id);

  Stream<Bom> findAllBy(@NotNull ItemId itemId);

  Stream<Bom> findAllBy(@NotNull ProcessId id);

  Optional<Bom> findBy(@NotNull BomId id);

  Optional<BomAggregator> findAggregatorBy(@NotNull BomId id);

  Optional<Bom> findBy(@NotNull ItemId id, int revision);

  Optional<Bom> findWithLastRevision(@NotNull ItemId itemId);

  void update(@NotNull Bom bom);

}