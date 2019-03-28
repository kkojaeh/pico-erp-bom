package pico.erp.bom.material;

import java.util.Optional;
import kkojaeh.spring.boot.component.Take;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import pico.erp.bom.Bom;
import pico.erp.bom.BomData;
import pico.erp.bom.BomId;
import pico.erp.bom.BomMapper;
import pico.erp.bom.material.BomMaterialEntity.BomMaterialKey;
import pico.erp.item.spec.ItemSpecData;
import pico.erp.item.spec.ItemSpecId;
import pico.erp.item.spec.ItemSpecService;

@Mapper(imports = {BomMaterialKey.class, BomId.class})
public abstract class BomMaterialMapper {

  @Lazy
  @Autowired
  protected BomMapper bomMapper;

  @Lazy
  @Autowired
  protected BomMaterialRepository bomMaterialRepository;

  @Take
  protected ItemSpecService itemSpecService;

  @AfterMapping
  protected void afterMapping(BomMaterialRequests.CreateRequest request,
    @MappingTarget BomMaterialMessages.CreateRequest message) {
    message.setOrder(
      (int) bomMaterialRepository.countIncludedMaterialBy(request.getBomId())
    );
  }

  @Mappings({
    @Mapping(target = "key", expression = "java(BomMaterialKey.from(material.getBom().getId(), material.getMaterial().getId()))"),
    @Mapping(target = "itemSpecId", source = "itemSpec.id"),
    @Mapping(target = "createdBy", ignore = true),
    @Mapping(target = "createdDate", ignore = true),
    @Mapping(target = "lastModifiedBy", ignore = true),
    @Mapping(target = "lastModifiedDate", ignore = true)
  })
  public abstract BomMaterialEntity jpa(BomMaterial material);

  public BomMaterial jpa(BomMaterialEntity entity) {
    return BomMaterial.builder()
      .bom(map(entity.getKey().getBomId()))
      .material(map(entity.getKey().getMaterialId()))
      .quantity(entity.getQuantity())
      .itemSpec(map(entity.getItemSpecId()))
      .order(entity.getOrder())
      .build();
  }

  @Mappings({
    @Mapping(target = "bom", source = "bomId"),
    @Mapping(target = "material", source = "materialId"),
    @Mapping(target = "itemSpec", source = "itemSpecId"),
    @Mapping(target = "order", ignore = true)
  })
  public abstract BomMaterialMessages.CreateRequest map(BomMaterialRequests.CreateRequest request);

  public abstract BomMaterialMessages.ChangeOrderRequest map(
    BomMaterialRequests.ChangeOrderRequest request);

  @Mappings({
    @Mapping(target = "itemSpec", source = "itemSpecId")
  })
  public abstract BomMaterialMessages.UpdateRequest map(BomMaterialRequests.UpdateRequest request);

  @Mappings({
  })
  public abstract BomMaterialMessages.DeleteRequest map(BomMaterialRequests.DeleteRequest request);

  @Mappings({
    @Mapping(target = "id", source = "material.id"),
    @Mapping(target = "itemId", source = "material.item.id"),
    @Mapping(target = "revision", source = "material.revision"),
    @Mapping(target = "status", source = "material.status"),
    @Mapping(target = "estimatedIsolatedUnitCost", source = "estimatedIsolatedUnitCost"),
    @Mapping(target = "estimatedAccumulatedUnitCost", source = "estimatedAccumulatedUnitCost"),
    @Mapping(target = "determinedBy", source = "material.determinedBy"),
    @Mapping(target = "determinedDate", source = "material.determinedDate"),
    @Mapping(target = "quantity", source = "quantity"),
    @Mapping(target = "specifiable", source = "material.specifiable"),
    @Mapping(target = "material", source = "material.material"),
    @Mapping(target = "updatable", source = "material.updatable"),
    @Mapping(target = "stable", source = "material.stable"),
    @Mapping(target = "itemSpecId", source = "itemSpec.id"),
    @Mapping(target = "parent", source = "bom"),
    @Mapping(target = "lossRate", source = "material.lossRate"),
  })
  public abstract BomData map(BomMaterial material);

  protected Bom map(BomId bomId) {
    return bomMapper.map(bomId);
  }

  protected BomData map(Bom bom) {
    return bomMapper.map(bom);
  }

  protected ItemSpecData map(ItemSpecId id) {
    return Optional.ofNullable(id)
      .map(itemSpecService::get)
      .orElse(null);
  }

  public abstract void pass(BomMaterialEntity from, @MappingTarget BomMaterialEntity to);

}
