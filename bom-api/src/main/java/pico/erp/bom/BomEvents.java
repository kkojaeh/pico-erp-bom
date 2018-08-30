package pico.erp.bom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pico.erp.bom.data.BomId;
import pico.erp.shared.event.Event;

public interface BomEvents {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class DeterminedEvent implements Event {

    public final static String CHANNEL = "event.bom.determined";

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class EstimatedUnitCostChangedEvent implements Event {

    public final static String CHANNEL = "event.bom.estimated-unit-cost-changed";

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class NextRevisionCreatedEvent implements Event {

    public final static String CHANNEL = "event.bom.next-revision-created";

    private BomId previousId;

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class CreatedEvent implements Event {

    public final static String CHANNEL = "event.bom.created";

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class UpdatedEvent implements Event {

    public final static String CHANNEL = "event.bom.updated";

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class DeletedEvent implements Event {

    public final static String CHANNEL = "event.bom.deleted";

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }
}
