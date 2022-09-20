package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CaseWorkerProfileRepository extends JpaRepository<CaseWorkerProfile, Long> {

    CaseWorkerProfile findByEmailId(String emailId);

    List<CaseWorkerProfile> findByEmailIdIn(Set<String> emailIds);

    List<CaseWorkerProfile> findByCaseWorkerIdIn(List<String> caseWorkerId);

    Optional<CaseWorkerProfile> findByCaseWorkerId(String caseWorkerId);

    List<CaseWorkerProfile> findByEmailIdIgnoreCaseContaining(String emailPattern);
    @Query(value = "select cw from case_worker_profile cw where lower(cw.firstName) like " +
            "lower(concat('%', :searchString, '%')) " +
            "or lower(cw.lastName) like lower(concat('%', :searchString, '%'))")
    Page<CaseWorkerProfile> findByFirstNameOrLastName(String searchString, Pageable pageable);

    @Query(value = """
            select cw from case_worker_profile cw 
            JOIN FETCH case_worker_work_area wa ON cw.caseWorkerId = wa.caseWorkerId 
            where wa.serviceCode IN :serviceCode""")
    Page<CaseWorkerProfile> findByServiceCodeIn(Set<String> serviceCode, Pageable pageable);
}
