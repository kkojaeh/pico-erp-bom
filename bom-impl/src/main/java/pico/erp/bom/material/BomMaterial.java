package pico.erp.bom.material;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import pico.erp.bom.Bom;
import pico.erp.bom.Bom.BomCalculateContext;
import pico.erp.bom.BomExceptions;
import pico.erp.bom.material.BomMaterialMessages.CreateRequest;
import pico.erp.bom.material.BomMaterialMessages.CreateResponse;
import pico.erp.bom.material.BomMaterialMessages.DeleteRequest;
import pico.erp.bom.material.BomMaterialMessages.DeleteResponse;
import pico.erp.bom.material.BomMaterialMessages.NextRevisionRequest;
import pico.erp.bom.material.BomMaterialMessages.NextRevisionResponse;
import pico.erp.bom.material.BomMaterialMessages.SwapRequest;
import pico.erp.bom.material.BomMaterialMessages.SwapResponse;
import pico.erp.bom.material.BomMaterialMessages.UpdateRequest;
import pico.erp.bom.material.BomMaterialMessages.UpdateResponse;
import pico.erp.bom.unit.cost.BomUnitCost;
import pico.erp.item.spec.ItemSpecData;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"bom", "material"})
@NoArgsConstructor
@AllArgsConstructor
public class BomMaterial implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  Bom bom;

  @Id
  Bom material;

  BigDecimal quantity;

  ItemSpecData itemSpecData;

  public CreateResponse apply(CreateRequest request) {
    if (!request.getBom().canModify()) {
      throw new BomExceptions.CannotModifyException();
    }
    this.bom = request.getBom();
    this.material = request.getMaterial();
    this.quantity = request.getQuantity();
    this.itemSpecData = request.getItemSpecData();
    return new CreateResponse(
      Arrays.asList(new BomMaterialEvents.CreatedEvent(bom.getId(), material.getId()))
    );
  }

  public UpdateResponse apply(UpdateRequest request) {
    if (!bom.canModify()) {
      throw new BomExceptions.CannotModifyException();
    }
    this.quantity = request.getQuantity();
    this.itemSpecData = request.getItemSpecData();
    return new UpdateResponse(
      Arrays.asList(new BomMaterialEvents.UpdatedEvent(bom.getId(), material.getId()))
    );
  }

  public DeleteResponse apply(DeleteRequest request) {
    if (!bom.canModify()) {
      throw new BomExceptions.CannotModifyException();
    }
    return new DeleteResponse(
      Arrays.asList(new BomMaterialEvents.DeletedEvent(bom.getId(), material.getId()))
    );
  }

  public NextRevisionResponse apply(
    NextRevisionRequest request) {
    val drafted = new BomMaterial();
    drafted.bom = request.getDrafted();
    drafted.material = request.getLastRevisionMaterial();
    drafted.quantity = quantity;
    drafted.itemSpecData = itemSpecData;
    return new NextRevisionResponse(
      drafted, Collections.emptyList()
    );
  }

  public SwapResponse apply(
    SwapRequest request) {
    val swapped = new BomMaterial();
    swapped.bom = bom;
    swapped.material = request.getMaterial();
    swapped.quantity = quantity;
    swapped.itemSpecData = itemSpecData;
    return new SwapResponse(
      swapped, Collections.emptyList()
    );
  }

  public BomUnitCost getEstimatedAccumulatedUnitCost() {
    val context = new BomCalculateContext(itemSpecData);
    return material.getEstimatedAccumulatedUnitCost().with(context);
  }

  public BomUnitCost getEstimatedIsolatedUnitCost() {
    val context = new BomCalculateContext(itemSpecData);
    return material.getEstimatedIsolatedUnitCost().with(context);
  }

}
