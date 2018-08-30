package pico.erp.bom.impl;

import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import pico.erp.bom.core.BomMaterialRepository;
import pico.erp.bom.data.BomId;
import pico.erp.bom.domain.Bom;
import pico.erp.bom.domain.BomAggregator;
import pico.erp.bom.domain.BomMaterial;
import pico.erp.bom.impl.jpa.BomEntity;
import pico.erp.bom.impl.jpa.BomMaterialEntity;
import pico.erp.item.ItemService;
import pico.erp.item.ItemSpecService;
import pico.erp.item.data.ItemData;
import pico.erp.item.data.ItemId;
import pico.erp.item.data.ItemSpecData;
import pico.erp.item.data.ItemSpecId;
import pico.erp.process.ProcessService;
import pico.erp.process.data.ProcessData;
import pico.erp.process.data.ProcessId;

@Mapper(imports = {BomEntity.class, BomId.class})
public abstract class BomJpaMapper {

  @Lazy
  @Autowired
  protected ItemService itemService;

  @Lazy
  @Autowired
  protected ItemSpecService itemSpecService;

  @Lazy
  @Autowired
  protected ProcessService processService;

  @Lazy
  @Autowired
  protected BomMaterialRepository bomMaterialRepository;

  @PersistenceContext
  protected EntityManager entityManager;

  @Mappings({
    @Mapping(target = "itemId", source = "itemData.id"),
    @Mapping(target = "processId", source = "processData.id"),
    @Mapping(target = "processName", source = "processData.name"),
    @Mapping(target = "lastModifiedBy", ignore = true),
    @Mapping(target = "lastModifiedDate", ignore = true)
  })
  public abstract BomEntity map(Bom bom);

  public Bom map(BomEntity entity) {
    Bom bom = Bom.builder()
      .id(entity.getId())
      .itemData(itemService.get(entity.getItemId()))
      .revision(entity.getRevision())
      .status(entity.getStatus())
      .processData(
        Optional.ofNullable(entity.getProcessId())
          .map(id -> processService.get(id))
          .orElse(null)
      )
      .estimatedIsolatedUnitCost(entity.getEstimatedIsolatedUnitCost().to())
      .estimatedAccumulatedUnitCost(entity.getEstimatedAccumulatedUnitCost().to())
      .draftedBy(entity.getDraftedBy())
      .draftedDate(entity.getDraftedDate())
      .determinedBy(entity.getDeterminedBy())
      .determinedDate(entity.getDeterminedDate())
      .stable(entity.isStable())
      .build();
    return bom;
  }

  @Mappings({
    @Mapping(target = "bom", expression = "java(entityManager.getReference(BomEntity.class, material.getBom().getId()))"),
    @Mapping(target = "material", expression = "java(entityManager.getReference(BomEntity.class, material.getMaterial().getId()))"),
    @Mapping(target = "itemSpecId", source = "itemSpecData.id"),
    @Mapping(target = "createdBy", ignore = true),
    @Mapping(target = "createdDate", ignore = true),
    @Mapping(target = "lastModifiedBy", ignore = true),
    @Mapping(target = "lastModifiedDate", ignore = true)
  })
  public abstract BomMaterialEntity map(BomMaterial material);

  public BomMaterial map(BomMaterialEntity entity) {

    return BomMaterial.builder()
      .bom(map(entity.getBom()))
      .material(map(entity.getMaterial()))
      .quantity(entity.getQuantity())
      .itemSpecData(map(entity.getItemSpecId()))
      .build();
  }

  public BomAggregator mapAggregator(BomEntity entity) {
    return BomAggregator.aggregatorBuilder()
      .id(entity.getId())
      .itemData(itemService.get(entity.getItemId()))
      .revision(entity.getRevision())
      .status(entity.getStatus())
      .processData(
        Optional.ofNullable(entity.getProcessId())
          .map(id -> processService.get(id))
          .orElse(null)
      )
      .estimatedIsolatedUnitCost(entity.getEstimatedIsolatedUnitCost().to())
      .estimatedAccumulatedUnitCost(entity.getEstimatedAccumulatedUnitCost().to())
      .draftedBy(entity.getDraftedBy())
      .draftedDate(entity.getDraftedDate())
      .determinedBy(entity.getDeterminedBy())
      .determinedDate(entity.getDeterminedDate())
      .stable(entity.isStable())
      .materials(
        bomMaterialRepository.findAllBy(entity.getId()).collect(Collectors.toList())
      )
      .build();
  }

  protected ItemData map(ItemId id) {
    return Optional.ofNullable(id)
      .map(itemService::get)
      .orElse(null);
  }

  protected ProcessData map(ProcessId id) {
    return Optional.ofNullable(id)
      .map(processService::get)
      .orElse(null);
  }

  protected ItemSpecData map(ItemSpecId id) {
    return Optional.ofNullable(id)
      .map(itemSpecService::get)
      .orElse(null);
  }

  public abstract void pass(BomEntity from, @MappingTarget BomEntity to);

  public abstract void pass(BomMaterialEntity from, @MappingTarget BomMaterialEntity to);

}
