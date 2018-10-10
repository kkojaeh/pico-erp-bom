package pico.erp.bom.material;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import pico.erp.bom.Bom;
import pico.erp.bom.BomData;
import pico.erp.bom.BomEntity;
import pico.erp.bom.BomId;
import pico.erp.bom.BomMapper;
import pico.erp.item.spec.ItemSpecData;
import pico.erp.item.spec.ItemSpecId;
import pico.erp.item.spec.ItemSpecService;

@Mapper(imports = {BomEntity.class, BomId.class})
public abstract class BomMaterialMapper {

  @Lazy
  @Autowired
  protected BomMapper bomMapper;

  @Lazy
  @Autowired
  protected ItemSpecService itemSpecService;

  @PersistenceContext
  protected EntityManager entityManager;

  public BomMaterial domain(BomMaterialEntity entity) {
    return BomMaterial.builder()
      .bom(bomMapper.domain(entity.getBom()))
      .material(bomMapper.domain(entity.getMaterial()))
      .quantity(entity.getQuantity())
      .itemSpecData(map(entity.getItemSpecId()))
      .build();
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
  public abstract BomMaterialEntity entity(BomMaterial material);

  @Mappings({
    @Mapping(target = "bom", source = "bomId"),
    @Mapping(target = "material", source = "materialId"),
    @Mapping(target = "itemSpecData", source = "itemSpecId")
  })
  public abstract BomMaterialMessages.CreateRequest map(BomMaterialRequests.CreateRequest request);

  @Mappings({
    @Mapping(target = "itemSpecData", source = "itemSpecId")
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
    @Mapping(target = "processId", source = "material.process.id"),
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
