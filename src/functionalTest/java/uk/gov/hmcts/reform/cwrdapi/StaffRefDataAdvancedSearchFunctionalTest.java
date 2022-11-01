package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.validateSearchUserProfileResponse;
import static uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
class StaffRefDataAdvancedSearchFunctionalTest extends AuthorizationFunctionalTest {

    public static final String SEARCH_STAFF_USER = "StaffRefDataController.searchStaffProfile";
    public static final String CASE_WORKER_PROFILE_URL = "/refdata/case-worker/profile";

    SearchRequest searchReq;

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_default_pagination() {
        String searchString = "serviceCode=ABA1&location=2&userType=1&jobTitle=2&role="
                + "case allocator";

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);


        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .serviceCode("ABA1")
                    .location("2")
                    .userType("1")
                    .role("case allocator")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse,HttpStatus.OK),
                    searchReq));

        }
    }




    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_flag_enabled_pagination() {
        String searchString = "serviceCode=ABA1&location=2&userType=1&jobTitle=2&role="
                + "case allocator";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .serviceCode("ABA1")
                    .location("2")
                    .userType("1")
                    .role("case allocator")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse,HttpStatus.OK),
                    searchReq));
        }
    }



    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_serviceCode() {
        String searchString = "serviceCode=ABA1";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .serviceCode("ABA1")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse,HttpStatus.OK),
                    searchReq));
        }
    }


    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_list_of_serviceCodes() {
        String searchString = "serviceCode=ABA1,BBA9";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .serviceCode("ABA1,BBA9")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse,HttpStatus.OK),
                    searchReq));
        }
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_location() {

        String searchString = "location=817181";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .location("817181")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse,HttpStatus.OK),
                    searchReq));
        }
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_list_of_location() {
        String searchString = "location=817181,271588";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .location("817181,271588")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse,HttpStatus.OK),
                    searchReq));
        }
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_userType() {

        String searchString = "userType=1";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .userType("1")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse,HttpStatus.OK),
                    searchReq));
        }
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_Jobtitle() {

        String searchString = "jobTitle=2";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .jobTitle("2")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse, HttpStatus.OK),
                    searchReq));
        }
    }


    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_role() {

        String searchString = "role=case allocator";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .role("case allocator")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse, HttpStatus.OK),
                    searchReq));
        }
    }


    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_list_of_roles() {
        String searchString = "role=task supervisor,case allocator,staff administrator";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .role("task supervisor,case allocator,staff administrator")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse, HttpStatus.OK),
                    searchReq));
        }
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_search_with_skill() {

        String searchString = "skill=1";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        int totalRecords = Integer.parseInt(fetchResponse.getHeader("total-records"));
        if (totalRecords > 0) {
            List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                    fetchResponse.getBody().as(SearchStaffUserResponse[].class));
            assertThat(searchStaffUserResponse).isNotNull().hasSizeGreaterThan(0);
            searchReq = SearchRequest.builder()
                    .skill("1")
                    .build();
            assertTrue(validateSearchUserProfileResponse(new ResponseEntity<>(searchStaffUserResponse, HttpStatus.OK),
                    searchReq));
        }
    }


    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_403_when_flag_false() {

        String searchString = "serviceCode=ABA1";

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)

                .andReturn();

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(fetchResponse.statusCode());
        assertThat(fetchResponse.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_401_when_un_authorized() {

        String searchString = "serviceCode=ABA1";

        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);

    }


    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_403_when_invalid_role() {
        String searchString = "serviceCode=ABA1&location=2&userType=1&role=case allocators";
        Response response = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);
    }


    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_status_code_400_when_page_size_is_zero() {
        String searchString = "serviceCode=ABA1&location=12345&userType=1&jobTitle=2"
                + "case allocators";

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"0","1")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_status_code_400_when_page_num_is_zero() {
        String searchString = "serviceCode=ABA1&location=12345&userType=1&jobTitle=2&role="
                + "case allocator";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","0")
                .get(CASE_WORKER_PROFILE_URL + "/search?" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(400);

    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_status_code_400_when_no_params_in_search_string() {
        String searchString = "";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search" + searchString)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(400);


    }


}