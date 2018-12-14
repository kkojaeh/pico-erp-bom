package pico.erp.bom.unit.cost;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;
import pico.erp.bom.Bom.BomCalculateContext;
import pico.erp.bom.BomAggregator;
import pico.erp.bom.process.BomProcess;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomUnitCost implements Serializable {

  private static final long serialVersionUID = 1L;

  public static BomUnitCost ZERO = BomUnitCost.builder()
    .total(BigDecimal.ZERO)
    .directLabor(BigDecimal.ZERO)
    .indirectLabor(BigDecimal.ZERO)
    .indirectMaterial(BigDecimal.ZERO)
    .directMaterial(BigDecimal.ZERO)
    .indirectExpenses(BigDecimal.ZERO)
    .build();

  /**
   * 누적되지 않은 품목 자체에서만 발생되는 단가
   */
  BigDecimal total;

  /**
   * 직접 노무비
   */
  BigDecimal directLabor;

  /**
   * 간접 노무비
   */
  BigDecimal indirectLabor;

  /**
   * 간접 재료비
   */
  BigDecimal indirectMaterial;

  /**
   * 재료 원가
   */
  BigDecimal directMaterial;

  /**
   * 간접 경비
   */
  BigDecimal indirectExpenses;

  public BomUnitCost(BomAggregator bom) {
    directMaterial =
      bom.getItem() != null ? bom.getItem().getBaseUnitCost() : BigDecimal.ZERO;
    indirectMaterial = BigDecimal.ZERO;
    directLabor = BigDecimal.ZERO;
    indirectLabor = BigDecimal.ZERO;
    indirectExpenses = BigDecimal.ZERO;
    val processes = bom.getProcesses();
    val size = processes.size();
    for (int i = 0; i < size; i++) {
      val process = processes.get(i);
      val stackedConversionRate = processes.subList(0, i + 1).stream()
        .map(BomProcess::getConversionRate)
        .reduce(BigDecimal.ONE, (acc, curr) -> curr.multiply(acc))
        .setScale(5, BigDecimal.ROUND_HALF_UP);
      this.add(process, stackedConversionRate);
    }
    total = directMaterial.add(indirectMaterial)
      .add(directLabor).add(indirectLabor).add(indirectExpenses);
  }

  private void add(BomProcess process, BigDecimal stackedConversionRate) {
    val cost = process.getEstimatedCost();
    indirectMaterial = indirectMaterial
      .add(cost.getIndirectMaterial().multiply(stackedConversionRate));
    directLabor = directLabor.add(cost.getDirectLabor().multiply(stackedConversionRate));
    indirectLabor = indirectLabor.add(cost.getIndirectLabor().multiply(stackedConversionRate));
    indirectExpenses = indirectExpenses
      .add(cost.getIndirectExpenses().multiply(stackedConversionRate));
  }

  public BomUnitCost add(BomUnitCost unitCost, BigDecimal quantity) {
    return BomUnitCost.builder()
      .total(total.add(unitCost.getTotal().multiply(quantity)))
      .directLabor(directLabor.add(unitCost.getDirectLabor().multiply(quantity)))
      .indirectLabor(indirectLabor.add(unitCost.getIndirectLabor().multiply(quantity)))
      .indirectMaterial(indirectMaterial.add(unitCost.getIndirectMaterial().multiply(quantity)))
      .directMaterial(directMaterial.add(unitCost.getDirectMaterial().multiply(quantity)))
      .indirectExpenses(indirectExpenses.add(unitCost.getIndirectExpenses().multiply(quantity)))
      .build();
  }

  public BomUnitCost with(BomCalculateContext context) {
    BigDecimal directMaterial = this.directMaterial;
    BigDecimal total = this.total;
    if (context.getItemSpec() != null) {
      total = total.subtract(directMaterial);
      directMaterial = context.getItemSpec().getBaseUnitCost();
      total = total.add(directMaterial);
    }
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
