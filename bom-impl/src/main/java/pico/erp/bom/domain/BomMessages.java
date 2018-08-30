package pico.erp.bom.domain;

import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import pico.erp.bom.data.BomId;
import pico.erp.item.data.ItemData;
import pico.erp.process.data.ProcessData;
import pico.erp.shared.data.Auditor;
import pico.erp.shared.event.Event;

public interface BomMessages {

  @Data
  class DetermineRequest {

    Auditor determinedBy;

  }

  @Data
  class DeleteRequest {

  }

  @Data
  class DraftRequest {

    @Valid
    @NotNull
    BomId id;

    @Valid
    @NotNull
    ItemData itemData;

    Bom lastRevision;

    Auditor draftedBy;

  }

  @AllArgsConstructor
  @Data
  class NextRevisionRequest {

    @Valid
    @NotNull
    Bom drafted;

    Auditor draftedBy;

  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  class UpdateRequest {

    @Valid
    ProcessData processData;

  }

  @Value
  class DetermineResponse {

    Collection<Event> events;

  }

  @Value
  class DeleteResponse {

    Collection<Event> events;

  }

  @Value
  class DraftResponse {

    Bom previous;

    Collection<Event> events;

  }

  @Value
  class NextRevisionResponse {

    Collection<Event> events;

  }



  @Value
  class UpdateResponse {

    Collection<Event> events;

  }


  @Data
  @NoArgsConstructor
  class VerifyRequest {

  }

  @Value
  class VerifyResponse {

    Collection<Event> events;

  }


}
