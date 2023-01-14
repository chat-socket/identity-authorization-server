package com.mtvu.identityauthorizationserver.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "chat.auth.provider")
public class ProviderConfigurationProperties {
    private String tokenUri;
    private String clientId;
    private String identifier;
    private String clientSecret;
    private Set<String> scopes;
    private String grantType;

}
