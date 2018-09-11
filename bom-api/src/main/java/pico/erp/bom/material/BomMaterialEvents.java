package pico.erp.bom.material;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pico.erp.bom.data.BomId;
import pico.erp.shared.event.Event;

public interface BomMaterialEvents {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class CreatedEvent implements Event {

    public final static String CHANNEL = "event.bom-material.created";

    private BomId bomId;

    private BomId materialId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class UpdatedEvent implements Event {

    public final static String CHANNEL = "event.bom-material.updated";

    private BomId bomId;

    private BomId materialId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class DeletedEvent implements Event {

    public final static String CHANNEL = "event.bom-material.deleted";

    private BomId bomId;

    private BomId materialId;

    public String channel() {
      return CHANNEL;
    }

  }
}
