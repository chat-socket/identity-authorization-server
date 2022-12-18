package com.mtvu.identityauthorizationserver.exception;

import org.springframework.security.core.AuthenticationException;


/**
 * @author mvu
 * @project chat-socket
 **/
public class UserAlreadyExistAuthenticationException extends AuthenticationException {

    public UserAlreadyExistAuthenticationException(final String msg) {
        super(msg);
    }

}
