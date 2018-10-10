package pico.erp.bom.material;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pico.erp.bom.BomEntity;
import pico.erp.bom.BomId;
import pico.erp.bom.material.BomMaterialEntity.BomMaterialKey;
import pico.erp.bom.unit.cost.BomUnitCostEmbeddable;
import pico.erp.item.spec.ItemSpecId;
import pico.erp.shared.TypeDefinitions;
import pico.erp.shared.data.Auditor;

@Builder
@Data
@Entity(name = "BomMaterial")
@Table(name = "BOM_BOM_MATERIAL")
@EqualsAndHashCode(of = {"bom", "material"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners({AuditingEntityListener.class, BomMaterialEntity.class})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BomMaterialKey.class)
public class BomMaterialEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @ManyToOne
  @JoinColumn(name = "MATERIAL_ID")
  BomEntity material;


  @Id
  @ManyToOne
  @JoinColumn(name = "BOM_ID")
  BomEntity bom;

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class BomMaterialKey implements Serializable {

    private static final long serialVersionUID = 1L;

    BomId bom;

    BomId material;

    public static BomMaterialKey from(BomId bomId, BomId materialId) {
      return new BomMaterialKey(bomId, materialId);
    }

    public static BomMaterialKey from(BomMaterial bomMaterial) {
      return new BomMaterialKey(bomMaterial.getBom().getId(), bomMaterial.getMaterial().getId());
    }

  }

  @Column(precision = 19, scale = 5)
  BigDecimal quantity;

  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "ITEM_SPEC_ID", length = TypeDefinitions.ID_LENGTH))
  })
  ItemSpecId itemSpecId;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "total", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_TOTAL")),
    @AttributeOverride(name = "directLabor", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_DIRECT_LABOR")),
    @AttributeOverride(name = "indirectLabor", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_INDIRECT_LABOR")),
    @AttributeOverride(name = "directMaterial", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_DIRECT_MATERIAL")),
    @AttributeOverride(name = "indirectMaterial", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_INDIRECT_MATERIAL")),
    @AttributeOverride(name = "indirectExpenses", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_INDIRECT_EXPENSES"))
  })
  BomUnitCostEmbeddable estimatedIsolatedUnitCost;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "total", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_TOTAL")),
    @AttributeOverride(name = "directLabor", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_DIRECT_LABOR")),
    @AttributeOverride(name = "indirectLabor", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_INDIRECT_LABOR")),
    @AttributeOverride(name = "directMaterial", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_DIRECT_MATERIAL")),
    @AttributeOverride(name = "indirectMaterial", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_INDIRECT_MATERIAL")),
    @AttributeOverride(name = "indirectExpenses", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_INDIRECT_EXPENSES"))
  })
  BomUnitCostEmbeddable estimatedAccumulatedUnitCost;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "CREATED_BY_ID", updatable = false, length = TypeDefinitions.ID_LENGTH)),
    @AttributeOverride(name = "name", column = @Column(name = "CREATED_BY_NAME", updatable = false, length = TypeDefinitions.NAME_LENGTH))
  })
  @CreatedBy
  Auditor createdBy;

  @CreatedDate
  @Column(updatable = false)
  OffsetDateTime createdDate;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "LAST_MODIFIED_BY_ID", length = TypeDefinitions.ID_LENGTH)),
    @AttributeOverride(name = "name", column = @Column(name = "LAST_MODIFIED_BY_NAME", length = TypeDefinitions.NAME_LENGTH))
  })
  @LastModifiedBy
  Auditor lastModifiedBy;

  @LastModifiedDate
  OffsetDateTime lastModifiedDate;

  /*
  @Builder
  public BomMaterialEntity(BomEntity bom, BigDecimal quantity, ItemSpecId itemSpecId,
    BomUnitCostEmbeddable estimatedIsolatedUnitCost
    , BomUnitCostEmbeddable estimatedAccumulatedUnitCost) {
    this.bom = bom;
    this.quantity = quantity;
    this.itemSpecIdValue = Optional.ofNullable(itemSpecId)
      .map(id -> id.getValue())
      .orElse(null);
    this.estimatedIsolatedUnitCost = estimatedIsolatedUnitCost;
    this.estimatedAccumulatedUnitCost = estimatedAccumulatedUnitCost;
  }

  public ItemSpecId getItemSpecId() {
    if (itemSpecIdValue != null) {
      return ItemSpecId.from(itemSpecIdValue);
    }
    return null;
  }
  */

}

