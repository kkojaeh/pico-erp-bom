package pico.erp.bom;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pico.erp.bom.BomEvents.EstimatedUnitCostChangedEvent;
import pico.erp.bom.BomRequests.DraftRequest;
import pico.erp.bom.material.BomMaterialEvents;
import pico.erp.bom.material.BomMaterialRepository;
import pico.erp.item.ItemEvents;
import pico.erp.item.spec.ItemSpecEvents;
import pico.erp.item.spec.ItemSpecRequests;
import pico.erp.item.spec.ItemSpecService;
import pico.erp.process.ProcessEvents;
import pico.erp.shared.event.EventPublisher;

@SuppressWarnings("unused")
@Component
@Transactional
public class BomEventListener {

  private static final String LISTENER_NAME = "listener.bom-event-listener";

  @Autowired
  private BomRepository bomRepository;

  @Autowired
  private BomMaterialRepository bomMaterialRepository;

  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private BomMapper bomMapper;

  @Lazy
  @Autowired
  private ItemSpecService itemSpecService;

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.CreatedEvent.CHANNEL)
  public void onBomCreated(BomEvents.CreatedEvent event) {
    this.verifyBom(event.getBomId());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + EstimatedUnitCostChangedEvent.CHANNEL)
  public void onBomEstimatedUnitCostChanged(EstimatedUnitCostChangedEvent event) {
    bomMaterialRepository.findAllReferencedBy(event.getBomId())
      .forEach(this::verifyBom);
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomMaterialEvents.CreatedEvent.CHANNEL)
  public void onBomMaterialCreated(BomMaterialEvents.CreatedEvent event) {
    this.verifyBom(event.getBomId());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomMaterialEvents.UpdatedEvent.CHANNEL)
  public void onBomMaterialUpdated(BomMaterialEvents.UpdatedEvent event) {
    this.verifyBom(event.getBomId());
  }

  /**
   * BOM 이 확정 되면 하위 BOM 과 연결된 품목 스펙을 잠금
   */
  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.DeterminedEvent.CHANNEL)
  public void onBomDetermined(BomEvents.DeterminedEvent event) {
    bomMaterialRepository.findAllBy(event.getBomId())
      .forEach(bomMaterial -> {
        val itemSpec = bomMaterial.getItemSpec();
        if (itemSpec != null && !itemSpec.isLocked()) {
          itemSpecService.lock(
            new ItemSpecRequests.LockRequest(itemSpec.getId())
          );
        }
      });
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomMaterialEvents.DeletedEvent.CHANNEL)
  public void onBomMaterialUpdated(BomMaterialEvents.DeletedEvent event) {
    this.verifyBom(event.getBomId());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ProcessEvents.DeletedEvent.CHANNEL)
  public void onProcessDeleted(ProcessEvents.DeletedEvent event) {
    bomRepository.findAllBy(event.getProcessId())
      .forEach(bom -> {
        if (!bom.isExpired()) {
          if (bom.canModify()) {
            val response = bom.apply(
              BomMessages.UpdateRequest.builder()
                .process(null)
                .build()
            );
            bomRepository.update(bom);
            eventPublisher.publishEvents(response.getEvents());
          } else {
            val nextRevision = new Bom();
            val response = nextRevision.apply(
              bomMapper.map(
                new DraftRequest(BomId.generate(), bom.getItem().getId())
              )
            );
            bomRepository.create(nextRevision);
            if (response.getPrevious() != null) {
              bomRepository.update(response.getPrevious());
            }
            eventPublisher.publishEvents(response.getEvents());
          }
        }
      });
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.UpdatedEvent.CHANNEL)
  public void onBomUpdated(BomEvents.UpdatedEvent event) {
    this.verifyBom(event.getBomId());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.DeletedEvent.CHANNEL)
  public void onBomDeleted(BomEvents.DeletedEvent event) {
    this.verifyBom(event.getBomId());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ItemSpecEvents.UpdatedEvent.CHANNEL)
  public void onItemSpecUpdated(ItemSpecEvents.UpdatedEvent event) {
    bomMaterialRepository.findBy(event.getItemSpecId())
      .ifPresent(material -> this.verifyBom(material.getBom()));
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ItemEvents.UpdatedEvent.CHANNEL)
  public void onItemUpdated(ItemEvents.UpdatedEvent event) {
    bomRepository.findAllBy(event.getItemId())
      .forEach(this::verifyBom);
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + ProcessEvents.EstimatedCostChangedEvent.CHANNEL)
  public void onProcessEstimatedCostChanged(ProcessEvents.EstimatedCostChangedEvent event) {
    bomRepository.findAllBy(event.getProcessId())
      .forEach(this::verifyBom);
  }

  protected void verifyBom(BomId bomId) {
    this.verifyBom(
      bomRepository.findBy(bomId)
        .orElseThrow(BomExceptions.NotFoundException::new)
    );
  }

  protected void verifyBom(Bom bom) {
    if (!bom.isExpired()) {
      bomRepository.findAggregatorBy(bom.getId())
        .ifPresent(aggregator -> {
          val response = aggregator.apply(new BomMessages.VerifyRequest());
          bomRepository.update(aggregator);
          eventPublisher.publishEvents(response.getEvents());
        });
    }
  }

  /**
   * BOM 이 새버전이되면 하위 BOM 과 연결된 품목 스펙을 잠금해제
   * @param event
   */
  @EventListener
  @JmsListener(destination = LISTENER_NAME + "." + BomEvents.NextRevisionCreatedEvent.CHANNEL)
  public void onBomNextRevisionCreated(BomEvents.NextRevisionCreatedEvent event) {
    bomMaterialRepository.findAllBy(event.getBomId())
      .forEach(bomMaterial -> {
        val itemSpec = bomMaterial.getItemSpec();
        if (itemSpec != null && itemSpec.isLocked()) {
          itemSpecService.unlock(
            new ItemSpecRequests.UnlockRequest(itemSpec.getId())
          );
        }
      });
    this.verifyBom(event.getBomId());
  }

}
