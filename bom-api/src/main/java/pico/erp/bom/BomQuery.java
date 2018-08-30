package pico.erp.bom;

import java.util.List;
import javax.validation.constraints.NotNull;
import pico.erp.bom.data.BomRevisionView;
import pico.erp.item.data.ItemId;

public interface BomQuery {

  List<BomRevisionView> findRevisions(@NotNull ItemId itemId);

}
