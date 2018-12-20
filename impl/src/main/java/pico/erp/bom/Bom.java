package pico.erp.bom;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.val;
import pico.erp.audit.annotation.Audit;
import pico.erp.bom.BomEvents.NextRevisionCreatedEvent;
import pico.erp.bom.BomExceptions.AlreadyDraftStatusException;
import pico.erp.bom.BomMessages.DraftResponse;
import pico.erp.bom.BomMessages.NextRevisionRequest;
import pico.erp.bom.unit.cost.BomUnitCost;
import pico.erp.item.ItemData;
import pico.erp.item.ItemTypeKind;
import pico.erp.item.spec.ItemSpecData;
import pico.erp.shared.data.Auditor;
import pico.erp.shared.event.Event;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString
@Audit(alias = "bom")
public class Bom implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * bom 아이디
   */
  @Id
  BomId id;

  int revision;

  /**
   * 관계 품목
   */
  ItemData item;

  /**
   * bom 상태
   */
  BomStatusKind status;


  BomUnitCost estimatedIsolatedUnitCost;


  BomUnitCost estimatedAccumulatedUnitCost;


  /**
   * bom 확정자
   */
  Auditor determinedBy;

  /**
   * bom 확정 일자
   */
  OffsetDateTime determinedDate;

  Auditor draftedBy;

  OffsetDateTime draftedDate;

  BigDecimal lossRate;

  /**
   * bom
   */


  boolean stable;

  public Bom() {
    this.status = BomStatusKind.DRAFT;
    this.estimatedIsolatedUnitCost = BomUnitCost.ZERO;
    this.estimatedAccumulatedUnitCost = BomUnitCost.ZERO;
    this.lossRate = BigDecimal.ZERO;
  }


  public BomMessages.DeleteResponse apply(BomMessages.DeleteRequest request) {
    return new BomMessages.DeleteResponse(Collections.emptyList());
  }

  public BomMessages.DraftResponse apply(BomMessages.DraftRequest request) {
    Bom lastRevision = request.getLastRevision();
    this.id = request.getId();
    this.item = request.getItem();
    if (lastRevision != null) {
      List<Event> events = new LinkedList<>();
      val nextRevisionResponse = lastRevision
        .apply(new NextRevisionRequest(this, request.getDraftedBy()));
      events.addAll(nextRevisionResponse.getEvents());
      return new DraftResponse(lastRevision, events);
    }
    this.revision = 1;
    this.draftedBy = request.getDraftedBy();
    this.draftedDate = OffsetDateTime.now();
    return new BomMessages.DraftResponse(null,
      Arrays.asList(new BomEvents.CreatedEvent(this.id))
    );
  }

  public boolean isUpdatable() {
    return status == BomStatusKind.DRAFT;
  }

  /**
   * 아이디를 유지하기 위해 새로 생성되는 bom 이 복사되어 예전 버전이 된다
   */
  public BomMessages.NextRevisionResponse apply(BomMessages.NextRevisionRequest request) {
    // DRAFT 상태에서는 다음 버전을 생성할 수 없다
    if (status == BomStatusKind.DRAFT) {
      throw new AlreadyDraftStatusException();
    }
    Bom drafted = request.getDrafted();
    drafted.revision = this.revision + 1;
    drafted.item = item;
    drafted.status = BomStatusKind.DRAFT;
    drafted.estimatedIsolatedUnitCost = estimatedIsolatedUnitCost;
    drafted.estimatedAccumulatedUnitCost = estimatedAccumulatedUnitCost;
    drafted.draftedBy = request.getDraftedBy();
    drafted.draftedDate = OffsetDateTime.now();

    this.status = BomStatusKind.EXPIRED;
    List<Event> events = new LinkedList<>();
    events.add(new NextRevisionCreatedEvent(this.id, drafted.id));
    return new BomMessages.NextRevisionResponse(events);
  }

  public boolean isExpired() {
    return status == BomStatusKind.EXPIRED;
  }

  public boolean isMaterial() {
    if (item != null) {
      return !Optional.ofNullable(item.getType())
        .orElse(ItemTypeKind.MATERIAL)
        .isProcessNeeded();
    }
    return false;
  }

  public boolean isSpecifiable() {
    if (item != null) {
      return item.isSpecifiable();
    }
    return false;
  }

  public boolean isDetermined() {
    return status == BomStatusKind.DETERMINED;
  }

  @Value
  @ToString
  public static class BomCalculateContext {

    ItemSpecData itemSpec;

  }

}
