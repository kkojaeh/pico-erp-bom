package pico.erp.bom.material;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import kkojaeh.spring.boot.component.ComponentBean;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pico.erp.bom.BomData;
import pico.erp.bom.BomExceptions;
import pico.erp.bom.BomId;
import pico.erp.bom.BomRepository;
import pico.erp.bom.BomRequests.DraftRequest;
import pico.erp.bom.BomService;
import pico.erp.bom.material.BomMaterialRequests.ChangeOrderRequest;
import pico.erp.bom.material.BomMaterialRequests.NextRevisionRequest;
import pico.erp.shared.event.Event;
import pico.erp.shared.event.EventPublisher;

@Service
@ComponentBean
@Transactional
@Validated
public class BomMaterialServiceLogic implements BomMaterialService {

  @Autowired
  private BomRepository bomRepository;

  @Autowired
  private BomMaterialRepository bomMaterialRepository;


  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private BomMaterialMapper mapper;


  @Autowired
  private BomService bomService;

  @Override
  public BomData create(BomMaterialRequests.CreateRequest request) {

    if (request.getBomId().equals(request.getMaterialId())) {
      throw new BomExceptions.MaterialCircularReferenceException();
    }

    val aggregator = bomRepository.findAggregatorBy(request.getBomId())
      .orElseThrow(BomExceptions.NotFoundException::new);

    val exists = aggregator.getMaterials().stream()
      .filter(material -> material.getMaterial().getId().equals(request.getMaterialId())).count()
      > 0;

    if (exists) {
      throw new BomExceptions.MaterialAlreadyExistsException();
    }

    val contained = bomMaterialRepository.findAllAscendBy(request.getBomId())
      .filter(bom -> bom.getId().equals(request.getMaterialId())).count() > 0;

    if (contained) {
      throw new BomExceptions.MaterialCircularReferenceException();
    }

    val material = new BomMaterial();
    val response = material.apply(mapper.map(request));
    val created = bomMaterialRepository.create(material);
    eventPublisher.publishEvents(response.getEvents());
    return mapper.map(created);
  }

  @Override
  public void delete(BomMaterialRequests.DeleteRequest request) {
    val material = bomMaterialRepository.findBy(request.getBomId(), request.getMaterialId())
      .orElseThrow(BomExceptions.MaterialNotFoundException::new);
    val response = material.apply(mapper.map(request));
    bomMaterialRepository.deleteBy(request.getBomId(), request.getMaterialId());
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public BomData get(BomId bomId, BomId materialId) {
    return bomMaterialRepository.findBy(bomId, materialId)
      .map(mapper::map)
      .orElseThrow(BomExceptions.MaterialNotFoundException::new);
  }

  @Override
  public List<BomData> getAll(BomId bomId) {
    return bomMaterialRepository.findAllIncludedMaterialBy(bomId)
      .map(mapper::map)
      .collect(Collectors.toList());
  }

  @Override
  public void update(BomMaterialRequests.UpdateRequest request) {
    val material = bomMaterialRepository.findBy(request.getBomId(), request.getMaterialId())
      .orElseThrow(BomExceptions.MaterialNotFoundException::new);
    val response = material.apply(mapper.map(request));
    bomMaterialRepository.update(material);
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public void changeOrder(ChangeOrderRequest request) {
    val material = bomMaterialRepository.findBy(request.getBomId(), request.getMaterialId())
      .orElseThrow(BomExceptions.MaterialNotFoundException::new);
    val response = material.apply(mapper.map(request));
    bomMaterialRepository.update(material);
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public void nextRevision(NextRevisionRequest request) {
    val events = new LinkedList<Event>();
    val previousId = request.getPreviousId();
    val created = bomRepository.findBy(request.getNextRevisionId()).get();
    // 새로 생성된 BOM 에 기존 BOM 의 자재를 동일하게 생성
    bomMaterialRepository.findAllIncludedMaterialBy(previousId)
      .forEach(material -> {
        val response = material.apply(new BomMaterialMessages.NextRevisionRequest(
          created,
          bomRepository.findWithLastRevision(material.getMaterial().getItem().getId()).get()
        ));
        val drafted = response.getDrafted();
        bomMaterialRepository.create(drafted);
        events.addAll(response.getEvents());

      });
    // 새로 생성된 BOM 을 참조하고 있는 BOM 의 새버전을 생성하거나 해당 자재만 교체
    bomMaterialRepository.findAllIncludeMaterialBomBy(previousId)
      .forEach(referenced -> {
        if (referenced.isDetermined()) {
          bomService.draft(new DraftRequest(BomId.generate(), referenced.getItem().getId()));
        } else {
          val material = bomMaterialRepository.findBy(referenced.getId(), previousId)
            .get();
          val response = material.apply(new BomMaterialMessages.SwapRequest(created));
          bomMaterialRepository.create(response.getSwapped());
          bomMaterialRepository.deleteBy(material);
          events.addAll(response.getEvents());
        }
      });
    eventPublisher.publishEvents(events);
  }

}
