package pico.erp.bom;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pico.erp.shared.data.Auditor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BomRevisionView {

  BomId id;

  int revision;

  BomStatusKind status;

  Auditor determinedBy;

  LocalDateTime determinedDate;

  Auditor draftedBy;

  LocalDateTime draftedDate;

}
