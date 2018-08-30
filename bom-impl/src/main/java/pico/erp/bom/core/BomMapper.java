package pico.erp.bom.core;

import java.math.BigDecimal;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.AuditorAware;
import pico.erp.bom.BomExceptions.NotFoundException;
import pico.erp.bom.BomMaterialRequests;
import pico.erp.bom.BomRequests;
import pico.erp.bom.BomRequests.MaterialRequestData;
import pico.erp.bom.data.BomData;
import pico.erp.bom.data.BomId;
import pico.erp.bom.data.BomUnitCostData;
import pico.erp.bom.domain.Bom;
import pico.erp.bom.domain.BomMaterial;
import pico.erp.bom.domain.BomMaterialMessages;
import pico.erp.bom.domain.BomMessages.DeleteRequest;
import pico.erp.bom.domain.BomMessages.DetermineRequest;
import pico.erp.bom.domain.BomMessages.DraftRequest;
import pico.erp.bom.domain.BomMessages.UpdateRequest;
import pico.erp.bom.domain.BomUnitCost;
import pico.erp.item.ItemService;
import pico.erp.item.ItemSpecService;
import pico.erp.item.data.ItemData;
import pico.erp.item.data.ItemId;
import pico.erp.item.data.ItemSpecData;
import pico.erp.item.data.ItemSpecId;
import pico.erp.process.ProcessService;
import pico.erp.process.data.ProcessData;
import pico.erp.process.data.ProcessId;
import pico.erp.shared.data.Auditor;

@Mapper(imports = BigDecimal.class)
public abstract class BomMapper {

  @Lazy
  @Autowired
  protected ItemService itemService;

  @Lazy
  @Autowired
  protected ItemSpecService itemSpecService;

  @Autowired
  protected BomRepository bomRepository;

  @Lazy
  @Autowired
  protected ProcessService processService;

  @Autowired
  protected AuditorAware<Auditor> auditorAware;

  protected Bom lastRevision(ItemId itemId) {
    return bomRepository.findWithLastRevision(itemId)
      .orElse(null);
  }

  protected ItemData map(ItemId id) {
    return Optional.ofNullable(id)
      .map(itemService::get)
      .orElse(null);
  }

  protected ItemSpecData map(ItemSpecId id) {
    return Optional.ofNullable(id)
      .map(itemSpecService::get)
      .orElse(null);
  }

  protected Bom map(BomId bomId) {
    return bomRepository.findBy(bomId).orElseThrow(NotFoundException::new);
  }

  protected ProcessData map(ProcessId id) {
    return Optional.ofNullable(id)
      .map(processService::get)
      .orElse(null);
  }

  @Mappings({
    @Mapping(target = "determinedBy", expression = "java(auditorAware.getCurrentAuditor())")
  })
  abstract DetermineRequest map(BomRequests.DetermineRequest request);

  abstract DeleteRequest map(BomRequests.DeleteRequest request);

  @Mappings({
    @Mapping(target = "itemData", source = "itemId"),
    @Mapping(target = "lastRevision", source = "itemId"),
    @Mapping(target = "draftedBy", expression = "java(auditorAware.getCurrentAuditor())")
  })
  abstract DraftRequest map(BomRequests.DraftRequest request);

  BomMaterial map(MaterialRequestData data) {
    return BomMaterial.builder()
      .material(map(data.getId()))
      .itemSpecData(map(data.getItemSpecId()))
      .quantity(data.getQuantity())
      .build();
  }


  @Mappings({
    @Mapping(target = "bom", source = "bomId"),
    @Mapping(target = "material", source = "materialId"),
    @Mapping(target = "itemSpecData", source = "itemSpecId")
  })
  abstract BomMaterialMessages.CreateRequest map(BomMaterialRequests.CreateRequest request);

  @Mappings({
    @Mapping(target = "itemSpecData", source = "itemSpecId")
  })
  abstract BomMaterialMessages.UpdateRequest map(BomMaterialRequests.UpdateRequest request);

  @Mappings({
  })
  abstract BomMaterialMessages.DeleteRequest map(BomMaterialRequests.DeleteRequest request);

  @Mappings({
    @Mapping(target = "processData", source = "processId")
  })
  abstract UpdateRequest map(BomRequests.UpdateRequest request);

  abstract BomUnitCostData map(BomUnitCost domain);

  @Mappings({
    @Mapping(target = "itemId", source = "itemData.id"),
    @Mapping(target = "processId", source = "processData.id"),
    @Mapping(target = "quantity", expression = "java(BigDecimal.ONE)"),
    @Mapping(target = "modifiable", expression = "java(bom.canModify())"),
    @Mapping(target = "itemSpecId", ignore = true),
    @Mapping(target = "parent", ignore = true),
  })
  abstract BomData map(Bom bom);

  @Mappings({
    @Mapping(target = "id", source = "material.id"),
    @Mapping(target = "itemId", source = "material.itemData.id"),
    @Mapping(target = "revision", source = "material.revision"),
    @Mapping(target = "status", source = "material.status"),
    @Mapping(target = "processId", source = "material.processData.id"),
    @Mapping(target = "estimatedIsolatedUnitCost", source = "material.estimatedIsolatedUnitCost"),
    @Mapping(target = "estimatedAccumulatedUnitCost", source = "material.estimatedAccumulatedUnitCost"),
    @Mapping(target = "determinedBy", source = "material.determinedBy"),
    @Mapping(target = "determinedDate", source = "material.determinedDate"),
    @Mapping(target = "quantity", source = "quantity"),
    @Mapping(target = "specifiable", source = "material.specifiable"),
    @Mapping(target = "material", source = "material.material"),
    @Mapping(target = "modifiable", expression = "java(material.getMaterial().canModify())"),
    @Mapping(target = "stable", expression = "java(material.getMaterial().isStable())"),
    @Mapping(target = "itemSpecId", source = "itemSpecData.id"),
    @Mapping(target = "parent", source = "bom"),

  })
  abstract BomData map(BomMaterial material);

}
