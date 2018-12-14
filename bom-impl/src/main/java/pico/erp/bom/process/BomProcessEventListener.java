package pico.erp.bom.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import pico.erp.bom.BomRequests;
import pico.erp.bom.BomService;
import pico.erp.process.ProcessEvents;
import pico.erp.process.ProcessRequests;
import pico.erp.process.ProcessService;

@SuppressWarnings("unused")
@Component
public class BomProcessEventListener {

  private static final String LISTENER_NAME = "listener.bom-process-event-listener";

  @Lazy
  @Autowired
  private ProcessService processService;

  @Lazy
  @Autowired
  private BomService bomService;

  @Lazy
  @Autowired
  private BomProcessService bomProcessService;

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomProcessEvents.DeletedEvent.CHANNEL)
  public void onBomProcessDeleted(BomProcessEvents.DeletedEvent event) {
    processService.delete(
      ProcessRequests.DeleteRequest.builder()
        .id(event.getProcessId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ProcessEvents.EstimatedCostChangedEvent.CHANNEL)
  public void onProcessEstimatedCostChanged(ProcessEvents.EstimatedCostChangedEvent event) {
    bomProcessService.getAll(event.getProcessId())
      .forEach(bomProcess -> bomService.verify(
        BomRequests.VerifyRequest.builder()
          .id(bomProcess.getBomId())
          .build()
      ));
  }


}
