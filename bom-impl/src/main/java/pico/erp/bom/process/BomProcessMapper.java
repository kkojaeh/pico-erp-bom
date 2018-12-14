package pico.erp.bom.process;

import java.util.Optional;
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
import pico.erp.bom.material.BomMaterialMessages;
import pico.erp.bom.material.BomMaterialRequests;
import pico.erp.process.ProcessData;
import pico.erp.process.ProcessId;
import pico.erp.process.ProcessService;

@Mapper(imports = {BomMaterialKey.class, BomId.class})
public abstract class BomProcessMapper {

  @Lazy
  @Autowired
  protected BomMapper bomMapper;

  @Lazy
  @Autowired
  protected BomProcessRepository bomProcessRepository;

  @Lazy
  @Autowired
  protected ProcessService processService;

  @AfterMapping
  protected void afterMapping(BomMaterialRequests.CreateRequest request,
    @MappingTarget BomMaterialMessages.CreateRequest message) {
    message.setOrder(
      (int) bomProcessRepository.countBy(request.getBomId())
    );
  }

  @Mappings({
    @Mapping(target = "bomId", source = "bom.id"),
    @Mapping(target = "processId", source = "process.id"),
    @Mapping(target = "createdBy", ignore = true),
    @Mapping(target = "createdDate", ignore = true),
    @Mapping(target = "lastModifiedBy", ignore = true),
    @Mapping(target = "lastModifiedDate", ignore = true)
  })
  public abstract BomProcessEntity jpa(BomProcess bomProcess);

  public BomProcess jpa(BomProcessEntity entity) {
    return BomProcess.builder()
      .id(entity.getId())
      .bom(map(entity.getBomId()))
      .process(map(entity.getProcessId()))
      .conversionRate(entity.getConversionRate())
      .order(entity.getOrder())
      .build();
  }

  @Mappings({
    @Mapping(target = "bom", source = "bomId"),
    @Mapping(target = "process", source = "processId"),
    @Mapping(target = "order", ignore = true)
  })
  public abstract BomProcessMessages.CreateRequest map(BomProcessRequests.CreateRequest request);

  public abstract BomProcessMessages.ChangeOrderRequest map(
    BomProcessRequests.ChangeOrderRequest request);

  @Mappings({
  })
  public abstract BomProcessMessages.UpdateRequest map(BomProcessRequests.UpdateRequest request);

  @Mappings({
  })
  public abstract BomProcessMessages.DeleteRequest map(BomProcessRequests.DeleteRequest request);

  @Mappings({
    @Mapping(target = "bomId", source = "bom.id"),
    @Mapping(target = "processId", source = "process.id")

  })
  public abstract BomProcessData map(BomProcess bomProcess);

  protected Bom map(BomId bomId) {
    return bomMapper.map(bomId);
  }

  protected BomData map(Bom bom) {
    return bomMapper.map(bom);
  }

  protected ProcessData map(ProcessId id) {
    return Optional.ofNullable(id)
      .map(processService::get)
      .orElse(null);
  }

  public abstract void pass(BomProcessEntity from, @MappingTarget BomProcessEntity to);

}
