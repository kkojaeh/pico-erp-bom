package pico.erp.bom;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import pico.erp.bom.BomHierarchyData.BomHierarchyDataBuilder;
import pico.erp.bom.unit.cost.BomUnitCostData;
import pico.erp.item.ItemId;
import pico.erp.item.spec.ItemSpecId;
import pico.erp.process.ProcessId;
import pico.erp.shared.data.Auditor;

@Getter
@NoArgsConstructor
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = BomHierarchyDataBuilder.class)
public class BomHierarchyData extends BomData {

  @Getter
  private List<BomHierarchyData> materials;

  public BomHierarchyData(BomData bom, List<BomHierarchyData> materials) {
    super(bom.getId(), bom.getItemId(), bom.getRevision(), bom.getStatus(), bom.getProcessId(),
      bom.getDeterminedBy(), bom.getDeterminedDate(), bom.getQuantity(),
      bom.getEstimatedIsolatedUnitCost(), bom.getEstimatedAccumulatedUnitCost(),
      bom.getItemSpecId(), bom.isSpecifiable(), bom.isMaterial(),
      bom.isModifiable(), bom.getParent(), bom.isStable());
    this.materials = materials;
  }

  @Builder
  public BomHierarchyData(BomId id, ItemId itemId, int revision,
    BomStatusKind status, ProcessId processId,
    Auditor determinedBy, OffsetDateTime determinedDate,
    BigDecimal quantity, BomUnitCostData estimatedIsolatedUnitCost,
    BomUnitCostData estimatedAccumulatedUnitCost, ItemSpecId itemSpecId,
    boolean specifiable, boolean material, boolean modifiable,
    BomData parent, List<BomHierarchyData> materials, boolean stable) {
    super(id, itemId, revision, status, processId, determinedBy, determinedDate, quantity,
      estimatedIsolatedUnitCost, estimatedAccumulatedUnitCost, itemSpecId, specifiable, material,
      modifiable, parent, stable);
    this.materials = materials;
  }

  public void visitInOrder(BomHierarchyVisitor visitor) {
    this.visitInOrder(visitor, 0);
  }

  private void visitInOrder(BomHierarchyVisitor visitor, int level) {
    visitor.visit(this, level);
    if (materials != null) {
      materials.forEach(material -> material.visitInOrder(visitor, level + 1));
    }
  }

  public void visitPostOrder(BomHierarchyVisitor visitor) {
    this.visitPostOrder(visitor, 0);
  }

  private void visitPostOrder(BomHierarchyVisitor visitor, int level) {
    if (materials != null) {
      materials.forEach(material -> material.visitPostOrder(visitor, level + 1));
    }
    visitor.visit(this, level);
  }

  public interface BomHierarchyVisitor {

    void visit(BomHierarchyData data, int level);
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class BomHierarchyDataBuilder {

  }
}
