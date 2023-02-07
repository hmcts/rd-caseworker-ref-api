package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserIdamStatusWithEmailResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil.toResponseEntity;

@Service
@Slf4j
@CacheConfig(cacheNames = {"userProfileCacheManager"})
@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataCache {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;


    @Cacheable
    public UserIdamStatusWithEmailResponse getUserProfileIdamStatus(String category) {

        UserIdamStatusWithEmailResponse userIdamStatusWithEmailResponse = null;
        try {
            Response response = userProfileFeignClient.getUserProfileIdamStatus(category);
            ResponseEntity<Object> responseEntity = toResponseEntity(response, UserIdamStatusWithEmailResponse.class);


            userIdamStatusWithEmailResponse = (UserIdamStatusWithEmailResponse) requireNonNull(responseEntity.getBody());


        } catch (Exception exception) {
            log.error("{}:: get  User profile idam status api failed::{}", loggingComponentName,
                    exception.getMessage());
        }

        return userIdamStatusWithEmailResponse;
    }
}
