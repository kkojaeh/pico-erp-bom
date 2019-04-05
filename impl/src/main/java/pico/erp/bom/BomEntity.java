package pico.erp.bom;


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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pico.erp.bom.unit.cost.BomUnitCostEmbeddable;
import pico.erp.item.ItemId;
import pico.erp.shared.TypeDefinitions;
import pico.erp.shared.data.Auditor;

@Entity(name = "Bom")
@Table(name = "BOM_BOM", indexes = {
  // revision 변경시 이전 예전 revision 이 insert 되기 때문에 unique 사용 불가
  @Index(columnList = "ITEM_ID, REVISION")
})
@Data
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomEntity implements Serializable {


  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "ID", length = TypeDefinitions.UUID_BINARY_LENGTH))
  })
  BomId id;

  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "ITEM_ID", length = TypeDefinitions.UUID_BINARY_LENGTH)),
  })
  ItemId itemId;

  @Column
  int revision;

  @Column(length = TypeDefinitions.ENUM_LENGTH)
  @Enumerated(EnumType.STRING)
  BomStatusKind status;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "total", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_TOTAL", scale = 2)),
    @AttributeOverride(name = "directLabor", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_DIRECT_LABOR", scale = 2)),
    @AttributeOverride(name = "indirectLabor", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_INDIRECT_LABOR", scale = 2)),
    @AttributeOverride(name = "directMaterial", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_DIRECT_MATERIAL", scale = 2)),
    @AttributeOverride(name = "indirectMaterial", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_INDIRECT_MATERIAL", scale = 2)),
    @AttributeOverride(name = "indirectExpenses", column = @Column(name = "ESTIMATED_ISOLATED_UNIT_COST_INDIRECT_EXPENSES", scale = 2))
  })
  BomUnitCostEmbeddable estimatedIsolatedUnitCost;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "total", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_TOTAL", scale = 2)),
    @AttributeOverride(name = "directLabor", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_DIRECT_LABOR", scale = 2)),
    @AttributeOverride(name = "indirectLabor", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_INDIRECT_LABOR", scale = 2)),
    @AttributeOverride(name = "directMaterial", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_DIRECT_MATERIAL", scale = 2)),
    @AttributeOverride(name = "indirectMaterial", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_INDIRECT_MATERIAL", scale = 2)),
    @AttributeOverride(name = "indirectExpenses", column = @Column(name = "ESTIMATED_ACCUMULATED_UNIT_COST_INDIRECT_EXPENSES", scale = 2))
  })
  BomUnitCostEmbeddable estimatedAccumulatedUnitCost;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "DETERMINED_BY_ID", length = TypeDefinitions.ID_LENGTH)),
    @AttributeOverride(name = "name", column = @Column(name = "DETERMINED_BY_NAME", length = TypeDefinitions.NAME_LENGTH))
  })
  Auditor determinedBy;

  @Column
  OffsetDateTime determinedDate;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "DRAFTED_BY_ID", length = TypeDefinitions.ID_LENGTH)),
    @AttributeOverride(name = "name", column = @Column(name = "DRAFTED_BY_NAME", length = TypeDefinitions.NAME_LENGTH))
  })
  Auditor draftedBy;

  @Column
  OffsetDateTime draftedDate;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "LAST_MODIFIED_BY_ID", length = TypeDefinitions.ID_LENGTH)),
    @AttributeOverride(name = "name", column = @Column(name = "LAST_MODIFIED_BY_NAME", length = TypeDefinitions.NAME_LENGTH))
  })
  @LastModifiedBy
  Auditor lastModifiedBy;

  OffsetDateTime lastModifiedDate;

  boolean stable;

  @Column(precision = 7, scale = 5)
  BigDecimal lossRate;

  @PrePersist
  private void onCreate() {
    lastModifiedDate = OffsetDateTime.now();
  }

  @PreUpdate
  private void onUpdate() {
    lastModifiedDate = OffsetDateTime.now();
  }

}
