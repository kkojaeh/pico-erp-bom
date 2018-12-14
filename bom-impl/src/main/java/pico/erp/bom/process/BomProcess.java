package pico.erp.bom.process;

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
import pico.erp.bom.BomExceptions.CannotUpdateException;
import pico.erp.process.ProcessData;
import pico.erp.process.cost.ProcessCostData;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@AllArgsConstructor
public class BomProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  BomProcessId id;

  Bom bom;

  ProcessData process;

  BigDecimal conversionRate;

  int order;

  public BomProcessMessages.CreateResponse apply(BomProcessMessages.CreateRequest request) {
    if (!request.getBom().isUpdatable()) {
      throw new CannotUpdateException();
    }
    id = request.getId();
    bom = request.getBom();
    process = request.getProcess();
    conversionRate = request.getConversionRate();
    order = request.getOrder();

    return new BomProcessMessages.CreateResponse(
      Arrays.asList(new BomProcessEvents.CreatedEvent(id, bom.getId()))
    );
  }

  public BomProcessMessages.UpdateResponse apply(BomProcessMessages.UpdateRequest request) {
    if (!bom.isUpdatable()) {
      throw new CannotUpdateException();
    }
    conversionRate = request.getConversionRate();
    return new BomProcessMessages.UpdateResponse(
      Arrays.asList(new BomProcessEvents.UpdatedEvent(id, bom.getId()))
    );
  }

  public BomProcessMessages.DeleteResponse apply(BomProcessMessages.DeleteRequest request) {
    if (!bom.isUpdatable()) {
      throw new CannotUpdateException();
    }
    return new BomProcessMessages.DeleteResponse(
      Arrays.asList(new BomProcessEvents.DeletedEvent(
        id,
        bom.getId(),
        process.getId()
      ))
    );
  }

  public BomProcessMessages.NextRevisionResponse apply(
    BomProcessMessages.NextRevisionRequest request) {
    val drafted = new BomProcess();
    drafted.id = BomProcessId.generate();
    drafted.bom = request.getDrafted();
    drafted.process = process;
    drafted.conversionRate = conversionRate;
    drafted.order = order;
    return new BomProcessMessages.NextRevisionResponse(
      drafted, Collections.emptyList()
    );
  }

  public BomProcessMessages.ChangeOrderResponse apply(
    BomProcessMessages.ChangeOrderRequest request) {
    if (!bom.isUpdatable() || order == request.getOrder()) {
      throw new BomProcessExceptions.CannotChangeOrderException();
    }
    order = request.getOrder();
    return new BomProcessMessages.ChangeOrderResponse(
      Collections.emptyList()
    );
  }

  public ProcessCostData getEstimatedCost() {
    return process.getEstimatedCost();
  }

  public BigDecimal getLossRate() {
    return process.getLossRate();
  }

  public boolean isPlanned() {
    return process.isPlanned();
  }

}
