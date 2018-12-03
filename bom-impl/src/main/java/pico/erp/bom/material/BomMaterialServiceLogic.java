package pico.erp.bom.material;

import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pico.erp.bom.BomData;
import pico.erp.bom.BomExceptions;
import pico.erp.bom.BomId;
import pico.erp.bom.BomRepository;
import pico.erp.shared.Public;
import pico.erp.shared.event.EventPublisher;

@Service
@Public
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

}
