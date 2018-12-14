package pico.erp.bom.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pico.erp.bom.BomId;
import pico.erp.process.ProcessId;
import pico.erp.shared.event.Event;

public interface BomProcessEvents {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class CreatedEvent implements Event {

    public final static String CHANNEL = "event.bom-process.created";

    private BomProcessId bomProcessId;

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class UpdatedEvent implements Event {

    public final static String CHANNEL = "event.bom-process.updated";

    private BomProcessId bomProcessId;

    private BomId bomId;

    public String channel() {
      return CHANNEL;
    }

  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class DeletedEvent implements Event {

    public final static String CHANNEL = "event.bom-process.deleted";

    private BomProcessId bomProcessId;

    private BomId bomId;

    private ProcessId processId;

    public String channel() {
      return CHANNEL;
    }

  }
}