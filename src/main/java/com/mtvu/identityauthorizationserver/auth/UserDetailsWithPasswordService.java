package com.mtvu.identityauthorizationserver.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserDetailsWithPasswordService {
    UserDetails loadUserByUsernameAndPassword(String username, String password);
}
