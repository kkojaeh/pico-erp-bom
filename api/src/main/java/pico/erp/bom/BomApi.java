package pico.erp.bom;

import javax.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pico.erp.shared.ApplicationId;
import pico.erp.shared.data.Role;

public final class BomApi {

  public final static ApplicationId ID = ApplicationId.from("bom");

  @RequiredArgsConstructor
  public enum Roles implements Role {

    BOM_MANAGER,
    BOM_ACCESSOR;

    @Id
    @Getter
    private final String id = name();

  }
}
