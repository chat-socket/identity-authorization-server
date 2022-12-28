package com.mtvu.identityauthorizationserver.config;

import com.mtvu.identityauthorizationserver.exception.UserAlreadyExistAuthenticationException;
import feign.codec.ErrorDecoder;
import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static feign.FeignException.errorStatus;

public class UserFeignConfiguration {

    @Bean
    public ErrorDecoder userErrorDecoder() {
        return (methodKey, response) -> {
            String bodyMessage;
            try (InputStream bodyIs = response.body().asInputStream()) {
                bodyMessage = IOUtils.toString(bodyIs, StandardCharsets.UTF_8);
            } catch (IOException e) {
                return new Exception(e.getMessage());
            }
            if (HttpStatus.NOT_FOUND.value() == response.status()) {
                return new UsernameNotFoundException(bodyMessage);
            }
            if (HttpStatus.CONFLICT.value() == response.status()) {
                return new UserAlreadyExistAuthenticationException(bodyMessage);
            }
            return errorStatus(methodKey, response);
        };
    }
}
