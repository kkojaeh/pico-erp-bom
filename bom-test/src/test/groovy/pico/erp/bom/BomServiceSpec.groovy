package pico.erp.bom

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import pico.erp.bom.material.BomMaterialRequests
import pico.erp.bom.material.BomMaterialService
import pico.erp.company.CompanyId
import pico.erp.item.ItemId
import pico.erp.item.ItemRequests
import pico.erp.item.ItemService
import pico.erp.item.ItemTypeKind
import pico.erp.item.category.ItemCategoryId
import pico.erp.process.ProcessId
import pico.erp.process.ProcessRequests
import pico.erp.process.ProcessService
import pico.erp.process.difficulty.grade.ProcessDifficultyKind
import pico.erp.process.type.ProcessTypeId
import pico.erp.shared.IntegrationConfiguration
import pico.erp.shared.data.UnitKind
import spock.lang.Specification

@SpringBootTest(classes = [IntegrationConfiguration])
@Transactional
@Rollback
@ActiveProfiles("test")
@Configuration
@ComponentScan("pico.erp.config")
class BomServiceSpec extends Specification {

  @Lazy
  @Autowired
  BomService bomService

  @Lazy
  @Autowired
  BomMaterialService bomMaterialService

  @Lazy
  @Autowired
  ItemService itemService

  @Lazy
  @Autowired
  ProcessService processService

  BomData bom

  BomData bom2

  def setup() {

    itemService.create(new ItemRequests.CreateRequest(id: ItemId.from("ACE"), name: "테스트", categoryId: ItemCategoryId.from("category-1"), customerId: CompanyId.from("CUST1"), unit: UnitKind.EA, type: ItemTypeKind.MATERIAL, baseUnitCost: 0))
    itemService.create(new ItemRequests.CreateRequest(id: ItemId.from("ACE-2"), name: "테스트 2", categoryId: ItemCategoryId.from("category-1"), customerId: CompanyId.from("CUST1"), unit: UnitKind.EA, type: ItemTypeKind.MATERIAL, baseUnitCost: 300))
    processService.create(
      new ProcessRequests.CreateRequest(
        id: ProcessId.from("process-1"),
        itemId: ItemId.from("ACE"),
        name: "품목 명과 공정명 합침",
        lossRate: 0.01,
        typeId: ProcessTypeId.from("printing-offset"),
        adjustCost: 0,
        difficulty: ProcessDifficultyKind.NORMAL,
        description: "좋은 보통 작업"
      )
    )


    bom = bomService.draft(new BomRequests.DraftRequest(
      id: BomId.from("ACE"),
      itemId: ItemId.from("ACE")))
    bom2 = bomService.draft(new BomRequests.DraftRequest(
      id: BomId.from("ACE-2"),
      itemId: ItemId.from("ACE-2")))
  }

  def "아이디로 존재하는 BOM 확인"() {
    when:
    def exists = bomService.exists(bom.id)

    then:
    exists == true
  }

  def "아이디로 존재하지 않는 BOM 확인"() {
    when:
    def exists = bomService.exists(BomId.from("!ACE"))

    then:
    exists == false
  }

  def "아이디로 존재하는 BOM를 조회"() {
    when:
    def bom = bomService.get(bom.id)

    then:
    // bom.id.itemId.value == "ACE" // mock 으로 인해 동일하지 않음
    bom.revision == 1
  }

  def "아이디로 존재하지 않는 BOM를 조회"() {
    when:
    bomService.get(BomId.from("!ACE"))

    then:
    thrown(BomExceptions.NotFoundException)
  }

  def "BOM 동일 부품을 추가하면 에러가 발생 한다"() {
    when:
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    then:
    thrown(BomExceptions.MaterialAlreadyExistsException)
  }

  def "BOM 부품을 수정하고 확인 한다"() {
    when:
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    bomMaterialService.update(
      new BomMaterialRequests.UpdateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 1
      )
    )
    def bom = bomService.get(bom.id)

    then:
    bomMaterialService.get(bom.id, bom2.id).quantity == 1
  }

  def "BOM 부품을 삭제하고 확인 한다"() {
    when:
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    bomMaterialService.delete(
      new BomMaterialRequests.DeleteRequest(
        bomId: bom.id,
        materialId: bom2.id
      )
    )

    then:
    bomMaterialService.getAll(bom.id).size() == 0
  }

  def "BOM 을 확정한다"() {
    when:
    bomService.determine(new BomRequests.DetermineRequest(id: bom2.id))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )

    bomService.determine(new BomRequests.DetermineRequest(id: bom.id))
    def bom = bomService.get(bom.id)

    then:

    bom.status == BomStatusKind.DETERMINED
    bom.determinedDate != null
  }

  def "BOM 을 확정 후 변경하면 에러 발생"() {
    when:
    bomService.determine(new BomRequests.DetermineRequest(id: bom2.id))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )

    bomService.determine(new BomRequests.DetermineRequest(id: bom.id))
    bomService.update(
      new BomRequests.UpdateRequest(
        id: bom.id,
        processId: ProcessId.from("process-1")
      )
    )

    then:
    thrown(BomExceptions.CannotModifyException)
  }

  def "BOM 을 확정 후 자재를 추가하면 에러 발생"() {
    when:
    bomService.determine(new BomRequests.DetermineRequest(id: bom.id))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )

    then:
    thrown(BomExceptions.CannotModifyException)
  }

  def "BOM 을 확정 후 자재를 수정하면 에러 발생"() {
    when:
    bomService.determine(new BomRequests.DetermineRequest(id: bom2.id))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    bomService.determine(new BomRequests.DetermineRequest(id: bom.id))
    bomMaterialService.update(
      new BomMaterialRequests.UpdateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 1
      )
    )

    then:
    thrown(BomExceptions.CannotModifyException)
  }

  def "BOM 을 확정 후 자재를 삭제하면 에러 발생"() {
    when:
    bomService.determine(new BomRequests.DetermineRequest(id: bom2.id))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    bomService.determine(new BomRequests.DetermineRequest(id: bom.id))
    bomMaterialService.delete(
      new BomMaterialRequests.DeleteRequest(
        bomId: bom.id,
        materialId: bom2.id
      )
    )

    then:
    thrown(BomExceptions.CannotModifyException)
  }

  def "BOM 을 확정 후 새 버전 생성"() {
    when:
    bomService.determine(new BomRequests.DetermineRequest(id: bom2.id))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    bomService.determine(new BomRequests.DetermineRequest(id: bom.id))
    def nextDraft = bomService.draft(new BomRequests.DraftRequest(
      id: BomId.from("ACE2"),
      itemId: ItemId.from("ACE")
    ))
    def previousBom = bomService.get(bom.id)

    then:
    nextDraft.revision == 2
    previousBom.status == BomStatusKind.EXPIRED
  }

  def "BOM 을 확정 전 새 버전을 생성하면 에러 발생"() {
    when:
    bomService.draft(new BomRequests.DraftRequest(
      id: BomId.from("ACE2"),
      itemId: ItemId.from("ACE-2")
    ))

    then:
    thrown(BomExceptions.AlreadyDraftStatusException)
  }

  def "새 버전 확정후 이전 버전의 만료 확인"() {
    when:
    bomService.determine(new BomRequests.DetermineRequest(id: bom2.id))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    bomService.determine(new BomRequests.DetermineRequest(id: bom.id))
    def nextDrafted = bomService.draft(new BomRequests.DraftRequest(
      id: BomId.from("ACE2"),
      itemId: ItemId.from("ACE")
    ))
    bomService.determine(new BomRequests.DetermineRequest(id: nextDrafted.id))
    def bom = bomService.get(bom.id)

    then:
    bom.status == BomStatusKind.EXPIRED
  }

  def "BOM의 누적 단가를 계산"() {
    when:
    bomService.update(new BomRequests.UpdateRequest(id: bom.id, processId: ProcessId.from("process-1")))
    bomService.update(new BomRequests.UpdateRequest(id: bom2.id, processId: ProcessId.from("process-1")))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    def bom = bomService.get(bom.id)

    then:
    bom.estimatedAccumulatedUnitCost.total == 900
    bom.estimatedAccumulatedUnitCost.directMaterial == 600
  }

  def "자재되는 BOM의 가격이 변경되면 자재로 사용하는 BOM의 누적 단가가 변경된다"() {
    when:
    bomService.update(new BomRequests.UpdateRequest(id: bom.id, processId: ProcessId.from("process-1")))
    bomService.update(new BomRequests.UpdateRequest(id: bom2.id, processId: ProcessId.from("process-1")))
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bom.id,
        materialId: bom2.id,
        quantity: 2
      )
    )
    itemService.update(
      new ItemRequests.UpdateRequest(
        id: ItemId.from("ACE-2"),
        name: "테스트 2",
        categoryId: ItemCategoryId.from("category-1"),
        customerId: CompanyId.from("CUST1"),
        unit: UnitKind.EA,
        type: ItemTypeKind.MATERIAL,
        baseUnitCost: 350
      )
    )

    def bom = bomService.get(bom.id)

    then:
    bom.estimatedAccumulatedUnitCost.total == 1000
    bom.estimatedAccumulatedUnitCost.directMaterial == 700
  }

  def "BOM visit in order"() {
    when:
    def levels = []

    def hierarchy = bomService.getHierarchy(BomId.from("bom-1"))
    hierarchy.visitInOrder({
      bom, parents -> levels.add(parents.size())
    })

    then:
    levels == [0, 1, 2, 3, 4]
  }

  def "BOM visit post order"() {
    when:
    def levels = []

    def hierarchy = bomService.getHierarchy(BomId.from("bom-1"))
    hierarchy.visitPostOrder({
      bom, parents -> levels.add(parents.size())
    })
    then:
    levels == [4, 3, 2, 1, 0]
  }

  def "BOM 여분율 계산"() {
    when:
    def hierarchy = bomService.getHierarchy(BomId.from("bom-1"))
    def leaf = hierarchy.materials[0].materials[0].materials[0].materials[0]

    then:
    leaf.spareRatio == 0.01
  }

}
