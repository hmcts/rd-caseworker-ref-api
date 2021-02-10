package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class CwrCommonRepository implements ICwrCommonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void flush() {
        entityManager.flush();
    }
}