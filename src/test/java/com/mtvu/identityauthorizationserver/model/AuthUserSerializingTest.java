package com.mtvu.identityauthorizationserver.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

class AuthUserSerializingTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModules(new CoreJackson2Module());
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
    }

    @Test
    public void testSerialisingAuthUserObject() throws JsonProcessingException {
        var oidcUserInfo = OidcUserInfo.builder()
                .subject("user@gmail.com")
                .email("user@gmail.com")
                .givenName("Luca")
                .familyName("James")
                .picture(null)
                .emailVerified(true)
                .build();
        var user = new AuthUser(oidcUserInfo.getClaims(), true, true, true, true);
        var json = objectMapper.writeValueAsString(user);
        Assertions.assertNotNull(json);
        var object = objectMapper.readValue(json, AuthUser.class);
        Assertions.assertNotNull(object);
    }
}