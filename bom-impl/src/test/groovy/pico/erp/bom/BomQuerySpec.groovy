package pico.erp.bom

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import pico.erp.item.data.ItemId
import pico.erp.shared.IntegrationConfiguration
import spock.lang.Specification

@SpringBootTest(classes = [IntegrationConfiguration])
@Transactional
@Rollback
@ActiveProfiles("test")
@Configuration
@ComponentScan("pico.erp.config")
class BomQuerySpec extends Specification {

  def setup() {
    bomService.draft(new BomRequests.DraftRequest(itemId: ItemId.from("ACE")))
  }

  @Autowired
  BomQuery bomQuery

  @Autowired
  BomService bomService

  /*
  def "BOM 조회 - 조회 조건에 맞게 조회"() {
    expect:
    def page = bomQuery.retrieve(condition, pageable)
    page.totalElements == totalElements

    where:
    condition                               | pageable               || totalElements
    new BomQueryCondition(itemName: "ACE")     | new PageRequest(0, 10) || 1
  }
  */

}
