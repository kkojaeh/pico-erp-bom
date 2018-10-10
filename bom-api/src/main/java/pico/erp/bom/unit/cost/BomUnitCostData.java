package pico.erp.bom.unit.cost;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomUnitCostData {


  /**
   * 총 단가
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
   * 직접 재료 원가
   */
  BigDecimal directMaterial;

  /**
   * 간접 경비
   */
  BigDecimal indirectExpenses;


}
