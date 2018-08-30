package pico.erp.bom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pico.erp.shared.data.Menu;
import pico.erp.shared.data.MenuCategory;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum MENU implements Menu {

  BOM_MANAGEMENT("/bom", "fas fa-sitemap", MenuCategory.ITEM);

  String url;

  String icon;

  MenuCategory category;

  public String getId() {
    return name();
  }

}
