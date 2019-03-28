package pico.erp.bom

import kkojaeh.spring.boot.component.SpringBootTestComponent
import kkojaeh.spring.boot.component.Take
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import pico.erp.bom.material.BomMaterialRequests
import pico.erp.bom.material.BomMaterialService
import pico.erp.company.CompanyApplication
import pico.erp.company.CompanyId
import pico.erp.item.*
import pico.erp.item.category.ItemCategoryId
import pico.erp.process.ProcessApplication
import pico.erp.process.ProcessId
import pico.erp.process.ProcessRequests
import pico.erp.process.ProcessService
import pico.erp.process.difficulty.ProcessDifficultyKind
import pico.erp.process.type.ProcessTypeId
import pico.erp.shared.TestParentApplication
import pico.erp.shared.data.UnitKind
import spock.lang.Specification

@SpringBootTest(classes = [BomApplication, TestConfig])
@SpringBootTestComponent(parent = TestParentApplication, siblings = [ItemApplication, ProcessApplication, CompanyApplication])
@Transactional
@Rollback
@ActiveProfiles("test")
class BomServiceSpec extends Specification {

  @Autowired
  BomService bomService

  @Autowired
  BomMaterialService bomMaterialService

  @Take
  ItemService itemService

  @Take
  ProcessService processService

  def itemCategoryId = ItemCategoryId.from("category-1")

  def customerId = CompanyId.from("CUST1")

  def itemId1 = ItemId.from("ACE")

  def itemId2 = ItemId.from("ACE-2")

  def itemId3 = ItemId.from("ACE-3")

  def unknownBomId = BomId.from("unknown")

  def bomId1 = BomId.from("ACE")

  def bomId2 = BomId.from("ACE-2")

  def bomId3 = BomId.from("ACE-3")

  def processId = ProcessId.from("process-1")

  def setup() {

    itemService.create(new ItemRequests.CreateRequest(id: itemId1, name: "테스트", categoryId: itemCategoryId, customerId: customerId, unit: UnitKind.EA, type: ItemTypeKind.MATERIAL, baseUnitCost: 0))
    itemService.create(new ItemRequests.CreateRequest(id: itemId2, name: "테스트 2", categoryId: itemCategoryId, customerId: customerId, unit: UnitKind.EA, type: ItemTypeKind.MATERIAL, baseUnitCost: 300))
    itemService.create(new ItemRequests.CreateRequest(id: itemId3, name: "테스트 3", categoryId: itemCategoryId, customerId: customerId, unit: UnitKind.EA, type: ItemTypeKind.MATERIAL, baseUnitCost: 300))
    processService.create(
      new ProcessRequests.CreateRequest(
        id: processId,
        lossRate: 0.01,
        typeId: ProcessTypeId.from("PO"),
        adjustCost: 0,
        difficulty: ProcessDifficultyKind.NORMAL,
        description: "좋은 보통 작업",
        itemId: itemId1
      )
    )

    bomService.draft(
      new BomRequests.DraftRequest(
        id: bomId1,
        itemId: itemId1
      )
    )
    bomService.draft(
      new BomRequests.DraftRequest(
        id: bomId2,
        itemId: itemId2
      )
    )
    bomService.draft(
      new BomRequests.DraftRequest(
        id: bomId3,
        itemId: itemId3
      )
    )
  }

  def addMaterial() {
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bomId1,
        materialId: bomId2,
        quantity: 2
      )
    )
  }

  def addMaterial2() {
    bomMaterialService.create(
      new BomMaterialRequests.CreateRequest(
        bomId: bomId1,
        materialId: bomId3,
        quantity: 2
      )
    )
  }

  def updateMaterial() {
    bomMaterialService.update(
      new BomMaterialRequests.UpdateRequest(
        bomId: bomId1,
        materialId: bomId2,
        quantity: 1
      )
    )
  }

  def removeMaterial() {
    bomMaterialService.delete(
      new BomMaterialRequests.DeleteRequest(
        bomId: bomId1,
        materialId: bomId2
      )
    )
  }

  def determine1() {
    processService.completePlan(
      new ProcessRequests.CompletePlanRequest(
        id: processId
      )
    )
    bomService.determine(
      new BomRequests.DetermineRequest(id: bomId1)
    )
  }

  def determine2() {
    bomService.determine(
      new BomRequests.DetermineRequest(id: bomId2)
    )
  }

  def nextDraft() {
    return bomService.draft(
      new BomRequests.DraftRequest(
        id: BomId.from("ACE-NEXT"),
        itemId: itemId1
      )
    )
  }

  def updateItem() {
    itemService.update(
      new ItemRequests.UpdateRequest(
        id: itemId2,
        name: "테스트 2",
        categoryId: itemCategoryId,
        customerId: customerId,
        unit: UnitKind.EA,
        type: ItemTypeKind.MATERIAL,
        baseUnitCost: 350
      )
    )
  }

  def "존재 - 아이디로 확인"() {
    when:
    def exists = bomService.exists(bomId1)

    then:
    exists == true
  }

  def "존재 - 존재하지 않는 아이디로 확인"() {
    when:
    def exists = bomService.exists(unknownBomId)

    then:
    exists == false
  }

  def "조회 - 아이디로 조회"() {
    when:
    def bom = bomService.get(bomId1)

    then:
    bom.id == bomId1
    bom.revision == 1
    bom.itemId == itemId1
  }

  def "조회 - 존재하지 않는 아이디로 조회"() {
    when:
    bomService.get(unknownBomId)

    then:
    thrown(BomExceptions.NotFoundException)
  }

  def "자재 추가 - 중복 자재 추가"() {
    when:
    addMaterial()
    addMaterial()

    then:
    thrown(BomExceptions.MaterialAlreadyExistsException)
  }

  def "자재 수정 - 수정"() {
    when:
    addMaterial()
    updateMaterial()

    def material = bomMaterialService.get(bomId1, bomId2)
    then:
    material.order == 0
    material.quantity == 1
  }

  def "자재 삭제 - 삭제"() {
    when:
    addMaterial()
    removeMaterial()

    then:
    bomMaterialService.getAll(bomId1).size() == 0
  }

  def "순서 - 추가한 순서 대로 증가함"() {
    when:
    addMaterial()
    addMaterial2()

    def material1 = bomMaterialService.get(bomId1, bomId2)
    def material2 = bomMaterialService.get(bomId1, bomId3)
    def materials = bomMaterialService.getAll(bomId1)
    then:
    material1.order == 0
    material2.order == 1
    materials[0].id == material1.id
    materials[1].id == material2.id
  }

  def "순서 - 순서 변경"() {
    when:
    addMaterial()
    addMaterial2()
    bomMaterialService.changeOrder(
      new BomMaterialRequests.ChangeOrderRequest(
        bomId: bomId1,
        materialId: bomId2,
        order: 1
      )
    )
    bomMaterialService.changeOrder(
      new BomMaterialRequests.ChangeOrderRequest(
        bomId: bomId1,
        materialId: bomId3,
        order: 0
      )
    )

    def material1 = bomMaterialService.get(bomId1, bomId2)
    def material2 = bomMaterialService.get(bomId1, bomId3)
    def materials = bomMaterialService.getAll(bomId1)
    then:
    material1.order == 1
    material2.order == 0
    materials[0].id == material2.id
    materials[1].id == material1.id
  }

  def "순서 - 동일 순서 변경"() {
    when:
    addMaterial()
    addMaterial2()
    bomMaterialService.changeOrder(
      new BomMaterialRequests.ChangeOrderRequest(
        bomId: bomId1,
        materialId: bomId2,
        order: 0
      )
    )

    then:
    thrown(BomExceptions.MaterialCannotChangeOrderException)
  }

  def "확정 - 확정"() {
    when:
    determine2()
    addMaterial()
    determine1()

    def bom = bomService.get(bomId1)

    then:

    bom.status == BomStatusKind.DETERMINED
    bom.determinedDate != null
  }

  def "자재 추가 - 확정 후 추가"() {
    when:
    determine1()
    addMaterial()

    then:
    thrown(BomExceptions.CannotUpdateException)
  }

  def "자재 수정 - 확정 후 자재 수정"() {
    when:
    determine2()
    addMaterial()
    determine1()
    updateMaterial()

    then:
    thrown(BomExceptions.CannotUpdateException)
  }

  def "자재 삭제 - 확정 후 자재 삭제"() {
    when:
    determine2()
    addMaterial()
    determine1()
    removeMaterial()

    then:
    thrown(BomExceptions.CannotUpdateException)
  }

  def "새버전 - 확정 후 새 버전"() {
    when:
    determine2()
    addMaterial()
    determine1()
    def nextDraft = nextDraft()
    def previousBom = bomService.get(bomId1)

    then:
    nextDraft.revision == 2
    previousBom.status == BomStatusKind.EXPIRED
  }

  def "새버전 - 확정 전 새 버전"() {
    when:
    nextDraft()

    then:
    thrown(BomExceptions.AlreadyDraftStatusException)
  }


  def "누적단가 - 누적 단가 확인"() {
    when:
    addMaterial()
    def bom = bomService.get(bomId1)

    then:
    bom.estimatedAccumulatedUnitCost.total == 700
    bom.estimatedAccumulatedUnitCost.directMaterial == 600
  }

  def "누적단가 - 자재의 가격 변경 영향"() {
    when:
    addMaterial()
    updateItem()

    def bom = bomService.get(bomId1)

    then:
    bom.estimatedAccumulatedUnitCost.total == 800
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

    println BomId.from("bom-2")

    def hierarchy = bomService.getHierarchy(BomId.from("bom-1"))
    def leaf = hierarchy.materials[0].materials[0].materials[0].materials[0]

    then:
    leaf.spareRatio == 0.0201
  }

}
