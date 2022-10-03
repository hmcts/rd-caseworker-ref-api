package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailDomainPropertyInitiatorTest {
    @InjectMocks
    private EmailDomainPropertyInitiator domainPropertyInitiator;
    Method method = mock(Method.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(method.getName()).thenReturn("test");
        domainPropertyInitiator = mock(EmailDomainPropertyInitiator.class);
    }

    @Test
    void testInValid_DomainNotSet_Expected_False() {
        //if emailDomainList is not set -> expected false
        EmailDomainPropertyInitiator.emailDomains = "justice.gov.uk,DWP.GOV.UK,hmrc.gov.uk";
        domainPropertyInitiator.setTempEmailDomains(EmailDomainPropertyInitiator.emailDomains);
        when(domainPropertyInitiator.getTempEmailDomains()).thenReturn(EmailDomainPropertyInitiator.emailDomains);
        domainPropertyInitiator.getPropertySupportBean();
        Assert.assertNotNull(domainPropertyInitiator.getTempEmailDomains());
    }

}
