package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CaseWorkerProfileRepository extends JpaRepository<CaseWorkerProfile, Long> {

    CaseWorkerProfile findByEmailIdIgnoreCase(String emailId);

    List<CaseWorkerProfile> findByEmailIdIgnoreCaseIn(Set<String> emailIds);

    List<CaseWorkerProfile> findByCaseWorkerIdIn(List<String> caseWorkerId);

    Optional<CaseWorkerProfile> findByCaseWorkerId(String caseWorkerId);

    List<CaseWorkerProfile> findByEmailIdIgnoreCaseContaining(String emailPattern);

    @Query(value = "select cw from case_worker_profile cw where "
            + "concat(lower(cw.firstName), ' ', lower(cw.lastName)) like concat('%', :searchString, '%')"
    )
    Page<CaseWorkerProfile> findByFirstNameOrLastName(String searchString, Pageable pageable);

    @Query(value = """
            select distinct cw from case_worker_profile cw 
            JOIN FETCH case_worker_work_area wa ON cw.caseWorkerId = wa.caseWorkerId 
            where wa.serviceCode IN :serviceCode""")
    Page<CaseWorkerProfile> findByServiceCodeIn(Set<String> serviceCode, Pageable pageable);

    @Query(value = "Select distinct cwp from case_worker_profile cwp "
            + "join case_worker_work_area cwa on cwp.caseWorkerId=cwa.caseWorkerId "
            + "join case_worker_role cwr on cwr.caseWorkerId=cwp.caseWorkerId "
            + "join case_worker_location cwl on cwl.caseWorkerId=cwp.caseWorkerId "
            + "left join case_worker_skill cws on cws.caseWorkerId=cwp.caseWorkerId "
            + "WHERE "
            + "(TRUE = :#{#searchRequest.userType == null} or "
            + "cwp.userTypeId = CAST(CAST(:#{#searchRequest.userType} AS text) AS int)) "
            + "and  (TRUE = :#{#searchRequest.jobTitle == null} or "
            + "cwr.roleId = CAST(CAST(:#{#searchRequest.jobTitle} AS text) AS int)) "
            + "and (TRUE = :#{#searchRequest.skill == null} or "
            + "cws.skillId = CAST(CAST(:#{#searchRequest.skill} AS text) AS int))"
            + "and (TRUE = :#{#searchRequest.serviceCode == null} or cwa.serviceCode IN (:serviceCodes)) "
            + "and (TRUE = :#{#searchRequest.location == null} or cwl.locationId IN (:locationId)) "
            + "and ((:taskSupervisor = true and cwp.taskSupervisor = true) "
            + "or (:caseAllocator = true and cwp.caseAllocator = true) "
            + "or (:staffAdmin = true and cwp.userAdmin = true) "
            + "or (:taskSupervisor = false and :caseAllocator = false and :staffAdmin = false)) "
    )
    Page<CaseWorkerProfile> findByCaseWorkerProfiles(@Param("searchRequest") SearchRequest searchRequest,
                                                     List<String> serviceCodes, List<Integer> locationId,
                                                     Boolean taskSupervisor, Boolean caseAllocator, Boolean staffAdmin,
                                                     Pageable pageable);

}
