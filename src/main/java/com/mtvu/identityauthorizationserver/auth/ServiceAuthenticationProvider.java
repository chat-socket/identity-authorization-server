package com.mtvu.identityauthorizationserver.auth;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

public class ServiceAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private UserDetailsWithPasswordService userDetailsWithPasswordService;

    public ServiceAuthenticationProvider() {
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }

    @Override
    protected void doAfterPropertiesSet() {
        Assert.notNull(this.userDetailsWithPasswordService, "A UserDetailsService must be set");
    }

    @Override
    protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        try {
            var password = authentication.getCredentials().toString();
            UserDetails loadedUser = this.getUserDetailsWithPasswordService()
                    .loadUserByUsernameAndPassword(username, password);
            if (loadedUser == null) {
                throw new InternalAuthenticationServiceException(
                        "UserDetailsService returned null, which is an interface contract violation");
            }
            return loadedUser;
        }
        catch (UsernameNotFoundException ex) {
            throw ex;
        }
        catch (InternalAuthenticationServiceException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    public void setUserDetailsWithPasswordService(UserDetailsWithPasswordService userDetailsWithPasswordService) {
        this.userDetailsWithPasswordService = userDetailsWithPasswordService;
    }

    protected UserDetailsWithPasswordService getUserDetailsWithPasswordService() {
        return this.userDetailsWithPasswordService;
    }
}
