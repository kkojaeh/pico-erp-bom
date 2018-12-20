package pico.erp.bom.process;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import pico.erp.bom.BomId;
import pico.erp.process.ProcessId;

@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class BomProcessData {

  BomProcessId id;

  BomId bomId;

  ProcessId processId;

  BigDecimal conversionRate;

  int order;

}
