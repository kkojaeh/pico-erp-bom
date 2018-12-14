package pico.erp.bom.process;

import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pico.erp.bom.BomId;
import pico.erp.process.ProcessId;

public interface BomProcessRequests {


  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class DeleteRequest {

    @Valid
    @NotNull
    BomProcessId id;

  }

  @Builder
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class CreateRequest {

    @Valid
    @NotNull
    BomProcessId id;

    @Valid
    @NotNull
    BomId bomId;

    @Valid
    @NotNull
    ProcessId processId;

    @NotNull
    @Min(0)
    BigDecimal conversionRate;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  class UpdateRequest {

    @Valid
    @NotNull
    BomProcessId id;

    @NotNull
    @Min(0)
    BigDecimal conversionRate;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class ChangeOrderRequest {

    @Valid
    @NotNull
    BomProcessId id;

    @NotNull
    @Min(0)
    Integer order;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  class NextRevisionRequest {

    @Valid
    @NotNull
    BomId previousId;

    @Valid
    @NotNull
    BomId nextRevisionId;

  }
}
