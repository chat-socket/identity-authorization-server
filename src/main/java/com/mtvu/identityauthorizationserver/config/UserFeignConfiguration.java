package com.mtvu.identityauthorizationserver.config;

import com.mtvu.identityauthorizationserver.exception.UserAlreadyExistAuthenticationException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static feign.FeignException.errorStatus;

public class UserFeignConfiguration {

    @Bean
    public ErrorDecoder userErrorDecoder() {
        return (methodKey, response) -> {
            if (HttpStatus.NOT_FOUND.value() == response.status()) {
                return new UsernameNotFoundException("User not found");
            }
            if (HttpStatus.CONFLICT.value() == response.status()) {
                return new UserAlreadyExistAuthenticationException("User is already exists");
            }
            return errorStatus(methodKey, response);
        };
    }
}
