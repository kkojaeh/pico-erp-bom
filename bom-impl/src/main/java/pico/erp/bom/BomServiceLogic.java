package pico.erp.bom;

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
import pico.erp.bom.BomRequests.VerifyByItemRequest;
import pico.erp.bom.BomRequests.VerifyByItemSpecRequest;
import pico.erp.bom.BomRequests.VerifyByMaterialRequest;
import pico.erp.bom.BomRequests.VerifyRequest;
import pico.erp.bom.material.BomMaterialMapper;
import pico.erp.bom.material.BomMaterialMessages;
import pico.erp.bom.material.BomMaterialRepository;
import pico.erp.item.ItemId;
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

  @Autowired
  private BomMaterialMapper materialMapper;

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
    return getHierarchy(get(id));
  }

  @Override
  public BomHierarchyData getHierarchy(ItemId id) {
    return getHierarchy(get(id));
  }

  private BomHierarchyData getHierarchy(BomData bom) {
    val materials = bomMaterialRepository.findAllIncludedMaterialBy(bom.getId())
      .map(materialMapper::map)
      .map(material -> this.getHierarchy(material))
      .collect(Collectors.toList());
    return new BomHierarchyData(bom, materials);
  }

  @Override
  public BomData draft(DraftRequest request) {
    if (bomRepository.exists(request.getId())) {
      throw new BomExceptions.AlreadyExistsException();
    }
    val bom = new Bom();
    val response = bom.apply(mapper.map(request));
    val created = bomRepository.create(bom);
    val previous = response.getPrevious();
    val events = new LinkedList<Event>();
    events.addAll(response.getEvents());
    if (previous != null) {
      bomRepository.update(previous);
      // 새로 생성된 BOM 에 기존 BOM 의 자재를 동일하게 생성
      bomMaterialRepository.findAllIncludedMaterialBy(previous.getId())
        .forEach(material -> {
          val nextRevisionResponse = material.apply(new BomMaterialMessages.NextRevisionRequest(
            created,
            bomRepository.findWithLastRevision(material.getMaterial().getItem().getId()).get()
          ));
          val draftedMaterial = nextRevisionResponse.getDrafted();
          bomMaterialRepository.create(draftedMaterial);
          events.addAll(nextRevisionResponse.getEvents());

        });
      // 새로 생성된 BOM 을 참조하고 있는 BOM 의 새버전을 생성하거나 해당 자재만 교체
      bomMaterialRepository.findAllIncludeMaterialBomBy(previous.getId())
        .forEach(referenced -> {
          if (referenced.isDetermined()) {
            this.draft(new DraftRequest(BomId.generate(), referenced.getItem().getId()));
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
  public void verify(VerifyRequest request) {
    val bom = bomRepository.findBy(request.getId())
      .orElseThrow(NotFoundException::new);
    if (!bom.isExpired()) {
      val aggregator = bomRepository.findAggregatorBy(bom.getId()).get();
      val response = aggregator.apply(new BomMessages.VerifyRequest());
      bomRepository.update(aggregator);
      eventPublisher.publishEvents(response.getEvents());
    }
  }
  @Override
  public void verify(VerifyByItemSpecRequest request) {
    bomMaterialRepository.findBy(request.getItemSpecId())
      .map(material ->
        BomRequests.VerifyRequest.builder()
          .id(material.getBom().getId())
          .build()
      )
      .ifPresent(this::verify);
  }

  @Override
  public void verify(VerifyByItemRequest request) {
    bomRepository.findAllBy(request.getItemId())
      .map(bom ->
        BomRequests.VerifyRequest.builder()
          .id(bom.getId())
          .build()
      )
      .forEach(this::verify);
  }

  @Override
  public void verify(VerifyByMaterialRequest request) {
    bomMaterialRepository.findAllIncludeMaterialBomBy(request.getMaterialId())
      .map(bom ->
        BomRequests.VerifyRequest.builder()
          .id(bom.getId())
          .build()
      )
      .forEach(this::verify);
  }

}
