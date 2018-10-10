package pico.erp.bom;

import java.util.List;
import javax.validation.constraints.NotNull;
import pico.erp.item.ItemId;

public interface BomQuery {

  List<BomRevisionView> findRevisions(@NotNull ItemId itemId);

}
