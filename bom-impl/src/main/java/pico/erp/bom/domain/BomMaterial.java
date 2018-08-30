package pico.erp.bom.domain;

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
import pico.erp.bom.BomExceptions;
import pico.erp.bom.BomMaterialEvents;
import pico.erp.bom.domain.Bom.BomCalculateContext;
import pico.erp.item.data.ItemSpecData;

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

  public BomMaterialMessages.CreateResponse apply(BomMaterialMessages.CreateRequest request) {
    if (!request.getBom().canModify()) {
      throw new BomExceptions.CannotModifyException();
    }
    this.bom = request.getBom();
    this.material = request.getMaterial();
    this.quantity = request.getQuantity();
    this.itemSpecData = request.getItemSpecData();
    return new BomMaterialMessages.CreateResponse(
      Arrays.asList(new BomMaterialEvents.CreatedEvent(bom.getId(), material.getId()))
    );
  }

  public BomMaterialMessages.UpdateResponse apply(BomMaterialMessages.UpdateRequest request) {
    if (!bom.canModify()) {
      throw new BomExceptions.CannotModifyException();
    }
    this.quantity = request.getQuantity();
    this.itemSpecData = request.getItemSpecData();
    return new BomMaterialMessages.UpdateResponse(
      Arrays.asList(new BomMaterialEvents.UpdatedEvent(bom.getId(), material.getId()))
    );
  }

  public BomMaterialMessages.DeleteResponse apply(BomMaterialMessages.DeleteRequest request) {
    if (!bom.canModify()) {
      throw new BomExceptions.CannotModifyException();
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
    drafted.itemSpecData = itemSpecData;
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
    swapped.itemSpecData = itemSpecData;
    return new BomMaterialMessages.SwapResponse(
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
