package pico.erp.bom;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Stack;
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
    super(bom.getId(), bom.getItemId(), bom.getRevision(), bom.getStatus(),
      bom.getDeterminedBy(), bom.getDeterminedDate(), bom.getQuantity(),
      bom.getEstimatedIsolatedUnitCost(), bom.getEstimatedAccumulatedUnitCost(),
      bom.getItemSpecId(), bom.isSpecifiable(), bom.isMaterial(),
      bom.isUpdatable(), bom.getOrder(), bom.getParent(), bom.isStable(), bom.getLossRate());
    this.materials = materials;
    this.materials.forEach(m -> m.setParent(this));
  }

  @Builder
  public BomHierarchyData(BomId id, ItemId itemId, int revision, BomStatusKind status,
    Auditor determinedBy, LocalDateTime determinedDate, BigDecimal quantity,
    BomUnitCostData estimatedIsolatedUnitCost,
    BomUnitCostData estimatedAccumulatedUnitCost, ItemSpecId itemSpecId, boolean specifiable,
    boolean material, boolean updatable, int order, BomData parent, boolean stable,
    BigDecimal lossRate, List<BomHierarchyData> materials) {
    super(id, itemId, revision, status, determinedBy, determinedDate, quantity,
      estimatedIsolatedUnitCost, estimatedAccumulatedUnitCost, itemSpecId, specifiable, material,
      updatable, order, parent, stable, lossRate);
    this.materials = materials;
    this.materials.forEach(m -> m.setParent(this));
  }

  public void visitInOrder(BomHierarchyVisitor visitor) {
    this.visitInOrder(visitor, new Stack<>());
  }

  private void visitInOrder(BomHierarchyVisitor visitor, Stack<BomHierarchyData> parents) {
    visitor.visit(this, parents);
    if (materials != null) {
      parents.push(this);
      materials.forEach(material -> material.visitInOrder(visitor, parents));
      parents.pop();
    }
  }

  public void visitPostOrder(BomHierarchyVisitor visitor) {
    this.visitPostOrder(visitor, new Stack<>());
  }

  private void visitPostOrder(BomHierarchyVisitor visitor, Stack<BomHierarchyData> parents) {
    if (materials != null) {
      parents.push(this);
      materials.forEach(material -> material.visitPostOrder(visitor, parents));
      parents.pop();
    }
    visitor.visit(this, parents);
  }

  public interface BomHierarchyVisitor {

    void visit(BomHierarchyData data, Stack<BomHierarchyData> parents);
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class BomHierarchyDataBuilder {

  }
}
