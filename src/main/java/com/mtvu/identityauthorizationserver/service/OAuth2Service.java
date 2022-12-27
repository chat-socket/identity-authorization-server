package com.mtvu.identityauthorizationserver.service;

import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OAuth2Service {

    private AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager;

    public OAuth2AccessToken getAccessToken(String registrationId, String principal) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                .principal(principal)
                .build();
        OAuth2AuthorizedClient authorizedClient = authorizedClientServiceAndManager.authorize(authorizeRequest);
        assert authorizedClient != null;
        return authorizedClient.getAccessToken();
    }

    public OAuth2AccessToken getDefaultClientAccessToken() {
        return getAccessToken("auth-service", "auth-service-principal");
    }

}
