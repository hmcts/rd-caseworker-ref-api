package uk.gov.hmcts.reform.cwrdapi.controllers.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchStaffUserResponse {

    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("email_id")
    private String emailId;
    @JsonProperty("services")
    private List<ServiceResponse> services;
    @JsonProperty("region")
    private String region;
    @JsonProperty("region_id")
    private Integer regionId;
    @JsonProperty("roles")
    private List<Role> roles;
    @JsonProperty("task_supervisor")
    private boolean taskSupervisor;
    @JsonProperty("case_allocator")
    private boolean caseAllocator;
    @JsonProperty("suspended")
    private boolean suspended;
    @JsonProperty("staff_admin")
    private boolean staffAdmin;
    @JsonProperty("base_locations")
    private List<Location> baseLocations;
    @JsonProperty("user_type")
    private String userType;
    @JsonProperty("skills")
    private List<SkillResponse> skills;
}
