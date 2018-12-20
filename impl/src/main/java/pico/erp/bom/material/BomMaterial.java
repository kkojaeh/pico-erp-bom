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
import pico.erp.bom.BomExceptions.CannotUpdateException;
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

  ItemSpecData itemSpec;

  int order;

  public BomMaterialMessages.CreateResponse apply(BomMaterialMessages.CreateRequest request) {
    if (!request.getBom().isUpdatable()) {
      throw new CannotUpdateException();
    }
    this.bom = request.getBom();
    this.material = request.getMaterial();
    this.quantity = request.getQuantity();
    this.itemSpec = request.getItemSpec();
    this.order = request.getOrder();
    return new BomMaterialMessages.CreateResponse(
      Arrays.asList(new BomMaterialEvents.CreatedEvent(bom.getId(), material.getId()))
    );
  }

  public BomMaterialMessages.UpdateResponse apply(BomMaterialMessages.UpdateRequest request) {
    if (!bom.isUpdatable()) {
      throw new CannotUpdateException();
    }
    this.quantity = request.getQuantity();
    this.itemSpec = request.getItemSpec();
    return new BomMaterialMessages.UpdateResponse(
      Arrays.asList(new BomMaterialEvents.UpdatedEvent(bom.getId(), material.getId()))
    );
  }

  public BomMaterialMessages.DeleteResponse apply(BomMaterialMessages.DeleteRequest request) {
    if (!bom.isUpdatable()) {
      throw new CannotUpdateException();
    }
    return new BomMaterialMessages.DeleteResponse(
      Arrays.asList(new BomMaterialEvents.DeletedEvent(bom.getId(), material.getId()))
    );
  }

  public BomMaterialMessages.NextRevisionResponse apply(
    BomMaterialMessages.NextRevisionRequest request) {
    val drafted = new BomMaterial();
    drafted.bom = request.getDrafted();
    drafted.material = request.getLastRevisionMaterial();
    drafted.quantity = quantity;
    drafted.itemSpec = itemSpec;
    return new BomMaterialMessages.NextRevisionResponse(
      drafted, Collections.emptyList()
    );
  }

  public BomMaterialMessages.SwapResponse apply(
    BomMaterialMessages.SwapRequest request) {
    val swapped = new BomMaterial();
    swapped.bom = bom;
    swapped.material = request.getMaterial();
    swapped.quantity = quantity;
    swapped.itemSpec = itemSpec;
    return new BomMaterialMessages.SwapResponse(
      swapped, Collections.emptyList()
    );
  }

  public BomMaterialMessages.ChangeOrderResponse apply(
    BomMaterialMessages.ChangeOrderRequest request) {
    if (order == request.getOrder()) {
      throw new BomExceptions.MaterialCannotChangeOrderException();
    }
    order = request.getOrder();
    return new BomMaterialMessages.ChangeOrderResponse(
      Collections.emptyList()
    );
  }

  public BomUnitCost getEstimatedAccumulatedUnitCost() {
    val context = new BomCalculateContext(itemSpec);
    return material.getEstimatedAccumulatedUnitCost().with(context);
  }

  public BomUnitCost getEstimatedIsolatedUnitCost() {
    val context = new BomCalculateContext(itemSpec);
    return material.getEstimatedIsolatedUnitCost().with(context);
  }

}
