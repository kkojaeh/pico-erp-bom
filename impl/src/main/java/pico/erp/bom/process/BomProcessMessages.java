package pico.erp.bom.process;

import java.math.BigDecimal;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import pico.erp.bom.Bom;
import pico.erp.process.ProcessData;
import pico.erp.shared.event.Event;

public interface BomProcessMessages {

  @Data
  class CreateRequest {

    BomProcessId id;

    Bom bom;

    ProcessData process;

    BigDecimal conversionRate;

    int order;

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

    BigDecimal conversionRate;

  }

  @Data
  class ChangeOrderRequest {

    int order;

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


  }

  @Value
  class NextRevisionResponse {

    BomProcess drafted;

    Collection<Event> events;

  }

  @Value
  class ChangeOrderResponse {

    Collection<Event> events;

  }


}
