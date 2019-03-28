package pico.erp.bom

import kkojaeh.spring.boot.component.SpringBootTestComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import pico.erp.company.CompanyApplication
import pico.erp.item.ItemApplication
import pico.erp.item.ItemId
import pico.erp.process.ProcessApplication
import pico.erp.shared.TestParentApplication
import spock.lang.Specification

@SpringBootTest(classes = [BomApplication, TestConfig])
@SpringBootTestComponent(parent = TestParentApplication, siblings = [ItemApplication, ProcessApplication, CompanyApplication])
@Transactional
@Rollback
@ActiveProfiles("test")
class BomQuerySpec extends Specification {

  def setup() {
  }

  @Autowired
  BomQuery bomQuery


  def "BOM 조회 - 조회 조건에 맞게 조회"() {
    expect:
    def list = bomQuery.findRevisions(ItemId.from("item-1"))
    list.size() == totalElements

    where:
    itemId                || totalElements
    ItemId.from("item-1") || 1
  }

}
