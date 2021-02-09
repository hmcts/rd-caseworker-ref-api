package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE;

@MultipartConfig
@AllArgsConstructor
@Component
public class AuditInterceptor implements HandlerInterceptor {

    @Autowired
    IValidationService validationServiceFacadeImpl;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object handler) {

        String fileName = EMPTY;
        MultipartFile multipartFile = ((MultipartHttpServletRequest) request).getFile(FILE);
        if (nonNull(multipartFile)) {
            fileName = multipartFile.getOriginalFilename();
        }
        //Starts CWR Auditing with Job Status in Progress.
        validationServiceFacadeImpl.startCaseworkerAuditing(IN_PROGRESS, fileName);
        return true;
    }
}
