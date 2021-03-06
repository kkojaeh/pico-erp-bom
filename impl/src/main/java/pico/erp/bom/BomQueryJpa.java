package pico.erp.bom;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import kkojaeh.spring.boot.component.ComponentBean;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pico.erp.bom.material.QBomMaterialEntity;
import pico.erp.item.ItemId;
import pico.erp.shared.jpa.QueryDslJpaSupport;

@Service
@ComponentBean
@Transactional(readOnly = true)
@Validated
public class BomQueryJpa implements BomQuery {

  private final QBomEntity bom = QBomEntity.bomEntity;

  private final QBomMaterialEntity bomMaterial = QBomMaterialEntity.bomMaterialEntity;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private QueryDslJpaSupport queryDslJpaSupport;

  @Override
  public List<BomRevisionView> findRevisions(ItemId itemId) {
    val query = new JPAQuery<BomRevisionView>(entityManager);
    val select = Projections.bean(BomRevisionView.class,
      bom.id,
      bom.revision,
      bom.status,
      bom.determinedBy,
      bom.determinedDate,
      bom.draftedBy,
      bom.draftedDate
    );
    query.select(select);
    query.from(bom);
    query.where(bom.itemId.eq(itemId));
    query.orderBy(bom.revision.desc());
    return query.fetch();
  }

}
