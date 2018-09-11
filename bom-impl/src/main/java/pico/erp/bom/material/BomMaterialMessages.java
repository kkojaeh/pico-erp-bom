package pico.erp.bom.material;

import java.math.BigDecimal;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import pico.erp.bom.Bom;
import pico.erp.item.data.ItemSpecData;
import pico.erp.shared.event.Event;

public interface BomMaterialMessages {

  @Data
  class CreateRequest {

    Bom bom;

    Bom material;

    BigDecimal quantity;

    ItemSpecData itemSpecData;

  }

  @Value
  class CreateResponse {

    Collection<Event> events;

  }

  @Data
  class DeleteRequest {

  }

  @Value
  class DeleteResponse {

    Collection<Event> events;

  }

  @Data
  class UpdateRequest {

    BigDecimal quantity;

    ItemSpecData itemSpecData;

  }

  @Value
  class UpdateResponse {


    Collection<Event> events;

  }

  @Data
  @AllArgsConstructor
  class NextRevisionRequest {

    @Valid
    @NotNull
    Bom drafted;

    @Valid
    @NotNull
    Bom lastRevisionMaterial;

  }

  @Value
  class NextRevisionResponse {

    BomMaterial drafted;

    Collection<Event> events;

  }

  @Data
  @AllArgsConstructor
  class SwapRequest {

    @Valid
    @NotNull
    Bom material;

  }

  @Value
  class SwapResponse {

    BomMaterial swapped;

    Collection<Event> events;

  }

}
