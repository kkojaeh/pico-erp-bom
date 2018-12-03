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

  def itemCategoryId = ItemCategoryId.from("category-1")

  def customerId = CompanyId.from("CUST1")

  def itemId1 = ItemId.from("ACE")

  def itemId2 = ItemId.from("ACE-2")

  def unknownBomId = BomId.from("unknown")

  def bomId1 = BomId.from("ACE")

  def bomId2 = BomId.from("ACE-2")

  def processId = ProcessId.from("process-1")

  def setup() {

    itemService.create(new ItemRequests.CreateRequest(id: itemId1, name: "테스트", categoryId: itemCategoryId, customerId: customerId, unit: UnitKind.EA, type: ItemTypeKind.MATERIAL, baseUnitCost: 0))
    itemService.create(new ItemRequests.CreateRequest(id: itemId2, name: "테스트 2", categoryId: itemCategoryId, customerId: customerId, unit: UnitKind.EA, type: ItemTypeKind.MATERIAL, baseUnitCost: 300))
    processService.create(
      new ProcessRequests.CreateRequest(
        id: processId,
        itemId: itemId1,
        lossRate: 0.01,
        typeId: ProcessTypeId.from("PO"),
        adjustCost: 0,
        difficulty: ProcessDifficultyKind.NORMAL,
        description: "좋은 보통 작업"
      )
    )


    bomService.draft(new BomRequests.DraftRequest(
      id: bomId1,
      itemId: itemId1))
    bomService.draft(new BomRequests.DraftRequest(
      id: bomId2,
      itemId: itemId2))
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
    bomService.determine(
      new BomRequests.DetermineRequest(id: bomId1)
    )
  }

  def determine2() {
    bomService.determine(
      new BomRequests.DetermineRequest(id: bomId2)
    )
  }

  def update1() {
    bomService.update(
      new BomRequests.UpdateRequest(
        id: bomId1,
        processId: processId
      )
    )
  }

  def nextDraft() {
    return bomService.draft(new BomRequests.DraftRequest(
      id: BomId.from("ACE-3"),
      itemId: itemId1
    ))
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
    material.quantity == 1
  }

  def "자재 삭제 - 삭제"() {
    when:
    addMaterial()
    removeMaterial()

    then:
    bomMaterialService.getAll(bomId1).size() == 0
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

  def "수정 - 확정 후 수정"() {
    when:
    determine2()
    addMaterial()
    determine1()
    update1()

    then:
    thrown(BomExceptions.CannotUpdateException)
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
    bomService.update(new BomRequests.UpdateRequest(id: bomId1, processId: processId))
    bomService.update(new BomRequests.UpdateRequest(id: bomId2, processId: processId))
    addMaterial()
    def bom = bomService.get(bomId1)

    then:
    bom.estimatedAccumulatedUnitCost.total == 900
    bom.estimatedAccumulatedUnitCost.directMaterial == 600
  }

  def "누적단가 - 자재의 가격 변경 영향"() {
    when:
    bomService.update(new BomRequests.UpdateRequest(id: bomId1, processId: processId))
    bomService.update(new BomRequests.UpdateRequest(id: bomId2, processId: processId))
    addMaterial()
    updateItem()

    def bom = bomService.get(bomId1)

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
