package org.apereo.cas.web.security.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MonitorEndpointLdapAuthenticationProviderRolesBasedTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.monitor.endpoints.ldap.ldapAuthz.roleAttribute=roomNumber",
    "cas.monitor.endpoints.ldap.ldapAuthz.searchFilter=cn={user}",
    "cas.monitor.endpoints.ldap.ldapAuthz.baseDn=ou=people,dc=example,dc=org",
    "cas.monitor.endpoints.ldap.ldapAuthz.rolePrefix=ROLE_"
})
@EnabledIfPortOpen(port = 10389)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Ldap")
public class MonitorEndpointLdapAuthenticationProviderRolesBasedTests extends BaseMonitorEndpointLdapAuthenticationProviderTests {

    @Test
    public void verifyAuthorizedByRole() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("ROLE_888"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        val token = provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456"));
        assertNotNull(token);
    }

    @Test
    public void verifyUnauthorizedByRole() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("SOME_BAD_ROLE"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "123456")));
    }

    @Test
    public void verifyUserNotFound() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("SOME_BAD_ROLE"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(new UsernamePasswordAuthenticationToken("UNKNOWN_USER", "123456")));
    }

    @Test
    public void verifyUserBadPassword() {
        val securityProperties = new SecurityProperties();
        securityProperties.getUser().setRoles(List.of("SOME_BAD_ROLE"));
        val provider = new MonitorEndpointLdapAuthenticationProvider(casProperties.getMonitor().getEndpoints().getLdap(), securityProperties);
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(new UsernamePasswordAuthenticationToken("authzcas", "BAD_PASSWORD")));
    }

}
