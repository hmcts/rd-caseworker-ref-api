package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;

import java.util.List;

@Repository
public interface ExceptionCaseWorkerRepository extends JpaRepository<ExceptionCaseWorker, Long> {

    List<ExceptionCaseWorker> findByJobId(Long jobId);
}
