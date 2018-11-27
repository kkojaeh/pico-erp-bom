package pico.erp.bom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import pico.erp.bom.unit.cost.BomUnitCostData;
import pico.erp.item.ItemId;
import pico.erp.item.spec.ItemSpecId;
import pico.erp.process.ProcessId;
import pico.erp.shared.data.Auditor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class BomData {

  BomId id;

  ItemId itemId;

  int revision;

  BomStatusKind status;

  ProcessId processId;

  Auditor determinedBy;

  OffsetDateTime determinedDate;

  BigDecimal quantity;

  BigDecimal lossRate;

  BomUnitCostData estimatedIsolatedUnitCost;

  BomUnitCostData estimatedAccumulatedUnitCost;

  ItemSpecId itemSpecId;

  boolean specifiable;

  boolean material;

  boolean modifiable;

  @JsonIgnore
  BomData parent;

  boolean stable;

  public BigDecimal getQuantityRatio() {
    if (parent != null) {
      return quantity.multiply(parent.getQuantityRatio());
    } else {
      return quantity;
    }
  }

  public BigDecimal getSpareRatio() {
    if (parent != null) {
      val one = BigDecimal.ONE;
      return parent.getLossRate().add(one).multiply(parent.getSpareRatio().add(one)).subtract(one);
    } else {
      return BigDecimal.ZERO;
    }
  }


}
