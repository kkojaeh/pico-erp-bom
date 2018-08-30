package pico.erp.bom.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pico.erp.item.data.ItemId;
import pico.erp.item.data.ItemSpecId;
import pico.erp.process.data.ProcessId;
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

  BomUnitCostData estimatedIsolatedUnitCost;

  BomUnitCostData estimatedAccumulatedUnitCost;

  ItemSpecId itemSpecId;

  boolean specifiable;

  boolean material;

  boolean modifiable;

  @JsonIgnore
  BomData parent;

  boolean stable;

  public BigDecimal getQuantityPerRoot() {
    if (parent != null) {
      return this.getQuantity().multiply(parent.getQuantityPerRoot());
    } else {
      return this.getQuantity();
    }
  }


}
