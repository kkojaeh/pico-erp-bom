package pico.erp.bom;

import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import pico.erp.item.ItemData;
import pico.erp.process.ProcessData;
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
    ItemData item;

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
    ProcessData process;

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
