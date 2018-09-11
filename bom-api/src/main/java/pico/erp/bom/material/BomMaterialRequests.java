package pico.erp.bom.material;

import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pico.erp.bom.data.BomId;
import pico.erp.item.data.ItemSpecId;

public interface BomMaterialRequests {


  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class DeleteRequest {

    @Valid
    @NotNull
    BomId bomId;

    @Valid
    @NotNull
    BomId materialId;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class CreateRequest {

    @Valid
    @NotNull
    BomId bomId;

    @Valid
    @NotNull
    BomId materialId;

    @NotNull
    @Min(0)
    BigDecimal quantity;

    @Valid
    ItemSpecId itemSpecId;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class UpdateRequest {

    @Valid
    @NotNull
    BomId bomId;

    @Valid
    @NotNull
    BomId materialId;

    @NotNull
    @Min(0)
    BigDecimal quantity;

    @Valid
    ItemSpecId itemSpecId;

  }

}
