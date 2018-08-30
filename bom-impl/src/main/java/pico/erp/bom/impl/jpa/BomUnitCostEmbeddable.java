package pico.erp.bom.impl.jpa;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pico.erp.bom.domain.BomUnitCost;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomUnitCostEmbeddable {

  /**
   * 총 단가
   */
  @Column(scale = 2)
  BigDecimal total;


  /**
   * 직접 노무비
   */
  @Column(scale = 2)
  BigDecimal directLabor;

  /**
   * 간접 노무비
   */
  @Column(scale = 2)
  BigDecimal indirectLabor;

  /**
   * 간접 재료비
   */
  @Column(scale = 2)
  BigDecimal indirectMaterial;

  /**
   * 직접 재료 원가
   */
  @Column(scale = 2)
  BigDecimal directMaterial;

  /**
   * 간접 경비
   */
  @Column(scale = 2)
  BigDecimal indirectExpenses;

  public BomUnitCostEmbeddable() {
    total = BigDecimal.ZERO;
    directLabor = BigDecimal.ZERO;
    indirectLabor = BigDecimal.ZERO;
    indirectMaterial = BigDecimal.ZERO;
    directMaterial = BigDecimal.ZERO;
    indirectExpenses = BigDecimal.ZERO;
  }

  public BomUnitCostEmbeddable(BomUnitCost cost) {
    this();
    if (cost != null) {
      total = cost.getTotal();
      directLabor = cost.getDirectLabor();
      indirectLabor = cost.getIndirectLabor();
      indirectMaterial = cost.getIndirectMaterial();
      directMaterial = cost.getDirectMaterial();
      indirectExpenses = cost.getIndirectExpenses();
    }
  }

  public BomUnitCost to() {
    return BomUnitCost.builder()
      .total(total)
      .directLabor(directLabor)
      .indirectLabor(indirectLabor)
      .indirectMaterial(indirectMaterial)
      .directMaterial(directMaterial)
      .indirectExpenses(indirectExpenses)
      .build();

  }

}
