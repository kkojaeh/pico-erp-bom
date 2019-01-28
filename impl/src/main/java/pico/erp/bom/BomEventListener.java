package pico.erp.bom;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pico.erp.bom.BomEvents.EstimatedUnitCostChangedEvent;
import pico.erp.bom.material.BomMaterialEvents;
import pico.erp.bom.material.BomMaterialService;
import pico.erp.item.ItemEvents;
import pico.erp.item.spec.ItemSpecEvents;
import pico.erp.item.spec.ItemSpecRequests;
import pico.erp.item.spec.ItemSpecService;
import pico.erp.process.ProcessEvents;
import pico.erp.process.ProcessService;

@SuppressWarnings("unused")
@Component
@Transactional
public class BomEventListener {

  private static final String LISTENER_NAME = "listener.bom-event-listener";

  @Lazy
  @Autowired
  private ItemSpecService itemSpecService;

  @Lazy
  @Autowired
  private BomMaterialService bomMaterialService;

  @Lazy
  @Autowired
  private BomServiceLogic bomService;

  @Lazy
  @Autowired
  private ProcessService processService;

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.CreatedEvent.CHANNEL)
  public void onBomCreated(BomEvents.CreatedEvent event) {
    bomService.verify(
      BomRequests.VerifyRequest.builder()
        .id(event.getBomId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.DeletedEvent.CHANNEL)
  public void onBomDeleted(BomEvents.DeletedEvent event) {
    bomService.verify(
      BomRequests.VerifyRequest.builder()
        .id(event.getBomId())
        .build()
    );
  }

  /**
   * BOM 이 확정 되면 하위 BOM 과 연결된 품목 스펙을 잠금
   */
  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.DeterminedEvent.CHANNEL)
  public void onBomDetermined(BomEvents.DeterminedEvent event) {
    bomMaterialService.getAll(event.getBomId())
      .forEach(bomMaterial -> {
        val itemSpecId = bomMaterial.getItemSpecId();
        if (itemSpecId != null) {
          val itemSpec = itemSpecService.get(itemSpecId);
          if (itemSpec != null && !itemSpec.isLocked()) {
            itemSpecService.lock(
              new ItemSpecRequests.LockRequest(itemSpec.getId())
            );
          }
        }
      });
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + EstimatedUnitCostChangedEvent.CHANNEL)
  public void onBomEstimatedUnitCostChanged(EstimatedUnitCostChangedEvent event) {
    bomService.verify(
      BomRequests.VerifyByMaterialRequest.builder()
        .materialId(event.getBomId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomMaterialEvents.CreatedEvent.CHANNEL)
  public void onBomMaterialCreated(BomMaterialEvents.CreatedEvent event) {
    bomService.verify(
      BomRequests.VerifyRequest.builder()
        .id(event.getBomId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomMaterialEvents.UpdatedEvent.CHANNEL)
  public void onBomMaterialUpdated(BomMaterialEvents.UpdatedEvent event) {
    bomService.verify(
      BomRequests.VerifyRequest.builder()
        .id(event.getBomId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomMaterialEvents.DeletedEvent.CHANNEL)
  public void onBomMaterialUpdated(BomMaterialEvents.DeletedEvent event) {
    bomService.verify(
      BomRequests.VerifyRequest.builder()
        .id(event.getBomId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ProcessEvents.CreatedEvent.CHANNEL)
  public void onProcessCreated(ProcessEvents.CreatedEvent event) {
    val process = processService.get(event.getProcessId());
    val itemId = process.getItemId();
    if (bomService.exists(itemId)) {
      val bom = bomService.get(itemId);
      bomService.verify(
        BomRequests.VerifyRequest.builder()
          .id(bom.getId())
          .build()
      );
    }
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ProcessEvents.UpdatedEvent.CHANNEL)
  public void onProcessUpdated(ProcessEvents.UpdatedEvent event) {
    val process = processService.get(event.getProcessId());
    val itemId = process.getItemId();
    if (bomService.exists(itemId)) {
      val bom = bomService.get(itemId);
      bomService.verify(
        BomRequests.VerifyRequest.builder()
          .id(bom.getId())
          .build()
      );
    }
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ProcessEvents.DeletedEvent.CHANNEL)
  public void onProcessUpdated(ProcessEvents.DeletedEvent event) {
    val process = processService.get(event.getProcessId());
    val itemId = process.getItemId();
    if (bomService.exists(itemId)) {
      val bom = bomService.get(itemId);
      bomService.verify(
        BomRequests.VerifyRequest.builder()
          .id(bom.getId())
          .build()
      );
    }
  }

  /**
   * BOM 이 새버전이되면 하위 BOM 과 연결된 품목 스펙을 잠금해제
   */
  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.NextRevisionCreatedEvent.CHANNEL)
  public void onBomNextRevisionCreated(BomEvents.NextRevisionCreatedEvent event) {
    bomMaterialService.getAll(event.getBomId())
      .forEach(bomMaterial -> {
        val itemSpecId = bomMaterial.getItemSpecId();
        if (itemSpecId != null) {
          val itemSpec = itemSpecService.get(itemSpecId);
          if (itemSpec != null && itemSpec.isLocked()) {
            itemSpecService.unlock(
              new ItemSpecRequests.UnlockRequest(itemSpec.getId())
            );
          }
        }
      });
    bomService.verify(
      BomRequests.VerifyRequest.builder()
        .id(event.getBomId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.UpdatedEvent.CHANNEL)
  public void onBomUpdated(BomEvents.UpdatedEvent event) {
    bomService.verify(
      BomRequests.VerifyRequest.builder()
        .id(event.getBomId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ItemSpecEvents.UpdatedEvent.CHANNEL)
  public void onItemSpecUpdated(ItemSpecEvents.UpdatedEvent event) {
    bomService.verify(
      BomRequests.VerifyByItemSpecRequest.builder()
        .itemSpecId(event.getItemSpecId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ItemEvents.UpdatedEvent.CHANNEL)
  public void onItemUpdated(ItemEvents.UpdatedEvent event) {
    bomService.verify(
      BomRequests.VerifyByItemRequest.builder()
        .itemId(event.getItemId())
        .build()
    );
  }


}
