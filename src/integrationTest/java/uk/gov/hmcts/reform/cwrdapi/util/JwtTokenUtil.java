package uk.gov.hmcts.reform.cwrdapi.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter.TOKEN_NAME;


@Slf4j
public final class JwtTokenUtil {

    private static final String SUBJECT = "sub";
    private static final RSAKey TEST_RSA_JWK;
    private static final String CRD_CLAIM = "CRD_Claim";

    private JwtTokenUtil() {
    }

    static {
        try {
            TEST_RSA_JWK = KeyGenUtil.getRsaJwk();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate JWT Signed Token.
     * @param issuer    Issuer
     * @param ttlMillis Time to live
     * @return String
     */
    public static String generateToken(String issuer, long ttlMillis, String userId, String role) {
        final long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + ttlMillis;

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(role + " " + userId)
                .issueTime(new Date())
                .issuer(issuer)
                .audience(role)
                .claim("tokenName", "access_token");

        getJwtClaimsBuilder(new Date(),new Date(expMillis));

        SignedJWT signedJwt = null;
        try {
            signedJwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(TEST_RSA_JWK.getKeyID()).build(),
                    builder.build());
            signedJwt.sign(new RSASSASigner(TEST_RSA_JWK));
        } catch (JOSEException e) {
            log.error("error while creating bearer token : " + (e.getMessage()));
        }
        return signedJwt.serialize();
    }

    public static String decodeJwtToken(String jwtToken) {

        String[] splitString = jwtToken.split("\\.");
        String base64EncodedBody = splitString[1];
        Base64 base64Url = new Base64(true);
        return new String(base64Url.decode(base64EncodedBody));

    }

    /**
     * Fetch userId and role from the token body.TokenBody is in json format and it fetches key 'sub'
     * to get comma separated value containing userId and role.
     *
     * @param tokenBody tokenBody in string format
     * @return List containing userId and role
     */
    public static LinkedList getUserIdAndRoleFromToken(String tokenBody) {
        String[] tokenElements = tokenBody.split(",");
        List<String> elements = Arrays.asList(tokenElements).stream()
                .filter(element -> element.contains(SUBJECT))
                .map(subElement -> subElement.split(":")[1].replace("\"", ""))
                .collect(Collectors.toList());

        if (elements.isEmpty()) {
            throw new InvalidTokenException("Token did not returned 'subject' element");
        }

        String[] tokenisedSubValue = elements.get(0).split(" ");
        LinkedList tokenResult = new LinkedList();
        tokenResult.add(tokenisedSubValue[0]);
        tokenResult.add(tokenisedSubValue[1]);
        return tokenResult;
    }

    public static String generateAuthToken(String issuer, boolean isExpired) throws Exception {

        Instant now = Instant.now();

        Instant issuedAt = isExpired
                ? now.minus(2, ChronoUnit.HOURS)
                : now.minusSeconds(60);

        Instant expiresAt = isExpired
                ? now.minus(1, ChronoUnit.HOURS)
                : now.plusSeconds(3600);

        JWTClaimsSet.Builder claimsBuilder =
                getJwtClaimsBuilder(Date.from(issuedAt), Date.from(expiresAt));

        if (issuer != null) {
            claimsBuilder.issuer(issuer);
        }

        JWSHeader header =
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(TEST_RSA_JWK.getKeyID())
                        .build();

        SignedJWT signedJwt = new SignedJWT(header, claimsBuilder.build());

        signedJwt.sign(new RSASSASigner(TEST_RSA_JWK));

        return signedJwt.serialize();
    }

    private static JWTClaimsSet.Builder getJwtClaimsBuilder(Date issuedAt,
                                                            Date expiresAt) {
        return new JWTClaimsSet.Builder()
                .subject(CRD_CLAIM)
                .issueTime(issuedAt)
                .claim(TOKEN_NAME, ACCESS_TOKEN)
                .expirationTime(expiresAt);
    }
}

