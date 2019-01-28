package pico.erp.bom;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.AuditorAware;
import pico.erp.bom.material.BomMaterialRepository;
import pico.erp.item.ItemData;
import pico.erp.item.ItemId;
import pico.erp.item.ItemService;
import pico.erp.process.ProcessData;
import pico.erp.process.ProcessId;
import pico.erp.process.ProcessService;
import pico.erp.shared.data.Auditor;

@Mapper(imports = BigDecimal.class)
public abstract class BomMapper {

  @Lazy
  @Autowired
  protected ItemService itemService;

  @Lazy
  @Autowired
  protected BomRepository bomRepository;

  @Lazy
  @Autowired
  protected ProcessService processService;

  @Autowired
  protected AuditorAware<Auditor> auditorAware;

  @Lazy
  @Autowired
  protected BomMaterialRepository bomMaterialRepository;

  protected Bom lastRevision(ItemId itemId) {
    return bomRepository.findWithLastRevision(itemId)
      .orElse(null);
  }

  protected ItemData map(ItemId id) {
    return Optional.ofNullable(id)
      .map(itemService::get)
      .orElse(null);
  }

  public BomAggregator aggregator(BomEntity entity) {
    return BomAggregator.aggregatorBuilder()
      .id(entity.getId())
      .item(itemService.get(entity.getItemId()))
      .revision(entity.getRevision())
      .status(entity.getStatus())
      .estimatedIsolatedUnitCost(entity.getEstimatedIsolatedUnitCost().to())
      .estimatedAccumulatedUnitCost(entity.getEstimatedAccumulatedUnitCost().to())
      .draftedBy(entity.getDraftedBy())
      .draftedDate(entity.getDraftedDate())
      .determinedBy(entity.getDeterminedBy())
      .determinedDate(entity.getDeterminedDate())
      .stable(entity.isStable())
      .lossRate(entity.getLossRate())
      .materials(
        bomMaterialRepository.findAllIncludedMaterialBy(entity.getId()).collect(Collectors.toList())
      )
      .processes(
        processService.getAll(entity.getItemId())
      )
      .build();
  }

  protected ProcessData map(ProcessId id) {
    return Optional.ofNullable(id)
      .map(processService::get)
      .orElse(null);
  }

  public Bom jpa(BomEntity entity) {
    Bom bom = Bom.builder()
      .id(entity.getId())
      .item(itemService.get(entity.getItemId()))
      .revision(entity.getRevision())
      .status(entity.getStatus())
      .estimatedIsolatedUnitCost(entity.getEstimatedIsolatedUnitCost().to())
      .estimatedAccumulatedUnitCost(entity.getEstimatedAccumulatedUnitCost().to())
      .draftedBy(entity.getDraftedBy())
      .draftedDate(entity.getDraftedDate())
      .determinedBy(entity.getDeterminedBy())
      .determinedDate(entity.getDeterminedDate())
      .stable(entity.isStable())
      .lossRate(entity.getLossRate())
      .build();
    return bom;
  }

  @Mappings({
    @Mapping(target = "itemId", source = "item.id"),
    @Mapping(target = "lastModifiedBy", ignore = true),
    @Mapping(target = "lastModifiedDate", ignore = true)
  })
  public abstract BomEntity jpa(Bom bom);

  public Bom map(BomId bomId) {
    return Optional.ofNullable(bomId)
      .map(id -> bomRepository.findBy(id).orElseThrow(BomExceptions.NotFoundException::new))
      .orElse(null);
  }

  @Mappings({
    @Mapping(target = "determinedBy", expression = "java(auditorAware.getCurrentAuditor())")
  })
  public abstract BomMessages.DetermineRequest map(BomRequests.DetermineRequest request);

  public abstract BomMessages.DeleteRequest map(BomRequests.DeleteRequest request);

  @Mappings({
    @Mapping(target = "item", source = "itemId"),
    @Mapping(target = "lastRevision", source = "itemId"),
    @Mapping(target = "draftedBy", expression = "java(auditorAware.getCurrentAuditor())")
  })
  public abstract BomMessages.DraftRequest map(BomRequests.DraftRequest request);

  @Mappings({
    @Mapping(target = "itemId", source = "item.id"),
    @Mapping(target = "quantity", expression = "java(BigDecimal.ONE)"),
    @Mapping(target = "itemSpecId", ignore = true),
    @Mapping(target = "parent", ignore = true),
    @Mapping(target = "order", expression = "java(0)")
  })
  public abstract BomData map(Bom bom);

  public abstract void pass(BomEntity from, @MappingTarget BomEntity to);


}
