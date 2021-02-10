package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class CwrdCommonRepository implements ICwrdCommonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void flush() {
        entityManager.flush();
    }
}
