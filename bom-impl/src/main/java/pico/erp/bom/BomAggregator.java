package pico.erp.bom;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import pico.erp.bom.BomEvents.DeterminedEvent;
import pico.erp.bom.BomEvents.EstimatedUnitCostChangedEvent;
import pico.erp.bom.BomExceptions.CannotDetermineException;
import pico.erp.bom.data.BomId;
import pico.erp.bom.data.BomStatusKind;
import pico.erp.bom.material.BomMaterial;
import pico.erp.item.data.ItemData;
import pico.erp.process.data.ProcessData;
import pico.erp.shared.data.Auditor;
import pico.erp.shared.event.Event;

@Getter
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
public class BomAggregator extends Bom {

  List<BomMaterial> materials;

  @Builder(builderMethodName = "aggregatorBuilder")
  public BomAggregator(BomId id, int revision, ItemData itemData,
    BomStatusKind status, ProcessData processData,
    BomUnitCost estimatedIsolatedUnitCost,
    BomUnitCost estimatedAccumulatedUnitCost, Auditor determinedBy,
    OffsetDateTime determinedDate, Auditor draftedBy, OffsetDateTime draftedDate, boolean stable,
    List<BomMaterial> materials) {
    super(id, revision, itemData, status, processData, estimatedIsolatedUnitCost,
      estimatedAccumulatedUnitCost, determinedBy, determinedDate, draftedBy, draftedDate, stable);
    this.materials = materials;
  }


  public BomMessages.DetermineResponse apply(BomMessages.DetermineRequest request) {
    if (status != BomStatusKind.DRAFT) {
      throw new CannotDetermineException();
    }
    boolean unstable = materials.stream()
      .filter(material -> !material.getMaterial().isStable())
      .count() > 0;
    boolean processPlanned = processData != null ? processData.isPlanned() : true;

    if (unstable || !processPlanned) {
      throw new CannotDetermineException();
    }
    stable = true;
    status = BomStatusKind.DETERMINED;
    determinedBy = request.getDeterminedBy();
    determinedDate = OffsetDateTime.now();
    return new BomMessages.DetermineResponse(
      Arrays.asList(new DeterminedEvent(this.id))
    );
  }

  public BomMessages.VerifyResponse apply(BomMessages.VerifyRequest request) {
    if (isExpired()) {
      return new BomMessages.VerifyResponse(Collections.emptyList());
    }
    val events = new LinkedList<Event>();
    stable =
      materials.stream().filter(material -> !material.getMaterial().isStable()).count() == 0;
    if (this.canModify()) {
      val oldIsolated = estimatedIsolatedUnitCost;
      val oldAccumulated = estimatedAccumulatedUnitCost;
      val newIsolated = new BomUnitCost(this);
      BomUnitCost newAccumulated = new BomUnitCost(this);
      for (val material : materials) {
        newAccumulated = newAccumulated
          .add(material.getEstimatedAccumulatedUnitCost(), material.getQuantity());
      }
      if (!newIsolated.equals(oldIsolated) || !newAccumulated.equals(oldAccumulated)) {
        estimatedIsolatedUnitCost = newIsolated;
        estimatedAccumulatedUnitCost = newAccumulated;
        events.add(new EstimatedUnitCostChangedEvent(this.id));
      }
    }
    return new BomMessages.VerifyResponse(events);
  }

}
