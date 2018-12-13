package pico.erp.bom;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pico.erp.item.ItemId;
import pico.erp.item.spec.ItemSpecId;
import pico.erp.process.ProcessId;

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

  @Builder
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
  class UpdateRequest {

    public UpdateRequest(BomData data) {
      this.id = data.getId();
      this.processId = data.getProcessId();
    }

    @Valid
    @NotNull
    BomId id;

    @Valid
    ProcessId processId;

  }

  @Getter
  @Builder
  class DeleteProcessRequest {

    @Valid
    @NotNull
    ProcessId processId;

  }

  @Getter
  @Builder
  class VerifyByProcessRequest {

    @Valid
    @NotNull
    ProcessId processId;

  }

  @Getter
  @Builder
  class VerifyByItemSpecRequest {

    @Valid
    @NotNull
    ItemSpecId itemSpecId;

  }

  @Getter
  @Builder
  class VerifyByItemRequest {

    @Valid
    @NotNull
    ItemId itemId;

  }

  @Getter
  @Builder
  class VerifyByMaterialRequest {

    @Valid
    @NotNull
    BomId materialId;

  }

  @Getter
  @Builder
  class VerifyRequest {

    @Valid
    @NotNull
    BomId id;

  }
}
