package pico.erp.bom.unit.cost;

import org.mapstruct.Mapper;

@Mapper
public abstract class BomUnitCostMapper {

  public abstract BomUnitCostData map(BomUnitCost domain);

}
