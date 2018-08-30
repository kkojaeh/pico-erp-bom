package pico.erp.bom.core;

import java.util.LinkedList;
import java.util.stream.Collectors;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pico.erp.bom.BomExceptions.NotFoundException;
import pico.erp.bom.BomRequests.DeleteRequest;
import pico.erp.bom.BomRequests.DetermineRequest;
import pico.erp.bom.BomRequests.DraftRequest;
import pico.erp.bom.BomRequests.UpdateRequest;
import pico.erp.bom.BomService;
import pico.erp.bom.data.BomData;
import pico.erp.bom.data.BomHierarchyData;
import pico.erp.bom.data.BomId;
import pico.erp.bom.domain.Bom;
import pico.erp.bom.domain.BomMaterialMessages;
import pico.erp.item.data.ItemId;
import pico.erp.shared.Public;
import pico.erp.shared.event.Event;
import pico.erp.shared.event.EventPublisher;

/**
 * determine 하게 되면 draft 중인 품목은 활성화 된다
 */
@Service
@Public
@Transactional
@Validated
public class BomServiceLogic implements BomService {

  @Autowired
  private BomRepository bomRepository;

  @Autowired
  private BomMaterialRepository bomMaterialRepository;

  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private BomMapper mapper;

  @Override
  public void delete(DeleteRequest request) {
    val bom = bomRepository.findBy(request.getId())
      .orElseThrow(NotFoundException::new);
    val response = bom.apply(mapper.map(request));
    bomRepository.deleteBy(request.getId());
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public void determine(DetermineRequest request) {
    val aggregator = bomRepository.findAggregatorBy(request.getId())
      .orElseThrow(NotFoundException::new);
    val response = aggregator.apply(mapper.map(request));
    bomRepository.update(aggregator);
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public BomData draft(DraftRequest request) {
    val bom = new Bom();
    val response = bom.apply(mapper.map(request));
    val created = bomRepository.create(bom);
    val previous = response.getPrevious();
    val events = new LinkedList<Event>();
    events.addAll(response.getEvents());
    if (previous != null) {
      bomRepository.update(previous);
      // 새로 생성된 BOM 에 기존 BOM 의 자재를 동일하게 생성
      bomMaterialRepository.findAllBy(previous.getId())
        .forEach(material -> {
          val nextRevisionResponse = material.apply(new BomMaterialMessages.NextRevisionRequest(
            created,
            bomRepository.findWithLastRevision(material.getMaterial().getItemData().getId()).get()
          ));
          val draftedMaterial = nextRevisionResponse.getDrafted();
          bomMaterialRepository.create(draftedMaterial);
          events.addAll(nextRevisionResponse.getEvents());

        });
      // 새로 생성된 BOM 을 참조하고 있는 BOM 의 새버전을 생성하거나 해당 자재만 교체
      bomMaterialRepository.findAllReferencedBy(previous.getId())
        .forEach(referenced -> {
          if (referenced.isDetermined()) {
            this.draft(new DraftRequest(BomId.generate(), referenced.getItemData().getId()));
          } else {
            val oldMaterial = bomMaterialRepository.findBy(referenced.getId(), previous.getId())
              .get();
            val swapResponse = oldMaterial.apply(new BomMaterialMessages.SwapRequest(created));
            bomMaterialRepository.create(swapResponse.getSwapped());
            bomMaterialRepository.deleteBy(oldMaterial);
            events.addAll(swapResponse.getEvents());
          }
        });
    }
    eventPublisher.publishEvents(events);
    return mapper.map(created);
  }

  @Override
  public boolean exists(BomId id) {
    return bomRepository.exists(id);
  }

  @Override
  public boolean exists(ItemId id) {
    return bomRepository.exists(id);
  }

  @Override
  public BomData get(BomId id) {
    return bomRepository.findBy(id)
      .map(mapper::map)
      .orElseThrow(NotFoundException::new);
  }

  @Override
  public BomData get(ItemId id) {
    return bomRepository.findWithLastRevision(id)
      .map(mapper::map)
      .orElseThrow(NotFoundException::new);
  }

  @Override
  public BomData get(ItemId id, int revision) {
    return bomRepository.findBy(id, revision)
      .map(mapper::map)
      .orElseThrow(NotFoundException::new);
  }

  @Override
  public BomHierarchyData getHierarchy(BomId id) {
    val materials = bomMaterialRepository.findAllBy(id)
      .map(mapper::map)
      .map(material -> this.getHierarchy(material))
      .collect(Collectors.toList());
    return new BomHierarchyData(get(id), materials);
  }

  private BomHierarchyData getHierarchy(BomData bom) {
    val materials = bomMaterialRepository.findAllBy(bom.getId())
      .map(mapper::map)
      .map(material -> this.getHierarchy(material))
      .collect(Collectors.toList());
    return new BomHierarchyData(bom, materials);
  }


  @Override
  public void update(UpdateRequest request) {
    val bom = bomRepository.findBy(request.getId())
      .orElseThrow(NotFoundException::new);
    val response = bom.apply(mapper.map(request));
    bomRepository.update(bom);
    eventPublisher.publishEvents(response.getEvents());
  }

}
