package pico.erp.bom;

import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pico.erp.bom.data.BomId;
import pico.erp.item.data.ItemId;
import pico.erp.item.data.ItemSpecId;
import pico.erp.process.data.ProcessId;

public interface BomRequests {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class DetermineRequest {

    @Valid
    @NotNull
    BomId id;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class DeleteRequest {

    @Valid
    @NotNull
    BomId id;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class DraftRequest {

    @Valid
    @NotNull
    BomId id;

    @Valid
    @NotNull
    ItemId itemId;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  class MaterialRequestData {

    @Valid
    @NotNull
    BomId id;

    @NotNull
    @Min(0)
    BigDecimal quantity;

    @Valid
    ItemSpecId itemSpecId;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  class UpdateRequest {

    @Valid
    @NotNull
    BomId id;

    @Valid
    ProcessId processId;

  }
}
