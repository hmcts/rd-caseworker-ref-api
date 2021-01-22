package uk.gov.hmcts.reform.cwrdapi.util;


import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.IdamRepository;

import java.util.Collection;
import java.util.stream.Collectors;


@Service
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private final IdamRepository idamRepository;


    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator,
                         JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
                         IdamRepository idamRepository) {
        this.authTokenGenerator = authTokenGenerator;
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;

        this.idamRepository = idamRepository;
    }

    /*public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CaseWorkerConstants.SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        //headers.add("user-id", getUserId())
        headers.add("user-roles", getUserRolesHeader());

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            headers.add(HttpHeaders.AUTHORIZATION, getUserBearerToken());
        }
        return headers;
    }*/

    /*private String getUserBearerToken() {
        return CaseWorkerConstants.BEARER + getUserToken();
    }


    public String getUserId() {
        if (jwtGrantedAuthoritiesConverter.getUserInfo() != null) {
            return jwtGrantedAuthoritiesConverter.getUserInfo().getUid();
        } else {
            return idamRepository.getUserInfo(getUserToken()).getUid();
        }
    }*/

    /*public UserRoles getUserRoles() {
        UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
        return UserRoles.builder()
                .uid(userInfo.getUid())
                .roles(userInfo.getRoles())
                .build();
    }*/


    /*public String getUserToken() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return jwt.getTokenValue();
        } else {
            return idamRepository.getUserToken();
        }
    }*/

    public String getUserRolesHeader() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }


    public String getServiceName() {
        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (servletRequestAttributes != null
                && servletRequestAttributes.getRequest().getHeader(CaseWorkerConstants.SERVICE_AUTHORIZATION) != null) {
            return JWT.decode(removeBearerFromToken(servletRequestAttributes.getRequest().getHeader(
                    CaseWorkerConstants.SERVICE_AUTHORIZATION))).getSubject();
        }
        return null;
    }

    private String removeBearerFromToken(String token) {
        if (!token.startsWith(CaseWorkerConstants.BEARER)) {
            return token;
        } else {
            return token.substring(CaseWorkerConstants.BEARER.length());
        }
    }

    public String getServiceAuthorizationHeader() {
        return authTokenGenerator.generate();
    }
}