package uk.gov.hmcts.reform.cwrdapi.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class CwrdCommonRepository implements ICwrdCommonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void flush() {
        entityManager.flush();
    }
}